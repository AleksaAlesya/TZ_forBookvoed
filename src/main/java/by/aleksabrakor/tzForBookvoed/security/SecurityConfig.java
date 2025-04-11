package by.aleksabrakor.tzForBookvoed.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
            .csrf(csrf -> csrf.disable()) // Отключаем CSRF для REST API
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
    public InMemoryUserDetailsManager userDetailsService() {
        UserDetails admin = User.withDefaultPasswordEncoder()
                .username("admin")
                .password("password")
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(admin);
    }

}
