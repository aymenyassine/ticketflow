package com.ticketflow.eventservice.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.UUID;

@Entity
public class Category {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false, unique = true)
    private String name;

    protected Category() {}
    public Category(String name) { this.name = name; }
    public UUID getId() { return id; }
    public String getName() { return name; }
    public void update(String name) { this.name = name; }
}
