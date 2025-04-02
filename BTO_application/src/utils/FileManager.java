package utils;

import java.io.*;
import java.util.*;

public class FileManager {

    // Reads the entire file and returns each line as a string in a list
    public static List<String> readFile(String filePath) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while((line = br.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + filePath);
            e.printStackTrace();
        }
        return lines;
    }

    // Optional: Writes a list of strings to a file (overwrite)
    public static void writeFile(String filePath, List<String> lines) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            for (String line : lines) {
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error writing to file: " + filePath);
        }
    }

    // Optional: Appends a single line to a file
    public static void appendToFile(String filePath, String line) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, true))) {
            bw.write(line);
            bw.newLine();
        } catch (IOException e) {
            System.out.println("Error appending to file: " + filePath);
        }
    }
}

