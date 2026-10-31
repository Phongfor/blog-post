package org.example.blogpost.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.blogpost.dto.response.ReactionSummaryResponse;
import org.example.blogpost.entities.Reaction;
import org.example.blogpost.enums.ReactionType;
import org.example.blogpost.exception.AppException;
import org.example.blogpost.exception.ErrorCode;
import org.example.blogpost.repositories.PostRepository;
import org.example.blogpost.repositories.ReactionRepository;
import org.example.blogpost.repositories.UserRepository;
import org.example.blogpost.service.ReactionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(readOnly = true)
@Slf4j
public class ReactionServiceImpl implements ReactionService {

    ReactionRepository reactionRepository;
    PostRepository postRepository;
    UserRepository userRepository;

    @Override
    @Transactional
    public ReactionSummaryResponse react(Long postId, Long userId, ReactionType type) {
        checkPostExists(postId);

        Optional<Reaction> existing = reactionRepository.findByPostIdAndUserId(postId, userId);
        ReactionType myReaction;

        if (existing.isEmpty()) {
            // Chưa có → tạo mới (dùng getReferenceById để khỏi truy vấn thêm)
            Reaction reaction = Reaction.builder()
                    .post(postRepository.getReferenceById(postId))
                    .user(userRepository.getReferenceById(userId))
                    .type(type)
                    .build();
            reactionRepository.save(reaction);
            myReaction = type;
            log.info("Reaction created: postId={}, userId={}, type={}", postId, userId, type);
        } else if (existing.get().getType() == type) {
            // Bấm lại đúng loại đang có → gỡ
            reactionRepository.delete(existing.get());
            myReaction = null;
            log.info("Reaction toggled off: postId={}, userId={}, type={}", postId, userId, type);
        } else {
            // Đang có loại khác → đổi loại
            existing.get().setType(type);
            myReaction = type;
            log.info("Reaction changed: postId={}, userId={}, newType={}", postId, userId, type);
        }

        return buildSummary(postId, myReaction);
    }

    @Override
    @Transactional
    public ReactionSummaryResponse removeReaction(Long postId, Long userId) {
        checkPostExists(postId);

        // Chưa có reaction thì bỏ qua, không báo lỗi
        reactionRepository.findByPostIdAndUserId(postId, userId).ifPresent(reaction -> {
            reactionRepository.delete(reaction);
            log.info("Reaction removed: postId={}, userId={}", postId, userId);
        });

        return buildSummary(postId, null);
    }

    @Override
    public ReactionSummaryResponse getSummary(Long postId, Long currentUserId) {
        checkPostExists(postId);

        ReactionType myReaction = null;
        if (currentUserId != null) {
            myReaction = reactionRepository.findByPostIdAndUserId(postId, currentUserId)
                    .map(Reaction::getType)
                    .orElse(null);
        }
        return buildSummary(postId, myReaction);
    }

    private void checkPostExists(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new AppException(ErrorCode.POST_NOT_FOUND);
        }
    }

    // Đếm theo từng loại; hiện đủ 6 loại, loại nào chưa có thì là 0
    private ReactionSummaryResponse buildSummary(Long postId, ReactionType myReaction) {
        Map<ReactionType, Long> counts = new EnumMap<>(ReactionType.class);
        for (ReactionType t : ReactionType.values()) {
            counts.put(t, 0L);
        }
        for (ReactionRepository.ReactionCount row : reactionRepository.countByType(postId)) {
            counts.put(row.getType(), row.getTotal());
        }
        long total = counts.values().stream().mapToLong(Long::longValue).sum();

        return ReactionSummaryResponse.builder()
                .postId(postId)
                .myReaction(myReaction)
                .total(total)
                .counts(counts)
                .build();
    }
}