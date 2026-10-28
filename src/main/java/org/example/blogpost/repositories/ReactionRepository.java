package org.example.blogpost.repositories;

import org.example.blogpost.entities.Reaction;
import org.example.blogpost.enums.ReactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReactionRepository extends JpaRepository<Reaction, Long> {

    Optional<Reaction> findByPostIdAndUserId(Long postId, Long userId);

    @Query("select r.type as type, count(r) as total " +
            "from Reaction r where r.post.id = :postId group by r.type")
    List<ReactionCount> countByType(@Param("postId") Long postId);

    interface ReactionCount {
        ReactionType getType();
        Long getTotal();
    }
}
