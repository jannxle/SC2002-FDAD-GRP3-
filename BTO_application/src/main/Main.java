package main;

import control.*;
import auth.LoginManager;
import entities.Applicant;
import entities.Officer;
import entities.Manager;


public class Main {

    public static void main(String[] args) { //

        System.out.println("Initializing BTO Management System...");

        UserManager<Applicant> applicantUserManager = new ApplicantUserManager();
        UserManager<Officer> officerUserManager = new OfficerUserManager();
        UserManager<Manager> managerUserManager = new ManagerUserManager();

        ProjectManager projectManager = new ProjectManager();
        EnquiryManager enquiryManager = new EnquiryManager();
        ApplicantManager applicantManager = new ApplicantManager(projectManager);
        ApplicationManager applicationManager = new ApplicationManager(projectManager, applicantUserManager);
        OfficerRegistrationManager officerRegistrationManager = new OfficerRegistrationManager(projectManager, officerUserManager);
        BookingManager bookingManager = new BookingManager(projectManager, applicantUserManager);
        ReportManager reportManager = new ReportManager(applicantUserManager, officerUserManager);

        applicantUserManager.loadUsers();
        officerUserManager.loadUsers();
        managerUserManager.loadUsers();

        projectManager.loadProjects("data/ProjectList.csv"); //

        enquiryManager.loadEnquiries();

        applicationManager.loadApplications(
             "data/Applications.csv",
             applicantUserManager.getUsers(),
             projectManager.getProjects()
        );

        System.out.println("Initialization complete. Starting Login...");
        System.out.println("-----------------------------------------");

        LoginManager loginManager = new LoginManager(
            applicantManager,
            applicantUserManager,
            officerUserManager,
            managerUserManager,
            enquiryManager,
            applicationManager,
            projectManager,
            officerRegistrationManager,
            bookingManager,
            reportManager
        );

        // --- Start the Application ---
        loginManager.login(); //

        // --- Save Data on Exit ---
        System.out.println("\n-----------------------------------------");
        System.out.println("Exiting BTO Management System. Saving data...");
        applicantUserManager.saveUsers();
        officerUserManager.saveUsers();
        managerUserManager.saveUsers();
        projectManager.saveProjects("data/ProjectList.csv");
        enquiryManager.saveEnquiries();

        System.out.println("Data saved. Goodbye!");
    }
}