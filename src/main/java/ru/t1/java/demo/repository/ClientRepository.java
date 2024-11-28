package ru.t1.java.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.java.demo.model.Client;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {
    @Override
    Optional<Client> findById(Long aLong);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Client c set c.status = 'BLOCKED' where c.clientId = :clientId")
    void setBloked(Long clientId);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Client c set c.status = null where c.clientId = :clientId")
    void setUnbloked(Long clientId);

    @Query(value = "select c.id from client c where c.status = 'BLOCKED' limit :clientAmount", nativeQuery = true)
    List<Long> findAllBlocked(Integer clientAmount);

    @Query("select count(c.clientId) from Client c where c.status = 'BLOCKED'")
    Long countAllBlocked();
}