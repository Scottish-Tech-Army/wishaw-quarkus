package org.scottishtecharmy.wishaw.quarkus.repository;

import java.util.UUID;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.scottishtecharmy.wishaw.quarkus.model.Centre;

@ApplicationScoped
public class CentreRepository implements PanacheRepositoryBase<Centre, UUID> {
}

