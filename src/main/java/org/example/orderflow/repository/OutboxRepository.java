package org.example.orderflow.repository;

import org.example.orderflow.entity.Outbox;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxRepository extends JpaRepository<Outbox, Long> {

    List<Outbox> findByProcessedFalseOrderByCreatedAtAsc();
}
