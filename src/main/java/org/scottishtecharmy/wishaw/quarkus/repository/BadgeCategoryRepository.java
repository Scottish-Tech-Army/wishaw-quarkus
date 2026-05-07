package org.scottishtecharmy.wishaw.quarkus.repository;

import java.util.Optional;
import java.util.UUID;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.scottishtecharmy.wishaw.quarkus.model.BadgeCategory;

@ApplicationScoped
public class BadgeCategoryRepository implements PanacheRepositoryBase<BadgeCategory, UUID> {

    public Optional<BadgeCategory> findByDisplayName(String displayName) {
        return find("displayName", displayName).firstResultOptional();
    }
}
