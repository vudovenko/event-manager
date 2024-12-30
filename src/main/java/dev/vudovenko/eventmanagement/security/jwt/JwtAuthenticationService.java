package dev.vudovenko.eventmanagement.security.jwt;

import dev.vudovenko.eventmanagement.users.domain.User;
import dev.vudovenko.eventmanagement.users.dto.UserCredentials;
import dev.vudovenko.eventmanagement.users.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtAuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenManager jwtTokenManager;
    private final UserService userService;

    public String authenticateUser(UserCredentials userCredentials) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        userCredentials.login(),
                        userCredentials.password()
                )
        );
        User user = userService.findByLogin(userCredentials.login());

        return jwtTokenManager.generateToken(
                userCredentials.login(),
                user.getRole()
        );
    }
}

