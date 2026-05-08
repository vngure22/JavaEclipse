import java.util.*;
import java.io.*;

/**
 * TestDroneHangar handles the User Interface and File I/O operations.
 * It coordinates between the user's input and the Hangar data management.
 */
public class TestDroneHangar {
    /**
     * Main entry point for the Drone Hangar Management System.
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        Hangar hangarManager = new Hangar();
        Scanner inputScanner = new Scanner(System.in);
        boolean isRunning = true;

        System.out.println("--- Drone Hangar Management System ---");

        while (isRunning) {
            displayMenu();
            String userChoice = inputScanner.nextLine().trim();

            if (userChoice.equals("1")) {
                System.out.print("Enter the name of the CSV file: ");
                String fileName = inputScanner.nextLine().trim();
                processFileLoading(fileName, hangarManager);
            } 
            else if (userChoice.equals("2")) {
                printDroneList(hangarManager.getAllDrones());
            } 
            else if (userChoice.equals("3")) {
                System.out.print("Enter Drone ID to search (e.g., D1000): ");
                String searchId = inputScanner.nextLine().trim();
                Drone foundDrone = hangarManager.findDroneById(searchId);
                System.out.println(foundDrone != null ? foundDrone : "Error: Drone ID not found.");
            } 
            else if (userChoice.equals("4")) {
                System.out.print("Enter Drone ID to add to Maintenance Queue: ");
                String queueId = inputScanner.nextLine().trim();
                boolean isAdded = hangarManager.addToMaintenanceQueue(queueId);
                if (isAdded) {
                    System.out.println("Success: Drone added to the maintenance queue.");
                } else {
                    System.out.println("Notice: Drone ID not found or already in the queue.");
                }
            } 
            else if (userChoice.equals("5")) {
                Drone processedDrone = hangarManager.processNextMaintenanceTask();
                if (processedDrone != null) {
                    System.out.println("Now repairing: " + processedDrone);
                } else {
                    System.out.println("Notice: The maintenance queue is currently empty.");
                }
            } 
            else if (userChoice.equals("6")) {
                System.out.println("Inventory sorted by Payload Capacity:");
                printDroneList(hangarManager.getDronesSortedByPayload());
            } 
            else if (userChoice.equals("7")) {
                System.out.print("Enter Manufacturer: ");
                String mfg = inputScanner.nextLine().trim();
                System.out.print("Enter Drone Type: ");
                String type = inputScanner.nextLine().trim();
                
                ArrayList<Drone> results = hangarManager.searchByManufacturerandType(mfg, type);
                System.out.println("\nSearch Results:");
                printDroneList(results);
            }
            else if (userChoice.equals("8")) {
                System.out.println("Exiting system. Goodbye.");
                isRunning = false;
            } 
            else {
                System.out.println("Invalid selection. Please choose 1-8.");
            }
        }
        inputScanner.close();
    }

    /**
     * Reads the CSV file and populates the Hangar.
     * Format expected: Category, Type, Manufacturer, Year, Payload
     * @param fileName The name of the file to load.
     * @param hangar The Hangar instance to populate.
     */
    private static void processFileLoading(String fileName, Hangar hangar) {
        try (Scanner fileScanner = new Scanner(new File(fileName))) {
            int count = 0;
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine();
                if (line.trim().isEmpty()) continue;

                String[] details = line.split(",");
                if (details.length == 5) {
                    String category = details[0].trim();
                    String droneType = details[1].trim();
                    String manufacturer = details[2].trim();
                    int modelYear = Integer.parseInt(details[3].trim());
                    double payload = Double.parseDouble(details[4].trim());

                    Drone newDrone;
                    if (category.equalsIgnoreCase("P")) {
                        newDrone = new PriorityDrone(droneType, manufacturer, modelYear, payload);
                    } else {
                        newDrone = new StandardDrone(droneType, manufacturer, modelYear, payload);
                    }
                    hangar.registerDrone(newDrone);
                    count++;
                }
            }
            System.out.println("Successfully loaded " + count + " drones into the hangar.");
        } catch (FileNotFoundException e) {
            System.out.println("Error: Could not find file '" + fileName + "'.");
        } catch (Exception e) {
            System.out.println("Error parsing file: " + e.getMessage());
        }
    }

    /**
     * Displays the interactive menu options to the console.
     */
    private static void displayMenu() {
        System.out.println("\nMain Menu:");
        System.out.println("1. Load Drones from CSV");
        System.out.println("2. Display All Drones");
        System.out.println("3. Search by Unique ID");
        System.out.println("4. Add Drone to Maintenance");
        System.out.println("5. Process Next Maintenance Task");
        System.out.println("6. View Drones Sorted by Payload");
        System.out.println("7. Search by Manufacturer and Type");
        System.out.println("8. Exit");
        System.out.print("Select an option: ");
    }

    /**
     * Iterates through a list of drones and prints their details.
     * @param droneList The list of drones to display.
     */
    private static void printDroneList(List<Drone> droneList) {
        if (droneList.isEmpty()) {
            System.out.println("No matching drones found.");
        } else {
            for (Drone drone : droneList) {
                System.out.println(drone);
            }
        }
    }
}

/**
 * Abstract class representing the general Drone properties.
 * Serves as the base for all specific drone types.
 */
abstract class Drone {
    private static int idGenerator = 1000;
    private String droneID;
    private String type;
    private String manufacturer;
    private int year;
    private double payloadCapacity;

    /**
     * Initializes a new Drone with unique ID generation.
     * @param type The specific model/type name.
     * @param manufacturer The company that built the drone.
     * @param year The year of manufacture.
     * @param payloadCapacity The maximum weight the drone can carry.
     */
    public Drone(String type, String manufacturer, int year, double payloadCapacity) {
        this.droneID = "D" + idGenerator++;
        this.type = type;
        this.manufacturer = manufacturer;
        this.year = year;
        this.payloadCapacity = payloadCapacity;
    }

    /** @return The unique system-generated ID. */
    public String getDroneID() { return droneID; }

    /** @return The payload capacity in kilograms. */
    public double getPayloadCapacity() { return payloadCapacity; }
    
    /** @return The specific model/type of the drone. */
    public String getType() { return type; }

    /** @return The manufacturer name. */
    public String getManufacturer() { return manufacturer; }

    /**
     * Returns a formatted string representing the drone.
     * @return Formatted drone details.
     */
    @Override
    public String toString() {
        return String.format("[%s] %s %s | Year: %d | Payload: %.1fkg", 
                droneID, manufacturer, type, year, payloadCapacity);
    }
}

/**
 * Represents a standard operational drone.
 */
class StandardDrone extends Drone {
    /**
     * Constructs a StandardDrone.
     * @param type The model name.
     * @param manufacturer The manufacturer name.
     * @param year The year of manufacture.
     * @param payload The payload capacity.
     */
    public StandardDrone(String type, String manufacturer, int year, double payload) {
        super(type, manufacturer, year, payload);
    }
    @Override
    public String toString() { return "Standard " + super.toString(); }
}

/**
 * Represents a drone with high priority for maintenance and operations.
 */
class PriorityDrone extends Drone {
    /**
     * Constructs a PriorityDrone.
     * @param type The model name.
     * @param manufacturer The manufacturer name.
     * @param year The year of manufacture.
     * @param payload The payload capacity.
     */
    public PriorityDrone(String type, String manufacturer, int year, double payload) {
        super(type, manufacturer, year, payload);
    }
    @Override
    public String toString() { return "PRIORITY " + super.toString(); }
}

/**
 * Hangar manages the data structures: ArrayList (Inventory), 
 * HashMap (ID Lookup), and Queue (Maintenance).
 */
class Hangar {
    private ArrayList<Drone> droneInventory = new ArrayList<>();
    private HashMap<String, Drone> idLookupMap = new HashMap<>();
    private Queue<Drone> maintenanceQueue = new LinkedList<>();

    /**
     * Registers a drone into both the inventory list and the ID lookup map.
     * @param drone The drone object to register.
     */
    public void registerDrone(Drone drone) {
        droneInventory.add(drone);
        idLookupMap.put(drone.getDroneID().toUpperCase(), drone);
    }

    /**
     * Retrieves a drone based on its unique ID.
     * @param id The ID string (case-insensitive).
     * @return The Drone object if found, otherwise null.
     */
    public Drone findDroneById(String id) {
        return idLookupMap.get(id.toUpperCase());
    }

    /**
     * Filters the inventory for drones matching a specific manufacturer and type.
     * @param mfg The manufacturer name to search for.
     * @param type The drone type to search for.
     * @return A list of matching drones.
     */
    public ArrayList<Drone> searchByManufacturerandType(String mfg, String type) {
        ArrayList<Drone> matches = new ArrayList<>();
        for (Drone drone : droneInventory) {
            if (drone.getManufacturer().equalsIgnoreCase(mfg) && 
                drone.getType().equalsIgnoreCase(type)) {
                matches.add(drone);
            }
        }
        return matches;
    }

    /**
     * Returns the full list of drones in the hangar.
     * @return ArrayList of all registered drones.
     */
    public ArrayList<Drone> getAllDrones() {
        return droneInventory;
    }

    /**
     * Adds a drone to the maintenance queue. Prevents duplicates.
     * @param id The ID of the drone to queue.
     * @return true if added successfully, false if not found or already in queue.
     */
    public boolean addToMaintenanceQueue(String id) {
        Drone targetDrone = findDroneById(id);
        if (targetDrone != null && !maintenanceQueue.contains(targetDrone)) {
            return maintenanceQueue.add(targetDrone);
        }
        return false;
    }

    /**
     * Removes and returns the next drone waiting for maintenance.
     * @return The next Drone in the queue, or null if empty.
     */
    public Drone processNextMaintenanceTask() {
        return maintenanceQueue.poll();
    }

    /**
     * Returns a copy of the inventory sorted by payload capacity (ascending).
     * @return A sorted ArrayList of drones.
     */
    public ArrayList<Drone> getDronesSortedByPayload() {
        ArrayList<Drone> sortedList = new ArrayList<>(droneInventory);
        Collections.sort(sortedList, new Comparator<Drone>() {
            @Override
            public int compare(Drone drone1, Drone drone2) {
                return Double.compare(drone1.getPayloadCapacity(), drone2.getPayloadCapacity());
            }
        });
        return sortedList;
    }
}
