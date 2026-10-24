package com.kolab.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Lo único que se carga de afuera son la tipografía de Google Fonts, los íconos de Bootstrap y
     * HTMX. Todo lo demás tiene que salir del propio sitio, y nada puede meter la aplicación dentro
     * de un marco ajeno.
     */
    private static final String POLITICA_DE_CONTENIDO = String.join("; ",
            "default-src 'self'",
            "script-src 'self' https://cdn.jsdelivr.net",
            // 'unsafe-inline' esta solo por los atributos style= que quedan en las plantillas;
            // cuando esos pasen a clases k-, se quita y la politica queda cerrada del todo
            "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com https://cdn.jsdelivr.net",
            "font-src 'self' https://fonts.gstatic.com https://cdn.jsdelivr.net",
            "img-src 'self' data:",
            "form-action 'self'",
            "frame-ancestors 'none'",
            "base-uri 'self'");

    @Bean
    public SecurityFilterChain filtros(HttpSecurity http) throws Exception {
        http
                .headers(cabeceras -> cabeceras
                        .contentSecurityPolicy(csp -> csp.policyDirectives(POLITICA_DE_CONTENIDO))
                        .frameOptions(marcos -> marcos.deny())
                        .referrerPolicy(remitente -> remitente.policy(
                                ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                        // el navegador solo la respeta por https, asi que en local no estorba
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000)))
                .authorizeHttpRequests(rutas -> rutas
                        .requestMatchers("/", "/login", "/registro", "/recuperar", "/recuperar/**",
                                "/css/**", "/js/**", "/img/**", "/error").permitAll()
                        // estas rutas solo existen con kolab.desarrollo=true; fuera de eso dan 404
                        .requestMatchers("/desarrollo/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .anyRequest().authenticated())
                .formLogin(login -> login
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error")
                        .permitAll())
                .logout(salida -> salida
                        .logoutUrl("/salir")
                        .logoutSuccessUrl("/login?cerrada")
                        .deleteCookies("JSESSIONID"));
        return http.build();
    }
}
