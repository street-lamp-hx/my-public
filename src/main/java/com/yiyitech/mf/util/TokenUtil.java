package com.yiyitech.mf.util;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.*;

/**
 * @author hx
 * @version 1.0.0
 * @ClassName TokenUtil.java
 * @Description
 * @createTime 2025年06月08日 16:58:00
 */
@Component
public class TokenUtil {
    @Value("${jwt.secret}")
    private String jwtSecret;

    private static SecretKey secretKey;
    @PostConstruct
    public void init() {
        byte[] keyBytes = Base64.getDecoder().decode(jwtSecret);
        secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    //生成token（新版jwt）
    public static String generateToken(String id, String userName, String parentId, String phone, List<String> roles, List<String> permissions) {
        long expirationTime = 1000L * 60 * 60 * 24 * 30;//30天有效期（单位：毫秒）
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationTime);
        return Jwts.builder()
                .setSubject(phone)
                .claim("id", id)
                .claim("userName", userName)
                .claim("parentId", parentId)
                .claim("roles", roles)
                .claim("permissions", permissions)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // 解析token（新版）
    public static Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
    }

//    //生成token（旧版jwt）
//    public static String generateToken(String parentId, String phone, List<String> roles, List<String> permissions) {
//        long expirationTime = 1000 * 60 * 60 * 24; // 24小时过期
//        Date now = new Date();
//        Date expiration = new Date(now.getTime() + expirationTime);
//
////        Map<String, Object> claims = new HashMap<>();
////        claims.put("sub", phone);
////        claims.put("role", role);
////        claims.put("permissions", permissions);
//        return Jwts.builder()
//                .setSubject(phone)
//                .claim("parentId", parentId)
//                .claim("role", roles)
//                .claim("permissions", permissions)
////                .setClaims(claims)
//                .setIssuedAt(now)
//                .setExpiration(expiration)
//                .signWith(SignatureAlgorithm.HS256, secretKey)
//                .compact();
//    }

//    //解析token（旧版）
//    public static Claims parseToken(String token) {
//        return Jwts.parser()
//                .setSigningKey(secretKey)
//                .parseClaimsJws(token)
//                .getBody();
//    }

    //验证token有效
    public static boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            return !claims.getExpiration().before(new Date());
        } catch (JwtException e) {
            return false;
        }
    }

    // 使token过期
    public static void expireToken(String token) {
        // 简单实现：token过期直接删除，实际中可能需要更复杂的失效机制
        // 例如，使用缓存或数据库来记录失效的token
    }
}
