package com.blog.service;

import com.blog.dto.CommentDto;
import com.blog.dto.PostDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PostService {

    // UPDATED: image support added
    PostDto createPost(PostDto dto, MultipartFile image, String username);

    Page<PostDto> getAllPosts(Pageable pageable);

    PostDto getPostById(Long id);

    PostDto updatePost(Long id, PostDto dto, String username);

    void deletePost(Long id, String username);

    void adminDeletePost(Long id);

    // likes
    void likePost(Long postId, String username);
    long getLikesCount(Long postId);

    // comments
    CommentDto addComment(Long postId, String username, String content);
    List<CommentDto> getComments(Long postId);
    long getCommentsCount(Long postId);
}
