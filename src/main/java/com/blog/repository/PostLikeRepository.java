package com.blog.repository;

import com.blog.entity.PostLike;
import com.blog.entity.Post;
import com.blog.entity.User;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    Optional<PostLike> findByPostAndUser(Post post, User user);
    long countByPost(Post post);

    @Modifying
    @Transactional
    void deleteByPost(Post post);

    @Modifying
    @Transactional
    void deleteAllByPost(Post post);
}
