package com.campus.eventmanagement.config;

import com.campus.eventmanagement.dto.LoginResponse;
import com.campus.eventmanagement.entity.User;
import com.campus.eventmanagement.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

import java.io.IOException;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

/**
 * Session-based authentication (course concept-friendly, works naturally with
 * the server-rendered Thymeleaf shell pages). The login form itself is
 * submitted via fetch() from login.html, so the success/failure handlers
 * below respond with JSON (LoginResponse) instead of doing a redirect.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // enables @PreAuthorize on service/controller methods
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return email -> {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("No account found for " + email));

            return org.springframework.security.core.userdetails.User
                    .withUsername(user.getEmail())
                    .password(user.getPassword())
                    .authorities("ROLE_" + user.getRole().name())
                    .disabled(!Boolean.TRUE.equals(user.getEnabled()))
                    .build();
        };
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Trade-off for this project's scope: CSRF is disabled so the vanilla-JS
                // fetch() calls in static/js/app.js don't need token plumbing. In a
                // production system you'd keep CSRF on and send the token with each request.
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        // Public pages & static assets
                        .requestMatchers("/", "/index", "/login", "/register",
                                "/css/**", "/js/**", "/images/**").permitAll()
                        // Public API endpoints
                        .requestMatchers("/api/auth/register").permitAll()
                        .requestMatchers("/api/events/**").permitAll() // browsing events doesn't require login
                        // Admin-only
                        .requestMatchers("/admin-dashboard", "/create-event", "/edit-event/**").hasRole("ADMIN")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // Everything else requires an authenticated session
                        .anyRequest().authenticated()
                )

                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .successHandler((request, response, authentication) -> {
                            User user = userRepository.findByEmail(authentication.getName()).orElse(null);
                            LoginResponse body = LoginResponse.builder()
                                    .success(true)
                                    .message("Login successful")
                                    .userId(user != null ? user.getId() : null)
                                    .fullName(user != null ? user.getFullName() : null)
                                    .email(authentication.getName())
                                    .role(user != null ? user.getRole().name() : null)
                                    .build();
                            writeJson(response, HttpStatus.OK, body);
                        })
                        .failureHandler((request, response, exception) -> {
                            LoginResponse body = LoginResponse.builder()
                                    .success(false)
                                    .message("Invalid email or password")
                                    .build();
                            writeJson(response, HttpStatus.UNAUTHORIZED, body);
                        })
                )

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler((request, response, authentication) ->
                                writeJson(response, HttpStatus.OK, Map.of("success", true, "message", "Logged out")))
                )

                // For AJAX calls hitting a protected endpoint without a session,
                // return 401 JSON instead of redirecting to the login page.
                .exceptionHandling(ex -> ex
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                request -> request.getRequestURI().startsWith("/api/"))
                );

        return http.build();
    }

    private void writeJson(jakarta.servlet.http.HttpServletResponse response, HttpStatus status, Object body)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
