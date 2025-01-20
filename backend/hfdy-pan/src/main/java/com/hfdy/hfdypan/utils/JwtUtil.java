package com.hfdy.hfdypan.utils;

import com.hfdy.hfdypan.constants.JwtConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecureDigestAlgorithm;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static com.hfdy.hfdypan.constants.JwtConstants.*;

/**
 * @author hf-dy
 * @date 2024/10/22 10:10
 */

public class JwtUtil {


    /**
     * 生成jwt
     * 使用Hs256算法, 私匙使用固定秘钥
     *
     * @param secretKey jwt秘钥
     * @param ttlMillis jwt过期时间(毫秒)
     * @param claims    设置的信息
     * @return 加密结果
     */
    private static String createJWT(String secretKey, long ttlMillis, Map<String, Object> claims) {
        // 密钥实例
        final SecretKey secretKey1 = Keys.hmacShaKeyFor(secretKey.getBytes());
        // 令牌id
        String uuid = UUID.randomUUID().toString();
        Date expireDate = Date.from(Instant.now().plusSeconds(ttlMillis));
        return Jwts.builder()
                // 头部信息
                .header().add("typ", "jwt").add("alg", "HS256").and()
                // 载荷
                .claims(claims)
                // 令牌id
                .id(uuid)
                // 过期时间
                .expiration(expireDate)
                // 签发者
                .issuer(JwtConstants.JWT_ISS)
                // 签名
                .signWith(secretKey1, ALGORITHM)
                .compact();
    }


    /**
     * 创建access token
     *
     * @param claims
     * @return
     */
    public static String createAccessToken(Map<String, Object> claims) {
        return createJWT(ACCESS_TOKEN_KEY, ACCESS_TOKEN_EXPIRES, claims);
    }

    /**
     * 创建refresh token
     *
     * @param claims
     * @return
     */
    public static String createRefreshToken(Map<String, Object> claims) {
        return createJWT(REFRESH_TOKEN_KEY, REFRESH_TOKEN_EXPIRES, claims);
    }

    /**
     * Token解密
     *
     * @param secretKey jwt秘钥 此秘钥一定要保留好在服务端, 不能暴露出去, 否则sign就可以被伪造, 如果对接多个客户端建议改造成多个
     * @param token     加密后的token
     * @return
     */
    private static Claims parseJWT(String secretKey, String token) {
        final SecretKey secretKey1 = Keys.hmacShaKeyFor(secretKey.getBytes());
        return Jwts.parser()
                .verifyWith(secretKey1).build().parseSignedClaims(token).getPayload();
    }


    /**
     * 解析access token
     *
     * @param token
     * @return
     */
    public static Claims parseAccessToken(String token) {
        return parseJWT(ACCESS_TOKEN_KEY, token);
    }


    /**
     * 解析refresh token
     *
     * @param token
     * @return
     */
    public static Claims parseRefreshToken(String token) {
        return parseJWT(REFRESH_TOKEN_KEY, token);
    }
}