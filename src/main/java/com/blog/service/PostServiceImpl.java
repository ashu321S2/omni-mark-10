package com.blog.service;
import com.blog.entity.Post;
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

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Base64;
@Transactional
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostLikeRepository likeRepository;
    private final PostCommentRepository commentRepository;

    // =========================
    // CREATE POST (TEXT + IMAGE)
    // =========================
    @Override
    public PostDto createPost(PostDto dto, MultipartFile image, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Post post = Post.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .author(user)
                .comments(0) // Initialize to avoid null
                .likes(0)    // Initialize to avoid null
                .build();

        if (image != null && !image.isEmpty()) {
            try {
                // 1. Define the absolute path to the Azure PVC mount
                String uploadDir = "/uploads/";
                String fileName = UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
                Path uploadPath = Paths.get(uploadDir);

                // 2. Create directory if it doesn't exist
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                // 3. Save the file to the disk
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(image.getInputStream(), filePath);

                // 4. Store the URL in the database (this is what the frontend will call)
                // Example: /uploads/abc-123-image.jpg
                post.setImageBase64("/uploads/" + fileName); 

            } catch (IOException e) {
                throw new RuntimeException("Could not save image file!", e);
            }
        }

        Post saved = postRepository.save(post);
        return mapToDto(saved);
    }

    // =========================
    // GET ALL POSTS
    // =========================
    @Override
    public Page<PostDto> getAllPosts(Pageable pageable) {
        Page<Post> page = postRepository.findAllByOrderByCreatedAtDesc(pageable);
        return page.map(this::mapToDto);
    }

    // =========================
    // GET SINGLE POST
    // =========================
    @Override
    public PostDto getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        return mapToDto(post);
    }

    // =========================
    // UPDATE POST (TEXT ONLY)
    // =========================
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

    // =========================
    // DELETE POST (OWNER)
    // =========================
    @Override
    @Transactional
    public void deletePost(Long postId, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        // Check ownership
        if (!post.getAuthor().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied");
        }

        // 1. Manually clear child blockers
        likeRepository.deleteByPost(post);
        commentRepository.deleteByPost(post);

        // 2. Safely delete the parent
        postRepository.delete(post);
    }



    // =========================
    // ADMIN DELETE
    // =========================
    @Override
    public void adminDeletePost(Long id) {
        if (!postRepository.existsById(id)) {
            throw new ResourceNotFoundException("Post not found");
        }
        postRepository.deleteById(id);
    }

    // =========================
    // LIKES
    // =========================
    @Override
    public void likePost(Long postId, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

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

    // =========================
    // COMMENTS
    // =========================
    @Override
    @Transactional
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

        // Ensure we don't hit a NullPointerException if post.comments is null
     // SAFE: Handle null and calculate new count
        Integer current = post.getComments();
        post.setComments((current == null ? 0 : current) + 1);

        // Save the post

        
        // Save the post so the database updates the count column
        postRepository.save(post); 

        return CommentDto.builder()
                .id(saved.getId())
                .postId(post.getId())
                .authorUsername(user.getUsername())
                .content(saved.getContent())
                .createdAt(saved.getCreatedAt())
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
                        .createdAt(c.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public long getCommentsCount(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        return commentRepository.countByPost(post);
    }

    // =========================
    // MAPPER
    // =========================
    private PostDto mapToDto(Post post) {
        return PostDto.builder()
            .id(post.getId())
            .title(post.getTitle())
            .content(post.getContent())
            .imageBase64(post.getImageBase64())

            // 👇 AUTHOR INFO (FIX)
            .authorUsername(
                post.getAuthor() != null
                    ? post.getAuthor().getUsername()
                    : null
            )
            .authorId( // 🔥 ADD THIS
                post.getAuthor() != null
                    ? post.getAuthor().getId()
                    : null
            )

            .createdAt(post.getCreatedAt())
            .likes(post.getLikes())
            .comments(post.getComments())
            .build();
    }

}
