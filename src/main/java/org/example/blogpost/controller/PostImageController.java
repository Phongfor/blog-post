package org.example.blogpost.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.blogpost.config.CustomUserDetails;
import org.example.blogpost.dto.response.ApiResponse;
import org.example.blogpost.dto.response.PostResponse;
import org.example.blogpost.service.PostImageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/posts/{postId}/images")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostImageController {

    PostImageService postImageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PostResponse> uploadImages(@AuthenticationPrincipal CustomUserDetails principal,
                                                  @PathVariable Long postId,
                                                  @RequestParam("files") List<MultipartFile> files) {
        return ApiResponse.<PostResponse>builder()
                .message("Images uploaded successfully")
                .result(postImageService.addImages(postId, principal.getId(), files))
                .build();
    }

    @DeleteMapping("/{imageId}")
    public ApiResponse<Void> deleteImage(@AuthenticationPrincipal CustomUserDetails principal,
                                         @PathVariable Long postId,
                                         @PathVariable Long imageId) {
        postImageService.deleteImage(postId, imageId, principal.getId());
        return ApiResponse.<Void>builder()
                .message("Image deleted successfully")
                .build();
    }
}