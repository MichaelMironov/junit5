package org.example.service;

import lombok.Value;

import java.util.Objects;

@Value(staticConstructor = "of")
public class User {
    Integer id;
    String username;
    String password;
}
