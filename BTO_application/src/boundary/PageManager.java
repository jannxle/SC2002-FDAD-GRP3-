package boundary;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.time.LocalDate;

import auth.LoginManager;
import control.UserManager;
import control.ApplicantManager;
import control.ApplicationManager;
import control.EnquiryManager;
import control.OfficerRegistrationManager;
import control.ProjectManager;
import control.ReportManager;
import control.BookingManager;

import entities.*;
import entities.Pages.*;


import enums.ApplicationStatus;
import enums.OfficerRegistrationStatus;
import enums.RoomType;

public class PageManager {
	private final ApplicantManager applicantManager;
	private final ApplicationManager applicationManager;
	private final UserManager<Applicant> applicantUserManager;
	private final UserManager<Officer> officerUserManager;
	private final UserManager<Manager> managerUserManager;
	private final EnquiryManager enquiryManager;
	private final ProjectManager projectManager;
	private final OfficerRegistrationManager officerRegistrationManager;
    private final BookingManager bookingManager;
    private final ReportManager reportManager;
    private final LoginManager loginManager;


	public PageManager(ApplicantManager applicantManager,
                        UserManager<Applicant> applicantUserManager,
                        UserManager<Officer> officerUserManager,
                        UserManager<Manager> managerUserManager,
                        EnquiryManager enquiryManager,
                        ApplicationManager applicationManager,
                        ProjectManager projectManager,
                        OfficerRegistrationManager officerRegistrationManager,
                        BookingManager bookingManager,
                        ReportManager reportManager,
                        LoginManager loginManager
                        ) {
		this.applicantManager = applicantManager;
		this.applicantUserManager = applicantUserManager;
		this.officerUserManager = officerUserManager;
		this.managerUserManager = managerUserManager;
		this.enquiryManager = enquiryManager;
		this.applicationManager = applicationManager;
		this.projectManager = projectManager;
        this.officerRegistrationManager = officerRegistrationManager;
        this.bookingManager = bookingManager;
        this.reportManager = reportManager;
        this.loginManager = loginManager;
	}
	
	private Pages p = new Pages();
	private Page currentActivePage;
	private List<Page> history = new ArrayList<>();
	private User currentUser = null;
	
	public void start() {
		Scanner sc = new Scanner(System.in);
		currentActivePage = p.new LoginPage(loginManager);
		appendPage(currentActivePage);
		
		while (currentUser == null) {
			currentUser = currentActivePage.run(sc);
		}
		
		//TODO: 
		
	}
	
	private void appendPage(Page page) {
		history.add(page);
	}
	
	
	
	
	
	
}