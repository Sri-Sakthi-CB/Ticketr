package com.Ticketr.CustomerService.service;

import com.Ticketr.CustomerService.domain.*;
import com.Ticketr.CustomerService.exception.ForbiddenActionException;
import com.Ticketr.CustomerService.exception.InvalidTransitionException;
import com.Ticketr.CustomerService.exception.NotFoundException;
import com.Ticketr.CustomerService.repository.TicketEventRepository;
import com.Ticketr.CustomerService.repository.TicketRepository;
import com.Ticketr.CustomerService.web.dto.CreateTicketRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketEventRepository ticketEventRepository;
    private final AssignmentService assignmentService;

    public TicketService(TicketRepository ticketRepository, TicketEventRepository ticketEventRepository, AssignmentService assignmentService) {
        this.ticketRepository = ticketRepository;
        this.ticketEventRepository = ticketEventRepository;
        this.assignmentService = assignmentService;
    }

    @Transactional
    public Ticket create(User customer, CreateTicketRequest request) {
        Department department = assignmentService.resolveDepartment(request.issueType());
        User agent = assignmentService.pickAgent(department);

        Ticket ticket = new Ticket();
        ticket.setSubject(request.subject());
        ticket.setDescription(request.description());
        ticket.setIssueType(request.issueType());
        ticket.setPriority(request.priority());
        ticket.setCustomer(customer);
        ticket.setDepartment(department);
        ticket.setAssignedAgent(agent);
        ticket.setStatus(TicketStatus.ASSIGNED);
        ticket.setDueAt(Instant.now().plus(request.priority().getSlaHours(), ChronoUnit.HOURS));
        ticket = ticketRepository.save(ticket);

        recordEvent(ticket, null, TicketStatus.OPEN, TicketStatus.ASSIGNED,
                "Auto-routed to " + department.getName() + ", assigned to " + agent.getName());

        return ticket;
    }

    public List<Ticket> listForViewer(User viewer) {
        return switch (viewer.getRole()) {
            case CUSTOMER -> ticketRepository.findByCustomerOrderByCreatedAtDesc(viewer);
            case AGENT -> ticketRepository.findByAssignedAgentOrderByCreatedAtDesc(viewer);
            case ADMIN -> ticketRepository.findAllByOrderByCreatedAtDesc();
        };
    }

    public Ticket getForViewer(User viewer, Long ticketId) {
        Ticket ticket = getById(ticketId);
        boolean allowed = switch (viewer.getRole()) {
            case CUSTOMER -> ticket.getCustomer().getId().equals(viewer.getId());
            case AGENT -> ticket.getAssignedAgent() != null && ticket.getAssignedAgent().getId().equals(viewer.getId());
            case ADMIN -> true;
        };
        if (!allowed) {
            throw new ForbiddenActionException("You do not have access to this ticket");
        }
        return ticket;
    }

    public Ticket getById(Long ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new NotFoundException("Ticket " + ticketId + " not found"));
    }

    @Transactional
    public Ticket startProgress(User agent, Long ticketId) {
        Ticket ticket = getById(ticketId);
        requireAssignedAgent(ticket, agent);
        if (ticket.getStatus() != TicketStatus.ASSIGNED) {
            throw new InvalidTransitionException("Ticket must be ASSIGNED to start progress (current status: " + ticket.getStatus() + ")");
        }
        TicketStatus from = ticket.getStatus();
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        recordEvent(ticket, agent, from, TicketStatus.IN_PROGRESS, "Agent started working the ticket");
        return ticket;
    }

    @Transactional
    public Ticket resolve(User agent, Long ticketId, String resolutionNote) {
        Ticket ticket = getById(ticketId);
        requireAssignedAgent(ticket, agent);
        if (ticket.getStatus() != TicketStatus.IN_PROGRESS && ticket.getStatus() != TicketStatus.REOPENED) {
            throw new InvalidTransitionException("Ticket must be IN_PROGRESS or REOPENED to resolve (current status: " + ticket.getStatus() + ")");
        }
        TicketStatus from = ticket.getStatus();
        ticket.setStatus(TicketStatus.RESOLVED);
        ticket.setResolutionNote(resolutionNote);
        ticket.setResolvedAt(Instant.now());
        recordEvent(ticket, agent, from, TicketStatus.RESOLVED, resolutionNote);
        return ticket;
    }

    /**
     * The checkpoint the whole application exists to enforce: a RESOLVED ticket only becomes CLOSED
     * when the stakeholder it is assigned to closes it. No one else — not the customer, not another
     * agent, not an admin — can perform this transition.
     */
    @Transactional
    public Ticket close(User agent, Long ticketId, String note) {
        Ticket ticket = getById(ticketId);
        requireAssignedAgent(ticket, agent);
        if (ticket.getStatus() != TicketStatus.RESOLVED) {
            throw new InvalidTransitionException("Only a RESOLVED ticket can be closed (current status: " + ticket.getStatus() + ")");
        }
        TicketStatus from = ticket.getStatus();
        ticket.setStatus(TicketStatus.CLOSED);
        ticket.setClosedAt(Instant.now());
        recordEvent(ticket, agent, from, TicketStatus.CLOSED, note != null ? note : "Closed by assigned agent");
        return ticket;
    }

    @Transactional
    public Ticket reopen(User customer, Long ticketId, String reason) {
        Ticket ticket = getById(ticketId);
        if (!ticket.getCustomer().getId().equals(customer.getId())) {
            throw new ForbiddenActionException("Only the customer who raised this ticket can reopen it");
        }
        if (ticket.getStatus() != TicketStatus.RESOLVED) {
            throw new InvalidTransitionException("Only a RESOLVED ticket can be reopened (current status: " + ticket.getStatus() + ")");
        }
        TicketStatus from = ticket.getStatus();
        ticket.setStatus(TicketStatus.REOPENED);
        recordEvent(ticket, customer, from, TicketStatus.REOPENED, reason);
        return ticket;
    }

    public List<TicketEvent> history(Ticket ticket) {
        return ticketEventRepository.findByTicketOrderByCreatedAtAsc(ticket);
    }

    private void requireAssignedAgent(Ticket ticket, User agent) {
        if (ticket.getAssignedAgent() == null || !ticket.getAssignedAgent().getId().equals(agent.getId())) {
            throw new ForbiddenActionException("Only the agent this ticket is assigned to can perform this action");
        }
    }

    private void recordEvent(Ticket ticket, User actor, TicketStatus from, TicketStatus to, String note) {
        ticketEventRepository.save(new TicketEvent(ticket, actor, from, to, note));
    }
}
