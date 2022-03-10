package main.java.com.fetchrewardsbackendexercise.repositories;

import java.util.List;

import main.java.com.fetchrewardsbackendexercise.models.Transaction;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface TransactionRepository extends CrudRepository<Transaction, Integer> {

    List<Transaction> findByOrderByTimestamp();
}
