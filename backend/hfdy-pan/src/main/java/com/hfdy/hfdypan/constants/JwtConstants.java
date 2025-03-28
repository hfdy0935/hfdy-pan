package com.hfdy.hfdypan.constants;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SecureDigestAlgorithm;

import javax.crypto.SecretKey;

/**
 * @author hf-dy
 * @date 2024/10/22 22:47
 */

public class JwtConstants {
    /**
     * token在header中的字段名
     */
    public static final String TOKEN_FIELD_IN_HEADER = "Authorization";
    /**
     * 载荷的key
     */
    public static final String CLAIMS_KEY = "user_id";

    /**
     * 加密算法
     */
    public static final SecureDigestAlgorithm<SecretKey, SecretKey> ALGORITHM = Jwts.SIG.HS256;
    /**
     * access token的key
     */
    public static final String ACCESS_TOKEN_KEY = "hfdy-pan-access-key-123456-111111111111111111111111";

    /**
     * refresh token的key
     */
    public static final String REFRESH_TOKEN_KEY = "hfdy-pan-refresh-key-654321-22222222222222222222222222222222";

    /**
     * access token有效时间
     */
    public static final Long ACCESS_TOKEN_EXPIRES = 2 * 60 * 60L;

    /**
     * refresh token有效时间
     */
    public static final Long REFRESH_TOKEN_EXPIRES = 12 * 60 * 60L;

    /**
     * jwt签发者
     */
    public static final String JWT_ISS = "hfdy";

}
