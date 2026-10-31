package org.example.blogpost.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.blogpost.config.CustomUserDetails;
import org.example.blogpost.dto.request.ReactionRequest;
import org.example.blogpost.dto.response.ApiResponse;
import org.example.blogpost.dto.response.ReactionSummaryResponse;
import org.example.blogpost.service.ReactionService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts/{postId}")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReactionController {

    ReactionService reactionService;

    @PutMapping("/reaction")
    public ApiResponse<ReactionSummaryResponse> react(@AuthenticationPrincipal CustomUserDetails principal,
                                                      @PathVariable Long postId,
                                                      @RequestBody @Valid ReactionRequest request) {
        return ApiResponse.<ReactionSummaryResponse>builder()
                .message("Reaction updated successfully")
                .result(reactionService.react(postId, principal.getId(), request.getType()))
                .build();
    }

    @DeleteMapping("/reaction")
    public ApiResponse<ReactionSummaryResponse> removeReaction(@AuthenticationPrincipal CustomUserDetails principal,
                                                               @PathVariable Long postId) {
        return ApiResponse.<ReactionSummaryResponse>builder()
                .message("Reaction removed successfully")
                .result(reactionService.removeReaction(postId, principal.getId()))
                .build();
    }

    // Public: guest xem được số lượng; có token thì kèm myReaction
    @GetMapping("/reactions")
    public ApiResponse<ReactionSummaryResponse> getReactions(@AuthenticationPrincipal CustomUserDetails principal,
                                                             @PathVariable Long postId) {
        Long currentUserId = principal != null ? principal.getId() : null;
        return ApiResponse.<ReactionSummaryResponse>builder()
                .result(reactionService.getSummary(postId, currentUserId))
                .build();
    }
}