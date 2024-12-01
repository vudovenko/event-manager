package dev.vudovenko.eventmanagement.common.users.domain;

import dev.vudovenko.eventmanagement.common.users.userRoles.UserRole;
import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private Long id;
    private String login;
    private String passwordHash;
    private UserRole role;
}
