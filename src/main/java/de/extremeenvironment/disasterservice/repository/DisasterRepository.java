package de.extremeenvironment.disasterservice.repository;

import de.extremeenvironment.disasterservice.domain.Disaster;

import org.springframework.data.jpa.repository.*;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the Disaster entity.
 */
@SuppressWarnings("unused")
public interface DisasterRepository extends JpaRepository<Disaster,Long> {

    Optional<Disaster> findById(Long id);

    List<Disaster> findByIsExpired(boolean isExpired);

}
