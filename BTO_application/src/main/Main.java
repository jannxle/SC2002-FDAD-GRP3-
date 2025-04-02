package main;

import auth.LoginManager;
import control.ApplicantManager;
import control.ApplicationManager;
import control.EnquiryManager;
import control.OfficerRegistrationManager;
import control.ProjectManager;
import control.UserManager;
import java.io.File;

public class Main {
    public static void main(String[] args) {
    	System.out.println("Working Directory: " + new File(".").getAbsolutePath());

    	//load user data
        UserManager userManager = new UserManager();
        userManager.loadAllUsers();
        
        //load project data
        ProjectManager projectManager = new ProjectManager();
        projectManager.loadProjects("data/ProjectList.csv");
        
        
        //load enquiry data
        EnquiryManager enquiryManager = new EnquiryManager();
        enquiryManager.loadEnquiries("data/enquiries.csv");
        
        //load applications
        ApplicationManager applicationManager = new ApplicationManager();
        applicationManager.loadApplications("data/applications.csv", userManager.getApplicants(), projectManager.getProjects());
        
        //Create control classes for applicant functionalities
        ApplicantManager applicantManager1 = new ApplicantManager(projectManager.getProjects());
        ApplicationManager applicationManager1 = new ApplicationManager();
        
        OfficerRegistrationManager officerRegistrationManager = new OfficerRegistrationManager();

		LoginManager loginManager = new LoginManager(applicantManager1 , userManager, enquiryManager, applicationManager1,projectManager, officerRegistrationManager);
        loginManager.login();
        
        // After the application is done (or on exit), save the enquiries back to file
        //enquiryManager.saveEnquiries("data/enquiries.csv");
        //projectManager.saveProjects("data/ProjectList.csv");
        //applicationManager1.saveApplications("data/Applications.csv", userManager.getApplicants());
    }
}
