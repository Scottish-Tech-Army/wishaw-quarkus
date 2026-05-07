package org.scottishtecharmy.wishaw.quarkus.model;

import java.util.UUID;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "game")
public class Game extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public UUID id;

    @Column(name = "display_name", nullable = false)
    public String displayName;

    @Column(nullable = false)
    public boolean active = true;
}

