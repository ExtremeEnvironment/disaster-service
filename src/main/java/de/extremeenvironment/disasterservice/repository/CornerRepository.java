package de.extremeenvironment.disasterservice.repository;

import de.extremeenvironment.disasterservice.domain.Corner;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the Corner entity.
 */
@SuppressWarnings("unused")
public interface CornerRepository extends JpaRepository<Corner,Long> {

}
