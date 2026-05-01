package com.example.eventsystem.config;

import com.example.eventsystem.model.entity.User;
import com.example.eventsystem.model.enums.AppRole;
import com.example.eventsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class BootstrapAdminInitializer implements ApplicationRunner {

    private final BootstrapProperties bootstrapProperties;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (!StringUtils.hasText(bootstrapProperties.getAdminEmail())
                || !StringUtils.hasText(bootstrapProperties.getAdminPassword())) {
            return;
        }

        if (userRepository.findByEmail(bootstrapProperties.getAdminEmail()).isPresent()) {
            return;
        }

        User admin = User.builder()
                .email(bootstrapProperties.getAdminEmail())
                .fullName(StringUtils.hasText(bootstrapProperties.getAdminFullName())
                        ? bootstrapProperties.getAdminFullName()
                        : "Administrator")
                .passwordHash(passwordEncoder.encode(bootstrapProperties.getAdminPassword()))
                .role(AppRole.ADMIN)
                .build();

        userRepository.save(admin);
        log.info("Bootstrapped ADMIN user for email {}", admin.getEmail());
    }
}
