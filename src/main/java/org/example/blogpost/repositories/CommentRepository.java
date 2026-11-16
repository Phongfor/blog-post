package org.example.blogpost.repositories;

import org.example.blogpost.entities.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // Chỉ comment gốc của bài (phân trang)
    @Query(value = "SELECT c FROM Comment c JOIN FETCH c.author WHERE c.post.id = :postId AND c.parent IS NULL",
            countQuery = "SELECT count(c) FROM Comment c WHERE c.post.id = :postId AND c.parent IS NULL")
    Page<Comment> findRootsByPostIdWithAuthor(@Param("postId") Long postId, Pageable pageable);

    // Reply của một comment gốc (phân trang)
    @Query(value = "SELECT c FROM Comment c JOIN FETCH c.author LEFT JOIN FETCH c.replyToUser WHERE c.parent.id = :parentId",
            countQuery = "SELECT count(c) FROM Comment c WHERE c.parent.id = :parentId")
    Page<Comment> findRepliesWithAuthor(@Param("parentId") Long parentId, Pageable pageable);

    @Query("SELECT c FROM Comment c JOIN FETCH c.author LEFT JOIN FETCH c.replyToUser WHERE c.id = :id")
    Optional<Comment> findByIdWithAuthor(@Param("id") Long id);

    // Đếm reply cho cả một trang comment gốc bằng 1 truy vấn (tránh N+1)
    @Query("SELECT c.parent.id AS parentId, COUNT(c) AS total FROM Comment c WHERE c.parent.id IN :parentIds GROUP BY c.parent.id")
    List<ReplyCount> countRepliesByParentIds(@Param("parentIds") Collection<Long> parentIds);

    long countByParentId(Long parentId);

    @Query("SELECT ci.fileName FROM CommentImage ci WHERE ci.comment.post.id = :postId")
    List<String> findImageFileNamesByPostId(@Param("postId") Long postId);

    // Tên file ảnh của các reply (để xóa file trên đĩa khi xóa comment gốc)
    @Query("SELECT ci.fileName FROM CommentImage ci WHERE ci.comment.parent.id = :parentId")
    List<String> findImageFileNamesByParentId(@Param("parentId") Long parentId);

    interface ReplyCount {
        Long getParentId();
        Long getTotal();
    }
}