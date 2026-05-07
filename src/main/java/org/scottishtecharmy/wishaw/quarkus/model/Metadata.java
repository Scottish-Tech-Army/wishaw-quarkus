package org.scottishtecharmy.wishaw.quarkus.model;

import java.util.UUID;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "metadata")
public class Metadata extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public UUID id;

    @Column
    public String icon;

    @Column
    public String link;
}

