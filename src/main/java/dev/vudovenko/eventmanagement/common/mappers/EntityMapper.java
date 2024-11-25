package dev.vudovenko.eventmanagement.common.mappers;

public interface EntityMapper<Domain, Entity> {

    Domain toDomain(Entity entity);

    Entity toEntity(Domain domain);
}
