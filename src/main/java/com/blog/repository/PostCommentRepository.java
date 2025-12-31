package com.blog.repository;

import com.blog.entity.PostComment;
import com.blog.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {
    List<PostComment> findByPostOrderByCreatedAtDesc(Post post);
    long countByPost(Post post);

    @Modifying
    @Transactional
    void deleteByPost(Post post);

    @Modifying
    @Transactional
    void deleteAllByPost(Post post);
}