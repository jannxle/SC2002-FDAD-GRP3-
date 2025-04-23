package entities;

import enums.RoomType;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Represents a booking confirmation receipt generated for an applicant
 * after they have successfully booked a flat (status is BOOKED).
 * Contains a snapshot of the applicant's details, the booked project details,
 * the flat type, price, and the date the receipt was generated.
 */
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

    /**
     * Constructs a new Receipt object.
     * Initializes the receipt with details from the booked applicant and project.
     * If any of the required details are missing, an error message is printed,
     * and the dateGenerated is set to the current date.
     *
     * @param bookedApplicant The Applicant who booked the flat.
     * @param project         The Project associated with the booking.
     */
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

    /**
     * Private helper method to find the price of a specific room type within a given project.
     * Iterates through the project's rooms to find a match.
     *
     * @param project The project containing the room information.
     * @param type    The room type whose price is needed.
     * @return The price of the specified room type, or 0.0 if the room type is not found in the project.
     */ 
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

    /**
     * Gets the applicant's name from the receipt.
     * @return The applicant's name.
     */
    public String getApplicantName() { return applicantName; }

    /**
     * Gets the applicant's NRIC from the receipt.
     * @return The applicant's NRIC.
     */
    public String getApplicantNric() { return applicantNric; }

    /**
     * Gets the applicant's age from the receipt.
     * @return The applicant's age.
     */
    public int getApplicantAge() { return applicantAge; }

    /**
     * Gets the applicant's marital status from the receipt.
     * @return true if married, false if single.
     */
    public boolean isApplicantIsMarried() { return applicantIsMarried; }

    /**
     * Gets the project name from the receipt.
     * @return The name of the booked project.
     */
    public String getProjectName() { return projectName; }

    /**
     * Gets the project neighbourhood from the receipt.
     * @return The neighbourhood of the booked project.
     */
    public String getNeighbourhood() { return neighbourhood; }

    /**
     * Gets the type of flat booked from the receipt.
     * @return The RoomType of the booked flat.
     */
    public RoomType getFlatTypeBooked() { return flatTypeBooked; }

    /**
     * Gets the price of the booked flat from the receipt.
     * @return The price.
     */
    public double getPrice() { return price; }

    /**
     * Gets the date when the receipt was generated.
     * @return The generation date as a LocalDate.
     */
    public LocalDate getDateGenerated() { return dateGenerated; }

    /**
     * Generates a formatted string representation of the receipt, suitable for display or printing.
     * Includes applicant details, booking details (project, flat type, price), and generation date.
     *
     * @return A multi-line string containing the formatted receipt information.
     */
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