package org.scottishtecharmy.wishaw.quarkus.repository;

import java.util.List;
import java.util.UUID;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.scottishtecharmy.wishaw.quarkus.model.LevelDefinition;

@ApplicationScoped
public class LevelDefinitionRepository implements PanacheRepositoryBase<LevelDefinition, UUID> {

    public List<LevelDefinition> findAllOrderedByMinXp() {
        return list("order by minXp");
    }
}
