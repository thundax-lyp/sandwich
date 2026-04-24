package com.github.thundax.common.utils.encrypt;

import javax.crypto.*;
import javax.crypto.spec.DESKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import static com.github.thundax.common.utils.StringUtils.byte2hex;
import static com.github.thundax.common.utils.StringUtils.hex2byte;

/**
 * @author thundax
 */
public class Des {

    private static final String ALGORITHM_DES = "DES";

    public static byte[] encrypt(byte[] plainBytes, byte[] password) {
        try {
            SecureRandom random = new SecureRandom();
            DESKeySpec keySpec = new DESKeySpec(password);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM_DES);
            SecretKey key = keyFactory.generateSecret(keySpec);
            Cipher cipher = Cipher.getInstance(ALGORITHM_DES);
            cipher.init(Cipher.ENCRYPT_MODE, key, random);
            return cipher.doFinal(plainBytes);

        } catch (InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException
                | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] decrypt(byte[] encryptedBytes, byte[] password) {
        try {
            SecureRandom random = new SecureRandom();
            DESKeySpec keySpec = new DESKeySpec(password);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM_DES);
            SecretKey key = keyFactory.generateSecret(keySpec);
            Cipher cipher = Cipher.getInstance(ALGORITHM_DES);
            cipher.init(Cipher.DECRYPT_MODE, key, random);
            return cipher.doFinal(encryptedBytes);

        } catch (InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException
                | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String encrypt(String dataSource, String password) {
        byte[] bytes = encrypt(dataSource.getBytes(), password.getBytes(StandardCharsets.UTF_8));
        if (bytes != null) {
            return byte2hex(bytes);
        }
        return null;
    }

    public static String decrypt(String hexText, String password) {
        byte[] bytes = decrypt(hex2byte(hexText), password.getBytes(StandardCharsets.UTF_8));
        if (bytes != null) {
            return new String(bytes, StandardCharsets.UTF_8);
        }
        return null;
    }

}
