package com.blog.service;

import com.blog.dto.CommentDto;
import com.blog.entity.Post;
import com.blog.entity.PostComment;
import com.blog.entity.User;
import com.blog.exception.ResourceNotFoundException;
import com.blog.repository.PostCommentRepository;
import com.blog.repository.PostRepository;
import com.blog.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final PostRepository postRepo;
    private final UserRepository userRepo;
    private final PostCommentRepository commentRepo;

    @Override
    @Transactional
    public List<CommentDto> getComments(Long postId) {
        Post post = postRepo.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        return commentRepo.findByPostOrderByCreatedAtDesc(post)
                .stream()
                .map(c -> CommentDto.builder()
                        .id(c.getId())
                        .postId(post.getId())
                        .authorUsername(c.getUser().getUsername()) // SAFE now
                        .content(c.getContent())
                        .createdAt(c.getCreatedAt())
                        .build()
                )
                .toList();
    }

    @Override
    @Transactional
    public CommentDto addComment(Long postId, String username, String content) {
        Post post = postRepo.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        PostComment comment = PostComment.builder()
                .post(post)
                .user(user)
                .content(content)
                .build();

        PostComment saved = commentRepo.save(comment);

        // Ensure we don't hit a NullPointerException if post.comments is null
     // SAFE: Handle null and calculate new count
        Integer current = post.getComments();
        post.setComments((current == null ? 0 : current) + 1);

        // Save the post

        
        // Save the post so the database updates the count column
        postRepo.save(post); 

        return CommentDto.builder()
                .id(saved.getId())
                .postId(post.getId())
                .authorUsername(user.getUsername())
                .content(saved.getContent())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, String username) {
        PostComment comment = commentRepo.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        // make sure the requesting user is the comment author (or admin check if you want)
        if (comment.getUser() == null || !comment.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Not authorized to delete this comment");
        }

        Post post = comment.getPost();
        if (post == null) {
            // still delete the comment but guard
            commentRepo.delete(comment);
            return;
        }

        // delete the comment
        commentRepo.delete(comment);

        // decrement comment count safely and save post
        int current = post.getComments() == 0 ? 0 : post.getComments();
        if (current > 0) {
            post.setComments(current - 1);
        } else {
            post.setComments(0);
        }
        postRepo.save(post);
    }
}
