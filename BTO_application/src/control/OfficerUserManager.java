package control;

import entities.Officer;
import utils.FileManager;

import java.util.ArrayList;
import java.util.List;

public class OfficerUserManager implements UserManager<Officer> {

    private List<Officer> officers = new ArrayList<>();
    private static final String FILE_PATH = "data/OfficerList.csv";

    @Override
    public void loadUsers() {
        officers.clear();
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
                    officers.add(new Officer(name, nric, age, isMarried, password));
                     // Note: Loading Officer-specific state (registeredProject, status)
                     // might require a different CSV structure or linking after all projects are loaded.
                     // This basic implementation only loads User/Applicant fields.
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
         System.out.println("Officer data loaded from " + FILE_PATH);
    }

    @Override
    public void saveUsers() {
        List<String> lines = new ArrayList<>();
        lines.add("Name,NRIC,Age,Status,Password");
        for (Officer officer : officers) {
            String status = officer.isMarried() ? "married" : "single";
            lines.add(
                    officer.getName() + "," +
                    officer.getNRIC() + "," +
                    officer.getAge() + "," +
                    status + "," +
                    officer.getPassword()
            );
        }
        FileManager.writeFile(FILE_PATH, lines);
         System.out.println("Officer data saved to " + FILE_PATH);
    }

    @Override
    public List<Officer> getUsers() {
        return officers;
    }

    @Override
    public Officer findByNRIC(String nric) {
        for (Officer officer : officers) {
            if (officer.getNRIC().equalsIgnoreCase(nric)) {
                return officer;
            }
        }
        return null;
    }

    @Override
    public boolean changePassword(String nric, String newPassword) {
        Officer user = findByNRIC(nric);
        if (user != null) {
            if (user.changePass(newPassword)) {
                saveUsers();
                 System.out.println("Password changed successfully for Officer: " + nric);
                return true;
            } else {
                 System.err.println("Password validation failed for Officer: " + nric);
                 return false;
            }
        }
         System.err.println("Officer not found for password change: " + nric);
        return false;
    }
}
