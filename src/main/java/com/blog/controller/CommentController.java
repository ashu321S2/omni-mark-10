package com.blog.controller;

import com.blog.dto.CommentDto;
import com.blog.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/{postId}/comments")
    public ResponseEntity<List<CommentDto>> getComments(@PathVariable Long postId) {
        return ResponseEntity.ok(commentService.getComments(postId));
    }

    @PostMapping("/{postId}/comments")
    public ResponseEntity<CommentDto> addComment(
            @PathVariable Long postId,
            @RequestBody CommentDto dto,
            Principal principal) {

        CommentDto saved = commentService.addComment(
                postId, principal.getName(), dto.getContent());

        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<?> deleteComment(
            @PathVariable Long id,
            Principal principal) {

        commentService.deleteComment(id, principal.getName());
        return ResponseEntity.noContent().build();
    }
}
