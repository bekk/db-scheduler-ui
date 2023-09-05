package com.github.bekk.exampleapp.model;

import java.io.Serializable;

public class TaskData implements Serializable{
    private long id;
    private String data;

    public TaskData(long id, String data) {
        this.id = id;
        this.data = data;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
