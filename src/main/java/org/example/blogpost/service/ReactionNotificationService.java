package org.example.blogpost.service;

import org.example.blogpost.event.ReactionEvent;

public interface ReactionNotificationService {

    void notifyPostOwner(ReactionEvent event);
}