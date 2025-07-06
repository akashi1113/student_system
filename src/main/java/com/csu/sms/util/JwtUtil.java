package com.csu.sms.util;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
public class JwtUtil {
    private String secret="6v9y$B&E)H@McQfTjWnZr4u7x!A%D*G-";
    private Long expiration=86400L;

    //内存缓存黑名单
    private final Cache<String, Boolean> tokenBlacklist = Caffeine.newBuilder()
            .expireAfterWrite(24, TimeUnit.HOURS)
            .maximumSize(10_000)
            .build();

    //生成密钥
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    //生成token
    public String generateToken(Long userid, String username, String role, int tokenVersion) {
        return Jwts.builder()
                .claim("user_id", userid)
                .claim("role", role)
                .claim("ver", tokenVersion)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration * 1000))
                .signWith(getSigningKey())
                .compact();
    }

    private Claims extractClaim(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    //从token中提取用户id
    public Long extractUserId(String token) {
        return extractClaim(token).get("user_id", Long.class);
    }

    //从token中提取用户名
    public String extractUsername(String token) {
        return extractClaim(token).getSubject();
    }

    //从token中提取角色
    public String extractRole(String token) {
        return extractClaim(token).get("role", String.class);
    }

    //从token中提取版本号
    public int extractTokenVersion(String token) {
        return extractClaim(token).get("ver", Integer.class);
    }

    //提取过期时间
    public Date extractExpiration(String token) {
        return extractClaim(token).getExpiration();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    //验证token
    public boolean validateToken(String token, Long userid, String username, String role, int tokenVersion) {
        return (extractUserId(token).equals(userid) &&
                extractUsername(token).equals(username) &&
                extractRole(token).equals(role) &&
                extractTokenVersion(token) >= tokenVersion &&
                !isTokenExpired(token));
    }

    public boolean isTokenValid(String token) {
        return !isTokenBlacklisted(token) && !isTokenExpired(token);
    }

    //使Token失效
    public void invalidateToken(String token) {
        if (token != null && !isTokenExpired(token)) {
            //加入内存黑名单（有效期至Token自然过期）
            long ttl = extractExpiration(token).getTime() - System.currentTimeMillis();
            if (ttl > 0) {
                tokenBlacklist.put(token, true);
            }
        }
    }

    private boolean isTokenBlacklisted(String token) {
        return tokenBlacklist.getIfPresent(token) != null;
    }
}