package com.example.demo.post;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends MyRepository<Post, Long> {

}
