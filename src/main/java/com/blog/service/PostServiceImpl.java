package com.blog.service;

import com.blog.dto.PostDto;
import com.blog.entity.Post;
import com.blog.entity.User;
import com.blog.exception.ResourceNotFoundException;
import com.blog.repository.PostRepository;
import com.blog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Override
    public PostDto createPost(PostDto dto, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Post post = Post.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .author(user)
                .build();

        Post saved = postRepository.save(post);
        return mapToDto(saved);
    }

    @Override
    public Page<PostDto> getAllPosts(Pageable pageable) {
        return postRepository.findAll(pageable).map(this::mapToDto);
    }

    @Override
    public PostDto getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        return mapToDto(post);
    }

    @Override
    public PostDto updatePost(Long id, PostDto dto, String username) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        if (!post.getAuthor().getUsername().equals(username)) {
            throw new RuntimeException("Not authorized to update this post");
        }

        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());

        Post updated = postRepository.save(post);
        return mapToDto(updated);
    }

    @Override
    public void deletePost(Long id, String username) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        if (!post.getAuthor().getUsername().equals(username)) {
            throw new RuntimeException("Not authorized to delete this post");
        }

        postRepository.delete(post);
    }

    @Override
    public void adminDeletePost(Long id) {
        if (!postRepository.existsById(id)) {
            throw new ResourceNotFoundException("Post not found");
        }
        postRepository.deleteById(id);
    }

    private PostDto mapToDto(Post post) {
        return PostDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .authorUsername(
                        post.getAuthor() != null 
                                ? post.getAuthor().getUsername() 
                                : null
                )
                .build();
    }
}
