package org.example.blogpost.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.blogpost.dto.response.CommentResponse;
import org.example.blogpost.dto.response.PageResponse;
import org.example.blogpost.entities.Comment;
import org.example.blogpost.entities.CommentImage;
import org.example.blogpost.entities.User;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

        Page<Comment> roots = commentRepository.findRootsByPostIdWithAuthor(postId, buildPageable(page, size));

        // Đếm reply cho cả trang bằng một truy vấn
        List<Long> rootIds = roots.getContent().stream().map(Comment::getId).toList();
        Map<Long, Long> replyCounts = rootIds.isEmpty() ? Map.of()
                : commentRepository.countRepliesByParentIds(rootIds).stream()
                .collect(Collectors.toMap(
                        CommentRepository.ReplyCount::getParentId,
                        CommentRepository.ReplyCount::getTotal));

        return PageResponse.from(roots.map(c ->
                CommentResponse.from(c, replyCounts.getOrDefault(c.getId(), 0L))));
    }

    @Override
    public PageResponse<CommentResponse> getReplies(Long postId, Long commentId, int page, int size) {
        if (!postRepository.existsById(postId)) {
            throw new AppException(ErrorCode.POST_NOT_FOUND);
        }

        // Chỉ comment gốc của đúng bài này mới có danh sách reply
        Comment root = findCommentOrThrow(commentId);
        if (!root.getPost().getId().equals(postId) || root.getParent() != null) {
            throw new AppException(ErrorCode.COMMENT_NOT_FOUND);
        }

        Page<Comment> replies = commentRepository.findRepliesWithAuthor(commentId, buildPageable(page, size));
        return PageResponse.from(replies.map(c -> CommentResponse.from(c, 0L)));
    }

    @Override
    @Transactional
    public CommentResponse createComment(Long postId, Long currentUserId, Long parentId,
                                         String content, List<MultipartFile> files) {
        if (!postRepository.existsById(postId)) {
            throw new AppException(ErrorCode.POST_NOT_FOUND);
        }

        // Xác định comment gốc và người được trả lời (nếu là reply)
        Comment parent = null;
        User replyToUser = null;
        if (parentId != null) {
            Comment target = findCommentOrThrow(parentId);
            if (!target.getPost().getId().equals(postId)) {
                throw new AppException(ErrorCode.INVALID_PARENT_COMMENT);
            }
            if (target.getParent() != null) {
                // Reply vào một reply: gắn về comment gốc, ghi nhớ người được trả lời
                parent = target.getParent();
                replyToUser = target.getAuthor();
            } else {
                parent = target;
            }
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
                .parent(parent)
                .replyToUser(replyToUser)
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
        log.info("Comment created: commentId={}, postId={}, parentId={}, images={}, by userId={}",
                saved.getId(), postId, parentId, validFiles.size(), currentUserId);
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
        return CommentResponse.from(saved, commentRepository.countByParentId(saved.getId()));
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Long currentUserId, boolean isAdmin) {
        Comment comment = findCommentOrThrow(commentId);

        if (!isAdmin) {
            checkOwner(comment, currentUserId);
        }

        // Ảnh của chính comment này và của các reply (database tự xóa reply bằng ON DELETE CASCADE,
        // nhưng file trên đĩa thì phải xóa tay)
        List<String> fileNames = new ArrayList<>(
                comment.getImages().stream().map(CommentImage::getFileName).toList());
        fileNames.addAll(commentRepository.findImageFileNamesByParentId(commentId));

        commentRepository.delete(comment);
        if (!fileNames.isEmpty()) {
            fileStorageService.deleteAfterCommit(FileStorageService.COMMENTS_FOLDER, fileNames);
        }
        log.info("Comment deleted: commentId={}, by userId={}", commentId, currentUserId);
    }

    private Pageable buildPageable(int page, int size) {
        return PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), MAX_PAGE_SIZE),
                Sort.by(Sort.Direction.ASC, "createdAt").and(Sort.by(Sort.Direction.ASC, "id")));
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