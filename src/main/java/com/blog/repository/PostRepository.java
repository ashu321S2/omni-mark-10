package com.blog.repository;

import com.blog.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByAuthorId(Long authorId);

    // page-based query sorted by createdAt desc
    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
