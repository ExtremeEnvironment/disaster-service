package de.extremeenvironment.disasterservice.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by on 03.06.16.
 *
 * @author David Steiman
 */
@Entity
@Table(name = "jhi_user")
public class User {


    public User() {
        this.actions = new ArrayList<>();
    }

    public User(long userId) {
        this.userId = userId;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NotNull
    @Column(unique = true)
    private long userId;


    @Override
    public String toString() {
        return "User{" +
            "id=" + id +
            ", userId=" + userId +
            ", actions=" + actions +
            '}';
    }


    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private List<Action> actions;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public List<Action> getActions() {
        return actions;
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

}
