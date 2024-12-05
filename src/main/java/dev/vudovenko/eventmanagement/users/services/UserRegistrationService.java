package dev.vudovenko.eventmanagement.users.services;

import dev.vudovenko.eventmanagement.users.domain.User;

public interface UserRegistrationService {

    User registerUser(User userRegistration);
}
