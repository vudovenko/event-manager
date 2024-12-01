package dev.vudovenko.eventmanagement.users.domain;

import dev.vudovenko.eventmanagement.users.userRoles.UserRole;
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
    private Integer age;
    private UserRole role;
}
