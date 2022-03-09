package com.fetchrewardsbackendexercise.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "PAYER")
public class Payer {

    @Column(name = "id")
    public @Id @GeneratedValue Integer id;

    @Column(name = "name")
    public String name;

    @Column(name = "points")
    public int points;

    public Payer() {
    }

    public Payer(String name, int points) {
        this.name = name;
        this.points = points;
    }
}
