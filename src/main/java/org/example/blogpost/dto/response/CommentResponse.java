package org.example.blogpost.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.blogpost.entities.Comment;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {

    Long id;
    Long postId;
    Long parentId;          // null nếu là comment gốc
    String content;
    AuthorResponse author;
    AuthorResponse replyTo;
    long replyCount;
    List<CommentImageResponse> images;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    public static CommentResponse from(Comment comment) {
        return from(comment, 0L);
    }

    public static CommentResponse from(Comment comment, long replyCount) {
        return CommentResponse.builder()
                .id(comment.getId())
                .postId(comment.getPost().getId())
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .content(comment.getContent())
                .author(AuthorResponse.builder()
                        .id(comment.getAuthor().getId())
                        .username(comment.getAuthor().getUsername())
                        .build())
                .replyTo(comment.getReplyToUser() == null ? null
                        : AuthorResponse.builder()
                        .id(comment.getReplyToUser().getId())
                        .username(comment.getReplyToUser().getUsername())
                        .build())
                .replyCount(replyCount)
                .images(comment.getImages().stream().map(CommentImageResponse::from).toList())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}