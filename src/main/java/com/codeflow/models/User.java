package com.codeflow.models;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    private UUID id;

    @Column
    private String username;

    @Column
    private String password;

    @OneToMany
    private List<Role> roles;

    /* TODO Это пока базовая модель, где имеется только имя и пароль
       TODO Далее планиурется добавить все остальное после настройки аутнетификации
    */

}
