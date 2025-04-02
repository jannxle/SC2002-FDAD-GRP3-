package control;

import entities.Officer;
import entities.Project;
import entities.Room;
import enums.RoomType;
import utils.FileManager;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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
    
    //Assigns an officer to a project
    public boolean assignOfficerToProject(String projectName, String officerName) {
    	Project project = findProjectByName(projectName);
    	if(project != null) {
    		// Check if an officer is already assigned
            if (project.getOfficer() != null && !project.getOfficer().isEmpty()) {
                System.out.println("Project " + projectName + " already has an officer assigned: " + project.getOfficer());
                return false;
            }
            // Optionally check if the project has an available officer slot
            if (project.getOfficerSlot() <= 0) {
                System.out.println("No officer slot available for project " + projectName);
                return false;
            }
            // Assign the officer
            project.setOfficer(officerName);
            System.out.println("Officer " + officerName + " assigned to project " + projectName);
            return true;
        }
        System.out.println("Project " + projectName + " not found.");
        return false;
    }
    
    public void updateOfficerProject(Officer officer) {
        for (Project project : projects) {
            if (project.getOfficer() != null && !project.getOfficer().isEmpty() &&
                project.getOfficer().equalsIgnoreCase(officer.getName())) {
                officer.setAppliedProject(project);
                // Optionally, if an officer can only have one project, you might break here.
                break;
            }
        }
    }
    
    
    /**
     * Helper method to convert a Project object into a CSV-formatted string.
     */
    private String toCSV(Project p) {
        // Assume each project has exactly two Room objects.
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
        String openDate = p.getOpenDate().format(DateTimeFormatter.ofPattern("d/M/yy"));
        String closeDate = p.getCloseDate().format(DateTimeFormatter.ofPattern("d/M/yy"));
        // Officer field is not managed here, so we leave it blank.
        String officer = "";
        
        return p.getName() + "," +
               p.getNeighbourhood() + "," +
               type1 + "," + units1 + "," + price1 + "," +
               type2 + "," + units2 + "," + price2 + "," +
               openDate + "," + closeDate + "," +
               p.getManager() + "," + p.getOfficerSlot() + "," +
               officer;
    }
}
