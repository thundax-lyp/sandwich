package com.github.thundax.common.utils;

import java.security.Key;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Sm4Util {

    public static final String ALGORITHM_NAME = "SM4";
    public static final String ALGORITHM_NAME_ECB_PADDING = "SM4/ECB/PKCS5Padding";
    public static final int DEFAULT_KEY_SIZE = 128;

    private static final Logger LOGGER = LoggerFactory.getLogger(Sm4Util.class);
    private static final String KEY = "F7EFA739963909A0BEA56F8C2DE7CAC8";
    private static final String ENCODING = "UTF-8";

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static String generateKey() throws Exception {
        return new String(Hex.encodeHex(generateKey(DEFAULT_KEY_SIZE), false));
    }

    public static byte[] generateKey(int keySize) throws Exception {
        KeyGenerator kg = KeyGenerator.getInstance(ALGORITHM_NAME, BouncyCastleProvider.PROVIDER_NAME);
        kg.init(keySize, new SecureRandom());
        return kg.generateKey().getEncoded();
    }

    private static Cipher generateEcbCipher(String algorithmName, int mode, byte[] key) throws Exception {
        Cipher cipher = Cipher.getInstance(algorithmName, BouncyCastleProvider.PROVIDER_NAME);
        Key sm4Key = new SecretKeySpec(key, ALGORITHM_NAME);
        cipher.init(mode, sm4Key);
        return cipher;
    }

    public static String encryptEcb(String paramStr) {
        try {
            String cipherText = "";
            byte[] keyData = ByteUtils.fromHexString(KEY);
            byte[] srcData = paramStr.getBytes(ENCODING);
            byte[] cipherArray = encryptEcbPadding(keyData, srcData);
            cipherText = ByteUtils.toHexString(cipherArray);
            return cipherText;
        } catch (Exception e) {
            return paramStr;
        }
    }

    public static byte[] encryptEcbPadding(byte[] key, byte[] data) throws Exception {
        Cipher cipher = generateEcbCipher(ALGORITHM_NAME_ECB_PADDING, Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data);
    }

    public static String decryptEcb(String cipherText) {
        String decryptStr = "";
        byte[] keyData = ByteUtils.fromHexString(KEY);
        byte[] cipherData = ByteUtils.fromHexString(cipherText);
        byte[] srcData = new byte[0];
        try {
            srcData = decryptEcbPadding(keyData, cipherData);
            decryptStr = new String(srcData, ENCODING);
        } catch (Exception e) {
            LOGGER.warn("can not decrypt sm4 ecb value", e);
        }
        return decryptStr;
    }

    public static byte[] decryptEcbPadding(byte[] key, byte[] cipherText) throws Exception {
        Cipher cipher = generateEcbCipher(ALGORITHM_NAME_ECB_PADDING, Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(cipherText);
    }

    public static boolean verifyEcb(String cipherText, String paramStr) throws Exception {
        boolean flag = false;
        byte[] keyData = ByteUtils.fromHexString(KEY);
        byte[] cipherData = ByteUtils.fromHexString(cipherText);
        byte[] decryptData = decryptEcbPadding(keyData, cipherData);
        byte[] srcData = paramStr.getBytes(ENCODING);
        flag = Arrays.equals(decryptData, srcData);
        return flag;
    }

    public static void main(String[] args) {
        try {
            String json =
                    "BF7B6BD7C1204BC4F3C87D235692DE9DBF7B6BD7C1204BC4F3C87D235692DE9DBF7B6BD7C1204BC4F3C87D235692DE9D";
            System.out.println("加密前源数据————" + json);
            String key = Sm4Util.generateKey();
            System.out.println(key + "-----生成key");
            String cipher = Sm4Util.encryptEcb(json);
            System.out.println("加密串---" + cipher);
            System.out.println(Sm4Util.verifyEcb(cipher, json));
            json = Sm4Util.decryptEcb(cipher);
            System.out.println("解密后数据---" + json);
        } catch (Exception e) {
            LOGGER.warn("can not run sm4 sample", e);
        }
    }
}
