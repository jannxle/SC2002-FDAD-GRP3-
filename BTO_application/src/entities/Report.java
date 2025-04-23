package entities;

import control.ReportManager.FilterCriteria;
import enums.RoomType;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;

/**
 * Represents a generated report containing a list of applicants who have booked flats,
 * potentially filtered by specific criteria. Includes the filters applied
 * and the date the report was generated.
 * This class is typically instantiated by control.ReportManager.
 */
public class Report {

    private FilterCriteria criteria;
    private List<Applicant> bookedApplicants;
    private LocalDate generationDate;

    /**
     * Constructs a new Report object.
     * Initializes the report with the filter criteria used and the list of booked applicants
     * that matched those criteria. Sets the generation date to the current date.
     *
     * @param criteria         The FilterCriteria used for generating the report.
     * @param bookedApplicants A List of Applicant objects who have booked flats
     * and match the criteria. If null, an empty list is used.
     */
    public Report(FilterCriteria criteria, List<Applicant> bookedApplicants) {
        this.criteria = criteria;
        this.bookedApplicants = bookedApplicants != null ? bookedApplicants : new ArrayList<>();
        this.generationDate = LocalDate.now();
    }

    /**
     * Gets the filter criteria that were applied when generating this report.
     * @return The {@link FilterCriteria} object, or null if no filters were used.
     */
    public FilterCriteria getCriteria() { return criteria; }

    /**
     * Gets the list of booked applicants included in this report.
     * @return A List of Applicant objects who have booked flats.
     */
    public List<Applicant> getBookedApplicants() { return bookedApplicants; }

    /**
     * Gets the date when the report was generated.
     * @return The LocalDate representing the generation date.
     */
    public LocalDate getGenerationDate() { return generationDate; }

    /**
     * Converts the report data into a formatted string for display.
     * The format includes headers, applicant details, and the generation date.
     * If no applicants are found, a message indicating this is included.
     *
     * @return A formatted string representation of the report.
     */
    public String toFormattedString() {
        StringBuilder report = new StringBuilder();
        String lineSeparator = System.lineSeparator();
        String divider = "=============================================================================================";
        String shortDivider = "---------------------------------------------------------------------------------------------";

        report.append(divider).append(lineSeparator);
        report.append("                                 BTO Applicant Booking Report                                ").append(lineSeparator);
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
        report.append(divider).append(lineSeparator);
        report.append(String.format(" %-12s | %-20s | %-3s | %-10s | %-10s | %-25s%n",
                                     "NRIC", "Name", "Age", "Status", "Room Booked", "Project Name"));
        report.append(shortDivider).append(lineSeparator);


        if (bookedApplicants.isEmpty()) {
            report.append("                    < No applicants found matching criteria >                            ").append(lineSeparator);
        } else {
            for (Applicant app : bookedApplicants) {
                String nric = app.getNRIC();
                String name = app.getName();
                int age = app.getAge();
                String maritalStatus = app.isMarried() ? "Married" : "Single";
                RoomType room = app.getRoomChosen();
                Project project = app.getAppliedProject();
                String roomName = (room != null) ? room.name() : "N/A";
                String projectName = (project != null) ? project.getName() : "N/A";

                report.append(String.format(" %-12s | %-20s | %-3d | %-10s | %-10s | %-25s%n",
                        nric,
                        name,
                        age,
                        maritalStatus,
                        roomName,
                        projectName
                ));
            }
        }

        report.append(shortDivider).append(lineSeparator);
        report.append(" Total Records Found: ").append(bookedApplicants.size()).append(lineSeparator);
        report.append(" Report Generated On: ").append(generationDate.format(DateTimeFormatter.ISO_LOCAL_DATE)).append(lineSeparator);
        report.append(divider).append(lineSeparator);

        return report.toString();
    }
}
