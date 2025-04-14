package control;

import entities.Enquiry;
import utils.FileManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

public class EnquiryManager {

    private List<Enquiry> allEnquiries = new ArrayList<>();
    private static final String FILE_PATH = "data/enquiries.csv";

    public void loadEnquiries() {
        loadEnquiries(FILE_PATH);
    }

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
        System.out.println("Enquiry data loaded from " + filePath);
    }

    public void saveEnquiries() {
        saveEnquiries(FILE_PATH);
    }

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
        System.out.println("Enquiry data saved to " + filePath);
    }

    public void submitEnquiry(Enquiry enquiry) {
        if (enquiry != null) {
             allEnquiries.add(enquiry);
             System.out.println("Enquiry submitted by " + enquiry.getApplicantNRIC() + " for project " + enquiry.getProjectName());
        } else {
            System.err.println("Cannot submit a null enquiry.");
        }
    }

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

    public void replyToEnquiry(Enquiry enquiry, String reply, String officerName) {
        if (enquiry != null) {
            enquiry.setReply(reply, officerName);
             System.out.println("Reply added to enquiry from " + enquiry.getApplicantNRIC() + " for project " + enquiry.getProjectName());
        } else {
             System.err.println("Cannot reply to a null enquiry.");
        }
    }

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

    public List<Enquiry> getAllEnquiries() {
        return allEnquiries;
    }

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