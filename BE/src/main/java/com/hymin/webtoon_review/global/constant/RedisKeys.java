package com.hymin.webtoon_review.global.constant;

public class RedisKeys {

    public static final String ACTIVE_CHAT_SERVER_PREFIX = "active-chat-servers:";
    public static final String CHAT_WORKER_HEALTH = "chat-worker:health";
    public static final String CHAT_WORKER_RECOVER_LOCK = "chat-worker:recover:lock:";

    public static final String CHAT_SERVER_PREFIX = "chat-server:";
    public static final String CHAT_SERVER_CONNECTED_USER_POSTFIX = ":connected-users";

    public static final String CHAT_WORKER_PREFIX = "chat-worker:";
    public static final String CHAT_WORKER_HASH_VALUES_POSTFIX = ":hash-values";
    public static final String CHAT_WORKER_HASH_RING_ZSET = "chat-worker:hash-ring";

    public static final String CHAT_ROOM_PREFIX = "chat:room:";
    public static final String CHAT_ROOM_MESSAGE_SEQUENCE_POSTFIX = ":seq";
    public static final String CHAT_ROOM_MEMBERS_POSTFIX = ":members";
    public static final String CHAT_ROOM_ONLINE_MEMBERS_POSTFIX = ":online:members";
    public static final String CHAT_ROOM_RECENT_MESSAGES_POSTFIX = ":recent-messages";

    public static final String CHAT_MESSAGE_SERVER_ID_PREFIX = "chat:message:server-id:";

    public static final String USER_PREFIX = "user:";
    public static final String USER_CHAT_SESSION_PREFIX = ":chat:session:";
    public static final String USER_CHAT_JOINED_ROOM_PREFIX = ":chat:joined-room:";
    public static final String USER_REFRESH_TOKEN_POSTFIX = ":refresh-token";

    public static final String ACCESS_TOKEN_BLACKLISTS_PREFIX = "access-token:blacklist:";
}
