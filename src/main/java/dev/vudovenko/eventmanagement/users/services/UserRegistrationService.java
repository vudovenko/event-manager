package dev.vudovenko.eventmanagement.users.services;

import dev.vudovenko.eventmanagement.users.domain.User;
import dev.vudovenko.eventmanagement.users.dto.UserRegistration;

public interface UserRegistrationService {

    User registerUser(UserRegistration userRegistration);
}
