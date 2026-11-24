package com.Ticketr.CustomerService.web.dto;

import jakarta.validation.constraints.NotBlank;

/** Body for resolve / reopen actions, both of which require a short explanatory note. */
public record NoteRequest(
        @NotBlank(message = "A note is required") String note
) {
}
