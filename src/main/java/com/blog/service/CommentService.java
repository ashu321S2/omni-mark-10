package com.blog.service;

import com.blog.dto.CommentDto;
import java.util.List;

public interface CommentService {
    CommentDto addComment(CommentDto dto, String username);
    List<CommentDto> getCommentsByPost(Long postId);
    CommentDto getCommentById(Long id);
    CommentDto updateComment(Long id, CommentDto dto, String username);
    void deleteComment(Long id, String username);
}
