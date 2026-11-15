package org.example.blogpost.service.impl;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.blogpost.entities.User;
import org.example.blogpost.event.ReactionEvent;
import org.example.blogpost.repositories.PostRepository;
import org.example.blogpost.repositories.UserRepository;
import org.example.blogpost.service.ReactionNotificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ReactionNotificationServiceImpl implements ReactionNotificationService {

    static final int MAX_TITLE_LENGTH = 60;

    JavaMailSender mailSender;
    PostRepository postRepository;
    UserRepository userRepository;
    String from;

    public ReactionNotificationServiceImpl(JavaMailSender mailSender,
                                           PostRepository postRepository,
                                           UserRepository userRepository,
                                           @Value("${app.mail.from:no-reply@blog.local}") String from) {
        this.mailSender = mailSender;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.from = from;
    }

    @Override
    public void notifyPostOwner(ReactionEvent event) {
        Optional<PostRepository.PostOwnerView> found = postRepository.findOwnerViewById(event.postId());
        if (found.isEmpty()) {
            log.info("Skip reaction mail: post no longer exists, postId={}", event.postId());
            return;
        }
        PostRepository.PostOwnerView owner = found.get();

        if (owner.getAuthorId().equals(event.actorId())) {
            log.info("Skip reaction mail: self reaction, postId={}", event.postId());
            return;
        }
        if (!Boolean.TRUE.equals(owner.getAuthorEnabled())) {
            log.info("Skip reaction mail: post owner is locked, postId={}", event.postId());
            return;
        }

        String actorName = userRepository.findById(event.actorId())
                .map(User::getUsername)
                .orElse("Someone");
        String postLabel = buildPostLabel(owner.getTitle());
        String reaction = event.type().name();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(owner.getAuthorEmail());
        message.setSubject(actorName + " reacted " + reaction + " to " + postLabel);
        message.setText("Hi " + owner.getAuthorUsername() + ",\n\n"
                + actorName + " reacted " + reaction + " to " + postLabel + ".\n\n"
                + "BlogPost");

        mailSender.send(message);
        log.info("Reaction mail sent: postId={}, actorId={}, type={}",
                event.postId(), event.actorId(), reaction);
    }

    private String buildPostLabel(String title) {
        if (title == null || title.isBlank()) {
            return "your post";
        }
        String clean = title.replaceAll("[\\r\\n]+", " ").trim();
        if (clean.length() > MAX_TITLE_LENGTH) {
            clean = clean.substring(0, MAX_TITLE_LENGTH) + "...";
        }
        return "your post \"" + clean + "\"";
    }
}