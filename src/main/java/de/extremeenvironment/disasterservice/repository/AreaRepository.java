package de.extremeenvironment.disasterservice.repository;

import de.extremeenvironment.disasterservice.domain.Area;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the Area entity.
 */
@SuppressWarnings("unused")
public interface AreaRepository extends JpaRepository<Area,Long> {

}
