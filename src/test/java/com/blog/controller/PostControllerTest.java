package com.blog.controller;

import com.blog.dto.PostDto;
import com.blog.security.JwtAuthenticationFilter;
import com.blog.service.PostService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PostController.class)
@AutoConfigureMockMvc(addFilters = false)   // disable security filters
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostService postService;

    // IMPORTANT: mock JwtAuthenticationFilter so Spring doesn't try to build the real one
    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void testGetAllPosts() throws Exception {
        PostDto post1 = new PostDto(1L, "Test Title", "Test Content", "alice");
        List<PostDto> list = List.of(post1);

        PageRequest pageable = PageRequest.of(0, 10);

        Mockito.when(postService.getAllPosts(Mockito.any()))
                .thenReturn(new PageImpl<>(list, pageable, list.size()));

        mockMvc.perform(get("/api/posts?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].title").value("Test Title"));
    }
}
