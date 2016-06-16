package de.extremeenvironment.disasterservice.repository;

import de.extremeenvironment.disasterservice.domain.DisasterType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data JPA repository for the DisasterType entity.
 */

public interface DisasterTypeRepository extends JpaRepository<DisasterType,Long> {

   Optional<DisasterType> findById(Long id);

    Optional<DisasterType> findByName(String name);

    @Override
    void delete(DisasterType disasterType);

}
