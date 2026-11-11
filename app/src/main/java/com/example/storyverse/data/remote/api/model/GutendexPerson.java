package com.example.storyverse.data.remote.api.model;

import com.google.gson.annotations.SerializedName;

public class GutendexPerson {

    @SerializedName("name")
    private String name;

    @SerializedName("birth_year")
    private Integer birthYear;

    @SerializedName("death_year")
    private Integer deathYear;

    public String getName() {
        return name;
    }

    public Integer getBirthYear() {
        return birthYear;
    }

    public Integer getDeathYear() {
        return deathYear;
    }
}