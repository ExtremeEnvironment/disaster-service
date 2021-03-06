package de.extremeenvironment.disasterservice.repository;

import de.extremeenvironment.disasterservice.domain.Action;

import de.extremeenvironment.disasterservice.domain.User;
import de.extremeenvironment.disasterservice.domain.enumeration.ActionType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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

    @Query("select distinct action from Action action left join fetch action.actionObjects left join fetch action.rejectedMatches")
    List<Action> findAllWithEagerRelationships();

    @Query("select action from Action action left join fetch action.actionObjects left join fetch action.rejectedMatches where action.id =:id")
    Action findOneWithEagerRelationships(@Param("id") Long id);

    List<Action> findByIsExpired(boolean expired);

    List<Action> findByUserId(Long id);

    List<Action> findByDisasterId(Long id);

    List<Action> findByDisasterIdAndActionType(Long id,ActionType actionType);

    Optional<Action> findActionById(Long id);

    @Query("select action from Action action, User user where action.user.id = user.id and action.isExpired=false")
    List<Action> findNotExpiredActionsByUser(@Param("user") User user);

    List<Action> findActionByActionType(ActionType actionType);

    @Query("select action from Action action where  action.user.id=:id and action.actionType=:actionType")
    List<Action> findByActionType(@Param("id") Long id, @Param("actionType") ActionType actionType );





}
