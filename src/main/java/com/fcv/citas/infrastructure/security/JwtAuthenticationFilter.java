package com.fcv.citas.infrastructure.security;

import com.fcv.citas.infrastructure.persistence.RoleEntity;
import com.fcv.citas.infrastructure.persistence.UserEntity;
import com.fcv.citas.infrastructure.persistence.UserRepository;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserRepository users;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository users) {
        this.jwtService = jwtService;
        this.users = users;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            UserEntity user = users.findById(jwtService.accessUserId(header.substring(7)))
                    .filter(UserEntity::isActive)
                    .orElseThrow(() -> new UnsupportedJwtException("Usuario no disponible"));
            var authorities = user.getRoles().stream()
                    .map(RoleEntity::getCode).map(code -> new SimpleGrantedAuthority("ROLE_" + code)).toList();
            var authentication = new UsernamePasswordAuthenticationToken(user.getEmail(), null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (JwtException exception) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Access token inválido o expirado");
        }
    }
}
