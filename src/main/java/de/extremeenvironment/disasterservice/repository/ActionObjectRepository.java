package de.extremeenvironment.disasterservice.repository;

import de.extremeenvironment.disasterservice.domain.ActionObject;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for the ActionObject entity.
 */
public interface ActionObjectRepository extends JpaRepository<ActionObject,Long> {

}
