package com.bgs.common;

import android.util.Base64;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Created by zhufre on 7/23/2016.
 */
public class UUIDUtils {

    public static String uuidAsBase64() {
        UUID uuid = UUID.randomUUID();
        return uuidToBase64(uuid);
    }

    private static String uuidToBase64(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return Base64.encodeToString(bb.array(), Base64.NO_WRAP);
    }
    public static String uuidFromBase64(String str) {
        byte[] bytes = Base64.decode(str, Base64.NO_WRAP);
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        UUID uuid = new UUID(bb.getLong(), bb.getLong());
        return uuid.toString();
    }
}
