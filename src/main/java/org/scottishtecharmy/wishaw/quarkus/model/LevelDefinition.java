package org.scottishtecharmy.wishaw.quarkus.model;

import java.util.UUID;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "level_definition")
public class LevelDefinition extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public UUID id;

    @Column(nullable = false)
    public String name;

    @Column(name = "min_xp", nullable = false)
    public int minXp;

    @Column(name = "max_xp", nullable = false)
    public int maxXp;
}
