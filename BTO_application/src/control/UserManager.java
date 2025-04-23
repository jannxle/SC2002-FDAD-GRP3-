package control;

import entities.User;
import java.util.List;

/**
 * Defines a generic contract for managing different types of users within the system.
 * Implementations of this interface handle loading, saving, retrieving, and modifying
 * user data for specific user subtypes (e.g., Applicant, Officer, Manager).
 *
 * @param <T> The specific subtype of User that this manager handles (must extend User).
 */
public interface UserManager<T extends User> {
    /**
     * Loads user data from a specified source (e.g., a file or database).
     * This method should be implemented to read user data and populate the internal list of users.
     */
    void loadUsers();

    /**
     * Saves the current state of user data to a specified destination (e.g., a file or database).
     * This method should be implemented to write the internal list of users back to the source.
     */
    void saveUsers();

    /**
     * Adds a new user to the internal list of users.
     *
     * @param user The user to be added.
     */
    List<T> getUsers();

    /**
     * Finds a user by their NRIC.
     *
     * @param nric The NRIC of the user to find.
     * @return The user with the specified NRIC, or null if not found.
     */
    T findByNRIC(String nric);

    /**
     * Checks if a user with the specified NRIC exists in the system.
     *
     * @param nric The NRIC of the user to check.
     * @return true if the user exists, false otherwise.
     */
    boolean changePassword(String nric, String newPassword);
}