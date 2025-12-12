package com.blog.service;

import com.blog.dto.CommentDto;
import java.util.List;

public interface CommentService {

    List<CommentDto> getComments(Long postId);

    CommentDto addComment(Long postId, String username, String content);

    void deleteComment(Long commentId, String username);
}
