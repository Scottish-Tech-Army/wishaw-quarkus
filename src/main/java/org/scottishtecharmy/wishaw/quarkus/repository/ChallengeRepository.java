package org.scottishtecharmy.wishaw.quarkus.repository;

import java.util.List;
import java.util.UUID;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.scottishtecharmy.wishaw.quarkus.model.Challenge;

@ApplicationScoped
public class ChallengeRepository implements PanacheRepositoryBase<Challenge, UUID> {

    public List<Challenge> findByModuleId(UUID moduleId) {
        return list("SELECT c FROM Challenge c JOIN FETCH c.module JOIN FETCH c.badgeCategory WHERE c.module.id = ?1", moduleId);
    }

    public List<Challenge> listAllWithAssociations() {
        return list("SELECT c FROM Challenge c JOIN FETCH c.module JOIN FETCH c.badgeCategory");
    }
}
