package org.example.blogpost.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.blogpost.entities.CommentImage;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentImageResponse {

    Long id;
    String fileUrl;

    public static CommentImageResponse from(CommentImage image) {
        return CommentImageResponse.builder()
                .id(image.getId())
                .fileUrl(image.getFileUrl())
                .build();
    }
}