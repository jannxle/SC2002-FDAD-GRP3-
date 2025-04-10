package control;

import entities.Officer;
import entities.Project;
import entities.Room;
import enums.RoomType;
import utils.FileManager;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

public class ProjectManager {

    private List<Project> projects = new ArrayList<>();
    private static final String FILE_PATH = "data/ProjectList.csv";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("d/M/yy");

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
    }

    public boolean addProject(Project project) {
        if (project != null && findProjectByName(project.getName()) == null) {
             projects.add(project);
             System.out.println("Project '" + project.getName() + "' added to memory.");
             return true;
        } else if (project == null) {
             System.err.println("Cannot add a null project.");
             return false;
        } else {
             System.err.println("Project with name '" + project.getName() + "' already exists.");
             return false;
        }
    }

    public List<Project> getProjects() {
        return projects;
    }

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

    public void saveProjects(String filePath) {
        List<String> lines = new ArrayList<>();
         lines.add("Project Name,Neighbourhood,Type 1,Num Units 1,Price 1,Type 2,Num Units 2,Price 2,Open Date,Close Date,Manager NRIC,Officer Slots,Officer Name,Visibility");
        for (Project p : projects) {
            lines.add(toCSV(p));
        }
        FileManager.writeFile(filePath, lines);
        System.out.println("Project data saved to " + filePath);
    }

    public boolean setProjectVisibility(String projectName, boolean visible) {
        Project p = findProjectByName(projectName);
        if (p != null) {
            p.setVisibility(visible);
            System.out.println("Visibility for project '" + projectName + "' set to " + visible + " in memory.");
            return true;
        }
         System.err.println("Project '" + projectName + "' not found for visibility toggle.");
        return false;
    }

    public boolean deleteProject(String projectName) {
         Iterator<Project> iterator = projects.iterator();
         while (iterator.hasNext()) {
             Project p = iterator.next();
             if (p.getName().equalsIgnoreCase(projectName)) {
                 iterator.remove();
                 System.out.println("Project '" + projectName + "' removed from memory.");
                 return true;
             }
         }
         System.err.println("Project '" + projectName + "' not found for deletion.");
         return false;
    }


    public boolean updateRoomAvailability(Project project, RoomType roomType, int change) {
        if (project == null) {
             System.err.println("Cannot update room availability for a null project.");
             return false;
        }
        Room targetRoom = null;
        for (Room r : project.getRooms()) {
            if (r.getRoomType() == roomType) {
                targetRoom = r;
                break;
            }
        }

        if (targetRoom == null) {
             System.err.println("Room type " + roomType + " not found in project '" + project.getName() + "'.");
             return false;
        }

        boolean success = false;
        if (change < 0) { // Decrement (Booking)
            int count = -change;
            success = true; // Assume success initially
            for (int i = 0; i < count; i++) {
                if (!targetRoom.decrementAvailableRooms()) { // Assuming Room.decrementAvailableRooms returns boolean success
                    System.err.println("Failed to decrement available rooms for " + roomType + " in project '" + project.getName() + "'. No more rooms available?");
                    // Optional: revert any decrements made in this loop if partial success is not desired
                    success = false;
                    break;
                }
            }
        } else if (change > 0) { // Increment (Withdrawn)
             int count = change;
             success = true; // Assume success initially
             for (int i = 0; i < count; i++) {
                 if (!targetRoom.incrementAvailableRooms()) { // Assuming Room.incrementAvailableRooms returns boolean success
                    System.err.println("Failed to increment available rooms for " + roomType + " in project '" + project.getName() + "'. Already at maximum?");
                    // Optional: revert any increments made in this loop if partial success is not desired
                    success = false;
                    break;
                 }
             }
        } else {
             // Change is zero, do nothing but consider it successful.
             success = true;
        }

        if (success && change != 0) {
             System.out.println("Room availability for " + roomType + " in project '" + project.getName() + "' updated by " + change + " in memory.");
        }
        return success;
    }


    public boolean assignOfficerToProject(String projectName, String officerName) {
    	Project project = findProjectByName(projectName);
    	if(project != null) {
            project.setOfficer(officerName);
            System.out.println("Officer '" + (officerName == null ? "<none>" : officerName) + "' assigned to project '" + projectName + "' in memory.");
            return true;
        }
        System.err.println("Project '" + projectName + "' not found for officer assignment.");
        return false;
    }

    private String toCSV(Project p) {
        if (p == null) return "";
        List<Room> rooms = p.getRooms();
        String type1 = "", units1 = "", price1 = "";
        String type2 = "", units2 = "", price2 = "";

        if (rooms != null && !rooms.isEmpty()) {
             Room r1 = rooms.get(0);
             type1 = r1.getRoomType() != null ? r1.getRoomType().name() : "";
             units1 = String.valueOf(r1.getTotalRooms());
             price1 = String.valueOf(r1.getPrice());
             if (rooms.size() >= 2) {
                Room r2 = rooms.get(1);
                type2 = r2.getRoomType() != null ? r2.getRoomType().name() : "";
                units2 = String.valueOf(r2.getTotalRooms());
                price2 = String.valueOf(r2.getPrice());
             }
        }

        // Format dates using the defined formatter
        String openDateStr = p.getOpenDate() != null ? p.getOpenDate().format(DATE_FORMATTER) : "";
        String closeDateStr = p.getCloseDate() != null ? p.getCloseDate().format(DATE_FORMATTER) : "";

        // Get other fields, handling potential nulls
        String managerNRIC = p.getManager() != null ? p.getManager() : "";
        String officerName = p.getOfficer() != null ? p.getOfficer() : "";
        String visibility = String.valueOf(p.isVisibility());

        return String.join(",",
                escapeCsvField(p.getName()),
                escapeCsvField(p.getNeighbourhood()),
                escapeCsvField(type1), units1, price1,
                escapeCsvField(type2), units2, price2,
                escapeCsvField(openDateStr),
                escapeCsvField(closeDateStr),
                escapeCsvField(managerNRIC),
                String.valueOf(p.getOfficerSlot()),
                escapeCsvField(officerName),
                visibility
        );
    }


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