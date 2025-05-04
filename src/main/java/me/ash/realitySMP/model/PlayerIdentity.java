package me.ash.realitySMP.model;

import java.util.UUID;

/**
 * Represents a player's identity information for the ID card system
 */
public class PlayerIdentity {
    private final UUID playerUuid;
    private String firstName;
    private String lastName;
    private String address;
    private String dateOfBirth;
    private String occupation;

    /**
     * Creates a new player identity
     *
     * @param playerUuid The UUID of the player
     */
    public PlayerIdentity(UUID playerUuid) {
        this.playerUuid = playerUuid;
        this.firstName = "Unknown";
        this.lastName = "Unknown";
        this.address = "No Address";
        this.dateOfBirth = "Unknown";
        this.occupation = "Unemployed";
    }

    /**
     * Get the player's UUID
     *
     * @return The UUID of the player
     */
    public UUID getPlayerUuid() {
        return playerUuid;
    }

    /**
     * Get the player's first name
     *
     * @return The first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Set the player's first name
     *
     * @param firstName The new first name
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Get the player's last name
     *
     * @return The last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Set the player's last name
     *
     * @param lastName The new last name
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Get the player's full name (first + last)
     *
     * @return The full name
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Get the player's address
     *
     * @return The address
     */
    public String getAddress() {
        return address;
    }

    /**
     * Set the player's address
     *
     * @param address The new address
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Get the player's date of birth
     *
     * @return The date of birth
     */
    public String getDateOfBirth() {
        return dateOfBirth;
    }

    /**
     * Set the player's date of birth
     *
     * @param dateOfBirth The new date of birth
     */
    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    /**
     * Get the player's occupation
     *
     * @return The occupation
     */
    public String getOccupation() {
        return occupation;
    }

    /**
     * Set the player's occupation
     *
     * @param occupation The new occupation
     */
    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    /**
     * Update a specific field of the identity
     *
     * @param field The field to update (firstName, lastName, address, dateOfBirth, occupation)
     * @param value The new value for the field
     * @return True if the field was updated, false if the field doesn't exist
     */
    public boolean updateField(String field, String value) {
        switch(field.toLowerCase()) {
            case "firstname":
                setFirstName(value);
                return true;
            case "lastname":
                setLastName(value);
                return true;
            case "address":
                setAddress(value);
                return true;
            case "dateofbirth":
            case "dob":
                setDateOfBirth(value);
                return true;
            case "occupation":
            case "job":
                setOccupation(value);
                return true;
            default:
                return false;
        }
    }
}