package org.example.blogpost.event;

import org.example.blogpost.enums.ReactionType;

public record ReactionEvent(Long postId, Long actorId, ReactionType type) {

    public String toMessage(){
        return postId + "|"  + actorId + "|" + type.name();
    }

    public static ReactionEvent fromMessage(String message){
        String[] parts = message.split("\\|");
        if(parts.length != 3){
            throw new IllegalArgumentException("Invalid reaction event: " + message);
        }
        return new ReactionEvent(Long.valueOf(parts[0]), Long.valueOf(parts[1]), ReactionType.valueOf(parts[2]));
    }
}
