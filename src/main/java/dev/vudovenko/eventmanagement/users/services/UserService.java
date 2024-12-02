package dev.vudovenko.eventmanagement.users.services;

import dev.vudovenko.eventmanagement.users.domain.User;
import dev.vudovenko.eventmanagement.users.dto.SignUpRequest;

public interface UserService {

    User registerUser(SignUpRequest signUpRequest);

    User findByLogin(String login);
}
