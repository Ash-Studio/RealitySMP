package me.ash.realitySMP.model;

public class Apartment {
    private final String id;
    private final String name;
    private final double price;
    private final double rent;
    private final double x;
    private final double y;
    private final double z;
    private final String world;

    public Apartment(String id, String name, double price, double rent, double x, double y, double z, String world) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.rent = rent;
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public double getRent() {
        return rent;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public String getWorld() {
        return world;
    }
}