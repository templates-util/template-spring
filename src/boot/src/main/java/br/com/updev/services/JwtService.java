package br.com.updev.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {
    private final ConfigService configService;

    private SecretKey key;

    @Autowired
    public JwtService(ConfigService configService) {
        this.configService = configService;
        key = Keys.hmacShaKeyFor(configService.info("jwt.secret").getBytes(StandardCharsets.UTF_8));
    }

    public Claims parseToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    public String criarToken(String subject, long now) {
        Claims claims = Jwts.claims().setSubject(subject);
        long timeToLive = Long.parseLong(configService.info("jwt.ttl"));
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(new Date(now + timeToLive))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }
}
