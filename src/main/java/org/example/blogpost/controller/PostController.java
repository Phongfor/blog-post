package org.example.blogpost.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.blogpost.config.CustomUserDetails;
import org.example.blogpost.dto.request.PostRequest;
import org.example.blogpost.dto.response.ApiResponse;
import org.example.blogpost.dto.response.PageResponse;
import org.example.blogpost.dto.response.PostResponse;
import org.example.blogpost.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostController {

    PostService postService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PostResponse> createPost(@AuthenticationPrincipal CustomUserDetails principal,
                                                @RequestBody @Valid PostRequest request) {
        return ApiResponse.<PostResponse>builder()
                .message("Post created successfully")
                .result(postService.createPost(principal.getId(), request))
                .build();
    }

    @GetMapping
    public ApiResponse<PageResponse<PostResponse>> getPosts(@RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.<PageResponse<PostResponse>>builder()
                .result(postService.getPosts(page, size))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<PostResponse> getPost(@PathVariable Long id) {
        return ApiResponse.<PostResponse>builder()
                .result(postService.getPost(id))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<PostResponse> updatePost(@AuthenticationPrincipal CustomUserDetails principal,
                                                @PathVariable Long id,
                                                @RequestBody @Valid PostRequest request) {
        return ApiResponse.<PostResponse>builder()
                .message("Post updated successfully")
                .result(postService.updatePost(id, principal.getId(), request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deletePost(@AuthenticationPrincipal CustomUserDetails principal,
                                        @PathVariable Long id) {
        postService.deletePost(id, principal.getId());
        return ApiResponse.<Void>builder()
                .message("Post deleted successfully")
                .build();
    }
}