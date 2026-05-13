package com.contractoriq.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "contractors")
public class Contractor implements Serializable {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String city;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Trade trade;

    private String phone;

    @Column(name = "joined_date", nullable = false)
    private LocalDate joinedDate;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    public Contractor() {}

    public Contractor(UUID id, String name, String city, Trade trade,
                      String phone, LocalDate joinedDate, boolean active) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.trade = trade;
        this.phone = phone;
        this.joinedDate = joinedDate;
        this.active = active;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public Trade getTrade() { return trade; }
    public void setTrade(Trade trade) { this.trade = trade; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public LocalDate getJoinedDate() { return joinedDate; }
    public void setJoinedDate(LocalDate joinedDate) { this.joinedDate = joinedDate; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
