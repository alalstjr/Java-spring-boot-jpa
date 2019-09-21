package com.example.demo.post;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Sort;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    public void save() {
        Post post = new Post();
        post.setTitle("Spring");
        Post savePost = postRepository.save(post);

        Post postUpdate = new Post();
        postUpdate.setId(post.getId());
        postUpdate.setTitle("Spring Update");
        Post updatedPost = postRepository.save(postUpdate);

        postUpdate.setTitle("Spring No! Update!");

        List<Post> all = postRepository.findAll();
        assertThat(all.size()).isEqualTo(1);
    }

    @Test
    public void findByTitle() {
        Post post = new Post();
        List<Post> all = postRepository.findByTitle("Spring", Sort.by("title"));
    }
}