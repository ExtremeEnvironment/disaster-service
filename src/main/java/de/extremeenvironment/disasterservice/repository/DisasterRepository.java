package de.extremeenvironment.disasterservice.repository;

import de.extremeenvironment.disasterservice.domain.Disaster;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the Disaster entity.
 */
@SuppressWarnings("unused")
public interface DisasterRepository extends JpaRepository<Disaster,Long> {

}
