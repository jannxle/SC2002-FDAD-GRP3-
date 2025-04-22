package boundary;

import java.util.InputMismatchException;
import java.util.Scanner;

import entities.Filter;
import enums.RoomType;

public class FilterUI {

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



