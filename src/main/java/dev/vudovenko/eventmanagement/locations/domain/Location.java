package dev.vudovenko.eventmanagement.locations.domain;

import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@AllArgsConstructor
public class Location {

    private Long id;
    private String name;
    private String address;
    private Integer capacity;
    private String description;
}
