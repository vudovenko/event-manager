package dev.vudovenko.eventmanagement.users.controllers;

import dev.vudovenko.eventmanagement.AbstractTest;
import dev.vudovenko.eventmanagement.common.exceptionHandling.dto.ErrorMessageResponse;
import dev.vudovenko.eventmanagement.common.exceptionHandling.exceptionMessages.ExceptionHandlerMessages;
import dev.vudovenko.eventmanagement.security.jwt.dto.JwtTokenResponse;
import dev.vudovenko.eventmanagement.users.domain.User;
import dev.vudovenko.eventmanagement.users.dto.UserCredentials;
import dev.vudovenko.eventmanagement.users.dto.UserDto;
import dev.vudovenko.eventmanagement.users.dto.UserRegistration;
import dev.vudovenko.eventmanagement.users.exceptions.LoginAlreadyTakenException;
import dev.vudovenko.eventmanagement.users.exceptions.UserIdNotFoundException;
import dev.vudovenko.eventmanagement.users.services.UserRegistrationService;
import dev.vudovenko.eventmanagement.users.userRoles.UserRole;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest extends AbstractTest {

    @Autowired
    private UserRegistrationService userRegistrationService;

    @Test
    void shouldSuccessfullyRegisterUser() throws Exception {
        UserRegistration userRegistration = new UserRegistration(
                "login-" + getRandomInt(),
                "password-" + getRandomInt(),
                20
        );

        String userRegistrationJson = objectMapper.writeValueAsString(userRegistration);

        String userDtoJson = mockMvc
                .perform(
                        post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(userRegistrationJson)
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserDto registeredUser = objectMapper.readValue(userDtoJson, UserDto.class);

        Assertions.assertNotNull(registeredUser.id());
        Assertions.assertEquals(registeredUser.login(), userRegistration.login());
        Assertions.assertEquals(registeredUser.age(), userRegistration.age());
        Assertions.assertEquals(registeredUser.role(), UserRole.USER);
    }

    @Test
    void shouldNotRegisterUserWhenLoginIsAlreadyTaken() throws Exception {
        User registeredUser = getRegisteredUser();

        UserRegistration userRegistrationWithExistingLogin = new UserRegistration(
                registeredUser.getLogin(),
                "password-" + getRandomInt(),
                35
        );

        String userRegistrationJson = objectMapper
                .writeValueAsString(userRegistrationWithExistingLogin);

        String errorMessageResponseJson = mockMvc
                .perform(
                        post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(userRegistrationJson)
                )
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();


        ErrorMessageResponse errorMessageResponse = objectMapper
                .readValue(errorMessageResponseJson, ErrorMessageResponse.class);

        Assertions.assertEquals(errorMessageResponse.message(),
                ExceptionHandlerMessages.LOGIN_ALREADY_TAKEN.getMessage());
        Assertions.assertEquals(
                errorMessageResponse.detailedMessage(),
                LoginAlreadyTakenException.MESSAGE_TEMPLATE.formatted(registeredUser.getLogin())
        );
        Assertions.assertNotNull(errorMessageResponse.dateTime());
        Assertions.assertTrue(
                errorMessageResponse.dateTime()
                        .isBefore(LocalDateTime.now().plusSeconds(1))
        );
    }

    @Test
    void shouldNotRegisterUserWhenRequestNotValid() throws Exception {
        UserRegistration wrongUserRegistration = getWrongUserRegistration();

        String wrongUserRegistrationJson = objectMapper.writeValueAsString(wrongUserRegistration);

        String errorMessageResponseJson = mockMvc
                .perform(
                        post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(wrongUserRegistrationJson)
                )
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ErrorMessageResponse errorMessageResponse = objectMapper
                .readValue(errorMessageResponseJson, ErrorMessageResponse.class);

        String detailedMessage = errorMessageResponse.detailedMessage();

        Assertions.assertEquals(
                errorMessageResponse.message(),
                ExceptionHandlerMessages.VALIDATION_FAILED.getMessage()
        );
        Assertions.assertTrue(detailedMessage.contains("login:"));
        Assertions.assertTrue(detailedMessage.contains("password:"));
        Assertions.assertTrue(detailedMessage.contains("age:"));
        Assertions.assertNotNull(errorMessageResponse.dateTime());
        Assertions.assertTrue(
                errorMessageResponse.dateTime()
                        .isBefore(LocalDateTime.now().plusSeconds(1))
        );
    }

    @Test
    void shouldSuccessfullyAuthorizeUser() throws Exception {
        String password = "password-" + getRandomInt();
        User registeredUser = userRegistrationService.registerUser(
                new UserRegistration(
                        "login-" + getRandomInt(),
                        password,
                        30
                )
        );

        UserCredentials userCredentials = new UserCredentials(
                registeredUser.getLogin(),
                password
        );

        String userCredentialsJson = objectMapper.writeValueAsString(userCredentials);

        String jwtTokenResponseJson = mockMvc
                .perform(
                        post("/users/auth")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(userCredentialsJson)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JwtTokenResponse jwtTokenResponse = objectMapper
                .readValue(jwtTokenResponseJson, JwtTokenResponse.class);

        Assertions.assertNotNull(jwtTokenResponse.jwt());
        Assertions.assertFalse(jwtTokenResponse.jwt().isEmpty());
    }

    @Test
    void shouldNotAuthorizeUserWhenCredentialsAreWrong() throws Exception {
        UserCredentials wrongUserCredentials = new UserCredentials(
                "login-" + getRandomInt(),
                "password-" + getRandomInt()
        );

        String wrongUserCredentialsJson = objectMapper
                .writeValueAsString(wrongUserCredentials);

        String errorMessageResponseJson = mockMvc
                .perform(
                        post("/users/auth")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(wrongUserCredentialsJson)
                )
                .andExpect(status().isUnauthorized())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ErrorMessageResponse errorMessageResponse = objectMapper
                .readValue(errorMessageResponseJson, ErrorMessageResponse.class);

        Assertions.assertEquals(
                errorMessageResponse.message(),
                ExceptionHandlerMessages.FAILED_TO_AUTHENTICATE.getMessage()
        );
        Assertions.assertNotNull(errorMessageResponse.detailedMessage());
        Assertions.assertNotNull(errorMessageResponse.dateTime());
        Assertions.assertTrue(
                errorMessageResponse.dateTime()
                        .isBefore(LocalDateTime.now().plusSeconds(1))
        );
    }

    @Test
    void shouldNotAuthorizeUserWhenRequestNotValid() throws Exception {
        UserCredentials wrongUserCredentials = new UserCredentials(
                "     ",
                null
        );

        String wrongUserCredentialsJson = objectMapper
                .writeValueAsString(wrongUserCredentials);

        String errorMessageResponseJson = mockMvc
                .perform(
                        post("/users/auth")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(wrongUserCredentialsJson)
                )
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ErrorMessageResponse errorMessageResponse = objectMapper
                .readValue(errorMessageResponseJson, ErrorMessageResponse.class);

        String detailedMessage = errorMessageResponse.detailedMessage();

        Assertions.assertEquals(
                errorMessageResponse.message(),
                ExceptionHandlerMessages.VALIDATION_FAILED.getMessage()
        );
        Assertions.assertTrue(detailedMessage.contains("login:"));
        Assertions.assertTrue(detailedMessage.contains("password:"));
        Assertions.assertNotNull(errorMessageResponse.dateTime());
        Assertions.assertTrue(
                errorMessageResponse.dateTime()
                        .isBefore(LocalDateTime.now().plusSeconds(1))
        );
    }

    @Test
    void shouldReturnInformationAboutUser() throws Exception {
        User registeredUser = getRegisteredUser();

        String userDtoJson = mockMvc
                .perform(
                        get("/users/" + registeredUser.getId())
                                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader(UserRole.ADMIN))
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserDto userDto = objectMapper
                .readValue(userDtoJson, UserDto.class);

        Assertions.assertNotNull(userDto);
        Assertions.assertEquals(userDto.id(), registeredUser.getId());
        Assertions.assertEquals(userDto.login(), registeredUser.getLogin());
        Assertions.assertEquals(userDto.age(), registeredUser.getAge());
        Assertions.assertEquals(userDto.role(), registeredUser.getRole());
    }

    @WithMockUser(authorities = "USER")
    @Test
    void shouldReturnForbiddenWhenUserRequestsInformationAboutUser() throws Exception {
        mockMvc
                .perform(get("/users/{userId}", 1L))
                .andExpect(status().isForbidden());
    }


    @Test
    void shouldReturnUnauthorizedWhenRequestInformationAboutUserWithoutAuthorization() throws Exception {
        mockMvc
                .perform(get("/users/{userId}", 1L))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldNotReturnInformationAboutUserWhenUserIdNotFound() throws Exception {
        String errorMessageResponseJson =
                mockMvc
                        .perform(
                                get("/users/{userId}", -1L)
                                        .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader(UserRole.ADMIN))
                        )
                        .andExpect(status().isNotFound())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        ErrorMessageResponse errorMessageResponse = objectMapper
                .readValue(errorMessageResponseJson, ErrorMessageResponse.class);

        Assertions.assertEquals(
                errorMessageResponse.message(),
                ExceptionHandlerMessages.USER_ID_NOT_FOUND.getMessage()
        );
        Assertions.assertEquals(
                errorMessageResponse.detailedMessage(),
                UserIdNotFoundException.MESSAGE_TEMPLATE.formatted(-1L)
        );
        Assertions.assertNotNull(errorMessageResponse.dateTime());
        Assertions.assertTrue(
                errorMessageResponse.dateTime()
                        .isBefore(LocalDateTime.now().plusSeconds(1))
        );
    }

    private User getRegisteredUser() {
        return userRegistrationService.registerUser(
                new UserRegistration(
                        "login-" + getRandomInt(),
                        "password-" + getRandomInt(),
                        20
                )
        );
    }

    private UserRegistration getWrongUserRegistration() {
        return new UserRegistration(
                null,
                "    ",
                10
        );
    }
}