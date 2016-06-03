package de.extremeenvironment.disasterservice.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * Created by on 03.06.16.
 *
 * @author David Steiman
 */
@Entity
@Table(name = "jhi_user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NotNull
    @Column(unique = true)
    private long userId;

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
}
