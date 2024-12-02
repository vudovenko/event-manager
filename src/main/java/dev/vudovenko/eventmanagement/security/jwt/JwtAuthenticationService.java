package dev.vudovenko.eventmanagement.security.jwt;

import dev.vudovenko.eventmanagement.users.dto.SignInRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtAuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenManager jwtTokenManager;

    public String authenticateUser(SignInRequest sigInRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        sigInRequest.login(),
                        sigInRequest.password()
                )
        );
        return jwtTokenManager.generateToken(sigInRequest.login());
    }
}
