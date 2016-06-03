package de.extremeenvironment.disasterservice.repository;

import de.extremeenvironment.disasterservice.domain.ActionObject;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the ActionObject entity.
 */
@SuppressWarnings("unused")
public interface ActionObjectRepository extends JpaRepository<ActionObject,Long> {

}
