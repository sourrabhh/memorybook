package com.context.memorybook.domain.memory.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tags")
@Data
@AllArgsConstructor
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @ManyToMany(mappedBy = "tags")
    private Set<Memory> memories = new HashSet<>();

    public Tag() {}

    public Tag(String name) {
        this.name = name;
    }

}
