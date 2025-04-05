package com.cashcard.repository;

import com.cashcard.entity.CashCard;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CashCardRepository extends JpaRepository<CashCard, Long> {
    // You can add custom query methods here later
}