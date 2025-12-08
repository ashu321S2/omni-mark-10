package com.blog.service;

import com.blog.dto.PostDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostService {

    PostDto createPost(PostDto dto, String username);

    Page<PostDto> getAllPosts(Pageable pageable);

    PostDto getPostById(Long id);

    PostDto updatePost(Long id, PostDto dto, String username);

    void deletePost(Long id, String username);

    // Admin delete (from your Step 3)
    void adminDeletePost(Long id);
}
