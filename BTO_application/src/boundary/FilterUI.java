package boundary;

import java.util.InputMismatchException;
import java.util.Scanner;

import entities.Filter;
import enums.RoomType;

/**
 * Provides the command-line user interface elements for managing project view filters.
 * This class contains static methods that interact with the user to set or reset
 * filter criteria based on neighbourhood and room type.
 */
public class FilterUI {

    /**
	 * Displays a menu to the user for managing project filter settings and prompts for input.
	 * Allows the user to set a neighbourhood filter, set a room type filter, reset all filters,
	 * or return to the previous menu. The provided Filter object is modified directly
	 * based on user input.
	 *
	 * @param scanner The Scanner object used to read user input from the console.
	 * @param filter  The Filter object representing the current user's filter settings.
	 * This object will be updated by the method based on user choices.
	 */
	public static void promptFilterSettings(Scanner scanner, Filter filter) {
        while (true) {
            System.out.println("\n========= Project Filter Settings =========");
            System.out.println("Current Neighbourhood Filter: " + (filter.getNeighbourhood() != null ? filter.getNeighbourhood() : "None"));
            System.out.println("Current Room Type Filter: " + (filter.getRoomType() != null ? filter.getRoomType() : "None"));
            System.out.println("===========================================");
            System.out.println("1. Set Neighbourhood Filter");
            System.out.println("2. Set Room Type Filter");
            System.out.println("3. Reset All Filters");
            System.out.println("0. Back to Main Menu");
            System.out.println();
            System.out.print("Choose an option: ");

            int choice = -1;
            try { choice = scanner.nextInt(); } catch (InputMismatchException e) {}
            finally { scanner.nextLine(); }

            switch (choice) {
                case 1:
                    System.out.print("Enter neighbourhood: ");
                    String n = scanner.nextLine().trim();
                    filter.setNeighbourhood(n.isEmpty() ? null : n);
                    break;
                case 2:
                    RoomType[] values = RoomType.values();
                    for (int i = 0; i < values.length; i++)
                        System.out.println((i + 1) + ". " + values[i]);
                    System.out.print("Enter your preferred room type: ");
                    int sel = scanner.nextInt(); scanner.nextLine();
                    if (sel > 0 && sel <= values.length)
                        filter.setRoomType(values[sel - 1]);
                    break;
                case 3:
                    filter.reset();
                    System.out.println("All filters reset.");
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Invalid option.");
            }
        }
	}
}



