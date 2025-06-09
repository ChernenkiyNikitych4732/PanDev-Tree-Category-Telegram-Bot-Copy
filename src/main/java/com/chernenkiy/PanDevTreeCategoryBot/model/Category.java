package com.chernenkiy.PanDevTreeCategoryBot.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
public class Category {

    // Уникальный идентификатор категории
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Название категории
    @Column(nullable = false, unique = true)
    private String name;

    // Родительская категория (если есть)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    // Список дочерних категорий
    @OneToMany(
            mappedBy = "parent",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER
    )
    private List<Category> children = new ArrayList<>();


    public Category() {
    }

    public Category(String name) {
        this.name = name;
    }

    public Category(String name, Category parent) {
        this.name = name;
        this.parent = parent;
    }


    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Category getParent() {
        return parent;
    }

    public void setParent(Category parent) {
        this.parent = parent;
    }

    public List<Category> getChildren() {
        return children;
    }

    public void setChildren(List<Category> children) {
        this.children = children;
    }

    // Метод для добавления дочерней категории
    public void addChild(Category child) {
        children.add(child);
        child.setParent(this);
    }

    // Метод для удаления дочерней категории
    public void removeChild(Category child) {
        children.remove(child);
        child.setParent(null);
    }

}