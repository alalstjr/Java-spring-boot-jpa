package com.example.demo.account;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Account {

    @Id
    @GeneratedValue
    private Long Id;

    private String username;

    private String firstName;

    private String lastName;
}
