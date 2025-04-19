package entities;

import enums.RoomType;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Receipt {

    private String applicantName;
    private String applicantNric;
    private int applicantAge;
    private boolean applicantIsMarried;
    private String projectName;
    private String neighbourhood;
    private RoomType flatTypeBooked;
    private double price;
    private LocalDate dateGenerated;

    public Receipt(Applicant bookedApplicant, Project project) {
        if (bookedApplicant != null && project != null && bookedApplicant.getRoomChosen() != null) {
            this.applicantName = bookedApplicant.getName();
            this.applicantNric = bookedApplicant.getNRIC();
            this.applicantAge = bookedApplicant.getAge();
            this.applicantIsMarried = bookedApplicant.isMarried();
            this.projectName = project.getName();
            this.neighbourhood = project.getNeighbourhood();
            this.flatTypeBooked = bookedApplicant.getRoomChosen();
            this.price = findRoomPrice(project, this.flatTypeBooked);
            this.dateGenerated = LocalDate.now();
        } else {
            System.err.println("Error creating Receipt: Invalid applicant or project data.");
            this.dateGenerated = LocalDate.now();
        }
    }

    private double findRoomPrice(Project project, RoomType type) {
        if (project.getRooms() != null) {
            for (Room room : project.getRooms()) {
                if (room.getRoomType() == type) {
                    return room.getPrice();
                }
            }
        }
        return 0.0;
    }

    public String getApplicantName() { return applicantName; }
    public String getApplicantNric() { return applicantNric; }
    public int getApplicantAge() { return applicantAge; }
    public boolean isApplicantIsMarried() { return applicantIsMarried; }
    public String getProjectName() { return projectName; }
    public String getNeighbourhood() { return neighbourhood; }
    public RoomType getFlatTypeBooked() { return flatTypeBooked; }
    public double getPrice() { return price; }
    public LocalDate getDateGenerated() { return dateGenerated; }

    public String toFormattedString() {
        StringBuilder sb = new StringBuilder();
        String lineSeparator = System.lineSeparator();
        sb.append("-------------------------------------------").append(lineSeparator);
        sb.append("        BTO Booking Confirmation").append(lineSeparator);
        sb.append("-------------------------------------------").append(lineSeparator);
        sb.append("Applicant Name: ").append(applicantName != null ? applicantName : "N/A").append(lineSeparator);
        sb.append("Applicant NRIC: ").append(applicantNric != null ? applicantNric : "N/A").append(lineSeparator);
        sb.append("Age:            ").append(applicantAge).append(lineSeparator);
        sb.append("Marital Status: ").append(applicantIsMarried ? "Married" : "Single").append(lineSeparator);
        sb.append(lineSeparator).append("Booking Details:").append(lineSeparator);
        sb.append("  Project Name:   ").append(projectName != null ? projectName : "N/A").append(lineSeparator);
        sb.append("  Neighbourhood:  ").append(neighbourhood != null ? neighbourhood : "N/A").append(lineSeparator);
        sb.append("  Flat Type Booked: ").append(flatTypeBooked != null ? flatTypeBooked.name() : "N/A").append(lineSeparator);
        sb.append("  Price:          SGD ").append(String.format("%.2f", price)).append(lineSeparator);
        sb.append("-------------------------------------------").append(lineSeparator);
        sb.append("Date Generated: ").append(dateGenerated.format(DateTimeFormatter.ISO_LOCAL_DATE)).append(lineSeparator);
        sb.append("-------------------------------------------").append(lineSeparator);
        return sb.toString();
    }
}