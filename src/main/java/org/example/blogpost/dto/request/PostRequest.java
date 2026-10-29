package org.example.blogpost.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostRequest {

    @Size(max = 255, message = "Title must not exceed 255 characters")
    String title;

    @NotBlank(message = "Content is required")
    @Size(max = 20000, message = "Content must not exceed 20000 characters")
    String content;
}