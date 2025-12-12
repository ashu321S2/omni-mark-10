package com.blog.service;

import com.blog.dto.CommentDto;
import com.blog.dto.PostDto;
import com.blog.entity.Post;
import com.blog.entity.PostComment;
import com.blog.entity.PostLike;
import com.blog.entity.User;
import com.blog.exception.ResourceNotFoundException;
import com.blog.repository.PostCommentRepository;
import com.blog.repository.PostLikeRepository;
import com.blog.repository.PostRepository;
import com.blog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostLikeRepository likeRepository;
    private final PostCommentRepository commentRepository;

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
        Page<Post> page = postRepository.findAllByOrderByCreatedAtDesc(pageable);
        return page.map(this::mapToDto);
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

        if (post.getAuthor() == null || !post.getAuthor().getUsername().equals(username)) {
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

        if (post.getAuthor() == null || !post.getAuthor().getUsername().equals(username)) {
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

    // ---------- likes ----------
    @Override
    public void likePost(Long postId, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // toggle like
        likeRepository.findByPostAndUser(post, user).ifPresentOrElse(
                pl -> {
                    likeRepository.delete(pl);
                    post.setLikes(Math.max(0, post.getLikes() - 1));
                    postRepository.save(post);
                },
                () -> {
                    PostLike newLike = PostLike.builder().post(post).user(user).build();
                    likeRepository.save(newLike);
                    post.setLikes(post.getLikes() + 1);
                    postRepository.save(post);
                }
        );
    }

    @Override
    public long getLikesCount(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        return likeRepository.countByPost(post);
    }

    // ---------- comments ----------
    @Override
    public CommentDto addComment(Long postId, String username, String content) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        PostComment comment = PostComment.builder()
                .post(post)
                .user(user)
                .content(content)
                .build();

        PostComment saved = commentRepository.save(comment);
        // increment comments count on post for quick display
        post.setComments(post.getComments() + 1);
        postRepository.save(post);

        return CommentDto.builder()
                .id(saved.getId())
                .postId(post.getId())
                .authorUsername(user.getUsername())
                .content(saved.getContent())
                .createdAt(saved.getCreatedAt())   // ensure CommentDto.createdAt type matches
                .build();
    }

    @Override
    public List<CommentDto> getComments(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        return commentRepository.findByPostOrderByCreatedAtDesc(post)
                .stream()
                .map(c -> CommentDto.builder()
                        .id(c.getId())
                        .postId(post.getId())
                        .authorUsername(c.getUser().getUsername())
                        .content(c.getContent())
                        .createdAt(c.getCreatedAt())   // ensure CommentDto.createdAt type matches
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public long getCommentsCount(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        return commentRepository.countByPost(post);
    }

    // ---------- utility ----------
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
                .createdAt(post.getCreatedAt())   // ensure PostDto.createdAt type matches
                .likes(post.getLikes())
                .comments(post.getComments())
                .build();
    }
}
