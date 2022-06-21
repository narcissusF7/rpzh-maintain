package com.rpzh.util;




import org.apache.tomcat.util.codec.binary.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class RSAUtil {

    //生成秘钥对
    public static KeyPair getKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        return keyPair;
    }

    //获取公钥(Base64编码)
    public static String getPublicKey(KeyPair keyPair){
        PublicKey publicKey = keyPair.getPublic();
        byte[] bytes = publicKey.getEncoded();
        return byte2Base64(bytes);
    }

    //获取私钥(Base64编码)
    public static String getPrivateKey(KeyPair keyPair){
        PrivateKey privateKey = keyPair.getPrivate();
        byte[] bytes = privateKey.getEncoded();
        return byte2Base64(bytes);
    }

    //将Base64编码后的公钥转换成PublicKey对象
    public static PublicKey string2PublicKey(String pubStr) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] keyBytes = base642Byte(pubStr);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(keySpec);
        return publicKey;
    }

    //将Base64编码后的私钥转换成PrivateKey对象
    public static PrivateKey string2PrivateKey(String priStr) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] keyBytes = base642Byte(priStr);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
        return privateKey;
    }

    //公钥加密
    public static byte[] publicEncrypt(byte[] content, PublicKey publicKey) throws NoSuchPaddingException,
            NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] bytes = cipher.doFinal(content);
        return bytes;
    }

    //私钥解密
    public static byte[] privateDecrypt(byte[] content, PrivateKey privateKey) throws Exception{
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] bytes = cipher.doFinal(content);
        return bytes;
    }

    //字节数组转Base64编码
    public static String byte2Base64(byte[] bytes){
        return new String(Base64.encodeBase64(bytes));
    }

    //Base64编码转字节数组
    public static byte[] base642Byte(String base64Key) {
        return Base64.decodeBase64(base64Key);
    }

//    public static String rsaSignHex(String content, String privateKey,String signType) {
//        try {
//            PKCS8EncodedKeySpec priPKCS8    = new PKCS8EncodedKeySpec( Base64.decode(privateKey) );
//            KeyFactory keyf = KeyFactory.getInstance("RSA");
//            PrivateKey priKey = keyf.generatePrivate(priPKCS8);//privateKey格式不会，这儿会crash
//            java.security.Signature signature = java.security.Signature.getInstance(signType);// "SHA-256"  "SHA1WithRSA"  SHA256withRSA
//            signature.initSign(priKey);
//            signature.update( content.getBytes());
//            byte[] signed = signature.sign();
//            return bytesToHex(signed);
//        }catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

    public static void main(String args[]) throws Exception {

        String publicKeyStr = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAoAXq3d8gH3CJBa7IMsRUVbEyZWW6a0JN1nKFGaeXTf2geo2Py3Z7/Lhtq6pY8bK9/Ux0LWkH8g09f3tWybg7nn9Xsj+x/AnucmGKG7qJQu1PqRB+NnbE9yIhxPQB0qRLDmruOLO4y0Jc6wTMbLyisEbZYtP3uPdaCZqjKYqVBNaz8u3YWdULDgt2FM1e1NaOwJXh1K2DyI9wnn+iLMk0+uCneA+8L6QlNasFwOer8htcBPYZCxIlHpJrYE54m/YPQNsJkrk4mHinDfNO+ba2ESo+nVzWpJmE+QIISz81rXx798com3O+wLfsq1zhjk5XwRuPibvmdRslQNs2dScHnwIDAQAB";
        String privateKeyStr = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCgBerd3yAfcIkFrsgyxFRVsTJlZbprQk3WcoUZp5dN/aB6jY/Ldnv8uG2rqljxsr39THQtaQfyDT1/e1bJuDuef1eyP7H8Ce5yYYobuolC7U+pEH42dsT3IiHE9AHSpEsOau44s7jLQlzrBMxsvKKwRtli0/e491oJmqMpipUE1rPy7dhZ1QsOC3YUzV7U1o7AleHUrYPIj3Cef6IsyTT64Kd4D7wvpCU1qwXA56vyG1wE9hkLEiUekmtgTnib9g9A2wmSuTiYeKcN8075trYRKj6dXNakmYT5AghLPzWtfHv3xyibc77At+yrXOGOTlfBG4+Ju+Z1GyVA2zZ1JwefAgMBAAECggEAPTWGPQ/Xza7R+qZ8VU7bo1uAF2DuCZD3DQwI9WMxcNeOxtHXKtBLZMN2z679DpZ8LaNM3QHL1D5UQ6r963LY+ytidAABKJLz7jR5C1oynrL/QX/ZhyOu+4jGuftLpzLIst8b9/xmtvjPaeYP+5e78wlmpXLG0q1qdzn7JyynC44pt4MHlJacmUMpEZK6+lRhymJ9BeIkZKntFzSJkkpgLzAH3QpHQ0ASn83Vqq2XbF5jhuEMX8wZEx0xA8lPTtTR9uF6jqofWmiD5vhm8sy/azYDZKoOzrG3EurNfOrS9DJJBbg3GtSSE+YuzITKqD+P09NsXAvBZYuzwJKYaW5fYQKBgQDLUzm/K3dUANUTP6MRgIU7Kc3YlNr6sxwafWgyldeTmYe0mu8PJNo1klZyR4bgOPCiEO4c1wn9yYwwiJPUfUsS1mY+r9I8Senrn1vxWl2paZW0izGjihvMlLMmy3TyM0T92uIW0SJJcXHdiGj6CgzbPcr5WlldKD1xNebYah47cwKBgQDJetml0+d8ecAazv/F0qEFf54CODEzMNYhyk/XHNjZxtBcFF/TQtccc1QHrxobEpuC4EEj8MotTdpGbwzrhbAAleJgGxp5tSFlnR1z+ZYOFbG4PmSt7aytrtaTpOCw4XhZJQLXBLxE0TYZusrRnGpqi7JLmbahJIl1qeoartnQJQKBgDJ1TPdsSgjGzBoiyM0nH+k2zqbZCh79JBRO2vTyyrUOfOcNLBaOBrkUv+9MYOR0PQN7QBxYIVqiEfBZdJZfIapjGqyj5YCgtzSKL78NtgC8+zH6+S6kr5FttagDBdq1boGxODqBXAjt5lwb5jFnHRmrlBudmF/OBLF7tvYuH6KRAoGACdsupoQVQOub0DPsIztIkUJK8oTZM03iS8jf2wAYQ58MNlTw0x7j8wpZQ/724fgpD1/1D9MJsOmsEeIA2cyybXTnnBf3VZjcx2HGPRQqmQnKpd63giKsXdq9uH9KVpzRDFtZJjefFia95MhUf6iNNCUdVcFxQ7TL3KuxPhjrotkCgYBuwC49FkQjCRxMKXv4BRIrMDaCy+LR2uqxOxsHFjO1kUFKlzRNDx6U+3pDbMeMjCPl+7Ycd/YHBD/pPUuUDTMQ+YmEiV7ujBDHwMglsnYt7MyjY9HYEEzF7BsEP14xX56b/xNw9ZPjc/3A8mCLIyTCQWeMO15nbMQvitkVq3S67A==";

        PublicKey publicKey = RSAUtil.string2PublicKey(publicKeyStr);

        byte[] encryptUser = RSAUtil.publicEncrypt("acc001".getBytes(), publicKey);
        byte[] encryptPwd = RSAUtil.publicEncrypt("22s2Yu3".getBytes(), publicKey);

//        System.out.println(RSAUtil.byte2Base64(encrypt));



        String secUser = URLEncoder.encode(RSAUtil.byte2Base64(encryptUser),"GBK");
        String secPwd = URLEncoder.encode(RSAUtil.byte2Base64(encryptPwd),"GBK");

        System.out.println("加密secUser："+secUser);
        System.out.println("加密secPwd："+secPwd);


        PrivateKey privateKey = RSAUtil.string2PrivateKey(privateKeyStr);
        byte[] base642ByteUser = RSAUtil.base642Byte(URLDecoder.decode(secUser,"GBK"));
        byte[] base642BytePwd = RSAUtil.base642Byte(URLDecoder.decode(secPwd,"GBK"));

        byte[] privateDecryptUser = RSAUtil.privateDecrypt(base642ByteUser, privateKey);
        byte[] privateDecryptPwd = RSAUtil.privateDecrypt(base642BytePwd, privateKey);

        System.out.println("解密privateDecryptUser："+new String(privateDecryptUser));
        System.out.println("解密privateDecryptPwd："+new String(privateDecryptPwd));

    }
}
