package org.example._citizncardproj3.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.example._citizncardproj3.model.entity.Member;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Value("${jwt.expiration}")
    private Long EXPIRATION_TIME;  // 預設為24小時 = 86400000 milliseconds

    /**
     * 生成JWT Token
     */
    public String generateToken(Member member) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", member.getRole().name());
        claims.put("memberId", member.getMemberId());
        claims.put("email", member.getEmail());

        return createToken(claims, member.getEmail());
    }

    /**
     * 建立Token
     */
    private String createToken(Map<String, Object> claims, String subject) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 從Token中提取資訊
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 提取所有資訊
     */
    private Claims extractAllClaims(String token) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 提取用戶名
     */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * 提取過期時間
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * 驗證Token是否過期
     */
    private Boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    /**
     * 驗證Token
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String email = extractEmail(token);
            return (email.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 刷新Token
     */
    public String refreshToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            claims.setIssuedAt(new Date(System.currentTimeMillis()));
            claims.setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME));

            return createToken(claims, claims.getSubject());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 從Token中獲取使用者角色
     */
    public String extractRole(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("role", String.class);
    }

    /**
     * 從Token中獲取會員ID
     */
    public Long extractMemberId(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("memberId", Long.class);
    }

    /**
     * 檢查Token是否需要刷新
     */
    public boolean needsRefresh(String token) {
        try {
            Date expiration = extractExpiration(token);
            // 如果過期時間小於12小時，則需要刷新
            return expiration.getTime() - System.currentTimeMillis() < 43200000;
        } catch (Exception e) {
            return false;
        }
    }
}