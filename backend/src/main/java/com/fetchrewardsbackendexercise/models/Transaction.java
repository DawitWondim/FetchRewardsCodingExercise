package main.java.com.fetchrewardsbackendexercise.models;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "TRANSACTION")
public class Transaction {

    @Column(name = "transaction_id")
    public @Id @GeneratedValue Integer id;

    @Column(name = "payer")
    public String payer;

    @Column(name = "points")
    public int points;

    @Column(name = "timestamp")
    public Timestamp timestamp;

    public Transaction() {
    }

    public Transaction(String payer, int points, Timestamp timestamp) {
        this.payer = payer;
        this.points = points;
        this.timestamp = timestamp;
    }

    public Integer getId() {
        return this.id;
    }

}
