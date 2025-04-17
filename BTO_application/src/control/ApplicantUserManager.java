package control;

import entities.Applicant;
import utils.FileManager;

import java.util.ArrayList;
import java.util.List;

public class ApplicantUserManager implements UserManager<Applicant> {

    private List<Applicant> applicants = new ArrayList<>();
    private static final String FILE_PATH = "data/ApplicantList.csv";

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

    @Override
    public List<Applicant> getUsers() {
        return applicants;
    }

    @Override
    public Applicant findByNRIC(String nric) {
        for (Applicant applicant : applicants) {
            if (applicant.getNRIC().equalsIgnoreCase(nric)) {
                return applicant;
            }
        }
        return null;
    }

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
