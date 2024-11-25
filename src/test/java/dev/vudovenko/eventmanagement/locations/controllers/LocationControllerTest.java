package dev.vudovenko.eventmanagement.locations.controllers;

import dev.vudovenko.eventmanagement.AbstractTest;
import dev.vudovenko.eventmanagement.common.exceptionHandling.dto.ErrorMessageResponse;
import dev.vudovenko.eventmanagement.locations.dto.LocationDto;
import dev.vudovenko.eventmanagement.locations.services.impl.LocationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LocationControllerTest extends AbstractTest {

    @Autowired
    private LocationService locationService;

    @Test
    void shouldSuccessCreateLocation() throws Exception {
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

        Assertions.assertEquals(errorMessageResponse.message(), "Request validation failed");
        Assertions.assertTrue(detailedMessage.contains("id:"));
        Assertions.assertTrue(detailedMessage.contains("name:"));
        Assertions.assertTrue(detailedMessage.contains("address:"));
        Assertions.assertTrue(detailedMessage.contains("capacity:"));
        Assertions.assertNotNull(errorMessageResponse.dateTime());
    }
}