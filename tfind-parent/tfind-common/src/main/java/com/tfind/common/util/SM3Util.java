package com.tfind.common.util;

import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.util.encoders.Hex;

public class SM3Util {

    public static String encrypt(String input) {
        byte[] bytes = input.getBytes();
        SM3Digest digest = new SM3Digest();
        digest.update(bytes, 0, bytes.length);
        byte[] result = new byte[digest.getDigestSize()];
        digest.doFinal(result, 0);
        return Hex.toHexString(result);
    }

    public static String hash(String input) {
        return encrypt(input);
    }

    public static boolean verify(String input, String hashed) {
        String encrypted = encrypt(input);
        return encrypted.equalsIgnoreCase(hashed);
    }

    private SM3Util() {
    }
}
