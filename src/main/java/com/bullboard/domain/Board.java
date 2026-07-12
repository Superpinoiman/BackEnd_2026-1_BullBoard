package com.bullboard.domain;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.CascadeType.ALL;

@Entity
@Table(name = "board")
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToMany(mappedBy = "board", orphanRemoval = true, cascade = ALL)
    private List<Article> articles = new ArrayList<>();

    protected Board() {
    }

    public Board(String name) {
        this.name = name;
    }

    public void update(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void addArticle(Article article) {
        articles.add(article);
        article.setBoard(this);
    }

    public void removeArticle(Article article) {
        articles.remove(article);
        article.setBoard(null);
    }
}
