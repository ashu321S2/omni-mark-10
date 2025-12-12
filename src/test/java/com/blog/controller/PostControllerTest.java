package com.blog.controller;

import com.blog.dto.PostDto;
import com.blog.service.PostService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(controllers = PostController.class)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostService postService;

    @Test
    void getAllPosts_returnsPagedPosts() throws Exception {
        PostDto dto = PostDto.builder()
                .id(1L)
                .title("Hello")
                .content("World")
                .authorUsername("alice")
                .createdAt(LocalDateTime.now())
                .likes(0)
                .comments(0)
                .build();

        PageRequest pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        Page<PostDto> page = new PageImpl<>(List.of(dto), pageable, 1);

        Mockito.when(postService.getAllPosts(Mockito.any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/posts?page=0&size=10")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].title").value("Hello"))
                .andExpect(jsonPath("$.content[0].authorUsername").value("alice"));
    }
}
