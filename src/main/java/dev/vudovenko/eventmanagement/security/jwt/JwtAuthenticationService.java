package dev.vudovenko.eventmanagement.security.jwt;

import dev.vudovenko.eventmanagement.users.dto.UserCredentials;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtAuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenManager jwtTokenManager;

    public String authenticateUser(UserCredentials userCredentials) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        userCredentials.login(),
                        userCredentials.password()
                )
        );
        return jwtTokenManager.generateToken(userCredentials.login());
    }
}

