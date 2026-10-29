package org.example.blogpost.repositories;


import io.lettuce.core.dynamic.annotation.Param;
import org.example.blogpost.entities.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;


import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long>, JpaSpecificationExecutor<Post> {
    @Query(value = "SELECT p FROM Post p JOIN FETCH p.author", countQuery = "SELECT COUNT(p) FROM Post p")
    Page<Post> findAllWithAuthor(Pageable pageable);

    @Query("SELECT p FROM Post p " +
            "JOIN FETCH p.author " +
            "LEFT JOIN FETCH p.images " +
            "WHERE p.id = :id")
    Optional<Post> findByIdWithDetails(@Param("id") Long id);
}
