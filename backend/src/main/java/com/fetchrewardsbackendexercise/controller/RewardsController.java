package main.java.com.fetchrewardsbackendexercise.controller;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.validation.Valid;

import main.java.com.fetchrewardsbackendexercise.models.Payer;
import main.java.com.fetchrewardsbackendexercise.models.PointsToSpend;
import main.java.com.fetchrewardsbackendexercise.models.SpendThesePoints;
import main.java.com.fetchrewardsbackendexercise.models.Transaction;
import main.java.com.fetchrewardsbackendexercise.repositories.PayerRepository;
import main.java.com.fetchrewardsbackendexercise.repositories.TransactionRepository;

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

        // Look for assert.notnull to improve below code.
        Payer payer = payerRepository.findByName(transaction.payer);
        if (null == payer) {
           if (transaction.points < 0) {
               payer = new Payer(transaction.payer, 0);
           } else {
               payer = new Payer(transaction.payer, transaction.points);
           }
        }
        if (payerRepository.existsPayerByName(transaction.payer)) {
            int value = payer.points + transaction.points;
            if (value < 0) {
                payer.points = 0;
            } else {
                payer.points = value;
            }
        } else {
            payerRepository.save(payer);
        }

        Transaction result = transactionRepository.save(transaction);
        return ResponseEntity.ok().body(result);
    }

    int pts = 0;
    PointsToSpend pointstospend;
    List<PointsToSpend> pointstospendList = new ArrayList<>();
    @PutMapping("/spendpoints")
    Collection<PointsToSpend> spendPoints(@Valid @RequestBody SpendThesePoints stp) throws URISyntaxException {
        
        pts = stp.getPoints();
        
        transactionRepository.findByOrderByTimestamp().forEach(transaction -> {
            boolean repeatPayer = false;
            for (int a = 0; a < pointstospendList.size(); a++) {
                if (transaction.payer.equals(pointstospendList.get(a).getPayer())) {
                    repeatPayer = true;
                    if (pts > 0) {
                        if (transaction.points > pts) {
                            transaction.points -= pts;
                            pointstospendList.get(a).points += ((-1) * pts);
                            pts = 0;
                        } else if ((transaction.points > 0) && (transaction.points <= pts)) {
                            pts -= transaction.points;
                            pointstospendList.get(a).points += ((-1) * transaction.points);
                            transactionRepository.deleteById(transaction.getId());
                        } else {
                            pts += transaction.points;
                            pointstospendList.get(a).points += ((-1) * transaction.points);
                            transactionRepository.deleteById(transaction.getId());
                        }
                    }
                } 
            }
            if (!repeatPayer) {
                pointstospend = new PointsToSpend();
                if (pts > 0) {
                    if (transaction.points > pts) {
                        transaction.points -= pts;
                        pts = 0;
                    } else if ((transaction.points > 0) && (transaction.points <= pts)) {
                        pts -= transaction.points;
                        transactionRepository.deleteById(transaction.getId());
                    } else {
                        pts += transaction.points;
                        transactionRepository.deleteById(transaction.getId());
                    }
                }
                pointstospendList.add(pointstospend);
            }
        });
        return pointstospendList;
    }
}

