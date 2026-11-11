package com.example.springstudy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.OneToMany;
import org.hibernate.annotations.BatchSize;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "contents")
public class Content {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 10000)
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ContentStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @BatchSize(size = 10)
    @OneToMany(mappedBy = "content")
    private List<Comment> comments = new ArrayList<>();

    protected Content() {
    }

    public Content(String title, String body, User author, Category category) {
        this.title = title;
        this.body = body;
        this.status = ContentStatus.DRAFT;
        this.author = author;
        this.category = category;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getBody() { return body; }
    public ContentStatus getStatus() { return status; }
    public User getAuthor() { return author; }
    public Category getCategory() { return category; }
    public List<Comment> getComments() { return comments; }

    public void changeTitle(String title) { this.title = title; }
    public void changeBody(String body) { this.body = body; }
    public void changeStatus(ContentStatus status) { this.status = status; }
}
