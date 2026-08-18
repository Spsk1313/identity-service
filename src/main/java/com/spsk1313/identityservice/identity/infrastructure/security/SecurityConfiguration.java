package com.spsk1313.identityservice.identity.infrastructure.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

   @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
       http
               .authorizeHttpRequests(auth ->
                       auth.requestMatchers(HttpMethod.POST, "/api/auth/register")
                               .permitAll()
                               .anyRequest().authenticated())
               .httpBasic(AbstractHttpConfigurer::disable)
               .formLogin(AbstractHttpConfigurer::disable)
               .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
               .csrf(csrf -> csrf.ignoringRequestMatchers(PathPatternRequestMatcher.pathPattern(HttpMethod.POST, "/api/auth/register")))
               .exceptionHandling(exceptions ->
                       exceptions.authenticationEntryPoint(
                               ((request, response, authException) -> {
                                   response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                               })
                       ));

       return http.build();
   }
}
