package control;

import entities.Applicant;
import entities.User;
import utils.FileManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages Applicant user data, implementing the UserManager interface.
 * This class is responsible for loading applicant details from a CSV file,
 * saving changes back to the file, providing access to the list of applicants,
 * finding applicants by NRIC, and handling password changes for applicants.
 */
public class ApplicantUserManager implements UserManager<Applicant> {

    private List<Applicant> applicants = new ArrayList<>();
    private static final String FILE_PATH = "data/ApplicantList.csv";

     /**
     * Loads applicant data from the CSV file.
     * Clears the current in-memory list before loading.
     * Parses each line (skipping the header) into an Applicant object.
     * Handles potential errors during file reading or data parsing (e.g., NumberFormatException).
     * Assumes CSV format: Name,NRIC,Age,Status,Password
     */
    @Override
    public void loadUsers() {
        applicants.clear();
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
                    applicants.add(new Applicant(name, nric, age, isMarried, password));
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
     * Saves the current list of Applicant objects back to the CSV file
     * Overwrites the existing file content. Writes the header row first,
     * then formats each Applicant object into a CSV line.
     * Assumes CSV format: Name,NRIC,Age,Status,Password
     */
    @Override
    public void saveUsers() {
        List<String> lines = new ArrayList<>();
        lines.add("Name,NRIC,Age,Status,Password");
        for (Applicant applicant : applicants) {
            String status = applicant.isMarried() ? "married" : "single";
            lines.add(
                    applicant.getName() + "," +
                    applicant.getNRIC() + "," +
                    applicant.getAge() + "," +
                    status + "," +
                    applicant.getPassword()
            );
        }
        FileManager.writeFile(FILE_PATH, lines);
    }

    /**
     * Retrieves the current in-memory list of all loaded applicants.
     *
     * @return A List containing all Applicant objects managed by this instance.
     * Returns an empty list if no applicants have been loaded.
     */
    @Override
    public List<Applicant> getUsers() {
        return applicants;
    }

    /**
     * Finds and returns an applicant based on their NRIC.
     * The search is case-insensitive.
     *
     * @param nric The NRIC of the applicant to find.
     * @return The {@link Applicant} object if found, or null if no applicant matches the given NRIC.
     */
    @Override
    public Applicant findByNRIC(String nric) {
        for (Applicant applicant : applicants) {
            if (applicant.getNRIC().equalsIgnoreCase(nric)) {
                return applicant;
            }
        }
        return null;
    }

    /**
     * Changes the password for the applicant identified by the given NRIC.
     * Finds the applicant and saves the updated user list if the change was successful.
     *
     * @param nric        The NRIC of the applicant whose password should be changed.
     * @param newPassword The new password to set.
     * @return true if the password was successfully updated and saved, false otherwise.
     */
    @Override
    public boolean changePassword(String nric, String newPassword) {
        Applicant user = findByNRIC(nric);
        if (user != null) {
            if (user.changePass(newPassword)) {
                saveUsers();
                return true;
            } else {
                 return false;
            }
        }
         System.err.println("Applicant not found for password change: " + nric);
        return false;
    }
}
