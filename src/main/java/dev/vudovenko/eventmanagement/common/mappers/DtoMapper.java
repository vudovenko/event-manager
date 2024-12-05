package dev.vudovenko.eventmanagement.common.mappers;

public interface DtoMapper<Domain, DTO> extends ToDomainMapper<Domain, DTO> {

    DTO toDto(Domain domain);
}
