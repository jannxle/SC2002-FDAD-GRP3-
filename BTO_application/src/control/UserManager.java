package control;
import entities.Applicant;
import entities.Manager;
import entities.Officer;
import java.util.*;
import utils.FileManager;

public class UserManager {
	private List<Applicant>applicants = new ArrayList<>();
	private List<Officer> officers = new ArrayList<>();
    private List<Manager> managers = new ArrayList<>();
    
    private void loadApplicants(String filepath) {
    	List<String> lines = FileManager.readFile(filepath);
        for (String line : lines.subList(1, lines.size())) { // Skip header
            String[] parts = line.split(",");
            String name = parts[0];
            String nric = parts[1];
            int age = Integer.parseInt(parts[2]);
            String status = parts[3].trim().toLowerCase();
            boolean isMarried = status.equals("married");
            String password = parts[4].trim();
            applicants.add(new Applicant(name, nric, age, isMarried, password));
        }
    }
    
    private void loadOfficers(String filepath) {
        List<String> lines = FileManager.readFile(filepath);
        for (String line : lines.subList(1, lines.size())) { // Skip header
            String[] parts = line.split(",");
            String name = parts[0];
            String nric = parts[1];
            int age = Integer.parseInt(parts[2]);
            String status = parts[3].trim().toLowerCase();
            boolean isMarried = status.equals("married");
            String password = parts[4].trim();
            officers.add(new Officer(name, nric, age, isMarried, password));
        }
    }
    
    private void loadManagers(String filepath) {
    	List<String> lines = FileManager.readFile(filepath);
        for (String line : lines.subList(1, lines.size())) { // Skip header
            String[] parts = line.split(",");
            String name = parts[0];
            String nric = parts[1];
            int age = Integer.parseInt(parts[2]);
            String status = parts[3].trim().toLowerCase();
            boolean isMarried = status.equals("married");
            String password = parts[4].trim();
            managers.add(new Manager(name, nric, age, isMarried, password));
        }
    }
	
    public void loadAllUsers() {
    	loadApplicants("data/ApplicantList.csv");
    	loadOfficers("data/OfficerList.csv");
    	loadManagers("data/ManagerList.csv");
    }
    
    public List<Applicant> getApplicants(){
    	return applicants;
    }
    public List<Officer> getOfficers(){
    	return officers;
    }
    public List<Manager> getManagers(){
    	return managers;
    }

    
    
    // Save methods for persisting changes to CSV including the password column
    private void saveApplicants(String filepath) {
        List<String> lines = new ArrayList<>();
        // Write header with password column
        lines.add("Name,NRIC,Age,Status,Password");
        for (Applicant applicant : applicants) {
            String status = applicant.isMarried() ? "married" : "single";
            lines.add(applicant.getName() + "," 
                    + applicant.getNRIC() + "," 
                    + applicant.getAge() + "," 
                    + status + "," 
                    + applicant.getPassword());
        }
        FileManager.writeFile(filepath, lines);
    }

    private void saveOfficers(String filepath) {
        List<String> lines = new ArrayList<>();
        lines.add("Name,NRIC,Age,Status,Password");
        for (Officer officer : officers) {
            String status = officer.isMarried() ? "married" : "single";
            lines.add(officer.getName() + "," 
                    + officer.getNRIC() + "," 
                    + officer.getAge() + "," 
                    + status + "," 
                    + officer.getPassword());
        }
        FileManager.writeFile(filepath, lines);
    }

    private void saveManagers(String filepath) {
        List<String> lines = new ArrayList<>();
        lines.add("Name,NRIC,Age,Status,Password");
        for (Manager manager : managers) {
            String status = manager.isMarried() ? "married" : "single";
            lines.add(manager.getName() + "," 
                    + manager.getNRIC() + "," 
                    + manager.getAge() + "," 
                    + status + "," 
                    + manager.getPassword());
        }
        FileManager.writeFile(filepath, lines);
    }

    // Call this to save all changes at once
    public void saveAllUsers() {
        saveApplicants("data/ApplicantList.csv");
        saveOfficers("data/OfficerList.csv");
        saveManagers("data/ManagerList.csv");
    }

    // Update the password for an applicant, officers and managers and persist the change.
    public boolean changeApplicantPassword(String nric, String newPassword) {
        for (Applicant applicant : applicants) {
            if (applicant.getNRIC().equalsIgnoreCase(nric)) {
                applicant.changePass(newPassword);
                saveApplicants("data/ApplicantList.csv");
                return true;
            }
        }
        return false;
    }
    public boolean changeOfficerPassword(String nric, String newPassword) {
        for (Officer officer : officers) {
            if (officer.getNRIC().equalsIgnoreCase(nric)) {
                officer.changePass(newPassword);
                saveOfficers("data/OfficerList.csv");
                return true;
            }
        }
        return false;
    }
    public boolean changeManagerPassword(String nric, String newPassword) {
        for (Manager manager : managers) {
            if (manager.getNRIC().equalsIgnoreCase(nric)) {
                manager.changePass(newPassword);
                saveManagers("data/ManagerList.csv");
                return true;
            }
        }
        return false;
    }
    
    public Officer findOfficerByName(String officerName) {
    	for (Officer o:getOfficers()) {
    		if (o.getName().trim().equalsIgnoreCase(officerName.trim())) {
    		return o;
    		}
    	}
    	return null;
    }
}
    