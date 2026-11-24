package com.Ticketr.CustomerService.web;

import com.Ticketr.CustomerService.domain.Role;
import com.Ticketr.CustomerService.domain.Ticket;
import com.Ticketr.CustomerService.domain.User;
import com.Ticketr.CustomerService.exception.ForbiddenActionException;
import com.Ticketr.CustomerService.security.UserPrincipal;
import com.Ticketr.CustomerService.service.TicketService;
import com.Ticketr.CustomerService.web.dto.CreateTicketRequest;
import com.Ticketr.CustomerService.web.dto.NoteRequest;
import com.Ticketr.CustomerService.web.dto.TicketEventResponse;
import com.Ticketr.CustomerService.web.dto.TicketResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    public ResponseEntity<TicketResponse> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateTicketRequest request) {
        User customer = requireRole(principal, Role.CUSTOMER, "Only customers can raise tickets");
        Ticket ticket = ticketService.create(customer, request);
        return ResponseEntity.ok(TicketResponse.from(ticket, customer));
    }

    @GetMapping
    public List<TicketResponse> list(@AuthenticationPrincipal UserPrincipal principal) {
        User viewer = principal.getUser();
        return ticketService.listForViewer(viewer).stream()
                .map(t -> TicketResponse.from(t, viewer))
                .toList();
    }

    @GetMapping("/{id}")
    public TicketResponse get(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        User viewer = principal.getUser();
        return TicketResponse.from(ticketService.getForViewer(viewer, id), viewer);
    }

    @GetMapping("/{id}/events")
    public List<TicketEventResponse> events(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        User viewer = principal.getUser();
        Ticket ticket = ticketService.getForViewer(viewer, id);
        return ticketService.history(ticket).stream().map(TicketEventResponse::from).toList();
    }

    @PatchMapping("/{id}/start")
    public TicketResponse start(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        User agent = principal.getUser();
        return TicketResponse.from(ticketService.startProgress(agent, id), agent);
    }

    @PatchMapping("/{id}/resolve")
    public TicketResponse resolve(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody NoteRequest request) {
        User agent = principal.getUser();
        return TicketResponse.from(ticketService.resolve(agent, id, request.note()), agent);
    }

    @PatchMapping("/{id}/close")
    public TicketResponse close(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        User agent = principal.getUser();
        return TicketResponse.from(ticketService.close(agent, id, null), agent);
    }

    @PatchMapping("/{id}/reopen")
    public TicketResponse reopen(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody NoteRequest request) {
        User customer = principal.getUser();
        return TicketResponse.from(ticketService.reopen(customer, id, request.note()), customer);
    }

    private User requireRole(UserPrincipal principal, Role role, String message) {
        User user = principal.getUser();
        if (user.getRole() != role) {
            throw new ForbiddenActionException(message);
        }
        return user;
    }
}
