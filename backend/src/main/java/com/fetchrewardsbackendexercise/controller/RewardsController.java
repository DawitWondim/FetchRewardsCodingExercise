package com.fetchrewardsbackendexercise.controller;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.validation.Valid;

import com.fetchrewardsbackendexercise.models.Payer;
import com.fetchrewardsbackendexercise.models.Transaction;
import com.fetchrewardsbackendexercise.repositories.PayerRepository;
import com.fetchrewardsbackendexercise.repositories.TransactionRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class RewardsController {

    PayerRepository payerRepository;
    TransactionRepository transactionRepository;

    public RewardsController(PayerRepository payerRepository, TransactionRepository transactionRepository) {
        this.payerRepository = payerRepository;
        this.transactionRepository = transactionRepository;
    }

    @GetMapping("/payers")
    Collection<Payer> getPayers() {
        return (Collection<Payer>) payerRepository.findAll();
    }

    @GetMapping("/transactions")
    Collection<Transaction> getTransactions() {
        return (Collection<Transaction>) transactionRepository.findAll();
    }

    @PostMapping("/payers")
    ResponseEntity<Payer> createPayer(@Valid @RequestBody Payer payer) throws URISyntaxException {
        Payer result = payerRepository.save(payer);
        System.out.println(payer.name);
        return ResponseEntity.ok().body(result);
    }

    @PostMapping("/transactions")
    ResponseEntity<Transaction> addTransaction(@Valid @RequestBody Transaction transaction)
            throws URISyntaxException {

        Payer payer = payerRepository.findByName(transaction.payer);

        if (payerRepository.existsPayerByName(transaction.payer)) {
            int value = payer.points + transaction.points;
            if (value < 0)
                payer.points = 0;
            payer.points = value;
        } else {
            if (transaction.points < 0)
                payer.points = transaction.points;
            payerRepository.save(payer);
        }

        Transaction result = transactionRepository.save(transaction);
        return ResponseEntity.ok().body(result);
    }

    int pts = 0;
    Payer payer;
    List<Payer> payers = new ArrayList<>();
    @PutMapping("/spendpoints")
    Collection<Payer> spendPoints(@Valid @RequestBody int pointsToSpend) throws URISyntaxException {       
        
        pts = pointsToSpend;
        
        transactionRepository.findByOrderByTimestamp().forEach(transaction -> {
            boolean repeatPayer = false;
            for (int a = 0; a < payers.size(); a++) {
                if (transaction.payer.equals(payers.get(a).name)) {
                    repeatPayer = true;
                    if (pts > 0) {
                        if (transaction.points > pts) {
                            transaction.points -= pts;
                            payers.get(a).points += ((-1) * pts);
                            pts = 0;
                        } else if ((transaction.points > 0) && (transaction.points <= pointsToSpend)) {
                            pts -= transaction.points;
                            payers.get(a).points += ((-1) * transaction.points);
                            transactionRepository.deleteById(transaction.getId());
                        } else {
                            pts += transaction.points;
                            payers.get(a).points += ((-1) * transaction.points);
                            transactionRepository.deleteById(transaction.getId());
                        }
                    }
                } 
            }
            if (!repeatPayer) {
                payer = new Payer();
                if (pts > 0) {
                    if (transaction.points > pts) {
                        transaction.points -= pts;
                        pts = 0;
                    } else if ((transaction.points > 0) && (transaction.points <= pointsToSpend)) {
                        pts -= transaction.points;
                        transactionRepository.deleteById(transaction.getId());
                    } else {
                        pts += transaction.points;
                        transactionRepository.deleteById(transaction.getId());
                    }
                }
            }
        });
        return payers;
    }
}

