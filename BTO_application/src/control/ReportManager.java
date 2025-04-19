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


public class ReportManager {

    private final UserManager<Applicant> applicantUserManager;
    private final UserManager<Officer> officerUserManager;
    private final ApplicationManager applicationManager;


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

    public static class FilterCriteria {
        private Boolean maritalStatusFilter = null;
        private RoomType roomTypeFilter = null;

        public Boolean getMaritalStatusFilter() { return maritalStatusFilter; }
        public void setMaritalStatusFilter(Boolean maritalStatusFilter) { this.maritalStatusFilter = maritalStatusFilter; }
        public RoomType getRoomTypeFilter() { return roomTypeFilter; }
        public void setRoomTypeFilter(RoomType roomTypeFilter) { this.roomTypeFilter = roomTypeFilter; }
        public boolean hasFilters() { return maritalStatusFilter != null || roomTypeFilter != null; }
    }


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
