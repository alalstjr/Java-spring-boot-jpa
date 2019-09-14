package com.example.demo;

import org.hibernate.Session;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

@Component
@Transactional
public class JpaRunner implements ApplicationRunner {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void run(ApplicationArguments args) throws Exception {
//        Post post = new Post();
//        post.setTitle("게시글의 제목");
//
//        Comment comment = new Comment();
//        comment.setComment("게시글의 댓글1");
//        post.addComment(comment);
//
//        Comment comment1 = new Comment();
//        comment1.setComment("게시글의 댓글2");
//        post.addComment(comment1);

        Session session = entityManager.unwrap(Session.class);
        Post post = session.get(Post.class, 1L);
        session.delete(post);
    }
}