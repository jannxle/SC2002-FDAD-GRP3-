package control;

import entities.Manager;
import utils.FileManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages Manager user data, implementing the UserManager interface.
 * Responsible for loading manager details from `ManagerList.csv`, saving changes,
 * finding managers by NRIC, and handling password changes.
 */
public class ManagerUserManager implements UserManager<Manager> {

    private List<Manager> managers = new ArrayList<>();
    private static final String FILE_PATH = "data/ManagerList.csv";

    /**
     * Loads manager data from the CSV file.
     * Clears current managers and parses lines into Manager objects.
     * Handles parsing errors. Assumes CSV format: Name,NRIC,Age,Status,Password
     */
    @Override
    public void loadUsers() {
        managers.clear();
        List<String> lines = FileManager.readFile(FILE_PATH);
        for (String line : lines.subList(1, lines.size())) {
             try {
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    String name = parts[0].trim();
                    String nric = parts[1].trim().toUpperCase();
                    int age = Integer.parseInt(parts[2].trim());
                    String status = parts[3].trim().toLowerCase();
                    boolean isMarried = status.equals("married");
                    String password = parts[4].trim();
                    managers.add(new Manager(name, nric, age, isMarried, password));
                } else {
                    System.err.println("Skipping malformed line in " + FILE_PATH + ": " + line);
                }
            } catch (NumberFormatException e) {
                System.err.println("Error parsing age in " + FILE_PATH + " for line: " + line + " - " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Error processing line in " + FILE_PATH + ": " + line + " - " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Saves the current list of Manager objects to the CSV file.
     * Overwrites the existing file. Format: Name,NRIC,Age,Status,Password
     */
    @Override
    public void saveUsers() {
        List<String> lines = new ArrayList<>();
        lines.add("Name,NRIC,Age,Status,Password");
        for (Manager manager : managers) {
            String status = manager.isMarried() ? "married" : "single";
            lines.add(
                    manager.getName() + "," +
                    manager.getNRIC() + "," +
                    manager.getAge() + "," +
                    status + "," +
                    manager.getPassword()
            );
        }
        FileManager.writeFile(FILE_PATH, lines);
    }

    /**
     * Retrieves the current in-memory list of all loaded managers.
     *
     * @return A List containing all Manager objects.
     */
    @Override
    public List<Manager> getUsers() {
        return managers;
    }

    /**
     * Finds and returns a manager based on their NRIC.
     *
     * @param nric The NRIC of the manager to find.
     * @return The Manager object if found, or null otherwise.
     */
    @Override
    public Manager findByNRIC(String nric) {
        for (Manager manager : managers) {
            if (manager.getNRIC().equalsIgnoreCase(nric)) {
                return manager;
            }
        }
        return null;
    }

    /**
     * Changes the password for the manager identified by the given NRIC.
     * Saves the updated manager list if the change is successful.
     *
     * @param nric        The NRIC of the manager whose password should be changed.
     * @param newPassword The new password to set.
     * @return true if the password was successfully updated and saved, false otherwise.
     */
    @Override
    public boolean changePassword(String nric, String newPassword) {
        Manager user = findByNRIC(nric);
        if (user != null) {
            if (user.changePass(newPassword)) {
                saveUsers();
                return true;
            } else {
                return false;
            }
        }
         System.err.println("Manager not found for password change: " + nric);
        return false;
    }
}