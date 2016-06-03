package de.extremeenvironment.disasterservice.repository;

import de.extremeenvironment.disasterservice.domain.Action;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

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

}
