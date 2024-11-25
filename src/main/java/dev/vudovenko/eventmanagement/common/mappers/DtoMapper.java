package dev.vudovenko.eventmanagement.common.mappers;

public interface DtoMapper<Domain, DTO> {

    Domain toDomain(DTO dto);

    DTO toDto(Domain domain);
}
