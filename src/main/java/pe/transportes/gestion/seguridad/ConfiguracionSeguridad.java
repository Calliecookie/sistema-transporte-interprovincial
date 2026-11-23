package pe.transportes.gestion.seguridad;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import pe.transportes.gestion.repositorio.UsuarioRepositorio;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class ConfiguracionSeguridad {

    @Bean
    public PasswordEncoder codificadorContrasenas() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain cadenaDeFiltros(HttpSecurity http, UsuarioRepositorio repositorio) throws Exception {
        HttpSessionRequestCache memoriaDeSolicitudes = new HttpSessionRequestCache();
        memoriaDeSolicitudes.setMatchingRequestParameterName(null);
        http
                .addFilterAfter(new FiltroSesionVigente(repositorio), SecurityContextHolderFilter.class)
                .authorizeHttpRequests(reglas -> reglas
                        .requestMatchers("/login", "/error", "/acceso-denegado", "/css/**", "/js/**", "/images/**")
                        .permitAll()
                        .requestMatchers("/").hasAnyRole("ADMIN", "VENDEDOR", "CONDUCTOR")
                        .requestMatchers("/mis-viajes/**").hasRole("CONDUCTOR")
                        .requestMatchers("/consultas/dni/conductor/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/pasajeros/*/eliminar").hasRole("ADMIN")
                        .requestMatchers("/pasajeros/**", "/reservas/**", "/pasajes/**", "/reportes/**", "/alertas/**",
                                "/consultas/**")
                        .hasAnyRole("ADMIN", "VENDEDOR")
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
