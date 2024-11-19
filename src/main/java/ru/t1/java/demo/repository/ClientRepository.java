package ru.t1.java.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.java.demo.model.Client;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {
    @Override
    Optional<Client> findById(Long aLong);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Client c set c.status = 'BLOCKED' where c.clientId = :clientId")
    void setBloked(Long clientId);
}