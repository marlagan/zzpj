package com.zzpj.purrsuit.userservice.service;


import com.zzpj.purrsuit.userservice.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Data
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;
    @Value("${jwt.expiration}")
    private Long expiration;

    private Key generateSignInKey(){
        byte[] bytes = Decoders.BASE64.decode(this.secretKey);
        return Keys.hmacShaKeyFor(bytes);
    }

    public String generateToken(User user){
        return Jwts.builder().setSubject(user.getEmail()).claim("role", user.getRoleName()).setIssuedAt(new Date()).
                setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(generateSignInKey(), SignatureAlgorithm.HS256).compact();
    }

    public boolean isTokenValid(String token, User user){
        return user.getEmail().equals(getEmailFromToken(token)) && isExpired(token);
    }

    private boolean isExpired(String token){
        if(getExpirationDateFromToken(token).getExpiration().before(new Date())){
            return true;
        }else {
            return false;
        }
    }

    public String getEmailFromToken(String token){
        return Jwts.parser().setSigningKey(getSecretKey()).build().parseClaimsJws(token).getBody().getSubject();
    }

    private Claims getExpirationDateFromToken(String token){
        return Jwts.parser().setSigningKey(getSecretKey()).build().parseClaimsJws(token).getBody();
    }

}
