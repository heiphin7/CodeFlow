package com.api.codeflow.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Table(name = "roles")
@Entity
public class Role {

    @Id
    private Long id;
    private String name;
}
