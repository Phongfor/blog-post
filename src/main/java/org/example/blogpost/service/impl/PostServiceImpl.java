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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    public PageResponse<PostResponse> getPosts(int page, int size){
        int safePage = Math.max(page,0);
        int safeSize = Math.min(Math.max(size,1),MAX_PAGE_SIZE);

        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt")
                .and(Sort.by(Sort.Direction.DESC,"id"));

        Pageable pageable = PageRequest.of(safePage,safeSize,sort);

        return PageResponse.from(postRepository.findAllWithAuthor(pageable).map(PostResponse::from));
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
