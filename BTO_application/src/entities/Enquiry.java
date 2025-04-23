package entities;

/**
 * Represents an enquiry submitted by an applicant regarding a specific BTO project.
 * Contains details about the applicant, the project in question, the enquiry message,
 * and any reply provided by an HDB Officer or Manager.
 */
public class Enquiry {
	
    private String applicantNRIC;
    private String applicantName;
    private String projectName;
    private String message;
    private String reply;
    private String replyingOfficer;

    /**
     * Constructs a new Enquiry object.
     * Initializes the enquiry with applicant details, project name, and the message.
     * The reply and replying officer fields are initially set to null.
     *
     * @param applicantNRIC The NRIC of the applicant submitting the enquiry.
     * @param applicantName The name of the applicant submitting the enquiry.
     * @param projectName   The name of the project the enquiry relates to.
     * @param message       The text content of the enquiry.
     */
    public Enquiry(String applicantNRIC, String applicantName,String projectName, String message) {
        this.applicantNRIC = applicantNRIC;
        this.applicantName = applicantName;
        this.projectName = projectName;
        this.message = message;
        this.reply = null;
        this.replyingOfficer = null;
    }

    /**
     * Gets the NRIC of the applicant who submitted the enquiry.
     * @return The applicant's NRIC.
     */
    public String getApplicantNRIC() {
        return applicantNRIC;
    }
    
    /**
     * Gets the name of the applicant who submitted the enquiry.
     * @return The applicant's name.
     */
    public String getApplicantName() {
    	return applicantName;
    }

    /**
     * Gets the name of the project this enquiry is about.
     * @return The project name.
     */
    public String getProjectName() {
        return projectName;
    }

    /**
     * Gets the message content of the enquiry.
     * @return The enquiry message string.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Gets the reply provided to this enquiry.
     * @return The reply string, or null if no reply has been given.
     */
    public String getReply() {
        return reply;
    }
    
    /**
     * Gets the name of the officer or manager who replied to the enquiry.
     * @return The name of the replier, or null if no reply has been given.
     */
    public String getReplyingOfficer() {
    	return replyingOfficer;
    }

    /**
     * Sets or updates the message content of the enquiry.
     * This might be used if an applicant edits their enquiry before it's replied to.
     * @param message The new enquiry message.
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Sets the reply for this enquiry and records the name of the replier.
     * This is typically called by an HDB Officer or Manager.
     *
     * @param reply           The reply message content.
     * @param replyingOfficer The name of the HDB staff member providing the reply.
     */
    public void setReply(String reply, String replyingOfficer) {
        this.reply = reply;
        this.replyingOfficer = replyingOfficer;
    }

    /**
     * Returns a string representation of the Enquiry, suitable for display to HDB staff.
     * Includes applicant NRIC, name, project, message, and reply details.
     *
     * @return A formatted string summarizing the enquiry details.
     */
    @Override
    public String toString() {
        return "Enquiry from " + applicantName + "("+ applicantNRIC + ")" +
                "\nProject: " + projectName +
                "\nMessage: " + message +
                "\nReply: " + (reply == null ? "[No reply yet]" : reply);
    }
    
    /**
     * Returns a string representation of the Enquiry, suitable for display to the applicant.
     *
     * @return A formatted string summarizing the enquiry details for the applicant.
     */
    public String toStringForApplicant() {
        return "Project: " + projectName +
                "\nMessage: " + message +
                "\nReply: " + (reply == null ? "[No reply yet]" : reply);
    }
}
