package com.cyberdyber.filters;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect(getterVisibility = NONE, setterVisibility = NONE, fieldVisibility = ANY)
public class Eugene {
    String a;

    public Eugene(String a) {
        this.a = a;
    }
}
