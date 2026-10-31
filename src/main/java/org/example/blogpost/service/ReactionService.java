package org.example.blogpost.service;

import org.example.blogpost.dto.response.ReactionSummaryResponse;
import org.example.blogpost.enums.ReactionType;

public interface ReactionService {

    ReactionSummaryResponse react(Long postId, Long userId, ReactionType type);

    ReactionSummaryResponse removeReaction(Long postId, Long userId);

    // currentUserId có thể null (guest)
    ReactionSummaryResponse getSummary(Long postId, Long currentUserId);
}