package com.blog.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDto {

    private Long id;
    private Long postId;
    private String authorUsername;
    private String content;
    private LocalDateTime createdAt;
}
