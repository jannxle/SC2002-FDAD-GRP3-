package control;

import entities.*;
import enums.ApplicationStatus;
import enums.OfficerRegistrationStatus;
import enums.RoomType;
import java.time.LocalDate;
import java.util.List;

/**
 * Manages the flat booking process within the BTO system.
 * This class handles the action where an HDB Officer books a flat for an applicant
 * whose application status is SUCCESSFUL. It interacts with ApplicationManager
 * to update the applicant's status to BOOKED and can generate booking receipts.
 */
public class BookingManager {

    private final ProjectManager projectManager;
    private final UserManager<Applicant> applicantUserManager;
    private final ApplicationManager applicationManager;

    /**
     * Constructs a BookingManager.
     *
     * @param projectManager       The manager for project data.
     * @param applicantUserManager The manager for applicant user data (including officers).
     * @param applicationManager   The manager for application data and status updates.
     * @throws IllegalArgumentException if any manager dependency is null.
     */
    public BookingManager(ProjectManager projectManager, UserManager<Applicant> applicantUserManager, ApplicationManager applicationManager) {
        if (projectManager == null || applicantUserManager == null || applicationManager == null) {
            throw new IllegalArgumentException("ProjectManager, ApplicantUserManager, and ApplicationManager cannot be null.");
        }
        this.projectManager = projectManager;
        this.applicantUserManager = applicantUserManager;
        this.applicationManager = applicationManager;
    }

    /**
     * Executes the flat booking process initiated by an HDB Officer for an applicant.
     * Validates the officer's authority for the project and the applicant's eligibility (status SUCCESSFUL).
     * If valid, updates the applicant's status to BOOKED via the ApplicationManager.
     *
     * @param bookingOfficer The HDB Officer performing the booking action.
     * @param applicantNRIC  The NRIC of the applicant whose status is SUCCESSFUL and for whom the flat is being booked.
     * @return true if the booking was successful (status updated to BOOKED), false otherwise.
     */
    public boolean bookFlat(Officer bookingOfficer, String applicantNRIC) {
        if (bookingOfficer == null || applicantNRIC == null || applicantNRIC.trim().isEmpty()) {
             System.err.println("Booking failed: Booking Officer and Applicant NRIC cannot be null or empty.");
             return false;
        }

        // 1. Find the Applicant
        Applicant applicant = null;
        for(Applicant app : applicationManager.getAllApplicants()) {
            if(app.getNRIC().equalsIgnoreCase(applicantNRIC)) {
                applicant = app;
                break;
            }
        }

        if (applicant == null) {
            System.err.println("Booking failed: Applicant with NRIC '" + applicantNRIC + "' not found.");
            return false;
        }

        // 2. Check Applicant Status
        if (applicant.getStatus() != ApplicationStatus.SUCCESSFUL) {
            System.out.println("Booking failed: Applicant '" + applicantNRIC + "' status must be SUCCESSFUL (Current: " + applicant.getStatus() + ")");
            return false;
        }

        // 3. Get Project and Room Type details from Applicant
        Project project = applicant.getAppliedProject();
        RoomType chosenRoom = applicant.getRoomChosen();

        if (project == null || chosenRoom == null) {
            System.err.println("Booking failed: Applicant '" + applicantNRIC + "' is missing Project or Chosen Room information.");
            return false;
        }

        // 4. Check Officer status
        boolean isOfficerApprovedForProject = false;
        for (Project handledProject : bookingOfficer.getRegisteredProjects()) {
            if (handledProject.equals(project)) {
                if (bookingOfficer.getRegistrationStatusForProject(handledProject) == OfficerRegistrationStatus.APPROVED) {
                    isOfficerApprovedForProject = true;
                    break;
                }
            }
        }

        if (!isOfficerApprovedForProject) {
            System.out.println("Booking failed: Officer " + bookingOfficer.getName() +
                               " is not assigned or not approved to handle project '" + project.getName() + "'.");
            return false;
        }


        // 5. Delegate status update AND saving to ApplicationManager
        boolean statusUpdated = applicationManager.updateAndSaveApplicantStatus(applicant, ApplicationStatus.BOOKED);

        if (statusUpdated) {
             System.out.println("Flat (" + chosenRoom + ") booked successfully by Officer " + bookingOfficer.getName() +
                               " for Applicant " + applicant.getNRIC() + " in project '" + project.getName() + "'. Status is now BOOKED.");
             return true;
        } else {
             System.err.println("Booking failed: Could not update applicant status via ApplicationManager.");
             return false;
        }
    }

    /**
     * Generates a booking receipt for an applicant whose status is BOOKED.
     * Creates a new Receipt object containing details from the applicant and their booked project.
     *
     * @param bookedApplicant The applicant for whom to generate the receipt. Must have status BOOKED.
     * @return A Receipt object containing the booking details, or null if the applicant is invalid or status is not BOOKED.
     */
    public Receipt generateBookingReceipt(Applicant bookedApplicant) {
         if (bookedApplicant == null || bookedApplicant.getStatus() != ApplicationStatus.BOOKED) {
              System.err.println("Error: Cannot generate receipt. Applicant is null or status is not BOOKED.");
              return null;
         }
         Project project = bookedApplicant.getAppliedProject();
          if (project == null || bookedApplicant.getRoomChosen() == null) {
               System.err.println("Error: Cannot generate receipt for Applicant " + bookedApplicant.getNRIC() + ". Missing project or flat type details.");
               return null;
          }
          return new Receipt(bookedApplicant, project);
    }
}
