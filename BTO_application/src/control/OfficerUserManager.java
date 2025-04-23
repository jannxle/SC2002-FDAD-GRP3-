package control;

import entities.Officer;
import entities.Project;
import enums.OfficerRegistrationStatus;
import utils.FileManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages Officer user data, implementing the UserManager interface.
 * Handles loading/saving officer details, including their project registrations and statuses,
 * from/to `OfficerList.csv`. Requires ProjectManager to link project names to objects.
 */
public class OfficerUserManager implements UserManager<Officer> {

    private List<Officer> officers = new ArrayList<>();
    private static final String FILE_PATH = "data/OfficerList.csv";
    private ProjectManager projectManager;
    
    /**
     * Constructs an OfficerUserManager.
     * Requires a ProjectManager instance to link registered project names from the CSV
     * to actual Project objects during loading.
     *
     * @param projectManager The manager for accessing project data.
     */
    public OfficerUserManager(ProjectManager projectManager) {
    	this.projectManager = projectManager;
    }

    /**
     * Loads officer data from the CSV file.
     * Clears current officers. Parses lines into Officer objects, including
     * semicolon-separated registered project names and their corresponding statuses.
     * Uses ProjectManager to find Project objects. Handles parsing errors.
     * Format: Name,NRIC,Age,Status,Password,RegisteredProjects,RegistrationStatuses
     */
    @Override
    public void loadUsers() {
        officers.clear();
        List<String> lines = FileManager.readFile(FILE_PATH);
        for (String line : lines.subList(1, lines.size())) {
            try {
                String[] parts = line.split(",", -1);
                if (parts.length >= 7) {
                    String name = parts[0].trim();
                    String nric = parts[1].trim().toUpperCase();
                    int age = Integer.parseInt(parts[2].trim());
                    String status = parts[3].trim().toLowerCase();
                    boolean isMarried = status.equals("married");
                    String password = parts[4].trim();
                    
                    
                    Officer officer = new Officer(name, nric, age, isMarried, password);
                 // Load multiple registered projects and statuses
                    String[] projectNames = parts[5].split(";");
                    String[] statusStrings = parts[6].split(";");

                    for (int i = 0; i < projectNames.length; i++) {
                        String projectName = projectNames[i].trim();
                        if (!projectName.isEmpty()) {
                            Project project = projectManager.findProjectByName(projectName);
                            if (project != null) {
                                OfficerRegistrationStatus status1 = OfficerRegistrationStatus.PENDING;
                                if (i < statusStrings.length && !statusStrings[i].trim().isEmpty()) {
                                    try {
                                        status1 = OfficerRegistrationStatus.valueOf(statusStrings[i].trim());
                                    } catch (IllegalArgumentException e) {
                                        System.err.println("Invalid status for project " + projectName + ": " + statusStrings[i]);
                                    }
                                }
                                officer.addRegisteredProject(project, status1);
                            } else {
                                System.err.println("Project not found: " + projectName + " for officer " + nric);
                            }
                        }
                    }

                    officers.add(officer);
                } else {
                    System.err.println("Skipping malformed line in " + FILE_PATH + ": " + line);
                }
            } catch (Exception e) {
                System.err.println("Error loading officer from line: " + line);
                e.printStackTrace();
            }
        }
    }

    /**
     * Saves the current list of Officer objects to the CSV file.
     * Overwrites the existing file. Formats officer details including semicolon-separated
     * lists of registered project names and their statuses.
     * Format: Name,NRIC,Age,Status,Password,RegisteredProjects,RegistrationStatuses
     */
    @Override
    public void saveUsers() {
        List<String> lines = new ArrayList<>();
        lines.add("Name,NRIC,Age,Status,Password,RegisteredProjects,RegistrationStatuses");
        for (Officer officer : officers) {
            String marriedStatus = officer.isMarried() ? "married" : "single";
            List<Project> projects = officer.getRegisteredProjects();
            List<String> projNames = new ArrayList<>();
            List<String> projStatuses = new ArrayList<>();
            for (Project p : projects) {
                projNames.add(p.getName());
                OfficerRegistrationStatus regStatus = officer.getRegistrationStatusForProject(p);
                projStatuses.add(regStatus != null ? regStatus.name() : "");
            }

            lines.add(String.join(",",
                officer.getName(),
                officer.getNRIC(),
                String.valueOf(officer.getAge()),
                marriedStatus,
                officer.getPassword(),
                String.join(";", projNames),
                String.join(";", projStatuses)
            ));
        }
        FileManager.writeFile(FILE_PATH, lines);
    }

    /**
     * Retrieves the current in-memory list of all loaded officers.
     *
     * @return A List containing all Officer objects.
     */
    @Override
    public List<Officer> getUsers() {
        return officers;
    }

    /**
     * Finds and returns an officer based on their NRIC.
     *
     * @param nric The NRIC of the officer to find.
     * @return The Officer object if found, or null otherwise.
     */
    @Override
    public Officer findByNRIC(String nric) {
        for (Officer officer : officers) {
            if (officer.getNRIC().equalsIgnoreCase(nric)) {
                return officer;
            }
        }
        return null;
    }

    /**
     * Changes the password for the officer identified by the given NRIC.
     * Saves the updated officer list if the change is successful.
     *
     * @param nric        The NRIC of the officer whose password should be changed.
     * @param newPassword The new password to set.
     * @return true if the password was successfully updated and saved, false otherwise.
     */
    @Override
    public boolean changePassword(String nric, String newPassword) {
        Officer user = findByNRIC(nric);
        if (user != null) {
            if (user.changePass(newPassword)) {
                saveUsers();
                return true;
            } else {
                 return false;
            }
        }
         System.err.println("Officer not found for password change: " + nric);
        return false;
    }
    
    /**
     * Updates the list of assigned officers in the `ProjectList.csv` file for a given project.
     * Appends the officer's name (if not already present) to the 'Officer Name' column
     * for the specified project row.
     *
     * @param project     The project whose officer list needs updating in the CSV.
     * @param officerName The name of the officer to add to the project's officer list.
     */
    public void updateProjectListCSV(Project project, String officerName) {
        List<String> lines = FileManager.readFile("data/ProjectList.csv");
        if (lines == null || lines.size() <= 1) return;

        List<String> updatedLines = new ArrayList<>();
        updatedLines.add(lines.get(0)); // header

        for (String line : lines.subList(1, lines.size())) {
            String[] parts = line.split(",", -1);
            if (parts.length >= 1 && parts[0].trim().equalsIgnoreCase(project.getName())) {
                if (parts.length >= 10) {
                    String existing = parts[9].trim();
                    if (!existing.contains(officerName)) {
                        parts[9] = existing.isEmpty() ? officerName : existing + ";" + officerName;
                    }
                }
                updatedLines.add(String.join(",", parts));
            } else {
                updatedLines.add(line);
            }
        }

        FileManager.writeFile("data/ProjectList.csv", updatedLines);
    }
}

