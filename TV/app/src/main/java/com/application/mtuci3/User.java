package com.application.mtuci3;

public class User {
    int id;
    String email;
    String name;
    String phone;
    public User(int id, String email, String name, String phone) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.phone = phone;
    }
    public int getID() {
        return this.id;
    }
    public String getEmail() {
        return this.email;
    }
    public String getName() {
        return this.name;
    }
    public String getPhone() {
        return this.phone;
    }
}
