package de.extremeenvironment.disasterservice.client;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Created by over on 30.06.2016.
 */
public class Conversation implements Serializable {

    private static final long serialVersionUID = 189369962L;


    private Long id;

    private Boolean active = true;


    private String title;

    public Conversation() {
    }

    public Conversation( Boolean active, String title) {


        this.active = active;
        this.title = title;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
