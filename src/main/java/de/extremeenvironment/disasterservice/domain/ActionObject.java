package de.extremeenvironment.disasterservice.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * was in einer Aktion angeboten/gesucht wird                                  
 * 
 */
@ApiModel(description = ""
    + "was in einer Aktion angeboten/gesucht wird                             "
    + "")
@Entity
@Table(name = "action_object")
public class ActionObject implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "name")
    private String name;

    @ManyToMany(mappedBy = "actionObjects")
    @JsonIgnore
    private Set<Action> actions = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Action> getActions() {
        return actions;
    }

    public void setActions(Set<Action> actions) {
        this.actions = actions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ActionObject actionObject = (ActionObject) o;
        if(actionObject.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, actionObject.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "ActionObject{" +
            "id=" + id +
            ", name='" + name + "'" +
            '}';
    }
}
