package org.scottishtecharmy.wishaw.quarkus.repository;

import java.util.List;
import java.util.UUID;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.scottishtecharmy.wishaw.quarkus.model.Module;

@ApplicationScoped
public class ModuleRepository implements PanacheRepositoryBase<Module, UUID> {

    public List<Module> listAllWithAssociations() {
        return list("SELECT m FROM Module m JOIN FETCH m.game");
    }

    public List<Module> listActiveWithAssociations() {
        return list("SELECT m FROM Module m JOIN FETCH m.game WHERE m.active = true");
    }
}
