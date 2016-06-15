package de.extremeenvironment.disasterservice.repository;

import de.extremeenvironment.disasterservice.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Created by on 03.06.16.
 *
 * @author David Steiman
 */
public interface UserRepository extends JpaRepository<User, Long>{


    Optional<User> findOneById(Long Id);

    Optional<User> findOneByUserId(Long userId);


}
