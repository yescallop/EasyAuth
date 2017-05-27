package cn.yescallop.easyauth.util;

import cn.nukkit.utils.Binary;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

public class PasswordUtil {

    public static String randomSaltString() {
        return Binary.bytesToHexString(randomSalt());
    }

    public static byte[] randomSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return salt;
    }

    public static byte[] digestPassword(String password, byte[] salt) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
            digest.update(salt);
            digest.update(password.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return new byte[0];
        }
        return digest.digest();
    }

    public static String digestPasswordToString(String password, byte[] salt) {
        return Binary.bytesToHexString(digestPassword(password, salt));
    }
}
