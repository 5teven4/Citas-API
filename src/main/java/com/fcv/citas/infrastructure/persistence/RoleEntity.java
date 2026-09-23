package com.fcv.citas.infrastructure.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class RoleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false, length = 80)
    private String name;

    protected RoleEntity() { }

    public Short getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
}
