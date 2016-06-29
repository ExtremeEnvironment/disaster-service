package de.extremeenvironment.disasterservice.domain;


import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A Disaster.
 */
@Entity
@Table(name = "disaster")
public class Disaster extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "is_expired")
    private Boolean isExpired;

    @Column(name = "lat")
    private Float lat;

    @Column(name = "lon")
    private Float lon;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @ManyToOne(fetch=FetchType.EAGER)
    private DisasterType disasterType;

    @ManyToOne(fetch=FetchType.EAGER,cascade = CascadeType.REMOVE)
    private Area area;

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

    public DisasterType getDisasterType() {
        return disasterType;
    }

    public void setDisasterType(DisasterType disasterType) {
        this.disasterType = disasterType;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
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
            ", title='" + title + "'" +
            ", description='" + description + "'" +
            '}';
    }
}
