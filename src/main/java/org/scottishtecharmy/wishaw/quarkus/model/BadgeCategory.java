package org.scottishtecharmy.wishaw.quarkus.model;

import java.util.UUID;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "badge_category")
public class BadgeCategory extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public UUID id;

    @Column(name = "display_name", nullable = false)
    public String displayName;

    @Column(columnDefinition = "clob")
    public String description;
}
