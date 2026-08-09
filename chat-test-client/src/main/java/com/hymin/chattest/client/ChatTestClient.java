package com.hymin.chattest.client;

import com.hymin.chattest.contract.ChatMessageRequest;
import com.hymin.chattest.contract.ChatMessageResponse;
import com.hymin.chattest.support.ChatTestProperties;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

public class ChatTestClient implements AutoCloseable {

    private static final String SEND_DESTINATION = "/pub/chat/messages";

    private final ChatTestProperties properties;
    private final WebSocketStompClient stompClient;
    private final List<ChatMessageResponse> receivedMessages = new CopyOnWriteArrayList<>();
    private final AtomicReference<Throwable> connectionFailure = new AtomicReference<>();
    private final Object receiveMonitor = new Object();

    private StompSession session;

    public ChatTestClient(ChatTestProperties properties) {
        this.properties = properties;
        this.stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        this.stompClient.setMessageConverter(new MappingJackson2MessageConverter());
    }

    public void connect(String accessToken) {
        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add("Authorization", "Bearer " + accessToken);
        connectHeaders.add("X-Client-Id", properties.clientId());

        try {
            session = stompClient.connectAsync(
                    properties.webSocketUrl().toString(),
                    new WebSocketHttpHeaders(),
                    connectHeaders,
                    new ConnectionHandler())
                .get(properties.timeout().toMillis(), TimeUnit.MILLISECONDS);
        } catch (Exception exception) {
            throw new IllegalStateException("STOMP 연결에 실패했습니다.", exception);
        }
    }

    public StompSession.Subscription subscribe(long roomId) {
        requireConnected();
        return session.subscribe("/user/queue/room/" + roomId, new MessageHandler());
    }

    public void send(ChatMessageRequest request) {
        requireConnected();
        session.send(SEND_DESTINATION, request);
    }

    public boolean awaitMessageCount(int expectedCount) {
        return awaitMessageCount(expectedCount, properties.timeout());
    }

    public boolean awaitMessageCount(int expectedCount, Duration timeout) {
        long deadline = System.nanoTime() + timeout.toNanos();

        synchronized (receiveMonitor) {
            while (receivedMessages.size() < expectedCount && connectionFailure.get() == null) {
                long remaining = deadline - System.nanoTime();
                if (remaining <= 0) {
                    break;
                }
                try {
                    TimeUnit.NANOSECONDS.timedWait(receiveMonitor, remaining);
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
        }
        return receivedMessages.size() >= expectedCount;
    }

    public List<ChatMessageResponse> receivedMessages() {
        return List.copyOf(receivedMessages);
    }

    public Throwable connectionFailure() {
        return connectionFailure.get();
    }

    public boolean isConnected() {
        return session != null && session.isConnected();
    }

    @Override
    public void close() {
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
        stompClient.stop();
    }

    private void requireConnected() {
        if (!isConnected()) {
            throw new IllegalStateException("STOMP 세션이 연결되어 있지 않습니다.");
        }
    }

    private void recordFailure(Throwable exception) {
        connectionFailure.compareAndSet(null, exception);
        synchronized (receiveMonitor) {
            receiveMonitor.notifyAll();
        }
    }

    private class ConnectionHandler extends StompSessionHandlerAdapter {

        @Override
        public void handleException(
            StompSession stompSession,
            StompCommand command,
            StompHeaders headers,
            byte[] payload,
            Throwable exception
        ) {
            recordFailure(exception);
        }

        @Override
        public void handleTransportError(StompSession stompSession, Throwable exception) {
            recordFailure(exception);
        }
    }

    private class MessageHandler implements StompFrameHandler {

        @Override
        public Type getPayloadType(StompHeaders headers) {
            return ChatMessageResponse.class;
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            receivedMessages.add((ChatMessageResponse) payload);
            synchronized (receiveMonitor) {
                receiveMonitor.notifyAll();
            }
        }
    }
}
