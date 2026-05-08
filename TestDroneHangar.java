import java.util.*;
import java.io.*;

/**
 * TestDroneHangar handles the User Interface and File I/O operations.
 * It coordinates between the user's input and the Hangar data management.
 */
public class TestDroneHangar {
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
                System.out.println("Exiting system. Goodbye.");
                isRunning = false;
            } 
            else {
                System.out.println("Invalid selection. Please choose 1-7.");
            }
        }
        inputScanner.close();
    }

    /**
     * Reads the CSV file and populates the Hangar.
     * Format expected: Category, Type, Manufacturer, Year, Payload
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

    private static void displayMenu() {
        System.out.println("\nMain Menu:");
        System.out.println("1. Load Drones from CSV");
        System.out.println("2. Display All Drones");
        System.out.println("3. Search by Unique ID");
        System.out.println("4. Add Drone to Maintenance");
        System.out.println("5. Process Next Maintenance Task");
        System.out.println("6. View Drones Sorted by Payload");
        System.out.println("7. Exit");
        System.out.print("Select an option: ");
    }

    private static void printDroneList(List<Drone> droneList) {
        if (droneList.isEmpty()) {
            System.out.println("The inventory is currently empty.");
        } else {
            for (Drone drone : droneList) {
                System.out.println(drone);
            }
        }
    }
}
/**
 * Abstract class representing the general Drone properties.
 */
abstract class Drone {
    private static int idGenerator = 1000;
    private String droneID;
    private String type;
    private String manufacturer;
    private int year;
    private double payloadCapacity;

    public Drone(String type, String manufacturer, int year, double payloadCapacity) {
        this.droneID = "D" + idGenerator++;
        this.type = type;
        this.manufacturer = manufacturer;
        this.year = year;
        this.payloadCapacity = payloadCapacity;
    }

    public String getDroneID() { return droneID; }
    public double getPayloadCapacity() { return payloadCapacity; }

    @Override
    public String toString() {
        return String.format("[%s] %s %s | Year: %d | Payload: %.1fkg", 
                droneID, manufacturer, type, year, payloadCapacity);
    }
}

class StandardDrone extends Drone {
    public StandardDrone(String type, String manufacturer, int year, double payload) {
        super(type, manufacturer, year, payload);
    }
    @Override
    public String toString() { return "Standard " + super.toString(); }
}

class PriorityDrone extends Drone {
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

    public void registerDrone(Drone drone) {
        droneInventory.add(drone);
        idLookupMap.put(drone.getDroneID().toUpperCase(), drone);
    }

    public Drone findDroneById(String id) {
        return idLookupMap.get(id.toUpperCase());
    }

    public ArrayList<Drone> getAllDrones() {
        return droneInventory;
    }

    /**
     * Prevents duplicates by checking if the drone is already in the queue.
     */
    public boolean addToMaintenanceQueue(String id) {
        Drone targetDrone = findDroneById(id);
        if (targetDrone != null && !maintenanceQueue.contains(targetDrone)) {
            return maintenanceQueue.add(targetDrone);
        }
        return false;
    }

    public Drone processNextMaintenanceTask() {
        return maintenanceQueue.poll();
    }

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
