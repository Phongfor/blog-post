package org.example.blogpost.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.blogpost.dto.response.CommentResponse;
import org.example.blogpost.dto.response.PageResponse;
import org.example.blogpost.entities.Comment;
import org.example.blogpost.entities.CommentImage;
import org.example.blogpost.exception.AppException;
import org.example.blogpost.exception.ErrorCode;
import org.example.blogpost.repositories.CommentRepository;
import org.example.blogpost.repositories.PostRepository;
import org.example.blogpost.repositories.UserRepository;
import org.example.blogpost.service.CommentService;
import org.example.blogpost.service.FileStorageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(readOnly = true)
@Slf4j
public class CommentServiceImpl implements CommentService {

    private static final int MAX_IMAGES_PER_COMMENT = 3;
    private static final int MAX_CONTENT_LENGTH = 5000;
    private static final int MAX_PAGE_SIZE = 50;

    CommentRepository commentRepository;
    PostRepository postRepository;
    UserRepository userRepository;
    FileStorageService fileStorageService;

    @Override
    public PageResponse<CommentResponse> getComments(Long postId, int page, int size) {
        if (!postRepository.existsById(postId)) {
            throw new AppException(ErrorCode.POST_NOT_FOUND);
        }

        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), MAX_PAGE_SIZE),
                Sort.by(Sort.Direction.ASC, "createdAt").and(Sort.by(Sort.Direction.ASC, "id")));

        Page<Comment> comments = commentRepository.findByPostIdWithAuthor(postId, pageable);
        return PageResponse.from(comments.map(CommentResponse::from));
    }

    @Override
    @Transactional
    public CommentResponse createComment(Long postId, Long currentUserId, String content, List<MultipartFile> files) {
        if (!postRepository.existsById(postId)) {
            throw new AppException(ErrorCode.POST_NOT_FOUND);
        }

        String text = normalize(content);
        validateLength(text);


        List<MultipartFile> validFiles = files == null ? List.of()
                : files.stream().filter(f -> f != null && !f.isEmpty()).toList();

        if (validFiles.size() > MAX_IMAGES_PER_COMMENT) {
            throw new AppException(ErrorCode.TOO_MANY_COMMENT_IMAGES);
        }
        if (text == null && validFiles.isEmpty()) {
            throw new AppException(ErrorCode.COMMENT_EMPTY);
        }

        Comment comment = Comment.builder()
                .post(postRepository.getReferenceById(postId))
                .author(userRepository.getReferenceById(currentUserId))
                .content(text)
                .build();

        for (MultipartFile file : validFiles) {
            FileStorageService.StoredFile stored =
                    fileStorageService.store(file, FileStorageService.COMMENTS_FOLDER);

            fileStorageService.deleteOnRollback(FileStorageService.COMMENTS_FOLDER, List.of(stored.fileName()));

            comment.getImages().add(CommentImage.builder()
                    .comment(comment)
                    .fileName(stored.fileName())
                    .fileUrl(stored.fileUrl())
                    .build());
        }

        Comment saved = commentRepository.saveAndFlush(comment);
        log.info("Comment created: commentId={}, postId={}, images={}, by userId={}",
                saved.getId(), postId, validFiles.size(), currentUserId);
        return CommentResponse.from(saved);
    }

    @Override
    @Transactional
    public CommentResponse updateComment(Long commentId, Long currentUserId, String content) {
        Comment comment = findCommentOrThrow(commentId);
        checkOwner(comment, currentUserId);

        String text = normalize(content);
        validateLength(text);
        if (text == null && comment.getImages().isEmpty()) {
            throw new AppException(ErrorCode.COMMENT_EMPTY);
        }

        comment.setContent(text);
        Comment saved = commentRepository.saveAndFlush(comment);
        log.info("Comment updated: commentId={}, by userId={}", commentId, currentUserId);
        return CommentResponse.from(saved);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Long currentUserId, boolean isAdmin) {
        Comment comment = findCommentOrThrow(commentId);

        if (!isAdmin) {
            checkOwner(comment, currentUserId);
        }

        List<String> fileNames = comment.getImages().stream().map(CommentImage::getFileName).toList();

        commentRepository.delete(comment);
        if (!fileNames.isEmpty()) {
            fileStorageService.deleteAfterCommit(FileStorageService.COMMENTS_FOLDER, fileNames);
        }
        log.info("Comment deleted: commentId={}, by userId={}", commentId, currentUserId);
    }

    private Comment findCommentOrThrow(Long commentId) {
        return commentRepository.findByIdWithAuthor(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));
    }

    private void checkOwner(Comment comment, Long currentUserId) {
        if (!comment.getAuthor().getId().equals(currentUserId)) {
            throw new AppException(ErrorCode.NOT_COMMENT_OWNER);
        }
    }

    private String normalize(String content) {
        if (content == null) {
            return null;
        }
        String trimmed = content.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void validateLength(String text) {
        if (text != null && text.length() > MAX_CONTENT_LENGTH) {
            throw new AppException(ErrorCode.COMMENT_CONTENT_TOO_LONG);
        }
    }
}