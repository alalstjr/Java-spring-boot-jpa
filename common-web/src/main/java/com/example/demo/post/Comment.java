package com.example.demo.post;

import javax.persistence.*;

@Entity
@NamedEntityGraph(name = "Comment.post", attributeNodes = @NamedAttributeNode("post"))
public class Comment {

    @Id
    @GeneratedValue
    private Long id;

    private String comment;

    @ManyToOne(fetch = FetchType.EAGER)
    private Post post;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }
}
