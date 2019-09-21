package com.example.demo.post;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class CommentRepositoryTest {

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    PostRepository postRepository;

    @Test
    public void getComment() {
        Post post = new Post();
        post.setTitle("title");
        Post savePost = postRepository.save(post);

        Comment comment = new Comment();
        comment.setComment("comment");
        comment.setPost(savePost);
        commentRepository.save(comment);

        Optional<Comment> byId = commentRepository.findById(1l);
        System.out.println(byId.get().getPost());
    }
}