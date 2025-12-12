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
            .cors(cors -> {}) // uses corsConfigurationSource bean below
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(eh -> eh.authenticationEntryPoint(restAuthenticationEntryPoint()))
            .authorizeHttpRequests(auth -> auth
                // Public static files (frontend)
                .requestMatchers(
                    "/", "/index.html", "/register.html", "/posts.html",
                    "/css/**", "/js/**"
                ).permitAll()

                // Public auth endpoints
                .requestMatchers("/api/auth/**").permitAll()

                // Permit Swagger/OpenAPI and related resources
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
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // For development you can use allowedOriginPatterns to accept localhost variants
        // In production replace with explicit origins (don't use "*")
        config.setAllowedOriginPatterns(List.of("*")); // DEV: allow all origins; change before prod
        // Or use explicit list:
        // config.setAllowedOrigins(List.of("http://localhost:5500","http://127.0.0.1:5500","http://localhost:3000"));

        config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
        // allow Authorization and common headers
        config.setAllowedHeaders(List.of("Authorization","Content-Type","Accept","Origin","X-Requested-With"));
        // expose Authorization header to browser if you need to read it (usually not necessary)
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(false); // set true only if you use cookies; tokens don't require it

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
