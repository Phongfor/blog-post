package org.example.blogpost.service;

import org.example.blogpost.dto.response.CommentResponse;
import org.example.blogpost.dto.response.PageResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CommentService {

    PageResponse<CommentResponse> getComments(Long postId, int page, int size);

    CommentResponse createComment(Long postId, Long currentUserId, String content, List<MultipartFile> files);

    CommentResponse updateComment(Long commentId, Long currentUserId, String content);

    void deleteComment(Long commentId, Long currentUserId, boolean isAdmin);
}