package dev.vudovenko.eventmanagement.common.mappers;

public interface EntityMapper<Domain, Entity> extends ToDomainMapper<Domain, Entity> {

    Entity toEntity(Domain domain);
}
