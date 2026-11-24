package com.Ticketr.CustomerService.repository;

import com.Ticketr.CustomerService.domain.Ticket;
import com.Ticketr.CustomerService.domain.TicketStatus;
import com.Ticketr.CustomerService.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByCustomerOrderByCreatedAtDesc(User customer);
    List<Ticket> findByAssignedAgentOrderByCreatedAtDesc(User assignedAgent);
    List<Ticket> findAllByOrderByCreatedAtDesc();
    long countByAssignedAgentAndStatusNot(User assignedAgent, TicketStatus status);
}
