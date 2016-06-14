package de.extremeenvironment.disasterservice.domain;


import com.fasterxml.jackson.annotation.JsonIgnore;
import de.extremeenvironment.disasterservice.domain.enumeration.ActionType;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * An Action.
 */
@Entity
@Table(name = "action")
public class Action implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Column(name = "lat", nullable = false)
    private Float lat;

    @NotNull
    @Column(name = "lon", nullable = false)
    private Float lon;

    @Column(name = "is_expired")
    private Boolean isExpired;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private ActionType actionType;


    @ManyToOne
    private User user;


    @ManyToMany
    @JoinTable(name = "action_action_object",
        joinColumns = @JoinColumn(name = "actions_id", referencedColumnName = "ID"),
        inverseJoinColumns = @JoinColumn(name = "action_objects_id", referencedColumnName = "ID"))
    private Set<ActionObject> actionObjects = new HashSet<>();

    @OneToOne
    private Action match;

    @OneToMany
    @JsonIgnore
    private Set<Action> rejectedMatches = new HashSet<>();



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Float getLat() {
        return lat;
    }

    public void setLat(Float lat) {
        this.lat = lat;
    }

    public Float getLon() {
        return lon;
    }

    public void setLon(Float lon) {
        this.lon = lon;
    }

    public Boolean isIsExpired() {
        return isExpired;
    }

    public void setIsExpired(Boolean isExpired) {
        this.isExpired = isExpired;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Set<ActionObject> getActionObjects() {
        return actionObjects;
    }

    public void setActionObjects(Set<ActionObject> actionObjects) {
        this.actionObjects = actionObjects;
    }

    public Action getMatch() {
        return match;
    }

    public void setMatch(Action match) {
        this.match = match;
    }

    public Set<Action> getRejectedMatches() {
        return rejectedMatches;
    }

    public void addRejectedMatch(Action rejectedMatch) {
        rejectedMatches.add(rejectedMatch);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Action action = (Action) o;
        if (action.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, action.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Action{" +
            "id=" + id +
            ", lat='" + lat + "'" +
            ", lon='" + lon + "'" +
            ", isExpired='" + isExpired + "'" +
            ", actionType='" + actionType + "'" +
            '}';
    }
}
