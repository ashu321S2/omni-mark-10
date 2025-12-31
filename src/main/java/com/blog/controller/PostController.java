package com.blog.controller;

import com.blog.dto.PostDto;
import com.blog.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // =========================
    // CREATE POST (TEXT + IMAGE)
    // =========================
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostDto> createPost(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam(value = "image", required = false) MultipartFile image,
            Principal principal) {

        PostDto dto = new PostDto();
        dto.setTitle(title);
        dto.setContent(content);

        PostDto created = postService.createPost(dto, image, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // =========================
    // GET ALL POSTS (PAGINATED)
    // =========================
    @GetMapping
    public ResponseEntity<Page<PostDto>> getAllPosts(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        return ResponseEntity.ok(postService.getAllPosts(pageable));
    }

    // =========================
    // LIKE / UNLIKE POST
    // =========================
    @PostMapping("/{id}/like")
    public ResponseEntity<?> likePost(@PathVariable("id") Long id, Principal principal) {
        postService.likePost(id, principal.getName());
        return ResponseEntity.ok().build();
    }

    // =========================
    // GET SINGLE POST
    // =========================
    @GetMapping("/{id}")
    public ResponseEntity<PostDto> getPost(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    // =========================
    // UPDATE POST (TEXT ONLY)
    // =========================
    @PutMapping("/{id}")
    public ResponseEntity<PostDto> updatePost(
            @PathVariable Long id,
            @RequestBody PostDto dto,
            Principal principal) {

        return ResponseEntity.ok(
                postService.updatePost(id, dto, principal.getName())
        );
    }

    // =========================
    // DELETE POST (OWNER)
    // =========================
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Long id, Principal principal) {
        postService.deletePost(id, principal.getName());
        return ResponseEntity.noContent().build();
    }

    // =========================
    // ADMIN DELETE (ANY POST)
    // =========================
    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> adminDeletePost(@PathVariable Long id) {
        postService.adminDeletePost(id);
        return ResponseEntity.noContent().build();
    }
}
