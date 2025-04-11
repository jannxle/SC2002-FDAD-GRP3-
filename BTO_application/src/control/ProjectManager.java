package control;

import entities.Officer;
import entities.Project;
import entities.Room;
import enums.RoomType;
import utils.FileManager;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ProjectManager {
    private List<Project> projects = new ArrayList<>();

    /**
     * Loads projects from the CSV file.
     * Expects the CSV file to have a header followed by lines in this format:
     * Project Name, Neighbourhood, Type 1, Number of units for Type 1, Selling price for Type 1,
     * Type 2, Number of units for Type 2, Selling price for Type 2, Application opening date,
     * Application closing date, Manager, Officer Slot, Officer
     *
     * @param filePath The path to the CSV file.
     */
    public void loadProjects(String filePath) {
        List<String> lines = FileManager.readFile(filePath);
        if (lines == null || lines.size() < 2) {
            System.out.println("No project data found in " + filePath);
            return;
        }
        // Skip header (first line)
        for (String line : lines.subList(1, lines.size())) {
            try {
                Project p = Project.fromCSV(line);
                projects.add(p);
            } catch (Exception e) {
                System.err.println("Error parsing project from line: " + line);
                e.printStackTrace();
            }
        }
    }

    
    /**
     * Adds a new project to the list.
     *
     * @param project The Project object to add.
     */
    public void addProject(Project project) {
        projects.add(project);
    }

    
    /**
     * Returns the list of projects.
     *
     * @return List of projects.
     */
    public List<Project> getProjects() {
        return projects;
    }
    
    
    /**
     * Finds a project by its name.
     *
     * @param projectName The name of the project.
     * @return The matching Project, or null if not found.
     */
    public Project findProjectByName(String projectName) {
        for (Project p : projects) {
            if (p.getName().equalsIgnoreCase(projectName)) {
                return p;
            }
        }
        return null;
    }
    
    /**
     * Returns a list of projects available for officer registration.
     * A project is available if the number of officers already registered is less than the available officer slots.
     *
     * @return List of projects that can still accept additional officers.
     */
    public List<Project> getAvailableProjects() {
        return projects.stream()
                .filter(p -> {
                    String officerField = p.getOfficer();
                    int assignedCount = 0;
                    if (officerField != null && !officerField.trim().isEmpty()) {
                        // Count officers by splitting on semicolon and trimming whitespace
                        assignedCount = (int) Arrays.stream(officerField.split(";"))
                                                    .filter(s -> !s.trim().isEmpty())
                                                    .count();
                    }
                    return assignedCount < p.getOfficerSlot();
                })
                .collect(Collectors.toList());
    }
    
    
    /**
     * Saves the current list of projects back to the CSV file.
     *
     * @param filePath The path to save the CSV file.
     */
    public void saveProjects(String filePath) {
        List<String> lines = new ArrayList<>();
        // Write header (make sure it matches your CSV format)
        lines.add("Project Name,Neighbourhood,Type 1,Number of units for Type 1,Selling price for Type 1," +
                  "Type 2,Number of units for Type 2,Selling price for Type 2," +
                  "Application opening date,Application closing date,Manager,Officer Slot,Officer");
        for (Project p : projects) {
            // Convert the project to a CSV line.
            // Since your Project class doesn't have a toCSV() method, we build the line here.
            lines.add(toCSV(p));
        }
        FileManager.writeFile(filePath, lines);
    }
    
    /**
     * Toggles the visibility of a project.
     */
    public boolean toggleProjectVisibility(String projectName, boolean visibility) {
        Project p = findProjectByName(projectName);
        if (p != null) {
            p.setVisibility(visibility);
            return true;
        }
        return false;
    }   
    
    /**
     * Deletes a project by name.
     */
    public boolean deleteProject(String projectName) {
        Project p = findProjectByName(projectName);
        if (p != null) {
            projects.remove(p);
            return true;
        }
        return false;
    }
    
    /**
     * Updates room availability for a given project and room type.
     * The 'change' parameter can be negative (decrement) or positive (increment).
     */
    public boolean updateRoomAvailability(Project project, RoomType roomType, int change) {
        if (project == null) return false;
        for (Room r : project.getRooms()) {
            if (r.getRoomType() == roomType) {
                if (change < 0) { //means want to decrement, input e.g: -1 
                    for (int i = 0; i < -change; i++) {
                        if (!r.decrementAvailableRooms()) {
                            return false;
                        }
                    } 
                } else {  //means want to increment, input e.g: +1
                    for (int i = 0; i < change; i++) {
                        if (!r.incrementAvailableRooms()) {
                            return false;
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }
    
    //Assigns an officer to a project (for Manager)
    public boolean assignOfficerToProject(String projectName, String officerName) {
    	Project project = findProjectByName(projectName);
    	if(project != null) {
    		String officerField = project.getOfficer();
    		List<String> officerList = new ArrayList<>();
    		if (officerField != null && !officerField.trim().isEmpty()) {
    			officerList = Arrays.stream(officerField.split(";"))
    							.map(String::trim)
    							.filter(s -> !s.isEmpty())
    							.collect(Collectors.toList());
    		}
    		
            // Check if there is an available officer slot.
            if (officerList.size() >= project.getOfficerSlot()) {
                System.out.println("No officer slot available for project " + projectName);
                return false;
            }
            // Assign the officer by adding to the semicolon-separated list.
            if (officerField == null || officerField.trim().isEmpty()) {
                project.setOfficer(officerName);
            } else {
                project.setOfficer(officerField + ";" + officerName);
            }
            System.out.println("Officer " + officerName + " assigned to project " + projectName);
            return true;
        }
        System.out.println("Project " + projectName + " not found.");
        return false;

    }
    
    public void updateOfficerProject(Officer officer) {
        for (Project project : projects) {
            String officerField = project.getOfficer();
            if (officerField != null && !officerField.isEmpty()) {
                List<String> officerList = Arrays.stream(officerField.split(";"))
                                                 .map(String::trim)
                                                 .filter(s -> !s.isEmpty())
                                                 .collect(Collectors.toList());
                // If the officer is registered in this project
                if (officerList.stream().anyMatch(name -> name.equalsIgnoreCase(officer.getName()))) {
                    officer.setAppliedProject(project);
                    // If an officer is only allowed to be assigned to one project, break out.
                    break;
                }
            }
        }
    }
    
    public void linkOfficersToProject(UserManager userManager) {
        for (Project p : projects) {
            String officerField = p.getOfficer();
            if (officerField != null && !officerField.isEmpty()) {
                // Split on semicolon and trim each name.
                String[] officerNames = officerField.split(";");
                for (String name : officerNames) {
                    name = name.trim();
                    if (!name.isEmpty()) {
                        // Make sure findOfficerByName also uses case-insensitive comparison.
                        Officer officer = userManager.findOfficerByName(name);
                        if (officer != null) {
                            // Set the registered project for the officer
                            officer.setRegisteredProject(p);
                        } else {
                            System.out.println("Officer " + name + " not found in user manager.");
                        }
                    }
                }
            }
        }
    }
    
    
    /**
     * Helper method to convert a Project object into a CSV-formatted string.
     */
    private String toCSV(Project p) {
        // Each project has exactly two Room objects.
        List<Room> rooms = p.getRooms();
        String type1 = "";
        String units1 = "";
        String price1 = "";
        String type2 = "";
        String units2 = "";
        String price2 = "";
        if (rooms != null && rooms.size() >= 2) {
            Room r1 = rooms.get(0);
            Room r2 = rooms.get(1);
            type1 = (r1.getRoomType() == RoomType.TwoRoom ? "2-Room" : "3-Room");
            units1 = String.valueOf(r1.getTotalRooms());
            price1 = String.valueOf(r1.getPrice());
            type2 = (r2.getRoomType() == RoomType.TwoRoom ? "2-Room" : "3-Room");
            units2 = String.valueOf(r2.getTotalRooms());
            price2 = String.valueOf(r2.getPrice());
        }
        // Format the dates using the same pattern as in your Project class.
        String openDate = p.getOpenDate().format(DateTimeFormatter.ofPattern("dd/M/yy"));
        String closeDate = p.getCloseDate().format(DateTimeFormatter.ofPattern("dd/M/yy"));
        // Officer field is not managed here, so we leave it blank.
        String officerStr = p.getOfficer();
        
        return p.getName() + "," +
               p.getNeighbourhood() + "," +
               type1 + "," + units1 + "," + price1 + "," +
               type2 + "," + units2 + "," + price2 + "," +
               openDate + "," + closeDate + "," +
               p.getManager() + "," + p.getOfficerSlot() + "," +
               officerStr;
    }
}
