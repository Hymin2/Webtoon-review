package com.hymin.webtoon_review.util;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.UUID;

public class UUIDCompressor {

    public static String encode(UUID uuid) {
        ByteBuffer buffer = ByteBuffer.wrap(new byte[16]);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buffer.array());
    }

    public static String decode(String compressedUUID) {
        byte[] bytes = Base64.getUrlDecoder().decode(compressedUUID);
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        return new UUID(buffer.getLong(), buffer.getLong()).toString();
    }
}
