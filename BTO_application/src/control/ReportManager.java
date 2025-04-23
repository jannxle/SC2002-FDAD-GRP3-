package control;

import entities.Applicant;
import entities.Officer;
import entities.Project;
import entities.Report;
import enums.ApplicationStatus;
import enums.RoomType;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Manages the generation of reports based on BTO application data.
 * Specifically handles creating reports of applicants who have booked flats,
 * allowing for filtering based on criteria like marital status or room type.
 */
public class ReportManager {

    private final UserManager<Applicant> applicantUserManager;
    private final UserManager<Officer> officerUserManager;
    private final ApplicationManager applicationManager;

    /**
     * Constructs a ReportManager.
     * Requires instances of UserManagers and ApplicationManager to access necessary data.
     *
     * @param applicantUserManager The manager for applicant user data.
     * @param officerUserManager   The manager for officer user data.
     * @param applicationManager   The manager for application data.
     * @throws IllegalArgumentException if any manager dependency is null.
     */
    public ReportManager(UserManager<Applicant> applicantUserManager,
                         UserManager<Officer> officerUserManager,
                         ApplicationManager applicationManager) {
        if (applicantUserManager == null || officerUserManager == null || applicationManager == null) {
            throw new IllegalArgumentException("UserManagers and ApplicationManager cannot be null.");
        }
        this.applicantUserManager = applicantUserManager;
        this.officerUserManager = officerUserManager;
        this.applicationManager = applicationManager;
    }

    /**
     * Defines the criteria for filtering the booking report.
     * Allows filtering by marital status and/or room type.
     */
    public static class FilterCriteria {
        private Boolean maritalStatusFilter = null;
        private RoomType roomTypeFilter = null;

        public Boolean getMaritalStatusFilter() { return maritalStatusFilter; }
        public void setMaritalStatusFilter(Boolean maritalStatusFilter) { this.maritalStatusFilter = maritalStatusFilter; }
        public RoomType getRoomTypeFilter() { return roomTypeFilter; }
        public void setRoomTypeFilter(RoomType roomTypeFilter) { this.roomTypeFilter = roomTypeFilter; }
        public boolean hasFilters() { return maritalStatusFilter != null || roomTypeFilter != null; }
    }

    /**
     * Generates a booking report containing applicants with status BOOKED.
     * Filters the applicants based on the provided FilterCriteria.
     *
     * @param criteria The criteria (marital status, room type) to filter the report by. Can be null for no filtering.
     * @return A Report object containing the filtered list of booked applicants and report metadata.
     */
    public Report generateBookingReport(FilterCriteria criteria) {

        List<Applicant> bookedApplicants = applicationManager.getAllApplicants().stream()
            .filter(applicant -> applicant.getStatus() == ApplicationStatus.BOOKED &&
                                 applicant.getAppliedProject() != null &&
                                 applicant.getRoomChosen() != null)
            .filter(applicant -> criteria == null || criteria.getMaritalStatusFilter() == null || applicant.isMarried() == criteria.getMaritalStatusFilter())
            .filter(applicant -> criteria == null || criteria.getRoomTypeFilter() == null || applicant.getRoomChosen() == criteria.getRoomTypeFilter())
            .collect(Collectors.toList());

        return new Report(criteria, bookedApplicants);
    }
}
