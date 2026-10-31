package org.example.blogpost.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.blogpost.enums.ReactionType;

@Getter
@Setter
@NoArgsConstructor
public class ReactionRequest {

    @NotNull(message = "Reaction type is required")
    private ReactionType type;
}