package main.java.com.fetchrewardsbackendexercise.models;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "PointsToSpend")
public class PointsToSpend {

    @Column(name = "id")
    public @Id @GeneratedValue Integer id;

    @Column(name = "payer")
    public String payer;

    @Column(name = "points")
    public int points;


    public PointsToSpend() {}

    public PointsToSpend(String payer, int points) {
        this.payer = payer;
        this.points = points;
    }
}
