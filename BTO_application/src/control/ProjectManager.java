package control;

import entities.Officer;
import entities.Project;
import entities.Room;
import enums.RoomType;
import utils.FileManager;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

/**
 * Manages the collection of Project objects within the system.
 * Handles loading projects from `ProjectList.csv`, saving changes back,
 * adding, deleting, finding projects, and modifying project attributes like
 * visibility, room availability, and assigned officers.
 */
public class ProjectManager {

    private List<Project> projects = new ArrayList<>();
    private static final String FILE_PATH = "data/ProjectList.csv";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("d/M/yy");

    /**
     * Loads project data from the specified CSV file path.
     * Clears existing projects.
     * Automatically sets project visibility to false if the project is outside its application date range upon loading.
     * Handles file reading and parsing errors.
     *
     * @param filePath The path to the CSV file containing project data.
     */
    public void loadProjects(String filePath) {
        projects.clear();
        List<String> lines = FileManager.readFile(filePath);
        if (lines == null || lines.size() < 2) {
            System.err.println("No project data found or file is empty in " + filePath);
            return;
        }
        for (String line : lines.subList(1, lines.size())) {
            try {
                // Delegate parsing to the Project class itself for better encapsulation
                Project p = Project.fromCSV(line);
                if (p != null) {
                	//Update visibility, if not active return True
                	if (!isProjectActive(p)) {
                		p.setVisibility(false);
                		System.out.println("Project " + p.getName() + " visibility set to false as it is outside date range.");
                	}
                    projects.add(p);
                } else {
                    System.err.println("Skipping line due to parsing error (Project.fromCSV returned null): " + line);
                }
            } catch (DateTimeParseException e) {
                 System.err.println("Error parsing date in project from line: " + line + " - " + e.getMessage());
            } catch (NumberFormatException e) {
                 System.err.println("Error parsing number (units, price, slots) in project from line: " + line + " - " + e.getMessage());
            } catch (IllegalArgumentException e) {
                 System.err.println("Error parsing enum (RoomType?) in project from line: " + line + " - " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Error parsing project from line: " + line);
                e.printStackTrace();
            }
        }
         System.out.println("Project data loaded from " + filePath);
         saveProjects(FILE_PATH);
    }

    /**
     * Adds a new project to the manager's list if it's not null and its name doesn't already exist.
     *
     * @param project The Project object to add.
     * @return true if the project was successfully added, false otherwise.
     */
    public boolean addProject(Project project) {
        if (project != null && findProjectByName(project.getName()) == null) {
             projects.add(project);
             return true;
        } else if (project == null) {
             System.err.println("Cannot add a null project.");
             return false;
        } else {
             System.err.println("Project with name '" + project.getName() + "' already exists.");
             return false;
        }
    }

    /**
     * Retrieves the current in-memory list of all managed projects.
     *
     * @return A List containing all Project objects.
     */
    public List<Project> getProjects() {
        return projects;
    }

    /**
     * Finds and returns a project based on its name.
     *
     * @param projectName The name of the project to find.
     * @return The Project object if found, or null otherwise.
     */
    public Project findProjectByName(String projectName) {
        if (projectName == null || projectName.trim().isEmpty()) {
            return null;
        }
        for (Project p : projects) {
            if (p.getName().equalsIgnoreCase(projectName.trim())) {
                return p;
            }
        }
        return null;
    }

    /**
     * Saves the current list of projects to the specified CSV file path.
     * Overwrites the existing file.
     *
     * @param filePath The path to the CSV file where project data should be saved.
     */
    public void saveProjects(String filePath) {
        List<String> lines = new ArrayList<>();
        lines.add("Project Name,Neighbourhood,Type 1,Num Units 1,Available Units 1,Price 1,Type 2,Num Units 2,Available Units 2,Price 2,Open Date,Close Date,Manager Name,Officer Slots,Officer Name,Visibility");
        for (Project p : projects) {
            lines.add(toCSV(p));
        }
        FileManager.writeFile(filePath, lines);
    }

    /**
     * Checks if a project's application period is currently active (today's date is within open/close dates).
     *
     * @param project The project to check.
     * @return true if the project has valid dates and today is within the range, false otherwise.
     */
    private boolean isProjectActive(Project project) {
    	LocalDate today = LocalDate.now();
    	if (project.getOpenDate()== null || project.getCloseDate()==null) {
    		return true; //default visible if dates not specified
    	}
    	//if within open and close dates -> return True
    	return !today.isBefore(project.getOpenDate()) && !today.isAfter(project.getCloseDate());
    }
    
    /**
     * Sets the visibility status for a project identified by its name.
     * Saves the updated project list afterwards.
     *
     * @param projectName The name of the project to modify.
     * @param visible     The new visibility status (true for visible, false for hidden).
     * @return true if the project was found and visibility was set, false otherwise.
     */
    public boolean setProjectVisibility(String projectName, boolean visible) {
        Project p = findProjectByName(projectName);
        if (p != null) {
            p.setVisibility(visible);
            System.out.println("Visibility for project '" + projectName + "' set to " + visible );
        
	        saveProjects(FILE_PATH);
	        
	        return true;
    	}
    
         System.err.println("Project '" + projectName + "' not found for visibility toggle.");
        return false;
    }

    /**
     * Deletes a project identified by its name from the in-memory list.
     *
     * @param projectName The name of the project to delete.
     * @return true if the project was found and removed, false otherwise.
     */
    public boolean deleteProject(String projectName) {
         Iterator<Project> iterator = projects.iterator();
         while (iterator.hasNext()) {
             Project p = iterator.next();
             if (p.getName().equalsIgnoreCase(projectName)) {
                 iterator.remove();
                 return true;
             }
         }
         System.err.println("Project '" + projectName + "' not found for deletion.");
         return false;
    }

    /**
     * Updates the available room count for a specific room type within a given project.
     * Handles both incrementing and decrementing the count. Saves the project list after a successful update.
     *
     * @param project   The project containing the room to update.
     * @param roomType  The RoomType whose availability should be changed.
     * @param change    The amount to change the availability by (+1 to increment, -1 to decrement).
     * @return true if the project and room type were found and the availability was successfully updated, false otherwise.
     */
    public boolean updateRoomAvailability(Project project, RoomType roomType, int change) {
        if (project == null) {
             System.err.println("Cannot update room availability for a null project.");
             return false;
        }

        Project managedProject = findProjectByName(project.getName());
        if (managedProject == null) {
             System.err.println("Project '" + project.getName() + "' not found in ProjectManager's list.");
             return false;
        }


        Room targetRoom = null;
        for (Room r : managedProject.getRooms()) {
            if (r.getRoomType() == roomType) {
                targetRoom = r;
                break;
            }
        }

        if (targetRoom == null) {
            System.err.println("Room type " + roomType + " not found in project '" + managedProject.getName() + "'.");
            return false;
        }

        boolean success = false;
        if (change < 0) { // Decrement
            success = true;
            for (int i = 0; i < -change; i++) {
                if (!targetRoom.decrementAvailableRooms()) {
                    success = false;
                    break;
                }
            }
        } else if (change > 0) { // Increment
            success = true;
            for (int i = 0; i < change; i++) {
                if (!targetRoom.incrementAvailableRooms()) {
                    success = false;
                    break;
                }
            }
        } else { // change == 0
            success = true;
        }

        if (success && change != 0) {
            System.out.println("Available units for " + roomType + " in project '" + managedProject.getName() + "' updated. New count: " + targetRoom.getAvailableRooms());
            saveProjects(FILE_PATH);
        } else if (!success && change !=0 ){
             System.out.println("Update to available units failed for " + roomType + " in project '" + managedProject.getName() + "'. Count remains: " + targetRoom.getAvailableRooms());
        }

        return success;
    }

    /**
     * Assigns an officer's name to a project.
     * Finds the project by name and updates its officer field.
     * Note: Does not automatically save changes.
     *
     * @param projectName The name of the project.
     * @param officerName The name of the officer to assign (can be null or empty to clear).
     * @return true if the project was found and updated, false otherwise.
     */
    public boolean assignOfficerToProject(String projectName, String officerName) {
    	Project project = findProjectByName(projectName);
    	if(project != null) {
            project.setOfficer(officerName);
            System.out.println("Officer '" + (officerName == null ? "<none>" : officerName) + "' assigned to project '" + projectName);
            return true;
        }
        System.err.println("Project '" + projectName + "' not found for officer assignment.");
        return false;
    }
    
    /**
     * Checks if a manager already handles another project with an overlapping application period.
     * Used to prevent a manager from creating/editing a project that conflicts with their existing assignments.
     *
     * @param currentProject The project being created or edited (null if creating). Used to exclude self-comparison.
     * @param managerName    The name of the manager.
     * @param newOpenDate    The proposed opening date for the project.
     * @param newCloseDate   The proposed closing date for the project.
     * @return true if a date conflict exists with another project managed by the same manager, false otherwise.
     */
    public boolean hasDateConflict(Project currentProject, String managerName, LocalDate newOpenDate, LocalDate newCloseDate) {
        for (Project p : projects) {
            if (!p.equals(currentProject) && p.getManager().equalsIgnoreCase(managerName)) {
                // Check for overlapping date ranges
                boolean overlap = !(newCloseDate.isBefore(p.getOpenDate()) || newOpenDate.isAfter(p.getCloseDate()));
                if (overlap) return true;
            }
        }
        return false;
    }

    /**
     * Converts a Project object into a CSV-formatted string line.
     * Handles formatting of different data types (String, int, double, LocalDate, boolean, List<Room>).
     * Uses the defined DATE_FORMATTER. Escapes fields containing commas or quotes.
     *
     * @param p The Project object to convert.
     * @return A string representing the project in CSV format according to the defined header. Returns an empty string if the project is null.
     */
    private String toCSV(Project p) {
        if (p == null) return "";
        List<Room> rooms = p.getRooms();
        String type1 = "", totalUnits1 = "0", availUnits1 = "0", price1 = "0.0";
        String type2 = "", totalUnits2 = "0", availUnits2 = "0", price2 = "0.0";

        if (rooms != null) {
            for(Room r : rooms) {
                 if (r.getRoomType() == RoomType.TwoRoom) {
                     type1 = r.getRoomType().name();
                     totalUnits1 = String.valueOf(r.getTotalRooms());
                     availUnits1 = String.valueOf(r.getAvailableRooms());
                     price1 = String.valueOf(r.getPrice());
                 } else if (r.getRoomType() == RoomType.ThreeRoom) {
                     type2 = r.getRoomType().name();
                     totalUnits2 = String.valueOf(r.getTotalRooms());
                     availUnits2 = String.valueOf(r.getAvailableRooms());
                     price2 = String.valueOf(r.getPrice());
                 }
            }
        }

        String openDateStr = p.getOpenDate() != null ? p.getOpenDate().format(DATE_FORMATTER) : "";
        String closeDateStr = p.getCloseDate() != null ? p.getCloseDate().format(DATE_FORMATTER) : "";

        String managerName = p.getManager() != null ? p.getManager() : "";
        String officerName = p.getOfficer() != null ? p.getOfficer() : "";
        String visibility = String.valueOf(p.isVisibility());

        return String.join(",",
                escapeCsvField(p.getName()),
                escapeCsvField(p.getNeighbourhood()),
                escapeCsvField(type1),
                totalUnits1,
                availUnits1,
                price1,
                escapeCsvField(type2),
                totalUnits2,
                availUnits2,
                price2,
                escapeCsvField(openDateStr),
                escapeCsvField(closeDateStr),
                escapeCsvField(managerName),
                String.valueOf(p.getOfficerSlot()),
                escapeCsvField(officerName),
                visibility
        );
    }

    /**
     * Escapes a string field for CSV output if necessary.
     * Adds double quotes around the field and escapes internal double quotes
     * if the field contains commas, double quotes, or newline characters.
     *
     * @param field The string field to escape.
     * @return The escaped string, or an empty string if the input field is null.
     */
    private String escapeCsvField(String field) {
        if (field == null) {
            return "";
        }
        // If the field contains a comma, double quote, or newline, it needs escaping
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            // Replace internal double quotes with two double quotes
            String escapedField = field.replace("\"", "\"\"");
            // Wrap the entire field in double quotes
            return "\"" + escapedField + "\"";
        }
        return field;
    }
}