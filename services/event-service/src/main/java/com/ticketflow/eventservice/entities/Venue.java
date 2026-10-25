package com.ticketflow.eventservice.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.UUID;

@Entity
public class Venue {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String name;
    private String city;
    private String address;
    private int capacity;

    protected Venue() {}
    public Venue(String name, String city, String address, int capacity) {
        update(name, city, address, capacity);
    }
    public void update(String name, String city, String address, int capacity) {
        this.name = name;
        this.city = city;
        this.address = address;
        this.capacity = capacity;
    }
    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getCity() { return city; }
    public String getAddress() { return address; }
    public int getCapacity() { return capacity; }
}
