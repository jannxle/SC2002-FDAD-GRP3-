package entities;

public class Enquiry {
	
	//NRIC is used for verification which is better than the Applicant's Name 
    private String applicantNRIC;
    private String applicantName;
    private String projectName;
    private String message;
    private String reply;
    private String replyingOfficer;

    public Enquiry(String applicantNRIC, String applicantName,String projectName, String message) {
        this.applicantNRIC = applicantNRIC;
        this.applicantName = applicantName;
        this.projectName = projectName;
        this.message = message;
        this.reply = null;
        this.replyingOfficer = null;
    }

    public String getApplicantNRIC() {
        return applicantNRIC;
    }
    
    public String getApplicantName() {
    	return applicantName;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getMessage() {
        return message;
    }

    public String getReply() {
        return reply;
    }
    
    public String getReplyingOfficer() {
    	return replyingOfficer;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setReply(String reply, String replyingOfficer) {
        this.reply = reply;
        this.replyingOfficer = replyingOfficer;
    }

    //for officer and manager to view enquires
    @Override
    public String toString() {
        return "Enquiry from " + applicantName + "("+ applicantNRIC + ")" +
                "\nProject: " + projectName +
                "\nMessage: " + message +
                "\nReply: " + (reply == null ? "[No reply yet]" : reply);
    }
    
    //for applicant to view, masks the NRIC
    public String toStringForApplicant() {
        return "Project: " + projectName +
                "\nMessage: " + message +
                "\nReply: " + (reply == null ? "[No reply yet]" : reply);
    }
}
