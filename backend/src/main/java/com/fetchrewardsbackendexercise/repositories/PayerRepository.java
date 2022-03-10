package main.java.com.fetchrewardsbackendexercise.repositories;

import main.java.com.fetchrewardsbackendexercise.models.Payer;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface PayerRepository extends CrudRepository<Payer, Integer> {

    public boolean existsPayerByName(String name);

    public Payer findByName(String name);
}
