package main;

import control.*;

import java.util.ArrayList;
import java.util.List;

import auth.LoginManager;
import entities.Applicant;
import entities.Officer;
import entities.Manager;


public class Main {

    public static void main(String[] args) { //

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
        ReportManager reportManager = new ReportManager(applicantUserManager, officerUserManager);

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