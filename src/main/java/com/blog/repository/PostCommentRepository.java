package com.blog.repository;

import com.blog.entity.PostComment;
import com.blog.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {
    List<PostComment> findByPostOrderByCreatedAtDesc(Post post);
    long countByPost(Post post);
}
