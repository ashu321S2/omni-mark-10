package com.blog.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PostDto {
    private Long id;
    private String title;
    private String content;
    private String authorUsername;
    private LocalDateTime createdAt;
    private int likes;
    private int comments;
}
