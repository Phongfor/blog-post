package org.example.blogpost.service;

import org.example.blogpost.dto.response.PostResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PostImageService {

    PostResponse addImages(Long postId, Long currentUserId, List<MultipartFile> files);

    void deleteImage(Long postId, Long imageId, Long currentUserId);
}