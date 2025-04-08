package entities;

public class Enquiry {
	
	//NRIC is used for verification which is better than the Applicant's Name 
    private String applicantNRIC;
    private String applicantName;
    private String projectName;
    private String message;
    private String reply;
    private String replyingOfficer; //to store the officer's name who replied

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
    
    public void setMessage(String message) {
    	this.message = message;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply, String replyingOfficer) {
        this.reply = reply;
        this.replyingOfficer = replyingOfficer;
    } 
    
    public String getReplyingOfficer() {
    	return replyingOfficer;
    }

    //for officer and manager to view enquires
    @Override
    public String toString() {
        String replyDisplay;
        if (reply != null && !reply.isEmpty()) {
            if (replyingOfficer != null && !replyingOfficer.isEmpty()) {
                replyDisplay = "Reply from Officer " + replyingOfficer + ": " + reply;
            } else {
                replyDisplay = reply; // Show the reply text even if officer name is absent.
            }
        } else {
            replyDisplay = "[No reply yet]";
        }
        return "Enquiry from " + applicantName + " ("+ applicantNRIC + ")" +
                "\nProject: " + projectName +
                "\nMessage: " + message +
                "\n" + replyDisplay;
    }
    
    //for applicant to view, masks the NRIC
    public String toStringForApplicant() {
        String replyDisplay;
        if (reply != null && !reply.isEmpty()) {
            if (replyingOfficer != null && !replyingOfficer.isEmpty()) {
                replyDisplay = "Reply from Officer " + replyingOfficer + ": " + reply;
            } else {
                replyDisplay = reply; // Show the reply text even if officer name is absent.
            }
        } else {
            replyDisplay = "[No reply yet]";
        }
        return "Project: " + projectName +
               "\nMessage: " + message +
               "\n" + replyDisplay;
    }
}
