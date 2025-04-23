package main;

import control.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import auth.LoginManager;
import entities.*;

/**
 * The main entry point for the BTO Management System application.
 * This class initializes all necessary components, loads initial data
 * from CSV files, starts the user login process, and ensures data is saved
 * back to the CSV files upon application exit.
 */
public class Main {

    /**
     * The main method that starts the BTO Management System.
     *
     * Initializes all manager classes (ProjectManager, UserManagers, ApplicationManager, etc.).
     * Loads data from various CSV files (Projects, Users, Applications, Enquiries, Filters).
     * Displays the current date and a welcome banner.
     * Instantiates and starts the LoginManager to handle user authentication and UI navigation.
     * Upon termination of the login loop (application exit), it saves all potentially modified data back to their respective CSV files.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {

        System.out.println("Initializing BTO Management System...");

        ProjectManager projectManager = new ProjectManager();
        projectManager.loadProjects("data/ProjectList.csv"); 
        
        UserManager<Applicant> applicantUserManager = new ApplicantUserManager();
        OfficerUserManager officerUserManager = new OfficerUserManager(projectManager); //need officer specific methods in the csv
        UserManager<Manager> managerUserManager = new ManagerUserManager();

        
        EnquiryManager enquiryManager = new EnquiryManager();
        ApplicantManager applicantManager = new ApplicantManager(projectManager);
        ApplicationManager applicationManager = new ApplicationManager(projectManager, applicantUserManager, officerUserManager);
        OfficerRegistrationManager officerRegistrationManager = new OfficerRegistrationManager(projectManager, officerUserManager);
        BookingManager bookingManager = new BookingManager(projectManager, applicantUserManager, applicationManager);
        ReportManager reportManager = new ReportManager(applicantUserManager, officerUserManager, applicationManager);
        FilterManager filterManager = new FilterManager("data/Filter.csv");

        applicantUserManager.loadUsers();
        officerUserManager.loadUsers();
        managerUserManager.loadUsers();

        enquiryManager.loadEnquiries();

        List<Applicant> allApplicants = new ArrayList<>();
        allApplicants.addAll(applicantUserManager.getUsers());
        allApplicants.addAll(officerUserManager.getUsers()); // include officers who apply as applicant

        applicationManager.loadApplications(
             "data/Applications.csv",
             allApplicants,
             projectManager.getProjects()
        );

        System.out.println("Initialization complete. Redirecting to Login Page...");
        System.out.println();
        System.out.println("-------------------------------------------------------------------");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        // Get the current date
        LocalDate currentDate = LocalDate.now();
        // Print the date at the start of the app
        System.out.println("                                          Today's date: " + currentDate.format(formatter));
        
        System.out.println("-------------------------------------------------------------------");
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
            reportManager,
            filterManager
        );
        loginManager.welcomeBanner();

        // --- Start the Application ---
        loginManager.login(); //

        // --- Save Data on Exit ---
        System.out.println("\n-----------------------------------------");
        System.out.println("Exiting BTO Management System. Saving data...");
        applicantUserManager.saveUsers();
        applicationManager.saveApplications("data/applications.csv", allApplicants);
        officerUserManager.saveUsers();
        managerUserManager.saveUsers();
        projectManager.saveProjects("data/ProjectList.csv");
        enquiryManager.saveEnquiries("data/enquiries.csv");
        filterManager.saveFilters();

        System.out.println("Goodbye!");
    }
}