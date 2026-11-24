package com.Ticketr.CustomerService.web.dto;

import com.Ticketr.CustomerService.domain.TicketEvent;
import com.Ticketr.CustomerService.domain.TicketStatus;

import java.time.Instant;

public record TicketEventResponse(
        Long id,
        TicketStatus fromStatus,
        TicketStatus toStatus,
        String actorName,
        String note,
        Instant createdAt
) {
    public static TicketEventResponse from(TicketEvent e) {
        return new TicketEventResponse(
                e.getId(),
                e.getFromStatus(),
                e.getToStatus(),
                e.getActor() == null ? "System" : e.getActor().getName(),
                e.getNote(),
                e.getCreatedAt()
        );
    }
}
