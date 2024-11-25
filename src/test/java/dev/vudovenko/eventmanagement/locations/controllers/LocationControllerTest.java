package dev.vudovenko.eventmanagement.locations.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import dev.vudovenko.eventmanagement.AbstractTest;
import dev.vudovenko.eventmanagement.common.exceptionHandling.dto.ErrorMessageResponse;
import dev.vudovenko.eventmanagement.common.exceptionHandling.exceptionMessages.ExceptionHandlerMessages;
import dev.vudovenko.eventmanagement.common.mappers.DtoMapper;
import dev.vudovenko.eventmanagement.locations.domain.Location;
import dev.vudovenko.eventmanagement.locations.dto.LocationDto;
import dev.vudovenko.eventmanagement.locations.exceptions.LocationNotFoundException;
import dev.vudovenko.eventmanagement.locations.repositories.LocationRepository;
import dev.vudovenko.eventmanagement.locations.services.impl.LocationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    @Test
    void shouldNotCreateLocationWhenRequestNotValid() throws Exception {
        LocationDto wrongLocationDto = new LocationDto(
                Long.MAX_VALUE,
                "",
                "   ",
                4,
                null
        );

        String wrongLocationDtoJson = objectMapper.writeValueAsString(wrongLocationDto);

        String errorMessageResponseJson = mockMvc
                .perform(
                        post("/locations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(wrongLocationDtoJson)
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
    }

    @Test
    void shouldSuccessfullyGetAllLocations() throws Exception {
        locationRepository.deleteAll();
        Set<LocationDto> locationDtoSet = new HashSet<>();
        IntStream.range(0, 10)
                .forEach(i -> {
                    Location location = locationService.createLocation(
                            new Location(
                                    null,
                                    "location-" + getRandomInt(),
                                    "address-" + getRandomInt(),
                                    100,
                                    "description"
                            ));

                    locationDtoSet.add(locationDtoMapper.toDto(location));
                });

        String locationsJson = mockMvc
                .perform(get("/locations"))
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
    void shouldSuccessfullyFindLocationById() throws Exception {
        Location locationToFind = locationService.createLocation(
                new Location(
                        null,
                        "location-" + getRandomInt(),
                        "address-" + getRandomInt(),
                        100,
                        "description"
                )
        );

        String foundLocationsJson = mockMvc
                .perform(get("/locations/{id}", locationToFind.getId()))
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
    void shouldNotFindLocationByNonExistentId() throws Exception {
        Long nonExistentId = Long.MAX_VALUE;

        String errorMessageResponseJson = mockMvc
                .perform(get("/locations/{id}", nonExistentId))
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
}