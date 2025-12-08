package com.blog.controller;

import com.blog.dto.PostDto;
import com.blog.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // ---------------------------------------------
    // Create Post (Authenticated users)
    // ---------------------------------------------
    @PostMapping
    public ResponseEntity<PostDto> createPost(@RequestBody PostDto dto, Principal principal) {
        PostDto created = postService.createPost(dto, principal.getName());
        return ResponseEntity.ok(created);
    }

    // ---------------------------------------------
    // Get All Posts (with Pagination + Sorting)
    // ---------------------------------------------
    @GetMapping
    public ResponseEntity<Page<PostDto>> getAllPosts(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(postService.getAllPosts(pageable));
    }

    // ---------------------------------------------
    // Get Single Post
    // ---------------------------------------------
    @GetMapping("/{id}")
    public ResponseEntity<PostDto> getPost(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    // ---------------------------------------------
    // Update Post (User can update only their post)
    // ---------------------------------------------
    @PutMapping("/{id}")
    public ResponseEntity<PostDto> updatePost(
            @PathVariable Long id,
            @RequestBody PostDto dto,
            Principal principal) {

        return ResponseEntity.ok(
                postService.updatePost(id, dto, principal.getName())
        );
    }

    // ---------------------------------------------
    // Delete Post (User can delete only their post)
    // ---------------------------------------------
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Long id, Principal principal) {
        postService.deletePost(id, principal.getName());
        return ResponseEntity.noContent().build();
    }

    // ---------------------------------------------
    // 🔥 ADMIN DELETE ROUTE — can delete ANY post
    // ---------------------------------------------
    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> adminDeletePost(@PathVariable Long id) {
        postService.adminDeletePost(id);
        return ResponseEntity.noContent().build();
    }
}
