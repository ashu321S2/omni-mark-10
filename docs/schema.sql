CREATE DATABASE IF NOT EXISTS blogdb;
USE blogdb;

CREATE TABLE users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(100) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  email VARCHAR(150),
  role VARCHAR(50)
);

CREATE TABLE posts (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  content TEXT,
  author_id BIGINT,
  created_at DATETIME,
  updated_at DATETIME,
  CONSTRAINT fk_posts_user FOREIGN KEY (author_id) REFERENCES users(id)
);

CREATE TABLE comments (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  post_id BIGINT,
  content TEXT,
  author_id BIGINT,
  created_at DATETIME,
  CONSTRAINT fk_comments_post FOREIGN KEY (post_id) REFERENCES posts(id),
  CONSTRAINT fk_comments_user FOREIGN KEY (author_id) REFERENCES users(id)
);
