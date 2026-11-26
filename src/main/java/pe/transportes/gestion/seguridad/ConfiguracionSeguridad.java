package pe.transportes.gestion.seguridad;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import pe.transportes.gestion.modelo.Modulo;
import pe.transportes.gestion.repositorio.UsuarioRepositorio;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class ConfiguracionSeguridad {

    @Bean
    public PasswordEncoder codificadorContrasenas() {
        return new BCryptPasswordEncoder();
    }

    private AuthorizationManager<RequestAuthorizationContext> modulo(ServicioPermisos permisos, Modulo... modulos) {
        return (autenticacion, contexto) -> {
            for (Modulo modulo : modulos) {
                if (permisos.permite(autenticacion.get(), modulo)) {
                    return new AuthorizationDecision(true);
                }
            }
            return new AuthorizationDecision(false);
        };
    }

    @Bean
    public SecurityFilterChain cadenaDeFiltros(HttpSecurity http, UsuarioRepositorio repositorio,
            ServicioPermisos permisos) throws Exception {
        HttpSessionRequestCache memoriaDeSolicitudes = new HttpSessionRequestCache();
        memoriaDeSolicitudes.setMatchingRequestParameterName(null);
        http
                .addFilterAfter(new FiltroSesionVigente(repositorio), SecurityContextHolderFilter.class)
                .authorizeHttpRequests(reglas -> reglas
                        .requestMatchers("/login", "/recuperar", "/restablecer", "/error", "/acceso-denegado", "/css/**", "/js/**", "/images/**")
                        .permitAll()
                        .requestMatchers("/").hasAnyRole("ADMIN", "VENDEDOR", "CONDUCTOR")
                        .requestMatchers("/consultas/dni/conductor/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/pasajeros/*/eliminar").hasRole("ADMIN")
                        .requestMatchers("/consultas/dni/pasajero/**")
                        .access(modulo(permisos, Modulo.PASAJEROS, Modulo.VENTAS))
                        .requestMatchers("/pasajeros/**").access(modulo(permisos, Modulo.PASAJEROS))
                        .requestMatchers("/reservas/**").access(modulo(permisos, Modulo.VENTAS))
                        .requestMatchers("/pasajes/**").access(modulo(permisos, Modulo.PASAJES))
                        .requestMatchers("/reportes/**").access(modulo(permisos, Modulo.REPORTES))
                        .requestMatchers("/alertas/**").access(modulo(permisos, Modulo.ALERTAS))
                        .requestMatchers("/mis-viajes/**").access(modulo(permisos, Modulo.MIS_VIAJES))
                        .anyRequest().hasRole("ADMIN"))
                .formLogin(acceso -> acceso
                        .loginPage("/login")
                        .defaultSuccessUrl("/", false)
                        .failureUrl("/login?error")
                        .permitAll())
                .logout(salida -> salida
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?salir")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID"))
                .requestCache(cache -> cache.requestCache(memoriaDeSolicitudes))
                .exceptionHandling(errores -> errores.accessDeniedPage("/acceso-denegado"));
        return http.build();
    }
}
