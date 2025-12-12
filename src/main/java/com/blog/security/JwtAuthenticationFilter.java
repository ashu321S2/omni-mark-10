package com.blog.security;

import com.blog.security.CustomUserDetailsService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.SecurityException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JwtAuthenticationFilter - validates JWT from Authorization: Bearer <token>
 * - uses JwtUtil to parse/validate tokens
 * - loads UserDetails and sets SecurityContext if token valid
 * - logs detailed reasons for token rejection (expired, signature invalid, parse error)
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {

        final String authHeader = req.getHeader("Authorization");
        String username = null;
        String token = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            try {
                // extract username (this will also throw if token invalid/expired)
                username = jwtUtil.getUsernameFromToken(token);
            } catch (ExpiredJwtException e) {
                logger.warn("JWT expired for request [{}]: {}", req.getRequestURI(), e.getMessage());
            } catch (SecurityException e) {
                // SecurityException covers signature problems (SignatureException is a subclass)
                logger.warn("JWT signature invalid for request [{}]: {}", req.getRequestURI(), e.getMessage());
            } catch (JwtException e) {
                // covers other parsing problems (malformed, unsupported, etc)
                logger.warn("JWT parsing/validation failed for request [{}]: {}", req.getRequestURI(), e.getMessage());
            } catch (Exception e) {
                // unexpected
                logger.error("Unexpected error while parsing JWT for request [{}]: {}", req.getRequestURI(), e.getMessage(), e);
            }
        } else {
            if (authHeader != null) {
                // header present but doesn't start with Bearer
                logger.debug("Authorization header present but does not start with 'Bearer ' for request [{}]", req.getRequestURI());
            } else {
                // no header - not necessarily an error for public endpoints
                logger.debug("No Authorization header for request [{}]", req.getRequestURI());
            }
        }

        // If we got a username and no authentication yet, validate token and set Authentication
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                // validate token (signature + expiration)
                boolean valid = jwtUtil.validateToken(token);
                if (valid) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    logger.debug("JWT validated and security context set for user '{}' on request [{}]", username, req.getRequestURI());
                } else {
                    logger.warn("JWT validation returned false for request [{}] (token may be invalid/expired)", req.getRequestURI());
                }
            } catch (Exception e) {
                // if loading user or setting authentication fails, log and continue without authentication
                logger.warn("Failed to set authentication for user '{}' on request [{}]: {}", username, req.getRequestURI(), e.getMessage());
            }
        }

        // proceed with the filter chain
        chain.doFilter(req, res);
    }
}
