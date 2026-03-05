package com.cardhub.repository;

import com.cardhub.model.SellTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SellTransactionRepository extends JpaRepository<SellTransaction, Long> {
    List<SellTransaction> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
}
