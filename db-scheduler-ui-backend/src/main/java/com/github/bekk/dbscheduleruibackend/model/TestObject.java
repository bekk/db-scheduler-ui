package com.github.bekk.dbscheduleruibackend.model;

import java.io.Serializable;

public class TestObject implements Serializable {
    private String name;
    private int id;
    private String email;

    public String getName() {
        return name;
    }
    public TestObject(String initialName, int id, String email) {
        this.id = id;
        this.name = initialName;
        this.email = email;

    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


}
