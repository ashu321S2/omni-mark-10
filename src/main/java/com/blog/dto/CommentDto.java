package com.blog.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CommentDto {

    private Long id;
    private Long postId;

    @NotBlank(message = "Comment content is required")
    private String content;

    private String authorUsername;
}
