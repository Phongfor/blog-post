package org.example.blogpost.service;

import org.example.blogpost.dto.request.PostRequest;
import org.example.blogpost.dto.response.PageResponse;
import org.example.blogpost.dto.response.PostResponse;

public interface PostService {
    PostResponse createPost(Long authorId, PostRequest request);
    PageResponse<PostResponse> searchPosts(String title, String author, String sort, int page, int size);
    PostResponse getPost(Long postId);
    PostResponse updatePost(Long postId, Long currentUserId, PostRequest request);
    void deletePost(Long postId, Long currentUserId);
}