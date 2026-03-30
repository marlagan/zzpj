package com.zzpj.purrsuit.userservice.security;

import com.zzpj.purrsuit.userservice.entity.User;
import com.zzpj.purrsuit.userservice.repository.UserRepository;
import com.zzpj.purrsuit.userservice.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Data
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private JwtService jwtService;
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String authorisationHeader = request.getHeader("Authorization");

        if(authorisationHeader == null || !authorisationHeader.startsWith("Bearer ")){
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authorisationHeader.substring(7);
        String email = jwtService.getEmailFromToken(jwt);

        if(email != null && SecurityContextHolder.getContext().getAuthentication() == null){
            User user = userRepository.findByEmail(email).get(0);
            if(jwtService.isTokenValid(jwt, user)){
                UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(user, null, List.of(new SimpleGrantedAuthority(user.getRoleName().toString())));
                token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(token);
            }

        }
        filterChain.doFilter(request, response);

    }
}
