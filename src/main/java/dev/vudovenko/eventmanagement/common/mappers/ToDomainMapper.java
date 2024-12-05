package dev.vudovenko.eventmanagement.common.mappers;

public interface ToDomainMapper<Domain, AnotherLayer> {

    Domain toDomain(AnotherLayer anotherLayer);
}
