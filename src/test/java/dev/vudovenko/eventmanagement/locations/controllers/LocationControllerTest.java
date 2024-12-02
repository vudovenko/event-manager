package dev.vudovenko.eventmanagement.locations.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import dev.vudovenko.eventmanagement.AbstractTest;
import dev.vudovenko.eventmanagement.common.exceptionHandling.dto.ErrorMessageResponse;
import dev.vudovenko.eventmanagement.common.exceptionHandling.exceptionMessages.ExceptionHandlerMessages;
import dev.vudovenko.eventmanagement.common.mappers.DtoMapper;
import dev.vudovenko.eventmanagement.locations.domain.Location;
import dev.vudovenko.eventmanagement.locations.dto.LocationDto;
import dev.vudovenko.eventmanagement.locations.exceptions.LocationCapacityIsLowerThanItWasException;
import dev.vudovenko.eventmanagement.locations.exceptions.LocationNotFoundException;
import dev.vudovenko.eventmanagement.locations.repositories.LocationRepository;
import dev.vudovenko.eventmanagement.locations.services.LocationService;
import dev.vudovenko.eventmanagement.users.userRoles.UserRole;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LocationControllerTest extends AbstractTest {

    @Autowired
    private LocationService locationService;
    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private DtoMapper<Location, LocationDto> locationDtoMapper;

    @Test
    void shouldSuccessfullyCreateLocation() throws Exception {
        LocationDto locationDtoToCreate = new LocationDto(
                null,
                "location-" + getRandomInt(),
                "address-" + getRandomInt(),
                100,
                "description"
        );

        String locationDtoJson = objectMapper.writeValueAsString(locationDtoToCreate);

        String createdAuthorJson = mockMvc
                .perform(
                        post("/locations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(locationDtoJson)
                                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader(UserRole.ADMIN))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        LocationDto createdLocation = objectMapper.readValue(createdAuthorJson, LocationDto.class);

        Assertions.assertNotNull(createdLocation.id());
        org.assertj.core.api.Assertions
                .assertThat(createdLocation)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(locationDtoToCreate);
    }

    @WithMockUser(authorities = "USER")
    @Test
    void shouldReturnForbiddenWhenUserCreateLocation() throws Exception {
        mockMvc
                .perform(
                        post("/locations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnUnauthorizedWhenCreateLocationWithoutAuthorization() throws Exception {
        mockMvc
                .perform(
                        post("/locations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldNotCreateLocationWhenRequestNotValid() throws Exception {
        LocationDto wrongLocationDto = getWrongLocationDto();

        String wrongLocationDtoJson = objectMapper.writeValueAsString(wrongLocationDto);

        String errorMessageResponseJson = mockMvc
                .perform(
                        post("/locations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(wrongLocationDtoJson)
                                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader(UserRole.ADMIN))
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
        Assertions.assertTrue(detailedMessage.contains("id:"));
        Assertions.assertTrue(detailedMessage.contains("name:"));
        Assertions.assertTrue(detailedMessage.contains("address:"));
        Assertions.assertTrue(detailedMessage.contains("capacity:"));
        Assertions.assertNotNull(errorMessageResponse.dateTime());
        Assertions.assertTrue(
                errorMessageResponse.dateTime()
                        .isBefore(LocalDateTime.now().plusSeconds(1))
        );
    }

    @ParameterizedTest
    @MethodSource("rolesProvider")
    void shouldSuccessfullyGetAllLocations(UserRole userRole) throws Exception {
        locationRepository.deleteAll();
        Set<LocationDto> locationDtoSet = new HashSet<>();
        IntStream.range(0, 10)
                .forEach(i -> {
                    Location location = getCreatedLocation();

                    locationDtoSet.add(locationDtoMapper.toDto(location));
                });

        String locationsJson = mockMvc
                .perform(
                        get("/locations")
                                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader(userRole))
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Set<LocationDto> locationDtoSetFromRequest = objectMapper.readValue(
                locationsJson,
                new TypeReference<>() {
                }
        );

        Assertions.assertEquals(locationDtoSetFromRequest.size(), locationDtoSet.size());
        locationDtoSetFromRequest
                .forEach(locationDto -> Assertions.assertTrue(locationDtoSet.contains(locationDto)));
    }

    @Test
    void shouldReturnUnauthorizedWhenGetAllLocationsWithoutAuthorization() throws Exception {
        mockMvc
                .perform(get("/locations"))
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @MethodSource("rolesProvider")
    void shouldSuccessfullyFindLocationById(UserRole userRole) throws Exception {
        Location locationToFind = getCreatedLocation();

        String foundLocationsJson = mockMvc
                .perform(
                        get("/locations/{id}", locationToFind.getId())
                                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader(userRole))
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Location foundLocation = locationDtoMapper.toDomain(
                objectMapper.readValue(foundLocationsJson, LocationDto.class)
        );

        org.assertj.core.api.Assertions
                .assertThat(foundLocation)
                .usingRecursiveComparison()
                .isEqualTo(locationToFind);
    }

    @Test
    void shouldReturnUnauthorizedWhenFindLocationByIdWithoutAuthorization() throws Exception {
        mockMvc
                .perform(get("/locations/{id}", 1L))
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @MethodSource("rolesProvider")
    void shouldNotFindLocationByNonExistentId(UserRole userRole) throws Exception {
        Long nonExistentId = Long.MAX_VALUE;

        String errorMessageResponseJson = mockMvc
                .perform(
                        get("/locations/{id}", nonExistentId)
                                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader(userRole)))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ErrorMessageResponse errorMessageResponse = objectMapper
                .readValue(errorMessageResponseJson, ErrorMessageResponse.class);

        Assertions.assertEquals(
                errorMessageResponse.message(),
                ExceptionHandlerMessages.ENTITY_NOT_FOUND.getMessage()
        );
        Assertions.assertEquals(errorMessageResponse.detailedMessage(),
                LocationNotFoundException.MESSAGE_TEMPLATE.formatted(nonExistentId));
        Assertions.assertNotNull(errorMessageResponse.dateTime());
        Assertions.assertTrue(
                errorMessageResponse.dateTime()
                        .isBefore(LocalDateTime.now().plusSeconds(1))
        );
    }

    @Test
    void shouldSuccessfullyDeleteLocationById() throws Exception {
        Location locationToDelete = getCreatedLocation();

        String deletedLocationJson = mockMvc
                .perform(
                        delete("/locations/{id}", locationToDelete.getId())
                                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader(UserRole.ADMIN)))
                .andExpect(status().isNoContent())
                .andReturn()
                .getResponse()
                .getContentAsString();

        LocationDto deletedLocationDto = objectMapper.readValue(deletedLocationJson, LocationDto.class);

        Assertions.assertFalse(locationRepository.existsById(locationToDelete.getId()));

        org.assertj.core.api.Assertions
                .assertThat(locationDtoMapper.toDomain(deletedLocationDto))
                .usingRecursiveComparison()
                .isEqualTo(locationToDelete);
    }

    @WithMockUser(authorities = "USER")
    @Test
    void shouldReturnForbiddenWhenUserDeleteLocationById() throws Exception {
        mockMvc
                .perform(delete("/locations/{id}", 1L))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnUnauthorizedWhenDeleteLocationByIdWithoutAuthorization() throws Exception {
        mockMvc
                .perform(delete("/locations/{id}", 1L))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldNotDeleteLocationByNonExistentId() throws Exception {
        Long nonExistentLocationId = Long.MAX_VALUE;

        String errorMessageResponseJson = mockMvc
                .perform(
                        delete("/locations/{id}", nonExistentLocationId)
                                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader(UserRole.ADMIN)))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ErrorMessageResponse errorMessageResponse = objectMapper
                .readValue(errorMessageResponseJson, ErrorMessageResponse.class);

        Assertions.assertEquals(
                errorMessageResponse.message(),
                ExceptionHandlerMessages.ENTITY_NOT_FOUND.getMessage()
        );
        Assertions.assertEquals(errorMessageResponse.detailedMessage(),
                LocationNotFoundException.MESSAGE_TEMPLATE.formatted(nonExistentLocationId));
        Assertions.assertNotNull(errorMessageResponse.dateTime());
        Assertions.assertTrue(
                errorMessageResponse.dateTime()
                        .isBefore(LocalDateTime.now().plusSeconds(1))
        );
    }

    @Test
    void shouldSuccessfullyUpdateLocation() throws Exception {
        Location createdLocation = getCreatedLocation();

        LocationDto locationDtoToUpdate = new LocationDto(
                null,
                "updated-" + createdLocation.getName(),
                "updated-" + createdLocation.getAddress(),
                createdLocation.getCapacity() + 100,
                "updated-" + createdLocation.getDescription()
        );

        String locationDtoToUpdateJson = objectMapper.writeValueAsString(locationDtoToUpdate);

        String updatedLocationDtoJson = mockMvc
                .perform(
                        put("/locations/{id}", createdLocation.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(locationDtoToUpdateJson)
                                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader(UserRole.ADMIN))
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Location updatedLocation = locationDtoMapper.toDomain(
                objectMapper.readValue(updatedLocationDtoJson, LocationDto.class)
        );

        Assertions.assertEquals(updatedLocation.getId(), createdLocation.getId());
        org.assertj.core.api.Assertions
                .assertThat(updatedLocation)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(locationDtoMapper.toDomain(locationDtoToUpdate));
    }

    @WithMockUser(authorities = "USER")
    @Test
    void shouldReturnForbiddenWhenUserUpdateLocation() throws Exception {
        mockMvc
                .perform(
                        put("/locations/{id}", 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnUnauthorizedWhenUpdateLocationWithoutAuthorization() throws Exception {
        mockMvc
                .perform(
                        put("/locations/{id}", 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldNotUpdateLocationWithNonExistentId() throws Exception {
        Location createdLocation = getCreatedLocation();

        Long nonExistentLocationId = Long.MAX_VALUE;
        LocationDto invalidLocationDtoToUpdate = new LocationDto(
                null,
                "invalid-" + createdLocation.getName(),
                "invalid-" + createdLocation.getAddress(),
                createdLocation.getCapacity() + 1000,
                "invalid-" + createdLocation.getDescription()
        );

        String invalidLocationDtoToUpdateJson = objectMapper.writeValueAsString(invalidLocationDtoToUpdate);

        String errorMessageResponseJson = mockMvc
                .perform(
                        put("/locations/{id}", nonExistentLocationId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidLocationDtoToUpdateJson)
                                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader(UserRole.ADMIN))
                )
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ErrorMessageResponse errorMessageResponse = objectMapper
                .readValue(errorMessageResponseJson, ErrorMessageResponse.class);

        Assertions.assertEquals(errorMessageResponse.message(),
                ExceptionHandlerMessages.ENTITY_NOT_FOUND.getMessage());
        Assertions.assertEquals(
                errorMessageResponse.detailedMessage(),
                LocationNotFoundException.MESSAGE_TEMPLATE.formatted(nonExistentLocationId)
        );
        Assertions.assertNotNull(errorMessageResponse.dateTime());
        Assertions.assertTrue(
                errorMessageResponse.dateTime()
                        .isBefore(LocalDateTime.now().plusSeconds(1))
        );
    }

    @Test
    void shouldNotUpdateLocationWhenRequestIsInvalid() throws Exception {
        Location createdLocation = getCreatedLocation();

        LocationDto invalidLocationDtoToUpdate = getWrongLocationDto();

        String invalidLocationDtoToUpdateJson = objectMapper.writeValueAsString(invalidLocationDtoToUpdate);

        String errorMessageResponseJson = mockMvc
                .perform(
                        put("/locations/{id}", createdLocation.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidLocationDtoToUpdateJson)
                                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader(UserRole.ADMIN))
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
        Assertions.assertTrue(detailedMessage.contains("id:"));
        Assertions.assertTrue(detailedMessage.contains("name:"));
        Assertions.assertTrue(detailedMessage.contains("address:"));
        Assertions.assertTrue(detailedMessage.contains("capacity:"));
        Assertions.assertNotNull(errorMessageResponse.dateTime());
        Assertions.assertTrue(
                errorMessageResponse.dateTime()
                        .isBefore(LocalDateTime.now().plusSeconds(1))
        );
    }

    @Test
    void shouldNotUpdateLocationWhenCapacityLowerThanItWas() throws Exception {
        Location createdLocation = locationService.createLocation(
                new Location(
                        null,
                        "location-" + getRandomInt(),
                        "address-" + getRandomInt(),
                        100,
                        "description"
                )
        );

        LocationDto locationDtoToUpdate = new LocationDto(
                null,
                "updated-" + createdLocation.getName(),
                "updated-" + createdLocation.getAddress(),
                99,
                "updated-" + createdLocation.getDescription()
        );

        String locationDtoToUpdateJson = objectMapper.writeValueAsString(locationDtoToUpdate);

        String errorMessageResponseJson = mockMvc
                .perform(
                        put("/locations/{id}", createdLocation.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(locationDtoToUpdateJson)
                                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader(UserRole.ADMIN))
                )
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ErrorMessageResponse errorMessageResponse = objectMapper
                .readValue(errorMessageResponseJson, ErrorMessageResponse.class);

        Assertions.assertEquals(errorMessageResponse.message(),
                ExceptionHandlerMessages.LOCATION_CAPACITY_IS_LOWER_THAN_IT_WAS.getMessage());
        Assertions.assertEquals(
                errorMessageResponse.detailedMessage(),
                LocationCapacityIsLowerThanItWasException
                        .MESSAGE_TEMPLATE.formatted(
                                createdLocation.getCapacity(),
                                locationDtoToUpdate.capacity()
                        )
        );
        Assertions.assertNotNull(errorMessageResponse.dateTime());
        Assertions.assertTrue(
                errorMessageResponse.dateTime()
                        .isBefore(LocalDateTime.now().plusSeconds(1))
        );
    }

    private Location getCreatedLocation() {
        return locationService.createLocation(
                new Location(
                        null,
                        "location-" + getRandomInt(),
                        "address-" + getRandomInt(),
                        100,
                        "description"
                )
        );
    }

    private LocationDto getWrongLocationDto() {
        return new LocationDto(
                Long.MAX_VALUE,
                null,
                "   ",
                1,
                null
        );
    }
}
