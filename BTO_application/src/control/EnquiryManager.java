package control;

import entities.Enquiry;
import utils.FileManager;

import java.util.ArrayList;
import java.util.List;

public class EnquiryManager {
    private List<Enquiry> allEnquiries = new ArrayList<>();

    public void submitEnquiry(Enquiry enquiry) {
        allEnquiries.add(enquiry);
    }

    // Filter enquiries using the unique NRIC
    public List<Enquiry> getEnquiriesByApplicant(String applicantNRIC) {
        List<Enquiry> list = new ArrayList<>();
        for (Enquiry e : allEnquiries) {
            if (e.getApplicantNRIC().equalsIgnoreCase(applicantNRIC)) {
                list.add(e);
            }
        }
        return list;
    }

    public List<Enquiry> getEnquiriesByProject(String projectName) {
        List<Enquiry> list = new ArrayList<>();
        for (Enquiry e : allEnquiries) {
            if (e.getProjectName().equalsIgnoreCase(projectName)) {
                list.add(e);
            }
        }
        return list;
    }

    public void replyToEnquiry(Enquiry enquiry, String reply) {
        enquiry.setReply(reply);
    }

    public List<Enquiry> getAllEnquiries() {
        return allEnquiries;
    }

    // Save enquiries to a CSV file including both NRIC and applicant name
    public void saveEnquiries(String filePath) {
        List<String> lines = new ArrayList<>();
        lines.add("applicantNRIC,applicantName,Project,Message,Reply");
        for (Enquiry e : allEnquiries) {
            lines.add(String.join(",",
                e.getApplicantNRIC(),
                e.getApplicantName(),
                e.getProjectName(),
                escape(e.getMessage()),
                escape(e.getReply() == null ? "" : e.getReply())
            ));
        }
        FileManager.writeFile(filePath, lines);
    }

    // Load enquiries from CSV file
    public void loadEnquiries(String filePath) {
        List<String> lines = FileManager.readFile(filePath);
        if (lines == null || lines.size() <= 1) return;

        for (String line : lines.subList(1, lines.size())) {
            String[] parts = line.split(",", 5);
            if (parts.length >= 4) {
                String nr = parts[0].trim();
                String name = parts[1].trim();
                String project = parts[2].trim();
                String message = unescape(parts[3].trim());
                String reply = parts.length == 5 ? unescape(parts[4].trim()) : null;

                Enquiry e = new Enquiry(nr, name, project, message);
                e.setReply(reply);
                allEnquiries.add(e);
            }
        }
    }

    // Simple CSV escaping
    private String escape(String text) {
        return "\"" + text.replace("\"", "\"\"") + "\"";
    }

    private String unescape(String text) {
        return text.replaceAll("^\"|\"$", "").replace("\"\"", "\"");
    }
}
