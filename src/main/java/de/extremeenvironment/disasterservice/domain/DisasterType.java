package de.extremeenvironment.disasterservice.domain;


import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DisasterType.
 */
@Entity
@Table(name = "disaster_type")
public class DisasterType implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "name")
    private String name;

    public DisasterType() {

    }

    public DisasterType(String name) {
        this.name=name;
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DisasterType disasterType = (DisasterType) o;
        if(disasterType.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, disasterType.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "DisasterType{" +
            "id=" + id +
            ", name='" + name + "'" +
            '}';
    }
}
