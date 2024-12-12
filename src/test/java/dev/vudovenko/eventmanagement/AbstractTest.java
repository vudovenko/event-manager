package dev.vudovenko.eventmanagement;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.vudovenko.eventmanagement.users.domain.User;
import dev.vudovenko.eventmanagement.users.userRoles.UserRole;
import dev.vudovenko.eventmanagement.utils.TokenTestUtils;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Log4j2
@AutoConfigureMockMvc
@SpringBootTest
public class AbstractTest {

    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected TokenTestUtils tokenTestUtils;

    private static volatile boolean isSharedSetupDone = false;

    public static PostgreSQLContainer<?> POSTGRES_CONTAINER
            = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("postgres")
            .withUsername("postgres")
            .withPassword("root");

    static {
        synchronized (AbstractTest.class) {
            if (!isSharedSetupDone) {
                POSTGRES_CONTAINER.start();
                isSharedSetupDone = true;
            }
        }
    }

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("test.postgres.port", POSTGRES_CONTAINER::getFirstMappedPort);
    }

    @EventListener
    public void stopContainer(ContextStoppedEvent e) {
        POSTGRES_CONTAINER.stop();
    }

    public String getAuthorizationHeader(UserRole role) {
        return "Bearer " + tokenTestUtils.getJwtTokenWithRole(role);
    }

    public String getAuthorizationHeader(User user) {
        return "Bearer " + tokenTestUtils.getJwtToken(user);
    }

    public static void compareDatesWithTruncatedToSeconds(LocalDateTime firstDate, LocalDateTime secondDate) {
        Assertions.assertEquals(
                firstDate.truncatedTo(ChronoUnit.SECONDS),
                secondDate.truncatedTo(ChronoUnit.SECONDS)
        );
    }
}
