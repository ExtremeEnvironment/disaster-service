package de.extremeenvironment.disasterservice.repository;

import de.extremeenvironment.disasterservice.domain.Ngo;
import org.springframework.data.jpa.repository.*;

/**
 * Spring Data JPA repository for the Ngo entity.
 */
@SuppressWarnings("unused")
public interface NGORepository extends JpaRepository<Ngo,Long> {

}
