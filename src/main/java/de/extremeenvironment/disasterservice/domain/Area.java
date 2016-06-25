package de.extremeenvironment.disasterservice.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * A Area.
 */
@Entity
@Table(name = "area")
public class Area implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToMany(mappedBy = "area")
    @JsonIgnore
    private Set<Corner> corners = new HashSet<>();

    @OneToOne
    @JoinColumn(unique = true)
    private Ngo ngo;

    @OneToMany(mappedBy = "area")
    @JsonIgnore
    private Set<Disaster> disasters = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<Corner> getCorners() {
        return corners;
    }

    public void setCorners(Set<Corner> corners) {
        this.corners = corners;
    }

    public Ngo getNgo() {
        return ngo;
    }

    public void setNgo(Ngo ngo) {
        this.ngo = ngo;
    }

    public Set<Disaster> getDisasters() {
        return disasters;
    }

    public void setDisasters(Set<Disaster> disasters) {
        this.disasters = disasters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Area area = (Area) o;
        if(area.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, area.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Area{" +
            "id=" + id +
            '}';
    }
}
