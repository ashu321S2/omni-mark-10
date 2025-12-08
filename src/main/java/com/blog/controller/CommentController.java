package com.blog.controller;

import com.blog.dto.CommentDto;
import com.blog.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentDto> addComment(@RequestBody CommentDto dto, Principal principal) {
        return ResponseEntity.ok(commentService.addComment(dto, principal.getName()));
    }

    @GetMapping
    public ResponseEntity<List<CommentDto>> getComments(@RequestParam Long postId) {
        return ResponseEntity.ok(commentService.getCommentsByPost(postId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommentDto> getComment(@PathVariable Long id) {
        return ResponseEntity.ok(commentService.getCommentById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommentDto> updateComment(@PathVariable Long id, @RequestBody CommentDto dto, Principal principal) {
        return ResponseEntity.ok(commentService.updateComment(id, dto, principal.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteComment(@PathVariable Long id, Principal principal) {
        commentService.deleteComment(id, principal.getName());
        return ResponseEntity.noContent().build();
    }
}
