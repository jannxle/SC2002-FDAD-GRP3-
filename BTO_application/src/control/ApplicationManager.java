package control;

import java.util.ArrayList;
import java.util.List;

import entities.Applicant;
import entities.Project;
import entities.Room;
import enums.RoomType;
import utils.FileManager;
import enums.ApplicationStatus;

//This class handles the process of applying for a project, 
//withdrawing an application, and (for later steps) booking a flat. 
//It updates the applicant’s application status and 
//(if needed) the project’s room availability.
public class ApplicationManager {
	//process an application by applicant for a given project and chosen flat type
	//returns true if application submitted successfully (STATUS set to PENDING)
	//to call in ApplicantUI
	public boolean apply(Applicant applicant, Project project, RoomType chosenRoom) {
		//each applicant can only apply for one
		if (applicant.getAppliedProject()!=null) {
			System.out.println("You have already applied for a project.");
			System.out.println("Each user is only allowed to apply for one project.");
			return false;
		}
		Room selectedRoom = null;
		for (Room room : project.getRooms()) {
			if (room.getRoomType() == chosenRoom && room.getAvailableRooms()>0) {
				selectedRoom = room;
				break;
			}
		}
		if (selectedRoom == null) {
			System.out.println("Selected Room type is not available in this project.");
		}
        // Update the applicant’s application details.
        applicant.setAppliedProject(project);
        applicant.setRoomChosen(chosenRoom);
        applicant.setStatus(ApplicationStatus.PENDING);
        return true;
	}
	
	public boolean withdraw(Applicant applicant) {
		if(applicant.getAppliedProject() == null) {
			System.out.println("You have no Applications to withdraw from");
			return false;
		}
		applicant.setAppliedProject(null);
		applicant.setRoomChosen(null);
		applicant.setStatus(null);
		return true;
	}
	
    /**
     * Books a flat for the applicant.
     * (Typically triggered by the HDB Officer after a successful application.)
     */
    public boolean bookFlat(Applicant applicant) {
        if (applicant.getStatus() != ApplicationStatus.SUCCESSFUL) {
            System.out.println("Your application is not approved for booking.");
            return false;
        }
        
        Project project = applicant.getAppliedProject();
        if (project == null) {
            System.out.println("No project found.");
            return false;
        }
        
        Room selectedRoom = null;
        for (Room r : project.getRooms()) {
            if (r.getRoomType() == applicant.getRoomChosen()) {
                selectedRoom = r;
                break;
            }
        }
        if (selectedRoom != null && selectedRoom.decrementAvailableRooms()) {
            applicant.setStatus(ApplicationStatus.BOOKED);
            System.out.println("Flat booked successfully.");
            return true;
        }
        
        System.out.println("Booking failed: flat unavailable.");
        return false;
    }
	
    public void saveApplications(String filePath, List<Applicant> applicants) {
        List<String> lines = new ArrayList<>();
        // CSV header
        lines.add("NRIC,Name,ProjectName,RoomType,Status");
        
        for (Applicant a : applicants) {
            if (a.getAppliedProject() != null) { // Only save if an application exists
                String line = a.getNRIC() + "," +
                              a.getName() + "," +
                              a.getAppliedProject().getName() + "," +
                              a.getRoomChosen() + "," +
                              (a.getStatus() != null ? a.getStatus() : "");
                lines.add(line);
            }
        }
        FileManager.writeFile(filePath, lines);
    }
    public void loadApplications(String filePath, List<Applicant> applicants, List<Project> projects) {
        List<String> lines = FileManager.readFile(filePath);
        if (lines == null || lines.size() <= 1) {
            return;
        }
        
        // Skip header (first line)
        for (String line : lines.subList(1, lines.size())) {
            String[] parts = line.split(",", 5);
            if (parts.length >= 5) {
                String nric = parts[0].trim();
                String projectName = parts[2].trim();
                String roomTypeStr = parts[3].trim();
                String statusStr = parts[4].trim();
                
                // Find applicant by NRIC
                for (Applicant a : applicants) {
                    if (a.getNRIC().equalsIgnoreCase(nric)) {
                        // Find matching project by name from your list of projects
                        for (Project p : projects) {
                            if (p.getName().equalsIgnoreCase(projectName)) {
                                a.setAppliedProject(p);
                                break;
                            }
                        }
                        // Set room type and status
                        a.setRoomChosen(RoomType.valueOf(roomTypeStr));;
                        a.setStatus(ApplicationStatus.valueOf(statusStr));
                        break;
                    }
                }
            }
        }
    }
}
