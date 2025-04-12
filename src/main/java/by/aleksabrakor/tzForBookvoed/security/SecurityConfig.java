package by.aleksabrakor.tzForBookvoed.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
            .csrf(csrf -> csrf
                    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                    // Разрешаем CSRF-незащищенные endpoints для REST API
                    .ignoringRequestMatchers("/api/**", "/rest/**")
            )
            .authorizeHttpRequests(auth -> auth
                    //Разрешаем доступ незарегистрированным пользователям для рестконтроллера
                    .requestMatchers("/api/books/**").permitAll()
                    // Разрешаем доступ к статическим ресурсам и страницам
                    .requestMatchers(
                            "/",
                            "/books",
                            "/login",
                            "/webjars/**",
                            "/css/**",
                            "/js/**",
                            "/images/**",
                            "/error/**,"
                    ).permitAll()

                    // Все остальные запросы требуют аутентификации
                    .anyRequest().authenticated()
            )
            .formLogin(form -> form
                    .loginPage("/login")
                    .defaultSuccessUrl("/books")
                    .permitAll()
            )
            .logout(logout -> logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/books?logout")
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID")
                    .clearAuthentication(true)
                    .permitAll()
            );

    return http.build();
}

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        UserDetails admin = User.builder()
                .username("admin")
                .password("$2a$12$M9nlusTuJYF5fm/EKeiWTuicuNcTyjXhfIoUUPvLRexUqet2bvf/.")
                .roles("ADMIN")
                .build();
        UserDetails user = User.builder()
                .username("user")
                .password("$2a$12$CaShku0NSwwzFaUXhhthyui5WLXiUov/EEtuawSSOcuKJ753BFGTO")
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(admin, user);
    }
}
