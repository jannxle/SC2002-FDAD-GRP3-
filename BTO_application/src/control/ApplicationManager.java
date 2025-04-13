package control;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import entities.Applicant;
import entities.Officer;
import entities.Project;
import entities.Room;
import enums.RoomType;
import utils.FileManager;
import enums.ApplicationStatus;

/**
 * Manages the lifecycle of BTO applications
 */
public class ApplicationManager {

    private static final String APPLICATIONS_FILE_PATH = "data/Applications.csv";
    private final ProjectManager projectManager;
    private final UserManager<Applicant> applicantUserManager;
    private UserManager<Officer> officerUserManager;

    public ApplicationManager(ProjectManager projectManager, UserManager<Applicant> applicantUserManager, UserManager<Officer> officerUserManager) {
        if (projectManager == null || applicantUserManager == null) {
             throw new IllegalArgumentException("ProjectManager and ApplicantUserManager cannot be null.");
        }
        this.projectManager = projectManager;
        this.applicantUserManager = applicantUserManager;
        this.officerUserManager = officerUserManager;
    }

    public List<Applicant> getAllApplicants() {
        List<Applicant> all = new ArrayList<>();
        all.addAll(applicantUserManager.getUsers());
        all.addAll(officerUserManager.getUsers());
        return all;
    }
    
    public boolean apply(Applicant applicant, Project project, RoomType chosenRoom) {
        if (applicant == null || project == null || chosenRoom == null) {
             System.err.println("Application failed: Applicant, project, or chosen room cannot be null.");
             return false;
        }

        // Rule: Each applicant can only apply for one project at a time unless previous is UNSUCCESSFUL.
        if (applicant.getStatus() != null && applicant.getStatus() != ApplicationStatus.UNSUCCESSFUL) {
            System.out.println("Application failed: You already have an active or successful application (Status: " + applicant.getStatus() + ").");
            System.out.println("Please withdraw your existing application before applying for a new one.");
            return false;
        }

        // Check if the chosenRoom type exists in the project.
        boolean roomTypeExists = false;
        for (Room room : project.getRooms()) { //
            if (room.getRoomType() == chosenRoom) { //
                roomTypeExists = true;
                break;
            }
        }
        if (!roomTypeExists) {
             System.out.println("Application failed: The chosen room type (" + chosenRoom + ") is not available in project '" + project.getName() + "'.");
             return false;
        }

        applicant.setAppliedProject(project);
        applicant.setRoomChosen(chosenRoom);
        applicant.setStatus(ApplicationStatus.PENDING);

        // Trigger saving of the applicant's state
        if (applicantUserManager != null) {
             applicantUserManager.saveUsers();
        } else {
             System.err.println("Warning: ApplicantUserManager not set. Cannot save applicant state automatically.");
        }

        System.out.println("Application for project '" + project.getName() + "' submitted successfully. Status is now PENDING."); //
        return true;
    }

    /**
     * Withdraws an Applicant's application.
     * Possible for PENDING, SUCCESSFUL, or BOOKED statuses.
     * Sets the status to UNSUCCESSFUL and clears application details.
     * Increments room count if status was BOOKED.
     */
    public boolean withdrawApplication(Applicant applicant) {
        if (applicant == null) {
             System.err.println("Withdrawal failed: Applicant cannot be null.");
             return false;
        }

        ApplicationStatus currentStatus = applicant.getStatus();
        Project currentProject = applicant.getAppliedProject();
        RoomType currentRoom = applicant.getRoomChosen();

        // Check if withdrawal is allowed based on current status
        if (currentStatus == ApplicationStatus.PENDING ||
            currentStatus == ApplicationStatus.SUCCESSFUL ||
            currentStatus == ApplicationStatus.BOOKED) {

            // If status was BOOKED, increment room availability first
            if (currentStatus == ApplicationStatus.BOOKED) { //
                if (currentProject == null || currentRoom == null) {
                    System.err.println("Withdrawal Error: Cannot process withdrawal from BOOKED status.");
                    return false;
                }
            }

            // Set status to UNSUCCESSFUL and clear details
            applicant.setStatus(ApplicationStatus.PENDING_WITHDRAWAL); //

            // SAV

            // Trigger saving of the applicant's state
            if (applicant instanceof Officer) {
				if (officerUserManager != null) {
                    officerUserManager.saveUsers();
                }
            } else {
                if (applicantUserManager != null) {
                    applicantUserManager.saveUsers();
                }
            }

            System.out.println("Application for project '" + (currentProject != null ? currentProject.getName() : "Unknown") + "' withdrawn. Status set to PENDING_WITHDRAWAL."); //
            return true;

        } else {
            // Status is null, UNSUCCESSFUL, or something else - cannot withdraw
            System.out.println("Withdrawal failed: No active application found, or application status ("+ currentStatus +") does not allow withdrawal."); //
            return false;
        }
    }


    // --- Methods typically called by HDB Manager ---
    public boolean approveApplication(Applicant applicant) {
         if (applicant == null || applicant.getStatus() != ApplicationStatus.PENDING) {
              System.err.println("Approval failed: Applicant is null or status is not PENDING.");
              return false;
         }

         Project project = applicant.getAppliedProject();
         RoomType chosenRoom = applicant.getRoomChosen();
         if (project == null || chosenRoom == null) {
             System.err.println("Approval failed: Project or chosen room data missing for applicant.");
             return false;
         }
         
         if (project.getManager() == null || project.getManager().trim().isEmpty()) {
             System.err.println("Approval failed: Project '" + project.getName() + "' does not have a manager assigned.");
             return false;
         }

         applicant.setStatus(ApplicationStatus.SUCCESSFUL);
         if (applicantUserManager != null) {
              applicantUserManager.saveUsers();
              System.out.println("Application approved for Applicant " + applicant.getNRIC());
              return true;
         } else {
              System.err.println("Warning: ApplicantUserManager not set. Cannot save applicant state automatically.");
              return false;
         }
    }

    public boolean rejectApplication(Applicant applicant) {
          if (applicant == null || applicant.getStatus() != ApplicationStatus.PENDING) {
              System.err.println("Rejection failed: Applicant is null or status is not PENDING.");
              return false;
         }
         // Set status to UNSUCCESSFUL
         applicant.setStatus(ApplicationStatus.UNSUCCESSFUL);

         if (applicantUserManager != null) {
              applicantUserManager.saveUsers();
               System.out.println("Application rejected for Applicant " + applicant.getNRIC() + ". Status set to UNSUCCESSFUL.");
              return true;
         } else {
              System.err.println("Warning: ApplicantUserManager not set. Cannot save applicant state automatically.");
              return false;
         }
    }

    // Load/Save for Applications.csv

    public void saveApplications(String filePath, List<Applicant> applicants) {
        List<String> lines = new ArrayList<>();
        lines.add("NRIC,Name,ProjectName,RoomType,Status");

        for (Applicant a : applicants) {
            if (a.getAppliedProject() != null && a.getStatus() != null && a.getStatus() != ApplicationStatus.UNSUCCESSFUL) {
                String line = a.getNRIC() + "," +
                              a.getName() + "," +
                              a.getAppliedProject().getName() + "," +
                              (a.getRoomChosen() != null ? a.getRoomChosen().name() : "") + "," +
                              (a.getStatus() != null ? a.getStatus().name() : "");
                lines.add(line);
            }
        }
        FileManager.writeFile(filePath, lines);
         System.out.println("Application snapshot saved to " + filePath);
    }


    public void loadApplications(String filePath, List<Applicant> applicants, List<Project> projects) {
        List<String> lines = FileManager.readFile(filePath);
        if (lines == null || lines.size() <= 1) {
            System.out.println("No application data found in " + filePath + " or file is empty.");
            return;
        }

        for (String line : lines.subList(1, lines.size())) {
            try {
                String[] parts = line.split(",", 5);
                if (parts.length >= 5) {
                    String nric = parts[0].trim().toUpperCase();
                    String projectName = parts[2].trim();
                    String roomTypeStr = parts[3].trim();
                    String statusStr = parts[4].trim();

                    Applicant targetApplicant = null;
                    for (Applicant a : applicants) {
                        if (a.getNRIC().equalsIgnoreCase(nric)) {
                            targetApplicant = a;
                            break;
                        }
                    }
                    if (targetApplicant == null) continue;

                    Project targetProject = null;
                    for (Project p : projects) {
                        if (p.getName().equalsIgnoreCase(projectName)) {
                            targetProject = p;
                            break;
                        }
                    }
                    if (targetProject == null) continue;

                    targetApplicant.setAppliedProject(targetProject);
                    try {
                        targetApplicant.setRoomChosen(!roomTypeStr.isEmpty() ? RoomType.valueOf(roomTypeStr) : null);
                    } catch (IllegalArgumentException e) { targetApplicant.setRoomChosen(null); }
                     try {
                        targetApplicant.setStatus(!statusStr.isEmpty() ? ApplicationStatus.valueOf(statusStr) : null);
                    } catch (IllegalArgumentException e) { targetApplicant.setStatus(null); }

                }
            } catch (Exception e) {
                 System.err.println("Error processing line in " + filePath + ": " + line + " - " + e.getMessage());
            }
        }
         System.out.println("Application data loaded from " + filePath + " and linked.");
    }
}