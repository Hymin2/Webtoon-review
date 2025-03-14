package com.hymin.webtoon_review.util;

import java.nio.ByteBuffer;
import java.util.UUID;

public class UUIDConverter {

    public static byte[] toByte(UUID uuid) {
        ByteBuffer buffer = ByteBuffer.wrap(new byte[16]);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());

        return buffer.array();
    }

    public static String toUUID(byte[] uuid) {
        ByteBuffer buffer = ByteBuffer.wrap(uuid);
        long mostSigBits = buffer.getLong();
        long leastSigBits = buffer.getLong();

        return new UUID(mostSigBits, leastSigBits).toString();
    }
}
