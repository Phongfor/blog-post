package org.example.blogpost.repositories;

import org.example.blogpost.entities.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {


    @Query(value = "SELECT c FROM Comment c JOIN FETCH c.author WHERE c.post.id = :postId",
            countQuery = "SELECT count(c) FROM Comment c WHERE c.post.id = :postId")
    Page<Comment> findByPostIdWithAuthor(@Param("postId") Long postId, Pageable pageable);

    @Query("SELECT c FROM Comment c JOIN FETCH c.author WHERE c.id = :id")
    Optional<Comment> findByIdWithAuthor(@Param("id") Long id);


    @Query("SELECT ci.fileName FROM CommentImage ci WHERE ci.comment.post.id = :postId")
    List<String> findImageFileNamesByPostId(@Param("postId") Long postId);
}