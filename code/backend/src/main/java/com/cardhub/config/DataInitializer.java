package com.cardhub.config;

import com.cardhub.model.User;
import com.cardhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public ApplicationRunner seedUsers() {
        return args -> {
            seedIfMissing("admin@gmail.com", "admin", "Admin", User.Role.ADMIN);
            seedIfMissing("user@gmail.com", "user", "User", User.Role.CUSTOMER);
        };
    }

    private void seedIfMissing(String email, String password, String name, User.Role role) {
        if (userRepository.findByEmail(email).isEmpty()) {
            var user = new User();
            user.setEmail(email);
            user.setPasswordHash(passwordEncoder.encode(password));
            user.setName(name);
            user.setRole(role);
            userRepository.save(user);
            log.info("Seeded user: {} ({})", email, role);
        }
    }
}
