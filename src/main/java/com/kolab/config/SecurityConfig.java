package com.kolab.config;

import com.kolab.common.BitacoraDeSeguridad;
import com.kolab.usuario.DetallesUsuarioService;
import com.kolab.usuario.Rol;
import jakarta.servlet.http.HttpServletResponse;
import java.security.Principal;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;

/**
 * Dos puertas: el panel en {@code /admin/**} y el mercado en todo lo demás. Cada una tiene su
 * ingreso y solo deja pasar cuentas de su rol.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Lo único que se carga de afuera son la tipografía de Google Fonts, los íconos de Bootstrap y
     * HTMX. Todo lo demás tiene que salir del propio sitio, y nada puede meter la aplicación dentro
     * de un marco ajeno.
     */
    private static final String POLITICA_DE_CONTENIDO = String.join("; ",
            "default-src 'self'",
            "script-src 'self' https://cdn.jsdelivr.net",
            "style-src 'self' https://fonts.googleapis.com https://cdn.jsdelivr.net",
            "font-src 'self' https://fonts.gstatic.com https://cdn.jsdelivr.net",
            "img-src 'self' data:",
            "form-action 'self'",
            "frame-ancestors 'none'",
            "base-uri 'self'");

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain panel(HttpSecurity http, DetallesUsuarioService detalles,
                                     AuthenticationEventPublisher eventos, BitacoraDeSeguridad bitacora)
            throws Exception {
        cabeceras(http);
        http
                .securityMatcher("/admin/**")
                .authenticationManager(ingresoPara(Rol.ADMINISTRADOR, detalles, eventos))
                .addFilterBefore(new SesionDeCuentaSuspendida(detalles, bitacora, "/admin/login"),
                        AuthorizationFilter.class)
                .authorizeHttpRequests(rutas -> rutas
                        .requestMatchers("/admin/login").permitAll()
                        .anyRequest().hasRole("ADMINISTRADOR"))
                // a quien no administra, el panel le responde igual que una ruta que no existe; un
                // formulario sin token sigue siendo un 403, como en el mercado
                .exceptionHandling(errores -> errores
                        .accessDeniedHandler((pedido, respuesta, e) -> respuesta.sendError(
                                e instanceof CsrfException ? HttpServletResponse.SC_FORBIDDEN
                                        : HttpServletResponse.SC_NOT_FOUND)))
                .formLogin(login -> login
                        .loginPage("/admin/login")
                        .loginProcessingUrl("/admin/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/admin", true)
                        .failureHandler(rechazo("/admin/login"))
                        .permitAll())
                .logout(salida -> salida
                        .logoutUrl("/admin/salir")
                        .logoutSuccessUrl("/admin/login?cerrada")
                        .deleteCookies("JSESSIONID"));
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain mercado(HttpSecurity http, DetallesUsuarioService detalles,
                                       AuthenticationEventPublisher eventos, BitacoraDeSeguridad bitacora)
            throws Exception {
        cabeceras(http);
        http
                .authenticationManager(ingresoPara(Rol.USUARIO, detalles, eventos))
                .addFilterBefore(new SesionDeCuentaSuspendida(detalles, bitacora, "/login"),
                        AuthorizationFilter.class)
                .authorizeHttpRequests(rutas -> rutas
                        .requestMatchers("/", "/login", "/registro", "/recuperar", "/recuperar/**",
                                "/css/**", "/js/**", "/img/**", "/error").permitAll()
                        // estas rutas solo existen con kolab.desarrollo=true; fuera de eso dan 404
                        .requestMatchers("/desarrollo/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .anyRequest().hasRole("USUARIO"))
                // quien administra vuelve a su panel; el resto, como un formulario sin token, es un 403
                .exceptionHandling(errores -> errores
                        .accessDeniedHandler((pedido, respuesta, e) -> {
                            if (administra(pedido.getUserPrincipal())) {
                                respuesta.sendRedirect(pedido.getContextPath() + "/admin");
                            } else {
                                respuesta.sendError(HttpServletResponse.SC_FORBIDDEN);
                            }
                        }))
                .formLogin(login -> login
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/", true)
                        .failureHandler(rechazo("/login"))
                        .permitAll())
                .logout(salida -> salida
                        .logoutUrl("/salir")
                        .logoutSuccessUrl("/login?cerrada")
                        .deleteCookies("JSESSIONID"));
        return http.build();
    }

    /**
     * Un ingreso que solo reconoce cuentas de un rol. Publica sus eventos como cualquier otro, así
     * el bloqueo por intentos y la bitácora valen igual en las dos puertas.
     */
    private AuthenticationManager ingresoPara(Rol rol, DetallesUsuarioService detalles,
                                              AuthenticationEventPublisher eventos) {
        DaoAuthenticationProvider proveedor = new DaoAuthenticationProvider(email -> detalles.cargar(email, rol));
        proveedor.setPasswordEncoder(passwordEncoder());
        // la suspensión solo se dice con la clave correcta, así no revela qué correos existen
        proveedor.setPreAuthenticationChecks(cuenta -> { });
        proveedor.setPostAuthenticationChecks(cuenta -> {
            if (!cuenta.isEnabled()) {
                throw new DisabledException("Cuenta suspendida: " + cuenta.getUsername());
            }
        });
        ProviderManager ingreso = new ProviderManager(proveedor);
        ingreso.setAuthenticationEventPublisher(eventos);
        return ingreso;
    }

    private static AuthenticationFailureHandler rechazo(String ingreso) {
        return (pedido, respuesta, e) -> respuesta.sendRedirect(pedido.getContextPath() + ingreso
                + (e instanceof DisabledException ? "?suspendida" : "?error"));
    }

    private static boolean administra(Principal quien) {
        return quien instanceof Authentication autenticado && autenticado.getAuthorities().stream()
                .anyMatch(permiso -> "ROLE_ADMINISTRADOR".equals(permiso.getAuthority()));
    }

    private void cabeceras(HttpSecurity http) throws Exception {
        http.headers(cabeceras -> cabeceras
                .contentSecurityPolicy(csp -> csp.policyDirectives(POLITICA_DE_CONTENIDO))
                .frameOptions(marcos -> marcos.deny())
                .referrerPolicy(remitente -> remitente.policy(ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                // el navegador solo la respeta por https, asi que en local no estorba
                .httpStrictTransportSecurity(hsts -> hsts
                        .includeSubDomains(true)
                        .maxAgeInSeconds(31536000)));
    }
}
