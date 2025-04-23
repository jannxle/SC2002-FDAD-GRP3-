package control;

import entities.Enquiry;
import utils.FileManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

/**
 * Manages the creation, retrieval, modification, and persistence of Enquiry objects.
 * Handles loading enquiries from and saving them to `enquiries.csv`.
 * Provides methods for submitting, replying to, editing, and deleting enquiries.
 */
public class EnquiryManager {

    private List<Enquiry> allEnquiries = new ArrayList<>();
    private static final String FILE_PATH = "data/enquiries.csv";

    /**
     * Loads enquiries from the default CSV file path.
     */
    public void loadEnquiries() {
        loadEnquiries(FILE_PATH);
    }

    /**
     * Loads enquiry data from the specified CSV file path.
     * Clears current enquiries. Parses lines into Enquiry objects,
     * handling potential CSV formatting issues and parsing errors.
     * Format: applicantNRIC,applicantName,Project,Message,Reply,ReplyingBy
     *
     * @param filePath The path to the CSV file containing enquiry data.
     */
    public void loadEnquiries(String filePath) {
        allEnquiries.clear();
        List<String> lines = FileManager.readFile(filePath);
        if (lines == null || lines.size() <= 1) {
             System.out.println("No enquiry data found in " + filePath + " or file is empty.");
             return;
        }

        for (String line : lines.subList(1, lines.size())) {
            try {
                 // Let's assume the simple comma split
                String[] parts = line.split(",", 6);
                if (parts.length >= 4) { // Need at least NRIC, Name, Project, Message
                    String nr = parts[0].trim();
                    String name = parts[1].trim();
                    String project = parts[2].trim();
                    String message = unescapeCsvField(parts[3].trim());
                    // Reply might be missing if parts.length is 4, or empty if parts[4] exists but is empty/""
                    String reply = (parts.length >= 5 && !unescapeCsvField(parts[4].trim()).isEmpty()) 
                    	    ? unescapeCsvField(parts[4].trim()) 
                    	    : null;

                    	String replyingOfficer = (parts.length >= 6 && !unescapeCsvField(parts[5].trim()).isEmpty()) 
                    	    ? unescapeCsvField(parts[5].trim()) 
                    	    : null;

                    Enquiry e = new Enquiry(nr, name, project, message);
                    if (reply != null) {
                         e.setReply(reply, replyingOfficer);
                    }
                    allEnquiries.add(e);
                } else {
                    System.err.println("Skipping malformed line in " + filePath + ": " + line);
                }
            } catch (Exception e) {
                 System.err.println("Error processing line in " + filePath + ": " + line + " - " + e.getMessage());
                 e.printStackTrace();
            }
        }
    }

    /**
     * Saves the current list of enquiries to the default CSV file path.
     */
    public void saveEnquiries() {
        saveEnquiries(FILE_PATH);
    }

    /**
     * Saves the current list of Enquiry objects to the specified CSV file path.
     * Overwrites the existing file. Formats each enquiry using CSV escaping rules.
     * Format: applicantNRIC,applicantName,Project,Message,Reply,ReplyingBy
     *
     * @param filePath The path to the CSV file where enquiry data should be saved.
     */
    public void saveEnquiries(String filePath) {
        List<String> lines = new ArrayList<>();
        lines.add("applicantNRIC,applicantName,Project,Message,Reply,ReplyingBy");
        for (Enquiry e : allEnquiries) {
            lines.add(String.join(",",
                escapeCsvField(e.getApplicantNRIC()),
                escapeCsvField(e.getApplicantName()),
                escapeCsvField(e.getProjectName()),
                escapeCsvField(e.getMessage()),
                escapeCsvField(e.getReply() == null ? "" : e.getReply()),
                escapeCsvField(e.getReplyingOfficer()== null ? "" : e.getReplyingOfficer())
            ));
        }
        FileManager.writeFile(filePath, lines);
    }

    /**
     * Adds a new enquiry to the in-memory list.
     *
     * @param enquiry The Enquiry object to submit. Must not be null.
     */
    public void submitEnquiry(Enquiry enquiry) {
        if (enquiry != null) {
             allEnquiries.add(enquiry);
             System.out.println("Enquiry submitted by " + enquiry.getApplicantNRIC() + " for project " + enquiry.getProjectName());
        } else {
            System.err.println("Cannot submit a null enquiry.");
        }
    }

    /**
     * Retrieves all enquiries submitted by a specific applicant, identified by NRIC.
     *
     * @param applicantNRIC The NRIC of the applicant whose enquiries are requested.
     * @return A List of Enquiry objects submitted by the applicant. Returns an empty list if NRIC is invalid or no enquiries are found.
     */
    public List<Enquiry> getEnquiriesByApplicant(String applicantNRIC) {
        List<Enquiry> applicantEnquiries = new ArrayList<>();
        if (applicantNRIC == null || applicantNRIC.trim().isEmpty()) {
            return applicantEnquiries;
        }
        for (Enquiry e : allEnquiries) {
            if (e.getApplicantNRIC().equalsIgnoreCase(applicantNRIC.trim())) {
                applicantEnquiries.add(e);
            }
        }
        return applicantEnquiries;
    }

    /**
     * Retrieves all enquiries related to a specific project, identified by project name.
     *
     * @param projectName The name of the project whose enquiries are requested.
     * @return A List of Enquiry objects for the project. Returns an empty list if name is invalid or no enquiries are found.
     */
    public List<Enquiry> getEnquiriesByProject(String projectName) {
        List<Enquiry> projectEnquiries = new ArrayList<>();
         if (projectName == null || projectName.trim().isEmpty()) {
            return projectEnquiries;
        }
        for (Enquiry e : allEnquiries) {
            if (e.getProjectName().equalsIgnoreCase(projectName.trim())) {
                projectEnquiries.add(e);
            }
        }
        return projectEnquiries;
    }

    /**
     * Adds or updates the reply for a given enquiry.
     * Sets the reply message and the name of the replying officer/manager.
     *
     * @param enquiry     The Enquiry object to reply to.
     * @param reply       The content of the reply message.
     * @param officerName The name of the HDB staff member providing the reply.
     */
    public void replyToEnquiry(Enquiry enquiry, String reply, String officerName) {
        if (enquiry != null) {
            enquiry.setReply(reply, officerName);
             System.out.println("Reply added to enquiry from " + enquiry.getApplicantNRIC() + " for project " + enquiry.getProjectName());
        } else {
             System.err.println("Cannot reply to a null enquiry.");
        }
    }

    /**
     * Edits the message of an existing enquiry, provided it hasn't been replied to yet.
     * Requires the NRIC of the editor to verify ownership.
     *
     * @param enquiry    The Enquiry to edit.
     * @param newMessage The new message content.
     * @param editorNric The NRIC of the user attempting the edit (must match applicant NRIC).
     * @return true if the enquiry was successfully edited, false otherwise (e.g., permission denied, already replied).
     */
    public boolean editEnquiry(Enquiry enquiry, String newMessage, String editorNric) {
        if (enquiry == null || newMessage == null || editorNric == null) {
            System.err.println("Cannot edit enquiry: null parameter provided.");
            return false;
        }
        // Check ownership
        if (!enquiry.getApplicantNRIC().equalsIgnoreCase(editorNric)) {
             System.err.println("Edit failed: User " + editorNric + " is not the owner of this enquiry.");
             return false;
        }
        // Check if already replied (cannot edit after reply)
        if (enquiry.getReply() != null && !enquiry.getReply().isEmpty()) {
             System.out.println("Edit failed: Enquiry has already been replied to.");
             return false;
        }

        enquiry.setMessage(newMessage);
        System.out.println("Enquiry message updated by " + editorNric);
        // Need to call saveEnquiries() later
        return true;
    }

    /**
     * Deletes an enquiry, provided it hasn't been replied to yet.
     * Requires the NRIC of the deleter to verify ownership.
     *
     * @param enquiryToDelete The Enquiry to delete.
     * @param deleterNric   The NRIC of the user attempting deletion (must match applicant NRIC).
     * @return true if the enquiry was successfully deleted, false otherwise (e.g., permission denied, already replied, not found).
     */
    public boolean deleteEnquiry(Enquiry enquiryToDelete, String deleterNric) {
        if (enquiryToDelete == null || deleterNric == null) {
             System.err.println("Cannot delete enquiry: null parameter provided.");
            return false;
        }
         // Check ownership
        if (!enquiryToDelete.getApplicantNRIC().equalsIgnoreCase(deleterNric)) {
             System.err.println("Delete failed: User " + deleterNric + " is not the owner of this enquiry.");
             return false;
        }
         // Check if already replied (cannot delete after reply)
        if (enquiryToDelete.getReply() != null && !enquiryToDelete.getReply().isEmpty()) {
             System.out.println("Delete failed: Enquiry has already been replied to.");
             return false;
        }

        // Remove from the list using iterator for safety
        Iterator<Enquiry> iterator = allEnquiries.iterator();
        boolean removed = false;
        while(iterator.hasNext()) {
            Enquiry current = iterator.next();
            if (current == enquiryToDelete) {
                 iterator.remove();
                 removed = true;
                 break;
            }
        }

        if (removed) {
             System.out.println("Enquiry deleted by " + deleterNric);
             // Need to call saveEnquiries() later
        } else {
             System.err.println("Delete failed: Enquiry object not found in the current list.");
        }
        return removed;
    }

    /**
     * Retrieves the current in-memory list of all enquiries loaded into the manager.
     *
     * @return A List containing all Enquiry objects.
     */
    public List<Enquiry> getAllEnquiries() {
        return allEnquiries;
    }

    /**
     * Escapes a string field for CSV output according to standard rules.
     * Wraps fields containing commas, quotes, or newlines in double quotes,
     * and escapes internal double quotes by doubling them ("").
     *
     * @param field The string field to escape.
     * @return The properly escaped string for CSV, or an empty string if input is null.
     */
    private String escapeCsvField(String field) {
        if (field == null) {
            return ""; // Represent null as empty string in CSV
        }
        // Check if escaping is needed
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            // Replace internal double quotes with two double quotes
            String escapedField = field.replace("\"", "\"\"");
            // Wrap the entire field in double quotes
            return "\"" + escapedField + "\"";
        }
        // No escaping needed
        return field;
    }

    /**
     * Unescapes a string field read from a CSV file.
     * Removes surrounding double quotes if present and replaces doubled internal quotes ("")
     * with single quotes ("). Basic implementation.
     *
     * @param field The potentially escaped string field from CSV.
     * @return The unescaped string, or the original string if no escaping was detected. Returns null if input is null.
     */
    private String unescapeCsvField(String field) {
        if (field == null) {
            return null;
        }
        // Check if it starts and ends with quotes (basic check)
        if (field.startsWith("\"") && field.endsWith("\"")) {
            // Remove surrounding quotes
            String unquoted = field.substring(1, field.length() - 1);
            // Replace escaped double quotes
            return unquoted.replace("\"\"", "\"");
        }
        // No escaping detected or invalid format, return as is
        return field;
    }
}