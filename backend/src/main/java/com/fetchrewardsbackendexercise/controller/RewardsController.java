package main.java.com.fetchrewardsbackendexercise.controller;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.validation.Valid;

import main.java.com.fetchrewardsbackendexercise.models.Payer;
import main.java.com.fetchrewardsbackendexercise.models.PointsToSpend;
import main.java.com.fetchrewardsbackendexercise.models.SpendThesePoints;
import main.java.com.fetchrewardsbackendexercise.models.Transaction;
import main.java.com.fetchrewardsbackendexercise.repositories.PayerRepository;
import main.java.com.fetchrewardsbackendexercise.repositories.TransactionRepository;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class RewardsController {

    PayerRepository payerRepository;
    TransactionRepository transactionRepository;

    public RewardsController(PayerRepository payerRepository, TransactionRepository transactionRepository) {
        this.payerRepository = payerRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     *
     * @return
     *
     * This endpoint handles the "RETURN ALL PAYER POINT BALANCES" functionality. The Spring Data JPA framework
     * provides methods signatures that work as queries to the SQL database. Thus, the findAll() method will
     * retrieve all of the payers in the Payers table. Each Payer model contains the Payer's name and their
     * respective points.
     */
    @GetMapping("/payers")
    Collection<Payer> getPayers() {
        return (Collection<Payer>) payerRepository.findAll();
    }

    /**
     *
     * @return
     * This endpoint was not required for the project but it may come in handy in POSTMAN or some other API
     * testing tool to verify the transactions in the database. It will return the entire list of records in
     * the transactions table.
     */
    @GetMapping("/transactions")
    Collection<Transaction> getTransactions() {
        return (Collection<Transaction>) transactionRepository.findAll();
    }

    /**
     *
     * @param transaction
     * @return
     * @throws URISyntaxException
     *
     *  This endpoint handles the "ADD TRANSACTIONS FOR A SPECIFIC PAYER AND DATE" functionality. The client will
     *  send a Transaction object in JSON format that will contain the respective values for the transaction,
     *  which are the Payer's name (String), the points for the transaction (int), and the timestamp. The
     *  implementation below will add the Payer to the Payer table in the database if the Payer does not exist,
     *  and it will update the Payers point balance if it does exist. Additionally, the transaction record will be
     *  added to the transaction table.
     */
    @PostMapping("/transactions")
    ResponseEntity<Transaction> addTransaction(@Valid @RequestBody Transaction transaction)
            throws URISyntaxException {

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

    /**
     *
     * @param stp
     * @return
     * @throws URISyntaxException
     *
     * This endpoint handles the "Spend points using the rules above and return a
     * list of { "payer": <string>, "points": <integer> } for each call" functionality. The client passes in
     * a SpendThesePoints object that contains the amount of points they want to spend. The logic for
     * implementing the functionality is to start by returning the records from the Transaction table with the records
     * ordered by timestamp. The findByOrderByTimestamp() function will return those records in order.  Once
     * we have the first transaction in the queue, we begin to deduct the stp (points) passed in as a parameter
     * until it reaches 0. If the transaction amount is less than the stp (points), I would delete the transaction
     * record from the table and move onto the next transaction. Additionally, while I spend the points, I
     * adjust the payer's points balances in the payers table.
     */
    @PutMapping("/spendpoints")
    Collection<PointsToSpend> spendPoints(@Valid @RequestBody SpendThesePoints stp) throws URISyntaxException {

        /**
         * The pointtospendList is the object that will be returned to the client after this endpoint is called. It
         * will return the values spent by each Payer object in the application.
         */
        List<PointsToSpend> pointstospendList = new ArrayList<>();

        /**
         * This HashSet will allow us to know whether or not a specific payer has been called on more than once
         * to spend its points. A HashSet is efficient for searching as it is O(1) time complexity.
         */
        HashSet<String> payersInPointsToSpendList = new HashSet<>();
        pts = stp.getPoints();

        /**
         * This is where I iterate through the transaction table, in order of timestamp. I have different conditions
         * for when the transaction points is negative or positive. As described in the PDF file, the rules dictate
         * that positive amounts would imply that the Payers are losing points and negative amounts imply that the
         * payers are gaining points.
         */
        transactionRepository.findByOrderByTimestamp().forEach(transaction -> {
           if (pts > 0) {
               Payer payer = payerRepository.findByName(transaction.payer);

               /**
                * This condition handles the logic for when the transaction amount is positive. It will deduct
                * Payer point balances and lower the pts(balance) to spend points. Additionally, it
                * updates the pointsToSpendList that will be returned to the client. The else condition will handle
                * the logic for when the transaction amount is negative.
                */
               if (transaction.points >= 0) {
                   int temp = Math.min(transaction.points, pts);
                   transaction.points -= temp;
                   pts -= temp;
                       payer.setPoints(payer.getPoints() - temp);
                       if (payer.getPoints() < 0) payer.setPoints(0);
                       payerRepository.delete(payer);
                       payerRepository.save(payer);

                   if (payersInPointsToSpendList.contains(transaction.getPayer())) {
                       for (int a = 0; a < pointstospendList.size(); a++) {
                           PointsToSpend currentPTS = pointstospendList.get(a);
                           if (transaction.getPayer().equals(currentPTS.getPayer())) {
                               currentPTS.setPoints(currentPTS.points -= temp);
                               break;
                           }
                       }
                   } else {
                       payersInPointsToSpendList.add(transaction.getPayer());
                       PointsToSpend currentPTS = new PointsToSpend(transaction.getPayer(), (-1) * temp );
                       pointstospendList.add(currentPTS);
                   }

                   if (transaction.points == 0) {
                       transactionRepository.deleteById(transaction.getId());
                   }

               } else {
                   payer.setPoints(payer.getPoints() + transaction.points);
                   pts += transaction.points;
                   if (payersInPointsToSpendList.contains(transaction.getPayer())) {
                       for (int a = 0; a < pointstospendList.size(); a++) {
                           PointsToSpend currentPTS = pointstospendList.get(a);
                           if (transaction.getPayer().equals(currentPTS.getPayer())) {
                               currentPTS.setPoints(currentPTS.points += transaction.points);
                               break;
                           }
                       }
                   } else {
                       payersInPointsToSpendList.add(transaction.getPayer());
                       PointsToSpend currentPTS = new PointsToSpend(transaction.getPayer(), transaction.points);
                       pointstospendList.add(currentPTS);
                   }

                   /**
                    * This is where I delete a transaction record in case the transaction amount is 0.
                    */
                   if (transaction.points == 0) {
                       transactionRepository.deleteById(transaction.getId());
                   }
               }
           }

        });
        return pointstospendList;
    }
}

