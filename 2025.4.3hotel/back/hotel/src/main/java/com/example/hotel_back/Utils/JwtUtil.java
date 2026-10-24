package com.example.hotel_back.Utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

public class JwtUtil {
    /**
     * Token加密
     *
     * @param secretKey jwt秘钥 此秘钥一定要保留好在服务端, 不能暴露出去, 否则sign就可以被伪造, 如果对接多个客户端建议改造成多个
     * @param ttlMillis 过期时间
     * @param claims    加密信息
     * @return
     */
    public static String createJWT(String secretKey, long ttlMillis, Map<String, Object> claims) {
        // 1. 使用SHA-256哈希将任意长度密钥转换为32字节
        byte[] keyBytes = hashTo32Bytes(secretKey);

        // 2. 生成安全的Key对象
        Key key = Keys.hmacShaKeyFor(keyBytes);

        return Jwts.builder()
                .setClaims(claims)
                .signWith(key, SignatureAlgorithm.HS256)
                .setExpiration(new Date(System.currentTimeMillis() + ttlMillis))
                .compact();
    }

    /**
     * Token解密
     *
     * @param secretKey jwt秘钥（需与加密时一致）
     * @param token     加密后的token
     * @return 解析后的Claims
     */
    public static Claims parseJWT(String secretKey, String token) {
        // 1. 哈希处理与加密时一致
        byte[] keyBytes = hashTo32Bytes(secretKey);

        // 2. 生成安全的Key对象
        Key key = Keys.hmacShaKeyFor(keyBytes);

        return Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 将任意长度字符串哈希为32字节（SHA-256）
     */
    private static byte[] hashTo32Bytes(String input) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            return sha.digest(input.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }
}