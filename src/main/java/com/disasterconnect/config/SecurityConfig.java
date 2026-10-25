package com.disasterconnect.config;

import com.disasterconnect.security.CustomAccessDeniedHandler;
import com.disasterconnect.security.JwtAuthenticationEntryPoint;
import com.disasterconnect.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          JwtAuthenticationEntryPoint authenticationEntryPoint,
                          CustomAccessDeniedHandler accessDeniedHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .authorizeHttpRequests(authorize -> authorize
                        // Public endpoints & Static Frontend Resources
                        .requestMatchers("/", "/index.html", "/favicon.ico", "/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/health", "/api/test/public").permitAll()

                        // Phase 3: Alerts — public GET, restricted write
                        .requestMatchers(HttpMethod.GET, "/api/alerts", "/api/alerts/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/alerts").hasAnyRole("AUTHORITY", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/alerts/**").hasAnyRole("AUTHORITY", "ADMIN")

                        // Phase 3: Incidents — citizens submit, /my is auth, authorities/NGOs view all
                        .requestMatchers(HttpMethod.POST, "/api/incidents").hasAnyRole("CITIZEN", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/incidents/my").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/incidents").hasAnyRole("AUTHORITY", "ADMIN", "NGO")
                        .requestMatchers(HttpMethod.GET, "/api/incidents/*").hasAnyRole("AUTHORITY", "ADMIN", "NGO", "CITIZEN")
                        .requestMatchers(HttpMethod.PATCH, "/api/incidents/**").hasAnyRole("AUTHORITY", "ADMIN")

                        // Phase 4: Emergency Assistance Requests
                        .requestMatchers(HttpMethod.POST, "/api/assistance-requests", "/api/help-requests").hasAnyRole("CITIZEN", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/assistance-requests/my", "/api/help-requests/my").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/assistance-requests/assigned-to-me", "/api/help-requests/assigned-to-me").hasAnyRole("VOLUNTEER", "NGO", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/assistance-requests/*/accept", "/api/help-requests/*/accept").hasAnyRole("VOLUNTEER", "NGO", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/assistance-requests/*/assign", "/api/help-requests/*/assign").hasAnyRole("NGO", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/assistance-requests/*/status", "/api/help-requests/*/status").hasAnyRole("VOLUNTEER", "NGO", "AUTHORITY", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/assistance-requests/*/cancel", "/api/help-requests/*/cancel").hasAnyRole("CITIZEN", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/assistance-requests", "/api/help-requests", "/api/assistance-requests/**", "/api/help-requests/**").hasAnyRole("CITIZEN", "VOLUNTEER", "NGO", "AUTHORITY", "ADMIN")

                        // Phase 4: Coordination Module
                        .requestMatchers("/api/coordination/dashboard", "/api/coordination/volunteers").hasAnyRole("NGO", "AUTHORITY", "ADMIN")
                        .requestMatchers("/api/coordination/my-tasks").hasAnyRole("VOLUNTEER", "ADMIN")

                        // Phase 5: Shelters and Essential Services
                        .requestMatchers(HttpMethod.GET, "/api/shelters", "/api/shelters/**", "/api/services", "/api/services/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/shelters", "/api/shelters/**", "/api/services", "/api/services/**").hasAnyRole("AUTHORITY", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/shelters/**", "/api/services/**").hasAnyRole("AUTHORITY", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/shelters/**", "/api/services/**").hasAnyRole("AUTHORITY", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/shelters/**", "/api/services/**").hasAnyRole("AUTHORITY", "ADMIN")

                        // Phase 6: Relief Resource Management Module
                        .requestMatchers(HttpMethod.GET, "/api/resources/area", "/api/resources/area/**").hasAnyRole("NGO", "AUTHORITY", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/resources", "/api/resources/**").hasAnyRole("NGO", "AUTHORITY", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/resources", "/api/resources/**").hasAnyRole("NGO", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/resources/**").hasAnyRole("NGO", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/resources/**").hasAnyRole("NGO", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/resources/**").hasAnyRole("NGO", "ADMIN")

                        // Phase 6: Missing Person and Family Safety Module (REQ-MISS-1 to REQ-MISS-4)
                        .requestMatchers(HttpMethod.GET, "/api/missing-persons", "/api/missing-persons/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/missing-persons").hasAnyRole("CITIZEN", "VOLUNTEER", "NGO", "AUTHORITY", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/missing-persons/**").authenticated()

                        // Phase 6: Relief Campaigns and Donations Module (REQ-CAMP-1 to REQ-CAMP-4)
                        .requestMatchers(HttpMethod.GET, "/api/campaigns/my").hasAnyRole("NGO", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/campaigns", "/api/campaigns/*").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/campaigns").hasAnyRole("NGO", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/campaigns/*/verify").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/campaigns/*/status").hasAnyRole("NGO", "ADMIN")

                        // Phase 6: Notifications Module (REQ-NOTIF-1 to REQ-NOTIF-4)
                        .requestMatchers("/api/notifications", "/api/notifications/**").authenticated()

                        // Role-based authorization rules
                        .requestMatchers("/api/admin/**", "/api/test/admin").hasRole("ADMIN")
                        .requestMatchers("/api/authority/**", "/api/test/authority").hasAnyRole("AUTHORITY", "ADMIN")
                        .requestMatchers("/api/ngo/**", "/api/test/ngo").hasAnyRole("NGO", "ADMIN")
                        .requestMatchers("/api/volunteer/**", "/api/test/volunteer").hasAnyRole("VOLUNTEER", "ADMIN")
                        .requestMatchers("/api/citizen/**", "/api/test/citizen").hasAnyRole("CITIZEN", "ADMIN")

                        // Authenticated endpoints (Profile, Me)
                        .requestMatchers("/api/auth/me", "/api/auth/profile").authenticated()

                        // Any other request must be authenticated
                        .anyRequest().authenticated()
                );

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
