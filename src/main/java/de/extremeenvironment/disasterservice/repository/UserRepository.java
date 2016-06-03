package de.extremeenvironment.disasterservice.repository;

import de.extremeenvironment.disasterservice.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by on 03.06.16.
 *
 * @author David Steiman
 */
public interface UserRepository extends JpaRepository<User, Long>{
}
