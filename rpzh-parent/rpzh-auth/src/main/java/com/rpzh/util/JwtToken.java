package com.rpzh.util;

import com.alibaba.fastjson.JSON;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.util.HashMap;
import java.util.Map;

/**
 * 服务间调用用的内部令牌生成器。
 * 密钥库参数可用 JVM 参数覆盖，便于轮换证书时与 AuthorizationServerConfig 保持一致。
 */
public class JwtToken {

    private static final String KEYSTORE = System.getProperty("rpzh.jwt.keystore", "fjxmrpzh.jks");
    private static final String KEYSTORE_SECRET = System.getProperty("rpzh.jwt.keystore-secret", "fjxmrpzh");
    private static final String KEYSTORE_ALIAS = System.getProperty("rpzh.jwt.keystore-alias", "fjxmrpzh");
    private static final String KEY_PASSWORD = System.getProperty("rpzh.jwt.key-password", "fjxmrpzh");

    private static volatile RsaSigner signer;

    private JwtToken() {
    }

    /**
     * 获取管理员令牌
     */
    public static String adminJwt() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("authorities", new String[]{"admin"});
        Jwt jwt = JwtHelper.encode(JSON.toJSONString(map), getSigner());
        return jwt.getEncoded();
    }

    /** 每次 Feign 调用都重新加载 JKS 开销过大，这里全局复用签名器。 */
    private static RsaSigner getSigner() {
        if (signer == null) {
            synchronized (JwtToken.class) {
                if (signer == null) {
                    Resource resource = new ClassPathResource(KEYSTORE);
                    KeyStoreKeyFactory factory = new KeyStoreKeyFactory(resource, KEYSTORE_SECRET.toCharArray());
                    KeyPair keyPair = factory.getKeyPair(KEYSTORE_ALIAS, KEY_PASSWORD.toCharArray());
                    PrivateKey privateKey = keyPair.getPrivate();
                    signer = new RsaSigner((RSAPrivateKey) privateKey);
                }
            }
        }
        return signer;
    }
}
