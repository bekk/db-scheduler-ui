package com.github.bekk.exampleapp.model;

import java.io.Serializable;

public record TaskData(long id, String data) implements Serializable {

}