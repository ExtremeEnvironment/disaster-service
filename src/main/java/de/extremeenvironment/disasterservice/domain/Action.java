package de.extremeenvironment.disasterservice.domain;


import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.deser.std.NumberDeserializers;
import de.extremeenvironment.disasterservice.domain.enumeration.ActionType;

/**
 * A Action.
 */
@Entity
@Table(name = "action")
public class Action extends AbstractAuditingEntity implements Serializable {

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

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name="like_counter")
    private Long likeCounter;

    @ManyToOne(fetch=FetchType.EAGER)
    private Disaster disaster;

    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToMany(fetch=FetchType.EAGER)
    @JoinTable(name = "action_action_object",
               joinColumns = @JoinColumn(name="actions_id", referencedColumnName="ID"),
               inverseJoinColumns = @JoinColumn(name="action_objects_id", referencedColumnName="ID"))
    private Set<ActionObject> actionObjects = new HashSet<>();

    @OneToOne(fetch=FetchType.EAGER)
    @JsonIgnore
    @JoinColumn(unique = true)
    private Action match;

    @ManyToMany(fetch=FetchType.EAGER)
    @JsonIgnore
    @JoinTable(name = "action_rejected_matches",
        joinColumns = @JoinColumn(name = "actions_id", referencedColumnName = "ID"),
        inverseJoinColumns = @JoinColumn(name = "rejected_matches_id", referencedColumnName = "ID"))
    private Set<Action> rejectedMatches = new HashSet<>();

    public Action() {
        this.likeCounter= new Long(0);
    }


    public void addActionObject(ActionObject ao){actionObjects.add(ao);}

    public void addRejectedMatch(Action action) {
        this.rejectedMatches.add(action);
    }

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Disaster getDisaster() {
        return disaster;
    }

    public void setDisaster(Disaster disaster) {
        this.disaster = disaster;
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

    public void setMatch(Action action) {
        this.match = action;
    }

    public Set<Action> getRejectedMatches() {
        return rejectedMatches;
    }

    public void setRejectedMatches(Set<Action> actions) {
        this.rejectedMatches = actions;
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
        if(action.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, action.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public Long getLikeCounter() {
        return likeCounter;
    }

    public void setLikeCounter(Long likeCounter) {
        this.likeCounter = likeCounter;
    }

    @Override
    public String toString() {
        return "Action{" +
            "id=" + id +
            ", lat='" + lat + "'" +
            ", lon='" + lon + "'" +
            ", isExpired='" + isExpired + "'" +
            ", actionType='" + actionType + "'" +
            ", title='" + title + "'" +
            ", description='" + description + "'" +
            "likeCounter='" + likeCounter + "'" +
            '}';
    }
}
