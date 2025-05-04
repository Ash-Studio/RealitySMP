package me.ash.realitySMP.model;

import org.bukkit.Material;

public class Job {
    private final String name;
    private final String displayName;
    private final int salary;
    private final Material icon;
    private final String[] description;

    public Job(String name, String displayName, int salary, Material icon, String[] description) {
        this.name = name;
        this.displayName = displayName;
        this.salary = salary;
        this.icon = icon;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getSalary() {
        return salary;
    }

    public Material getIcon() {
        return icon;
    }

    public String[] getDescription() {
        return description;
    }
}