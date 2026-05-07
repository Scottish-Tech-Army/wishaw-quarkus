package org.scottishtecharmy.wishaw.quarkus.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.scottishtecharmy.wishaw.quarkus.model.AppUser;

@ApplicationScoped
public class AppUserRepository implements PanacheRepositoryBase<AppUser, UUID> {

    public Optional<AppUser> findByUsername(String username) {
        return find("username", username).firstResultOptional();
    }

    public List<AppUser> findByCentreId(UUID centreId) {
        return list("SELECT u FROM AppUser u JOIN FETCH u.centre WHERE u.centre.id = ?1", centreId);
    }

    public List<AppUser> listAllWithCentre() {
        return list("SELECT u FROM AppUser u JOIN FETCH u.centre");
    }
}
