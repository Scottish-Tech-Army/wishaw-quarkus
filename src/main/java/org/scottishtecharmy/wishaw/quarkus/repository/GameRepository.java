package org.scottishtecharmy.wishaw.quarkus.repository;

import java.util.UUID;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.scottishtecharmy.wishaw.quarkus.model.Game;

@ApplicationScoped
public class GameRepository implements PanacheRepositoryBase<Game, UUID> {
}

