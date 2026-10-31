package org.example.blogpost.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.blogpost.enums.ReactionType;

import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReactionSummaryResponse {
    private Long postId;
    private ReactionType myReaction;          // null nếu chưa thả hoặc là guest
    private long total;
    private Map<ReactionType, Long> counts;   // đủ 6 loại, loại không có thì là 0
}