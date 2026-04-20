import java.util.ArrayList;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * The main starting point for the program. 
 * This class handles the user menu and runs the hangar system.
 */
public class TestDroneHangar {
    /**
     * Starts the program and keeps the menu running until the user quits.
     * @param args Command line arguments (not used here).
     */
    public static void main(String[] args) {
        Hangar hangar = new Hangar();
        boolean running = true;

        System.out.println("Welcome to the Drone Hangar Management System");

        while (running) {
            hangar.displayConsoleMenu();
            String choice = hangar.getUserInput();

            if (choice.equals("1")) {
                hangar.loadFromCSV();
            } 
            else if (choice.equals("2")) {
                hangar.displayHangarInventory();
            } 
            else if (choice.equals("3")) {
                hangar.displayDronesByTypeAndMFG();
            } 
            else if (choice.equals("4")) {
                System.out.print("Enter manufacturer name: ");
                String name = hangar.getUserInput();
                hangar.displayManufacturerCount(name);
            } 
            else if (choice.equals("5")) {
                hangar.displayByPayload();
            } 
            else if (choice.equals("6")) {
                hangar.displayDronebyYear();
            } 
            else if (choice.equals("7")) {
                System.out.println("Exiting program. Goodbye!");
                running = false; 
            } 
            else {
                System.out.println("Invalid option. Please pick 1-7.");
            }
        }
    }
}

/**
 * The general template for all drones.
 * This class is abstract, meaning you can't make a "generic" drone object.
 */
abstract class Drone {
    private String type;
    private String manufacturer;
    private int year;
    private double payloadKg;

    /**
     * Sets up the basic data for any drone.
     * @param type The model name.
     * @param manufacturer Who made it.
     * @param year When it was built.
     * @param payload How much weight it can carry.
     */
    public Drone(String type, String manufacturer, int year, double payload) {
        this.type = type;
        this.manufacturer = manufacturer;
        this.year = year;
        this.payloadKg = payload;
    }

    public String getType() { return type; }
    public String getManufacturer() { return manufacturer; }
    public int getYear() { return year; }
    public double getPayloadKg() { return payloadKg; }

    /**
     * Formats the drone data into a readable string for printing.
     * @return A nice summary of the drone's details.
     */
    @Override
    public String toString() {
        return String.format("[Type: %s, Maker: %s, Year: %d, Payload: %.2fkg]",
                type, manufacturer, year, payloadKg);
    }

    /**
     * Compares this drone to another to see if they are identical.
     * @param other The other object to check.
     * @return True if all the details match.
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || !(other instanceof Drone)) return false;
        Drone d = (Drone) other;
        return year == d.year
            && Math.abs(payloadKg - d.payloadKg) < 0.0001
            && type.equalsIgnoreCase(d.type)
            && manufacturer.equalsIgnoreCase(d.manufacturer);
    }
}

/**
 * A regular drone with no special priority status.
 */
class StandardDrone extends Drone {
    public StandardDrone(String type, String manufacturer, int year, double payload) {
        super(type, manufacturer, year, payload);
    }

    @Override
    public String toString() {
        return "STANDARD " + super.toString();
    }
}

/**
 * A high-importance drone that gets special labeling.
 */
class PriorityDrone extends Drone {
    public PriorityDrone(String type, String manufacturer, int year, double payload) {
        super(type, manufacturer, year, payload);
    }

    @Override
    public String toString() {
        return "PRIORITY " + super.toString();
    }
}

/**
 * The main manager for the hangar.
 * It stores the drones and handles all the sorting and file reading.
 */
class Hangar {
    private ArrayList<Drone> drones; 
    private Drone[] sortedDrones;    
    private final Scanner scanner = new Scanner(System.in);

    /**
     * Creates a new hangar with an empty list.
     */
    public Hangar() {
        drones = new ArrayList<>();
        sortedDrones = new Drone[0];
    }

    /**
     * Prints the user menu options.
     */
    public void displayConsoleMenu() {
        System.out.println("\n--- HANGAR OPERATIONS ---");
        System.out.println("1. Load CSV (Category,Type,Maker,Year,Payload)");
        System.out.println("2. Display All");
        System.out.println("3. Search Type/Maker");
        System.out.println("4. Count Maker");
        System.out.println("5. Sort Payload (Selection Sort)");
        System.out.println("6. Sort Year (Insertion Sort)");
        System.out.println("7. Exit");
        System.out.print("Select: ");
    }

    /**
     * Gets whatever the user types into the console.
     * @return The text typed by the user.
     */
    public String getUserInput() { return scanner.nextLine().trim(); }

    /**
     * Checks if there are any drones in the hangar.
     * @return True if the list has at least one drone.
     */
    public boolean hasDrones() { return !drones.isEmpty(); }

    /**
     * Reads a file and adds drones to the list.
     * @param filename The name of the file to open.
     */
    public void loadDronesFromCSV(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line = reader.readLine();
            int lineNum = 0;
            while (line != null) {
                lineNum++;
                if (!line.trim().isEmpty()) {
                    Drone d = parseDroneLine(line, lineNum);
                    if (d != null) {
                        if (!addDrone(d)) {
                            System.out.println("Line " + lineNum + ": Skipping duplicate.");
                        }
                    }
                }
                line = reader.readLine();
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }

    /**
     * Asks the user for a filename to load data from.
     */
    public void loadFromCSV() {
        System.out.print("Filename: ");
        String name = getUserInput();
        if (!name.isEmpty()) {
            loadDronesFromCSV(name);
        }
    }

    /**
     * Takes a line of text from the CSV and converts it into a Drone object.
     * @param line The text line from the file.
     * @param lineNum Used to tell the user where an error happened.
     * @return A Standard or Priority drone, or null if the data is bad.
     */
    public Drone parseDroneLine(String line, int lineNum) {
        String[] p = line.split(",");
        if (p.length != 5) return null;

        try {
            String category = p[0].trim().toUpperCase();
            String type = p[1].trim();
            String mfg = p[2].trim();
            int year = Integer.parseInt(p[3].trim());
            double payload = Double.parseDouble(p[4].trim());

            if (category.equals("P")) {
                return new PriorityDrone(type, mfg, year, payload);
            } else {
                return new StandardDrone(type, mfg, year, payload);
            }
        } catch (Exception e) {
            System.out.println("Line " + lineNum + ": Data format error.");
            return null;
        }
    }

    /**
     * Adds a drone to the list, but only if it's not already in there.
     * @param d The drone to be added.
     * @return True if the drone was added successfully.
     */
    public boolean addDrone(Drone d) {
        if (d == null || findDuplicateDrone(d)) return false;
        drones.add(d);
        return true;
    }

    /**
     * Looks through the list to see if a drone already exists.
     * @param d The drone to look for.
     * @return True if it's already in the hangar.
     */
    public boolean findDuplicateDrone(Drone d) {
        boolean found = false;
        int i = 0;
        while (i < drones.size() && !found) {
            if (drones.get(i).equals(d)) { found = true; }
            i++;
        }
        return found;
    }

    /**
     * Prints every drone currently in the hangar list.
     */
    public void displayHangarInventory() {
        if (!hasDrones()) {
            System.out.println("Hangar empty.");
        } else {
            for (Drone d : drones) { System.out.println(d); }
        }
    }

    /**
     * Sorts the drones by payload capacity using Selection Sort.
     */
    public void sortDronesByPayload() {
        sortedDrones = drones.toArray(new Drone[0]);
        int n = sortedDrones.length;
        for (int i = 0; i < n - 1; i++) {
            int minIdx = i;
            for (int j = i + 1; j < n; j++) {
                if (sortedDrones[j].getPayloadKg() < sortedDrones[minIdx].getPayloadKg()) {
                    minIdx = j;
                }
            }
            Drone temp = sortedDrones[minIdx];
            sortedDrones[minIdx] = sortedDrones[i];
            sortedDrones[i] = temp;
        }
    }

    /**
     * Runs the payload sort and then prints the sorted list.
     */
    public void displayByPayload() {
        if (hasDrones()) {
            sortDronesByPayload();
            for (Drone d : sortedDrones) { System.out.println(d); }
        }
    }

    /**
     * Sorts the drones by their manufacturing year using Insertion Sort.
     * @return A sorted array of drones.
     */
    public Drone[] sortDronesByYear() {
        Drone[] sorted = drones.toArray(new Drone[0]);
        for (int i = 1; i < sorted.length; i++) {
            Drone key = sorted[i];
            int j = i - 1;
            while (j >= 0 && sorted[j].getYear() > key.getYear()) {
                sorted[j + 1] = sorted[j];
                j--;
            }
            sorted[j + 1] = key;
        }
        return sorted;
    }

    /**
     * Runs the year sort and then prints the results.
     */
    public void displayDronebyYear() {
        if (hasDrones()) {
            Drone[] results = sortDronesByYear();
            for (Drone d : results) { System.out.println(d); }
        }
    }

    /**
     * Searches for drones that match a specific type and maker.
     */
    public void displayDronesByTypeAndMFG() {
        System.out.print("Search Type: ");
        String t = getUserInput().toLowerCase();
        System.out.print("Search Maker: ");
        String m = getUserInput().toLowerCase();

        for (Drone d : drones) {
            if (d.getType().toLowerCase().contains(t) && d.getManufacturer().toLowerCase().contains(m)) {
                System.out.println(d);
            }
        }
    }

    /**
     * Counts how many drones in the hangar were made by a specific company.
     * @param key The manufacturer name to search for.
     */
    public void displayManufacturerCount(String key) {
        int count = 0;
        for (Drone d : drones) {
            if (d.getManufacturer().equalsIgnoreCase(key)) { count++; }
        }
        System.out.println("Found: " + count);
    }
}
