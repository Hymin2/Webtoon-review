package com.hymin.webtoon_review.chat.connection.manager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageListenerManager {

    private final RedisMessageListenerContainer redisMessageListenerContainer;
    private final Map<String, MessageListener> channelListeners = new ConcurrentHashMap<>();

    public void addSubscription(String channelName, MessageListener listener) {
        ChannelTopic topic = new ChannelTopic(channelName);

        if (channelListeners.containsKey(channelName)) {
            return;
        }

        redisMessageListenerContainer.addMessageListener(listener, topic);
        channelListeners.put(channelName, listener);
    }

    public void removeSubscription(String channelName) {
        MessageListener listener = channelListeners.remove(channelName);

        if (listener != null) {
            redisMessageListenerContainer.removeMessageListener(listener,
                new ChannelTopic(channelName));
        }
    }
}
