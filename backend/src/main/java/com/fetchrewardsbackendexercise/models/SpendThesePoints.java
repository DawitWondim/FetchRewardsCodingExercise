package main.java.com.fetchrewardsbackendexercise.models;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "SpendThesePoints")
public class SpendThesePoints {

    @Column(name = "id")
    public @Id
    @GeneratedValue
    Integer id;

    @Column(name = "points")
    int points;

    public SpendThesePoints(int points) {
        this.points = points;
    }

    public SpendThesePoints() {

    }
}
