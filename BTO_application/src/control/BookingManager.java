package control;

import entities.Applicant;
import entities.Officer;
import entities.Project;
import entities.Room;
import enums.ApplicationStatus;
import enums.RoomType;
import java.time.LocalDate;

/**
 * Manages the flat booking process after an application has been approved.
 */
public class BookingManager {

    private final ProjectManager projectManager;
    private final UserManager<Applicant> applicantUserManager;
    private ApplicationManager applicationManager;

    public BookingManager(ProjectManager projectManager, UserManager<Applicant> applicantUserManager, ApplicationManager applicationManager) {
        if (projectManager == null || applicantUserManager == null) {
            throw new IllegalArgumentException("ProjectManager and ApplicantUserManager cannot be null.");
        }
        this.projectManager = projectManager;
        this.applicantUserManager = applicantUserManager;
        this.applicationManager = applicationManager;
    }

    public boolean bookFlat(Officer bookingOfficer, String applicantNRIC) {
        if (bookingOfficer == null || applicantNRIC == null || applicantNRIC.trim().isEmpty()) {
             System.err.println("Booking failed: Booking Officer and Applicant NRIC cannot be null or empty.");
             return false;
        }

        // 1. Find the Applicant using the injected UserManager
        Applicant applicant = applicantUserManager.findByNRIC(applicantNRIC);
        if (applicant == null) {
            System.err.println("Booking failed: Applicant with NRIC '" + applicantNRIC + "' not found.");
            return false;
        }

        // 2. Check Applicant Status
        if (applicant.getStatus() != ApplicationStatus.SUCCESSFUL) { // Why is this method going to out instead of err? - Shrey
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

        applicant.setStatus(ApplicationStatus.BOOKED);

        // Persist changes - Both Applicant and Project lists need saving
        applicantUserManager.saveUsers();
        applicationManager.saveApplications("data/Applications.csv", applicationManager.getAllApplicants());
        projectManager.saveProjects("data/ProjectList.csv"); // No changes seen here to save - Shrey

        System.out.println("Flat (" + chosenRoom + ") booked successfully by Officer " + bookingOfficer.getName() +
                               " for Applicant " + applicant.getNRIC() + " in project '" + project.getName() + "'. Status is now BOOKED.");
        return true;
    }

     /**
     * Generates a formatted receipt string containing booking details for an applicant.
     */
    public String generateBookingReceipt(Applicant bookedApplicant) {
         if (bookedApplicant == null || bookedApplicant.getStatus() != ApplicationStatus.BOOKED) {
              return "Error: Cannot generate receipt. Applicant is null or status is not BOOKED.";
         }

         Project project = bookedApplicant.getAppliedProject();
         RoomType flatType = bookedApplicant.getRoomChosen();

          if (project == null || flatType == null) {
               return "Error: Cannot generate receipt for Applicant " + bookedApplicant.getNRIC() + ". Missing project or flat type details.";
          }

          // --- Receipt Formatting ---
          StringBuilder receipt = new StringBuilder();
          receipt.append("------------------------------------------\n");
          receipt.append("        BTO Booking Confirmation\n");
          receipt.append("------------------------------------------\n");
          receipt.append("Applicant Name: ").append(bookedApplicant.getName()).append("\n");
          receipt.append("Applicant NRIC: ").append(bookedApplicant.getNRIC()).append("\n");
          receipt.append("Age:            ").append(bookedApplicant.getAge()).append("\n");
          receipt.append("Marital Status: ").append(bookedApplicant.isMarried() ? "Married" : "Single").append("\n");
          receipt.append("\nBooking Details:\n");
          receipt.append("  Project Name:   ").append(project.getName()).append("\n");
          receipt.append("  Neighbourhood:  ").append(project.getNeighbourhood()).append("\n");
          receipt.append("  Flat Type Booked: ").append(flatType.name()).append("\n");

           for (Room room : project.getRooms()) {
               if (room.getRoomType() == flatType) {
                    receipt.append("  Price:          SGD ").append(String.format("%.2f", room.getPrice())).append("\n");
                    break;
               }
           }
          receipt.append("------------------------------------------\n");
          receipt.append("Date Generated: ").append(LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)).append("\n");
          receipt.append("------------------------------------------\n");


          return receipt.toString();
    }

}
