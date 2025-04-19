package entities;

import control.ReportManager.FilterCriteria;
import enums.RoomType;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;

public class Report {

    private FilterCriteria criteria;
    private List<Applicant> bookedApplicants;
    private LocalDate generationDate;

    public Report(FilterCriteria criteria, List<Applicant> bookedApplicants) {
        this.criteria = criteria;
        this.bookedApplicants = bookedApplicants != null ? bookedApplicants : new ArrayList<>();
        this.generationDate = LocalDate.now();
    }

    public FilterCriteria getCriteria() { return criteria; }
    public List<Applicant> getBookedApplicants() { return bookedApplicants; }
    public LocalDate getGenerationDate() { return generationDate; }

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
