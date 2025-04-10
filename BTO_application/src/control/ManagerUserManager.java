package control;

import entities.Manager;
import utils.FileManager;

import java.util.ArrayList;
import java.util.List;

public class ManagerUserManager implements UserManager<Manager> {

    private List<Manager> managers = new ArrayList<>();
    private static final String FILE_PATH = "data/ManagerList.csv";

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
         System.out.println("Manager data loaded from " + FILE_PATH);
    }

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
        System.out.println("Manager data saved to " + FILE_PATH);
    }

    @Override
    public List<Manager> getUsers() {
        return managers;
    }

    @Override
    public Manager findByNRIC(String nric) {
        for (Manager manager : managers) {
            if (manager.getNRIC().equalsIgnoreCase(nric)) {
                return manager;
            }
        }
        return null;
    }

    @Override
    public boolean changePassword(String nric, String newPassword) {
        Manager user = findByNRIC(nric);
        if (user != null) {
            if (user.changePass(newPassword)) {
                saveUsers();
                System.out.println("Password changed successfully for Manager: " + nric);
                return true;
            } else {
                System.err.println("Password validation failed for Manager: " + nric);
                return false;
            }
        }
         System.err.println("Manager not found for password change: " + nric);
        return false;
    }
}