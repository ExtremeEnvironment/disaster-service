package de.extremeenvironment.disasterservice.client;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.extremeenvironment.disasterservice.domain.Disaster;

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

    private String type;

    private Long matchedActionId;

    public Conversation() {
    }

    public Conversation( Boolean active, String title) {


        this.active = active;
        this.title = title;
    }

    public Conversation(Long id, Boolean active, String title) {
        this.id = id;
        this.active = active;
        this.title = title;
    }

    public Conversation(Long id, Boolean active, String title, String type) {
        this.id = id;
        this.active = active;
        this.title = title;
        this.type = type;
    }

    public Conversation(Long id, Boolean active, String title, String type, Long matchedActionId) {
        this.id = id;
        this.active = active;
        this.title = title;
        this.type = type;
        this.matchedActionId = matchedActionId;
    }

    public static Conversation forDisaster(Disaster disaster) {
        return new Conversation(null, true, String.format("public '%s' chat", disaster.getTitle()), "public");
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getMatchedActionId() {
        return matchedActionId;
    }

    public void setMatchedActionId(Long matchedActionId) {
        this.matchedActionId = matchedActionId;
    }
}
