package com.android.bingmaps;

public class Placemark {
    private String name;
    private String description;
    private String coordinates;
    private String color;

    public Placemark(String name, String description, String coordinates,String color) {
        this.name = name;
        this.description = description;
        this.coordinates = coordinates;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getCoordinates() {
        return coordinates;
    }

    public String getColor() {
        return color;
    }
}
