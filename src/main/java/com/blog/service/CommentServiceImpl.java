package com.blog.service;

import com.blog.dto.CommentDto;
import com.blog.entity.Comment;
import com.blog.entity.Post;
import com.blog.entity.User;
import com.blog.exception.ResourceNotFoundException;
import com.blog.repository.CommentRepository;
import com.blog.repository.PostRepository;
import com.blog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Override
    public CommentDto addComment(CommentDto dto, String username) {
        Post post = postRepository.findById(dto.getPostId()).orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        User user = userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Comment comment = Comment.builder()
                .post(post)
                .content(dto.getContent())
                .author(user)
                .build();
        Comment saved = commentRepository.save(comment);
        return mapToDto(saved);
    }

    @Override
    public List<CommentDto> getCommentsByPost(Long postId) {
        return commentRepository.findByPostId(postId).stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public CommentDto getCommentById(Long id) {
        return commentRepository.findById(id).map(this::mapToDto).orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
    }

    @Override
    public CommentDto updateComment(Long id, CommentDto dto, String username) {
        Comment comment = commentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        if (!comment.getAuthor().getUsername().equals(username)) {
            throw new RuntimeException("Not authorized to update this comment");
        }
        comment.setContent(dto.getContent());
        Comment updated = commentRepository.save(comment);
        return mapToDto(updated);
    }

    @Override
    public void deleteComment(Long id, String username) {
        Comment comment = commentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        if (!comment.getAuthor().getUsername().equals(username)) {
            throw new RuntimeException("Not authorized to delete this comment");
        }
        commentRepository.delete(comment);
    }

    private CommentDto mapToDto(Comment c) {
        return CommentDto.builder()
                .id(c.getId())
                .postId(c.getPost() != null ? c.getPost().getId() : null)
                .content(c.getContent())
                .authorUsername(c.getAuthor() != null ? c.getAuthor().getUsername() : null)
                .build();
    }
}
