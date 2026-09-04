package com.hymin.webtoon_review.chat.server.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hymin.webtoon_review.chat.common.entity.ChatMessage;
import com.hymin.webtoon_review.chat.common.entity.MessageCreatedOutbox;
import com.hymin.webtoon_review.chat.common.entity.RoomSequence;
import com.hymin.webtoon_review.chat.common.entity.enums.MessageBlockType;
import com.hymin.webtoon_review.chat.common.entity.vo.MessageBlock;
import com.hymin.webtoon_review.chat.common.repository.UserChatRoomRepository;
import com.hymin.webtoon_review.chat.server.controller.ChatMessageCommandController;
import com.hymin.webtoon_review.chat.server.dto.CreateChatMessageCommand;
import com.hymin.webtoon_review.chat.server.dto.CreateChatMessageResult;
import com.hymin.webtoon_review.chat.server.metrics.ChatMessageCommandMetrics;
import com.hymin.webtoon_review.chat.server.repository.ChatMessageCommandRepository;
import com.hymin.webtoon_review.global.config.MongoIndexConfiguration;
import com.hymin.webtoon_review.global.handler.GlobalExceptionHandler;
import com.hymin.webtoon_review.global.resolver.AuthenticationResolver;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@Tag("mongo-replica-set")
class CreateChatMessageMongoIntegrationTest {

    private static final String DATABASE = "webtoon_review_task004_integration";
    private static final String MONGODB_URI = System.getProperty(
        "task004.mongodb.uri",
        "mongodb://localhost:27017/" + DATABASE + "?replicaSet=rs0&directConnection=true"
    );

    private static MongoClient mongoClient;
    private static MongoClient probeClient;
    private static MongoTemplate mongoTemplate;
    private static MongoTemplate probeTemplate;

    private ChatMessageCommandRepository commandRepository;
    private MongoTransactionManager transactionManager;
    private CreateChatMessageCommandService commandService;

    @BeforeAll
    static void connect() {
        mongoClient = MongoClients.create(MONGODB_URI);
        probeClient = MongoClients.create(MONGODB_URI);
        mongoTemplate = new MongoTemplate(mongoClient, DATABASE);
        probeTemplate = new MongoTemplate(probeClient, DATABASE);
    }

    @AfterAll
    static void disconnect() {
        if (probeClient != null) {
            probeClient.close();
        }
        if (mongoClient != null) {
            mongoClient.close();
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        mongoTemplate.getDb().drop();
        new MongoIndexConfiguration().chatMessageMongoIndexInitializer(mongoTemplate).run(null);
        commandRepository = new ChatMessageCommandRepository(mongoTemplate);
        transactionManager = new MongoTransactionManager(
            mongoTemplate.getMongoDatabaseFactory()
        );
        commandService = service(commandRepository);
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken("sender-name", "");
        authentication.setDetails(7L);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void initialCreateReturns201AndCommitsSequenceMessageAndOutbox() throws Exception {
        MvcResult httpResult = mockMvc(commandService).perform(post("/chat/room/101/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson("client-normal", "normal")))
            .andExpect(status().isCreated())
            .andReturn();
        JsonNode response = new ObjectMapper().readTree(
            httpResult.getResponse().getContentAsString()
        );
        assertThat(response.path("status").asInt()).isEqualTo(201);
        String messageId = response.path("data").path("messageId").asText();
        ChatMessage message = probeTemplate.findById(messageId, ChatMessage.class);
        MessageCreatedOutbox outbox = probeTemplate.findOne(
            Query.query(Criteria.where("messageId").is(messageId)),
            MessageCreatedOutbox.class
        );
        RoomSequence sequence = probeTemplate.findById(101L, RoomSequence.class);

        assertThat(message).isNotNull();
        assertThat(outbox).isNotNull();
        assertThat(sequence.getSequence()).isEqualTo(1L);
        assertThat(message.getMessageSequence()).isEqualTo(1L);
        assertThat(outbox.getRoomSequence()).isEqualTo(1L);
        assertThat(outbox.getMessageId()).isEqualTo(message.getId());
        assertThat(outbox.getMessageCreatedAt()).isEqualTo(message.getCreatedAt());
        assertThat(outbox.getMessageBlocks()).usingRecursiveComparison()
            .isEqualTo(message.getMessageBlocks());
    }

    @Test
    void outboxFailureReturnsNon2xxAndRollsBackAllThreeWrites() throws Exception {
        ChatMessageCommandRepository failingRepository = spy(commandRepository);
        doThrow(new IllegalStateException("forced outbox failure"))
            .when(failingRepository).insertOutbox(any(MessageCreatedOutbox.class));
        MockMvc mockMvc = mockMvc(service(failingRepository));

        mockMvc.perform(post("/chat/room/102/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson("client-failure", "failure")))
            .andExpect(status().is5xxServerError());

        assertThat(probeTemplate.count(new Query(), RoomSequence.class)).isZero();
        assertThat(probeTemplate.count(new Query(), ChatMessage.class)).isZero();
        assertThat(probeTemplate.count(new Query(), MessageCreatedOutbox.class)).isZero();
    }

    @Test
    void sequentialRetryReturns200WithoutConsumingAnotherSequence() throws Exception {
        MockMvc mockMvc = mockMvc(commandService);
        String body = requestJson("client-retry", "retry");
        MvcResult firstResult = mockMvc.perform(post("/chat/room/103/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isCreated())
            .andReturn();
        MvcResult retryResult = mockMvc.perform(post("/chat/room/103/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andReturn();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode first = objectMapper.readTree(firstResult.getResponse().getContentAsString());
        JsonNode retry = objectMapper.readTree(retryResult.getResponse().getContentAsString());

        assertThat(retry.path("status").asInt()).isEqualTo(200);
        assertThat(retry.path("data").path("messageId").asText())
            .isEqualTo(first.path("data").path("messageId").asText());
        assertThat(retry.path("data").path("messageSequence").asLong()).isEqualTo(1L);
        assertThat(probeTemplate.count(new Query(), ChatMessage.class)).isEqualTo(1L);
        assertThat(probeTemplate.count(new Query(), MessageCreatedOutbox.class)).isEqualTo(1L);
        assertThat(probeTemplate.findById(103L, RoomSequence.class).getSequence()).isEqualTo(1L);
    }

    @Test
    void sequentialSameKeyDifferentPayloadReturnsExistingDurableMessage() throws Exception {
        MockMvc mockMvc = mockMvc(commandService);
        MvcResult firstResult = mockMvc.perform(post("/chat/room/108/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson("client-payload-variant", "original-content")))
            .andExpect(status().isCreated())
            .andReturn();
        MvcResult retryResult = mockMvc.perform(post("/chat/room/108/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson("client-payload-variant", "changed-content")))
            .andExpect(status().isOk())
            .andReturn();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode first = objectMapper.readTree(firstResult.getResponse().getContentAsString());
        JsonNode retry = objectMapper.readTree(retryResult.getResponse().getContentAsString());

        assertThat(retry.path("status").asInt()).isEqualTo(200);
        assertThat(retry.path("data").path("messageId").asText())
            .isEqualTo(first.path("data").path("messageId").asText());
        assertThat(retry.path("data").path("messageSequence").asLong()).isEqualTo(1L);
        assertThat(probeTemplate.count(new Query(), ChatMessage.class)).isEqualTo(1L);
        assertThat(probeTemplate.count(new Query(), MessageCreatedOutbox.class)).isEqualTo(1L);
        assertThat(probeTemplate.findById(108L, RoomSequence.class).getSequence()).isEqualTo(1L);
    }

    @Test
    void concurrentSameKeyConvergesToOneMessageAndOutbox() throws Exception {
        CreateChatMessageCommand command = command(104L, "same-key");

        List<CreateChatMessageResult> results = runConcurrently(2, ignored ->
            commandService.create(command)
        );

        assertThat(results).extracting(result -> result.message().getId()).containsOnly(
            results.get(0).message().getId()
        );
        assertThat(results).extracting(CreateChatMessageResult::created)
            .containsExactlyInAnyOrder(true, false);
        assertThat(probeTemplate.count(new Query(), ChatMessage.class)).isEqualTo(1L);
        assertThat(probeTemplate.count(new Query(), MessageCreatedOutbox.class)).isEqualTo(1L);
        assertThat(probeTemplate.findById(104L, RoomSequence.class).getSequence()).isEqualTo(1L);
    }

    @Test
    void concurrentSameRoomCreatesUniqueGaplessSequencesForSuccessfulMessages() throws Exception {
        int messageCount = 12;

        List<CreateChatMessageResult> results = runConcurrently(messageCount, index ->
            commandService.create(command(105L, "same-room-" + index))
        );

        List<Long> sequences = results.stream()
            .map(result -> result.message().getMessageSequence())
            .sorted()
            .toList();
        assertThat(results).allMatch(CreateChatMessageResult::created);
        assertThat(sequences).containsExactlyElementsOf(
            LongStream.rangeClosed(1, messageCount).boxed().toList()
        );
        assertThat(probeTemplate.findById(105L, RoomSequence.class).getSequence())
            .isEqualTo(messageCount);
        assertThat(probeTemplate.count(new Query(), MessageCreatedOutbox.class))
            .isEqualTo(messageCount);
    }

    @Test
    void concurrentDifferentRoomsUseIndependentCounters() throws Exception {
        int messagesPerRoom = 6;
        int taskCount = messagesPerRoom * 2;

        List<CreateChatMessageResult> results = runConcurrently(taskCount, index -> {
            long roomId = index % 2 == 0 ? 106L : 107L;
            return commandService.create(command(roomId, "room-" + roomId + "-" + index));
        });

        assertThat(results).hasSize(taskCount).allMatch(CreateChatMessageResult::created);
        assertRoomSequences(106L, messagesPerRoom);
        assertRoomSequences(107L, messagesPerRoom);
    }

    private CreateChatMessageCommandService service(
        ChatMessageCommandRepository repository
    ) {
        UserChatRoomRepository userChatRoomRepository = mock(UserChatRoomRepository.class);
        when(userChatRoomRepository.findRoomMemberId(any(), any()))
            .thenReturn(java.util.Optional.of("member-7"));
        return new CreateChatMessageCommandService(
            userChatRoomRepository,
            repository,
            new ChatMessageCommandMetrics(new SimpleMeterRegistry()),
            transactionManager,
            50
        );
    }

    private MockMvc mockMvc(CreateChatMessageCommandService service) {
        return MockMvcBuilders.standaloneSetup(new ChatMessageCommandController(service))
            .setCustomArgumentResolvers(new AuthenticationResolver())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    private CreateChatMessageCommand command(Long roomId, String clientMessageId) {
        return new CreateChatMessageCommand(
            roomId,
            7L,
            "sender-name",
            clientMessageId,
            blocks(clientMessageId)
        );
    }

    private List<MessageBlock> blocks(String content) {
        return List.of(new MessageBlock(MessageBlockType.TEXT, content, Map.of()));
    }

    private String requestJson(String clientMessageId, String content) {
        return "{\"clientMessageId\":\"" + clientMessageId
            + "\",\"messageBlocks\":[{\"type\":\"TEXT\",\"content\":\"" + content
            + "\",\"metadata\":{}}]}";
    }

    private <T> List<T> runConcurrently(
        int taskCount,
        ThrowingFunction<Integer, T> function
    ) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(taskCount);
        CountDownLatch ready = new CountDownLatch(taskCount);
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Future<T>> futures = new ArrayList<>();
            for (int index = 0; index < taskCount; index++) {
                int taskIndex = index;
                futures.add(executor.submit(() -> {
                    ready.countDown();
                    assertThat(start.await(10, TimeUnit.SECONDS)).isTrue();
                    return function.apply(taskIndex);
                }));
            }
            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            List<T> results = new ArrayList<>();
            for (Future<T> future : futures) {
                results.add(future.get(30, TimeUnit.SECONDS));
            }
            return results;
        } finally {
            executor.shutdownNow();
        }
    }

    private void assertRoomSequences(long roomId, int expectedCount) {
        List<Long> sequences = probeTemplate.find(
                Query.query(Criteria.where("roomId").is(roomId)),
                ChatMessage.class
            ).stream()
            .map(ChatMessage::getMessageSequence)
            .sorted(Comparator.naturalOrder())
            .toList();
        assertThat(sequences).containsExactlyElementsOf(
            LongStream.rangeClosed(1, expectedCount).boxed().toList()
        );
        assertThat(probeTemplate.findById(roomId, RoomSequence.class).getSequence())
            .isEqualTo(expectedCount);
    }

    @FunctionalInterface
    private interface ThrowingFunction<I, O> {

        O apply(I input) throws Exception;
    }

}
