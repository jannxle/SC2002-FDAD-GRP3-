package control;

import entities.Applicant;
import entities.Officer;
import entities.Project;
import enums.ApplicationStatus;
import enums.RoomType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Manages the generation of reports based on BTO application and booking data.
 * Primarily used by HDB Managers.
 */
public class ReportManager {

    private final UserManager<Applicant> applicantUserManager;
    private final UserManager<Officer> officerUserManager;

    public ReportManager(UserManager<Applicant> applicantUserManager, UserManager<Officer> officerUserManager) {
        if (applicantUserManager == null || officerUserManager == null) {
            throw new IllegalArgumentException("UserManagers cannot be null.");
        }
        this.applicantUserManager = applicantUserManager;
        this.officerUserManager = officerUserManager;
    }

    public static class FilterCriteria {
        private Boolean maritalStatusFilter = null; // true=Married, false=Single, null=All
        private RoomType roomTypeFilter = null; // Specific RoomType or null=All

        public Boolean getMaritalStatusFilter() {
            return maritalStatusFilter;
        }
        public void setMaritalStatusFilter(Boolean maritalStatusFilter) {
            this.maritalStatusFilter = maritalStatusFilter;
        }
        public RoomType getRoomTypeFilter() {
            return roomTypeFilter;
        }
        public void setRoomTypeFilter(RoomType roomTypeFilter) {
            this.roomTypeFilter = roomTypeFilter;
        }

        public boolean hasFilters() {
             return maritalStatusFilter != null || roomTypeFilter != null;
        }
    }


    /**
     * Generates a report listing applicants who have booked a flat,
     * Report includes: Applicant NRIC, Name, Age, Marital Status, Flat Type Booked, Project Name.
     */
    public String generateBookingReport(FilterCriteria criteria) {
        StringBuilder report = new StringBuilder();
        String lineSeparator = System.lineSeparator();

        report.append("=============================================================================================").append(lineSeparator);
        report.append("                         BTO Applicant Booking Report                                        ").append(lineSeparator);
        if (criteria != null && criteria.hasFilters()) {
             report.append(" Filters Applied: ");
             if (criteria.getMaritalStatusFilter() != null) {
                  report.append("Marital Status=").append(criteria.getMaritalStatusFilter() ? "Married" : "Single").append(" ");
             }
              if (criteria.getRoomTypeFilter() != null) {
                  report.append("RoomType=").append(criteria.getRoomTypeFilter().name()).append(" ");
             }
             report.append(lineSeparator);
        }
        report.append("=============================================================================================").append(lineSeparator);
        report.append(String.format(" %-12s | %-20s | %-3s | %-10s | %-10s | %-25s%n",
                                     "NRIC", "Name", "Age", "Status", "Room Booked", "Project Name"));
        report.append("---------------------------------------------------------------------------------------------").append(lineSeparator);

        // Combine Applicants and Officers into a single stream for processing
        List<Applicant> bookedApplicants = Stream.concat(
                applicantUserManager.getUsers().stream(),

                officerUserManager.getUsers().stream()
            )
            // 1. Filter for BOOKED status and valid project/room data
            .filter(user -> user instanceof Applicant)
            .map(user -> (Applicant) user)
            .filter(applicant -> applicant.getStatus() == ApplicationStatus.BOOKED &&
                                 applicant.getAppliedProject() != null &&
                                 applicant.getRoomChosen() != null)
            // 2. Apply optional filters from criteria
            .filter(applicant -> criteria == null || criteria.getMaritalStatusFilter() == null || applicant.isMarried() == criteria.getMaritalStatusFilter())
            .filter(applicant -> criteria == null || criteria.getRoomTypeFilter() == null || applicant.getRoomChosen() == criteria.getRoomTypeFilter())
            .collect(Collectors.toList());

        // Format the report lines
        if (bookedApplicants.isEmpty()) {
            report.append("                    < No applicants found matching criteria >                            ").append(lineSeparator);
        } else {
            for (Applicant app : bookedApplicants) {
                Project project = app.getAppliedProject();
                RoomType room = app.getRoomChosen();

                report.append(String.format(" %-12s | %-20s | %-3d | %-10s | %-10s | %-25s%n",
                        app.getNRIC(),
                        app.getName(),
                        app.getAge(),
                        app.isMarried() ? "Married" : "Single",
                        room.name(),
                        project.getName()
                )).append(lineSeparator);
            }
        }

        report.append("---------------------------------------------------------------------------------------------").append(lineSeparator);
        report.append(" Total Records Found: ").append(bookedApplicants.size()).append(lineSeparator);
        report.append(" Report Generated On: ").append(java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)).append(lineSeparator);
        report.append("=============================================================================================").append(lineSeparator);

        return report.toString();
    }
}