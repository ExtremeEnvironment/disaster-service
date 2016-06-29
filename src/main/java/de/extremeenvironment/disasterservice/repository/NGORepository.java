package de.extremeenvironment.disasterservice.repository;

import de.extremeenvironment.disasterservice.domain.Ngo;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the Ngo entity.
 */
@SuppressWarnings("unused")
public interface NgoRepository extends JpaRepository<Ngo,Long> {

}
