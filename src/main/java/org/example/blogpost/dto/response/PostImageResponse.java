package org.example.blogpost.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.blogpost.entities.PostImage;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostImageResponse {
    private Long id;
    private String fileUrl;

    public static PostImageResponse from(PostImage image) {
        return PostImageResponse.builder()
                .id(image.getId())
                .fileUrl(image.getFileUrl())
                .build();
    }
}