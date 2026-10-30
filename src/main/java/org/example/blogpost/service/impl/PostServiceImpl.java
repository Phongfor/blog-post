package org.example.blogpost.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.blogpost.dto.request.PostRequest;
import org.example.blogpost.dto.response.PageResponse;
import org.example.blogpost.dto.response.PostResponse;
import org.example.blogpost.entities.Post;
import org.example.blogpost.entities.User;
import org.example.blogpost.exception.AppException;
import org.example.blogpost.exception.ErrorCode;
import org.example.blogpost.repositories.PostRepository;
import org.example.blogpost.repositories.UserRepository;
import org.example.blogpost.service.PostService;
import org.example.blogpost.specification.PostSpecification;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(readOnly = true)
@Slf4j
public class PostServiceImpl implements PostService {
    private static final int MAX_PAGE_SIZE = 50;

    PostRepository postRepository;
    UserRepository userRepository;

    @Override
    @Transactional
    public PostResponse createPost(Long authorId, PostRequest request){
        User author = userRepository.findById((authorId))
                .orElseThrow(() -> new AppException((ErrorCode.USER_NOT_FOUND)));

                Post post = Post.builder()
                        .author(author)
                        .title(request.getTitle())
                        .content(request.getContent())
                        .build();

                Post saved = postRepository.save(post);
                log.info("Post created: id={}, authorId={}", saved.getId(), authorId);
                return PostResponse.from(saved);
    }

    @Override
    public PageResponse<PostResponse> searchPosts(String title, String author, String sort, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);

        Pageable pageable = PageRequest.of(safePage, safeSize, buildSort(sort));

        // Ghép các điều kiện lọc; param nào trống thì tự bỏ qua
        Specification<Post> spec = PostSpecification.titleContains(title)
                .and(PostSpecification.withAuthor(author));

        return PageResponse.from(postRepository.findAll(spec, pageable).map(PostResponse::from));
    }

    // Whitelist kiểu sắp xếp, luôn thêm id để thứ tự ổn định khi phân trang
    private Sort buildSort(String sort) {
        String key = (sort == null || sort.isBlank()) ? "newest" : sort.trim().toLowerCase();
        return switch (key) {
            case "newest" -> Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
            case "oldest" -> Sort.by(Sort.Order.asc("createdAt"), Sort.Order.asc("id"));
            case "title" -> Sort.by(Sort.Order.asc("title"), Sort.Order.desc("id"));
            default -> throw new AppException(ErrorCode.VALIDATION_ERROR);
        };
    }

    @Override
    @Transactional
    public PostResponse updatePost(Long postId, Long currentUserId, PostRequest request) {
        Post post = findPostOrThrow(postId);
        checkOwner(post, currentUserId);

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());

        Post saved = postRepository.saveAndFlush(post);
        log.info("Post updated: id={}, by userId={}", postId, currentUserId);
        return PostResponse.from(saved);
    }

    @Override
    @Transactional
    public void deletePost(Long postId, Long currentUserId) {
        Post post = findPostOrThrow(postId);
        checkOwner(post, currentUserId);

        postRepository.delete(post);
        log.info("Post deleted: id={}, by userId={}", postId, currentUserId);
    }

    private Post findPostOrThrow(Long postId) {
        return postRepository.findByIdWithDetails(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
    }

    @Override
    public PostResponse getPost(Long postId) {
        return PostResponse.from(findPostOrThrow(postId));
    }

    private void checkOwner(Post post, Long currentUserId) {
        if (!post.getAuthor().getId().equals(currentUserId)) {
            throw new AppException(ErrorCode.NOT_POST_OWNER);
        }
    }

}
