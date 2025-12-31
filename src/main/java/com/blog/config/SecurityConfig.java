package com.blog.config;

import com.blog.security.JwtAuthenticationFilter;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.http.MediaType;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;

import java.io.IOException;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
             // uses corsConfigurationSource bean below
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(eh -> eh.authenticationEntryPoint(restAuthenticationEntryPoint()))
            .authorizeHttpRequests(auth -> auth

            	    // Public static frontend
            	    .requestMatchers(
            	        "/", "/index.html", "/register.html", "/posts.html",
            	        "/css/**", "/js/**", "/health", "/actuator/health",
            	        "/actuator/health/**", "/uposts.html", "/uploads/**",
            	        "/portfolio.html", "/developed.html"
            	    ).permitAll()

            	    // Auth APIs
            	    .requestMatchers("/api/auth/**").permitAll()
                    
            	    
            	     .requestMatchers("/uploads/**").permitAll()
            	      
            	    // READ posts allowed
            	 // READ posts allowed publicly
            	    .requestMatchers(HttpMethod.GET, "/api/posts/**").permitAll()

            	    // CREATE posts
            	    .requestMatchers(HttpMethod.POST, "/api/posts/**").authenticated()

            	    // UPDATE posts
            	    .requestMatchers(HttpMethod.PUT, "/api/posts/**").authenticated()

            	    // DELETE posts
            	    .requestMatchers(HttpMethod.DELETE, "/api/posts/**").authenticated()

            	    // Swagger
            	    .requestMatchers(
            	        "/v3/api-docs/**",
            	        "/v3/api-docs.yaml",
            	        "/swagger-ui/**",
            	        "/swagger-ui.html",
            	        "/swagger-resources/**",
            	        "/webjars/**"
            	    ).permitAll()
                // everything else requires authentication
                .anyRequest().authenticated()
            );

        // Add JWT filter before UsernamePasswordAuthenticationFilter
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationEntryPoint restAuthenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            String json = "{\"error\":\"Unauthorized\",\"message\":\"" + (authException==null ? "Unauthorized" : authException.getMessage()) + "\"}";
            try {
                response.getWriter().write(json);
            } catch (IOException ignored) {}
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // CORS policy for development. Replace/add origins your frontend uses.
  
}
