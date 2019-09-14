package com.example.demo;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
class Address {

    @Column
    private String street;

    @Column
    private String city;

    @Column
    private String state;

    @Column
    private String zipCode;
}
