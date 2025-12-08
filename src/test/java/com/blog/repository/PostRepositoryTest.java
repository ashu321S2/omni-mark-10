package com.blog.repository;

import com.blog.entity.Post;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)  // <--- use real MySQL
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Test
    void testSavePost() {
        Post post = new Post();
        post.setTitle("Test title");
        post.setContent("Test content");

        Post saved = postRepository.save(post);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTitle()).isEqualTo("Test title");
    }
}
