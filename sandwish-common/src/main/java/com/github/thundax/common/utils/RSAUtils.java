package com.github.thundax.common.utils;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

public class RSAUtils {

    private static final String ALGOR = "RSA";
    private static final String PROVIDER = "BC";
    private static final int KEY_SIZE = 1024;
    private static final String CLIPHER_TRANSFORMATION = "RSA/None/PKCS1Padding";
    private static final String CHARSET = "UTF-8";

    static {
        /**
         * 这里需使用BouncyCastleProvider，作为编解码的内核，原因是：
         * 客户单的jsencrypt.js在作编码的时候，使用了随机数，对同一个pubkey的每次编码，会产生不同的结果
         */
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    /**
     * 生成钥对，生成的数据存储在map<String, String>中
     * RSAUtils.PUBKEY：base64编码的public key,用于发送给浏览器的jsencrypt.js
     * RSAUtils.PUBKEY_MODULUS与RSAUtils.PRIKEY_EXPONENT用于存储在Session中，以解码时使用
     */
    public static ReadableKeyPair generateKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(ALGOR, PROVIDER);
            generator.initialize(KEY_SIZE, new SecureRandom());
            KeyPair keyPair = generator.generateKeyPair();

            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

            String publicKeyText = new String(Base64.encodeBase64(publicKey.getEncoded()));
            String modulus = publicKey.getModulus().toString(16);
            String privateKeyExponent = privateKey.getPrivateExponent().toString(16);
            String publicKeyExponent = publicKey.getPublicExponent().toString(16);

            return new ReadableKeyPair(publicKeyText, modulus, publicKeyExponent, privateKeyExponent);
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    /**
     * 编码
     *
     * @param plainText 原文
     */
    public static String encrypt(String plainText, ReadableKeyPair keyPair) {
        if (StringUtils.isBlank(plainText) || keyPair == null || keyPair.getModulus() == null) {
            return StringUtils.EMPTY;
        }
        try {
            BigInteger modulus = new BigInteger(keyPair.getModulus(), 16);
            BigInteger publicKeyExponent = new BigInteger(keyPair.getPublicKeyExponent(), 16);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGOR);
            RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(modulus, publicKeyExponent);
            RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(publicKeySpec);

            Cipher cipher = Cipher.getInstance(CLIPHER_TRANSFORMATION, PROVIDER);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeBase64String(encryptedBytes);
        } catch (Exception e) {
            return StringUtils.EMPTY;
        }
    }

    /**
     * 解码，这里可能会存在加密数据过长的问题
     *
     * @param encryptBase64Text base64编码后的加密数据
     */
    public static String decryptBase64(String encryptBase64Text, ReadableKeyPair keyPair) {
        if (StringUtils.isBlank(encryptBase64Text) || keyPair == null || keyPair.getModulus() == null) {
            return StringUtils.EMPTY;
        }
        try {
            BigInteger modulus = new BigInteger(keyPair.getModulus(), 16);
            BigInteger privateKeyExponent = new BigInteger(keyPair.getPrivateKeyExponent(), 16);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGOR);
            RSAPrivateKeySpec privateKeySpec = new RSAPrivateKeySpec(modulus, privateKeyExponent);
            RSAPrivateKey privateKey = (RSAPrivateKey) keyFactory.generatePrivate(privateKeySpec);

            Cipher cipher = Cipher.getInstance(CLIPHER_TRANSFORMATION, PROVIDER);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] plainText = cipher.doFinal(Base64.decodeBase64(encryptBase64Text));
            return new String(plainText, CHARSET);

        } catch (Exception e) {
            return StringUtils.EMPTY;
        }
    }

    /**
     * 存储生成好的public key（base64 encode），modulus,private key exponents
     */
    public static class ReadableKeyPair implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * Base64编码的公钥，用于发送给客户端的jsencrypt.js
         */
        private String publicKey;
        /**
         * public key modulus，用于存储在会话中，与privateKeyExponent一起用来还原key，以解密
         */
        private String modulus;
        /**
         * private key exponent，用于存储在会话中，与modulus一起用来还原key，以解密
         */
        private String privateKeyExponent;

        private String publicKeyExponent;

        public ReadableKeyPair(String publicKey, String modulus, String publicKeyExponent, String privateKeyExponent) {
            this.setPublicKey(publicKey);
            this.setModulus(modulus);
            this.setPublicKeyExponent(publicKeyExponent);
            this.setPrivateKeyExponent(privateKeyExponent);
        }

        /**
         * get/set publicKey
         */
        public String getPublicKey() {
            return this.publicKey;
        }

        public void setPublicKey(String publicKey) {
            this.publicKey = publicKey;
        }

        /**
         * get/set modulus
         */
        public String getModulus() {
            return this.modulus;
        }

        public void setModulus(String modulus) {
            this.modulus = modulus;
        }

        /**
         * get/set privateKeyExponent
         */
        public String getPrivateKeyExponent() {
            return this.privateKeyExponent;
        }

        public void setPrivateKeyExponent(String privateKeyExponent) {
            this.privateKeyExponent = privateKeyExponent;
        }

        /**
         * get/set publicKeyExponent
         */
        public String getPublicKeyExponent() {
            return this.publicKeyExponent;
        }

        public void setPublicKeyExponent(String publicKeyExponent) {
            this.publicKeyExponent = publicKeyExponent;
        }
    }

    public static void main(String[] args) throws IOException {
        int port = 8899;
        //定义一个ServerSocket监听在端口8899上
        ServerSocket server = new ServerSocket(port);
        //server尝试接收其他Socket的连接请求，server的accept方法是阻塞式的
        Socket socket = server.accept();
        //跟客户端建立好连接之后，我们就可以获取socket的InputStream，并从中读取客户端发过来的信息了。
        Reader reader = new InputStreamReader(socket.getInputStream());
        char[] chars = new char[12800];
        int len;
        StringBuilder sb = new StringBuilder();
        while ((len = reader.read(chars)) != -1) {
            sb.append(new String(chars, 0, len));
            System.out.println("from client: " + sb);
            System.out.println("from client: " + sb);
        }
        System.out.println("from client: " + sb);
        reader.close();
        socket.close();
        server.close();
    }

}
