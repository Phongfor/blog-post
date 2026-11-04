package org.example.blogpost.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.blogpost.dto.response.PostResponse;
import org.example.blogpost.entities.Post;
import org.example.blogpost.entities.PostImage;
import org.example.blogpost.exception.AppException;
import org.example.blogpost.exception.ErrorCode;
import org.example.blogpost.repositories.PostRepository;
import org.example.blogpost.service.FileStorageService;
import org.example.blogpost.service.PostImageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(readOnly = true)
@Slf4j
public class PostImageServiceImpl implements PostImageService {

    private static final int MAX_IMAGES_PER_POST = 10;

    PostRepository postRepository;
    FileStorageService fileStorageService;

    @Override
    @Transactional
    public PostResponse addImages(Long postId, Long currentUserId, List<MultipartFile> files) {
        Post post = findPostOrThrow(postId);
        checkOwner(post, currentUserId);

        if (files == null || files.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_IMAGE_FILE);
        }
        if (post.getImages().size() + files.size() > MAX_IMAGES_PER_POST) {
            throw new AppException(ErrorCode.TOO_MANY_IMAGES);
        }

        for (MultipartFile file : files) {
            FileStorageService.StoredFile stored = fileStorageService.store(file, FileStorageService.POSTS_FOLDER);
            // Nếu có lỗi sau đó làm transaction rollback thì file vừa ghi sẽ bị xóa
            fileStorageService.deleteOnRollback(FileStorageService.POSTS_FOLDER, List.of(stored.fileName()));

            post.getImages().add(PostImage.builder()
                    .post(post)
                    .fileName(stored.fileName())
                    .fileUrl(stored.fileUrl())
                    .build());
        }

        // saveAndFlush để ảnh mới có id trước khi dựng response
        Post saved = postRepository.saveAndFlush(post);
        log.info("Images added: postId={}, count={}, by userId={}", postId, files.size(), currentUserId);
        return PostResponse.from(saved);
    }

    @Override
    @Transactional
    public void deleteImage(Long postId, Long imageId, Long currentUserId) {
        Post post = findPostOrThrow(postId);
        checkOwner(post, currentUserId);

        PostImage image = post.getImages().stream()
                .filter(i -> i.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.POST_IMAGE_NOT_FOUND));

        // orphanRemoval sẽ xóa dòng trong DB khi commit; file xóa sau khi commit thành công
        post.getImages().remove(image);
        fileStorageService.deleteAfterCommit(FileStorageService.POSTS_FOLDER, List.of(image.getFileName()));
        log.info("Image deleted: postId={}, imageId={}, by userId={}", postId, imageId, currentUserId);
    }

    private Post findPostOrThrow(Long postId) {
        return postRepository.findByIdWithDetails(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
    }

    private void checkOwner(Post post, Long currentUserId) {
        if (!post.getAuthor().getId().equals(currentUserId)) {
            throw new AppException(ErrorCode.NOT_POST_OWNER);
        }
    }
}