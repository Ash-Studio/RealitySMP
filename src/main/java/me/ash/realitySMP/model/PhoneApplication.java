package me.ash.realitySMP.model;

/**
 * Represents an application that can be installed on a player's phone
 */
public class PhoneApplication {
    private final String id;
    private final String name;
    private final String description;

    /**
     * Creates a new phone application
     *
     * @param id The unique identifier for the application
     * @param name The display name of the application
     * @param description A brief description of what the application does
     */
    public PhoneApplication(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    /**
     * Get the unique identifier for this application
     *
     * @return The application ID
     */
    public String getId() {
        return id;
    }

    /**
     * Get the display name of this application
     *
     * @return The application name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the description of this application
     *
     * @return The application description
     */
    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        PhoneApplication that = (PhoneApplication) obj;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}