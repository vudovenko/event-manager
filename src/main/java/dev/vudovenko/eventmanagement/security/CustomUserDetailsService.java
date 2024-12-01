package dev.vudovenko.eventmanagement.security;

import dev.vudovenko.eventmanagement.users.entity.UserEntity;
import dev.vudovenko.eventmanagement.users.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String login)
            throws UsernameNotFoundException {

        UserEntity user = userRepository.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return User.withUsername(login)
                .password(user.getPasswordHash())
                .authorities(user.getRole().toString())
                .build();
    }
}
