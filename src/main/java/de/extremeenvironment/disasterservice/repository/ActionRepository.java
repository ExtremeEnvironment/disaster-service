package de.extremeenvironment.disasterservice.repository;

import de.extremeenvironment.disasterservice.domain.Action;

import de.extremeenvironment.disasterservice.domain.User;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the Action entity.
 */
@SuppressWarnings("unused")
public interface ActionRepository extends JpaRepository<Action,Long> {
/*
    @Query("select action from Action action where action.user.login = ?#{principal.username}")
    List<Action> findByUserIsCurrentUser();*/

    @Query("select distinct action from Action action left join fetch action.actionObjects")
    List<Action> findAllWithEagerRelationships();

    @Query("select action from Action action left join fetch action.actionObjects where action.id =:id")
    Action findOneWithEagerRelationships(@Param("id") Long id);

    List<Action> findByIsExpired(boolean expired);

    List<Action> findByUserId(Long id);

    List<Action> findByDisasterId(Long id);

    Optional<Action> findActionById(Long id);

    @Query("select action from Action action, User user where action.user.id = user.id and action.isExpired=false")
    List<Action> findNotExpiredActionsByUser(@Param("user") User user);





}
