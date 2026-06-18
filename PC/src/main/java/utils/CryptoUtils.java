package utils;

import java.nio.charset.StandardCharsets;

public class CryptoUtils {
    private static final byte[] XOR_KEY = "gfh98H6dEG6H47d".getBytes(StandardCharsets.UTF_8);

    public static byte[] xor(byte[] data) {
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = (byte) (data[i] ^ XOR_KEY[i % XOR_KEY.length]);
        }
        return result;
    }

    public static void xorInPlace(byte[] data, int off, int len, long fileOffset) {
        for (int i = 0; i < len; i++) {
            data[off + i] ^= XOR_KEY[(int) ((fileOffset + i) % XOR_KEY.length)];
        }
    }
}
