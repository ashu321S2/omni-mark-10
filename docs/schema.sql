CREATE DATABASE IF NOT EXISTS blogdb;
USE blogdb;

------------------------------------------------------
-- USERS TABLE
------------------------------------------------------
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(150),
    role VARCHAR(50)
);

------------------------------------------------------
-- POSTS TABLE
-- Updated with likes, comments count, timestamps
------------------------------------------------------
CREATE TABLE posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    author_id BIGINT,
    
    likes BIGINT DEFAULT 0,
    comments BIGINT DEFAULT 0,

    created_at DATETIME,
    updated_at DATETIME,

    CONSTRAINT fk_posts_user
        FOREIGN KEY (author_id)
        REFERENCES users(id)
        ON DELETE SET NULL
);

------------------------------------------------------
-- POST COMMENTS TABLE (corresponds to PostComment entity)
------------------------------------------------------
CREATE TABLE post_comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    content TEXT,
    created_at DATETIME,

    CONSTRAINT fk_comment_post
        FOREIGN KEY (post_id)
        REFERENCES posts(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_comment_user
        FOREIGN KEY (author_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

------------------------------------------------------
-- POST LIKES TABLE (corresponds to PostLike entity)
------------------------------------------------------
CREATE TABLE post_likes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,

    CONSTRAINT fk_like_post
        FOREIGN KEY (post_id)
        REFERENCES posts(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_like_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);
