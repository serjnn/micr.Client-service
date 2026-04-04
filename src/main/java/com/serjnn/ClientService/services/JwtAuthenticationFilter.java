package com.serjnn.ClientService.services;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final ClientDetailService clientDetailService;

    public JwtAuthenticationFilter(JwtService jwtService, ClientDetailService clientDetailService) {
        this.jwtService = jwtService;
        this.clientDetailService = clientDetailService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String mail;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.trace("No Bearer token found in request: {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        try {
            mail = jwtService.extractUsername(jwt);
            log.debug("Extracted mail {} from JWT", mail);

            if (mail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.clientDetailService.loadUserByUsername(mail);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    log.debug("JWT token is valid for user: {}", mail);
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    log.warn("JWT token is invalid for user: {}", mail);
                }
            }
        } catch (Exception e) {
            log.error("Failed to process JWT token: {}", e.getMessage());
        }
        filterChain.doFilter(request, response);
    }
}
