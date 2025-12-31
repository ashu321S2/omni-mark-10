package com.blog.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import com.blog.entity.PostLike;
import com.blog.entity.PostComment;
@Entity
@Table(name = "posts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="author_id")
    private User author;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // optional aggregates stored for quick display
 // Inside Post.java

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostLike> likeRecords;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostComment> commentRecords;
    
    @Column(nullable = false)
    @Builder.Default  // CRITICAL: If using Lombok @Builder, this ensures the default value is used
    private Integer comments = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer likes = 0;
    
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String imageBase64;


    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
