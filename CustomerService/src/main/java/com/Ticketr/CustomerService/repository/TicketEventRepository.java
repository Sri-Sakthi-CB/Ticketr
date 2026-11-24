package com.Ticketr.CustomerService.repository;

import com.Ticketr.CustomerService.domain.Ticket;
import com.Ticketr.CustomerService.domain.TicketEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketEventRepository extends JpaRepository<TicketEvent, Long> {
    List<TicketEvent> findByTicketOrderByCreatedAtAsc(Ticket ticket);
}
