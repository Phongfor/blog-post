package org.example.blogpost.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.blogpost.config.CustomUserDetails;
import org.example.blogpost.dto.request.CommentRequest;
import org.example.blogpost.dto.response.ApiResponse;
import org.example.blogpost.dto.response.CommentResponse;
import org.example.blogpost.dto.response.PageResponse;
import org.example.blogpost.service.CommentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentController {

    CommentService commentService;

    @GetMapping("/posts/{postId}/comments")
    public ApiResponse<PageResponse<CommentResponse>> getComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.<PageResponse<CommentResponse>>builder()
                .message("Comments retrieved successfully")
                .result(commentService.getComments(postId, page, size))
                .build();
    }

    @PostMapping(value = "/posts/{postId}/comments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CommentResponse> createComment(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "files", required = false) List<MultipartFile> files) {
        return ApiResponse.<CommentResponse>builder()
                .message("Comment created successfully")
                .result(commentService.createComment(postId, principal.getId(), content, files))
                .build();
    }

    @PutMapping("/comments/{commentId}")
    public ApiResponse<CommentResponse> updateComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody CommentRequest request) {
        return ApiResponse.<CommentResponse>builder()
                .message("Comment updated successfully")
                .result(commentService.updateComment(commentId, principal.getId(), request.getContent()))
                .build();
    }

    @DeleteMapping("/comments/{commentId}")
    public ApiResponse<Void> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails principal) {
        commentService.deleteComment(commentId, principal.getId());
        return ApiResponse.<Void>builder()
                .message("Comment deleted successfully")
                .build();
    }
}