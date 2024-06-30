package com.codeflow.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

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

    /* TODO Это пока базовая модель, где имеется только имя и пароль
       TODO Далее планиурется добавить все остальное после настройки аутнетификации
    */

}
