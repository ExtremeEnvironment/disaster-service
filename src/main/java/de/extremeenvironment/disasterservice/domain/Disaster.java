package de.extremeenvironment.disasterservice.domain;


import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A Disaster.
 */
@Entity
@Table(name = "disaster")
public class Disaster implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "is_expired")
    private Boolean isExpired;

    @Column(name = "lat")
    private Long lat;

    @Column(name = "lon")
    private Long lon;

    public Disaster() {

    }

    public Disaster(Long lon, Long lat ) {
        this.lon=lon;
        this.lat=lat;
        isExpired=false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean isIsExpired() {
        return isExpired;
    }

    public void setIsExpired(Boolean isExpired) {
        this.isExpired = isExpired;
    }

    public Long getLat() {
        return lat;
    }

    public void setLat(Long lat) {
        this.lat = lat;
    }

    public Long getLon() {
        return lon;
    }

    public void setLon(Long lon) {
        this.lon = lon;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Disaster disaster = (Disaster) o;
        if(disaster.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, disaster.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Disaster{" +
            "id=" + id +
            ", isExpired='" + isExpired + "'" +
            ", lat='" + lat + "'" +
            ", lon='" + lon + "'" +
            '}';
    }
}
