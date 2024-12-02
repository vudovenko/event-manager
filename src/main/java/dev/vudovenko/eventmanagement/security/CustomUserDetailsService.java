package dev.vudovenko.eventmanagement.security;

import dev.vudovenko.eventmanagement.users.domain.User;
import dev.vudovenko.eventmanagement.users.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String login)
            throws UsernameNotFoundException {

        User user = userService.findByLogin(login);

        return org.springframework.security.core.userdetails.User
                .withUsername(login)
                .password(user.getPasswordHash())
                .authorities(user.getRole().toString())
                .build();
    }
}
