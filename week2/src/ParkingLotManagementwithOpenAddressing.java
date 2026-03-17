import java.util.*;

public class ParkingLotManagementwithOpenAddressing {

    enum Status {
        EMPTY, OCCUPIED, DELETED
    }

    static class ParkingSpot {
        String licensePlate;
        long entryTime;
        Status status;

        public ParkingSpot() {
            this.status = Status.EMPTY;
        }
    }

    private ParkingSpot[] table;
    private int capacity = 500;
    private int size = 0;

    private int totalProbes = 0;
    private int totalOperations = 0;

    private Map<Integer, Integer> hourlyTraffic = new HashMap<>();

    public ParkingLotManagementwithOpenAddressing() {
        table = new ParkingSpot[capacity];
        for (int i = 0; i < capacity; i++) {
            table[i] = new ParkingSpot();
        }
    }

    // Hash function
    private int hash(String licensePlate) {
        int hash = 0;
        for (char c : licensePlate.toCharArray()) {
            hash = (hash * 31 + c) % capacity;
        }
        return hash;
    }

    // Park vehicle
    public String parkVehicle(String licensePlate) {
        int index = hash(licensePlate);
        int probes = 0;

        for (int i = 0; i < capacity; i++) {
            int probeIndex = (index + i) % capacity;
            probes++;

            if (table[probeIndex].status == Status.EMPTY ||
                    table[probeIndex].status == Status.DELETED) {

                table[probeIndex].licensePlate = licensePlate;
                table[probeIndex].entryTime = System.currentTimeMillis();
                table[probeIndex].status = Status.OCCUPIED;

                size++;
                totalProbes += probes;
                totalOperations++;

                int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                hourlyTraffic.put(hour, hourlyTraffic.getOrDefault(hour, 0) + 1);

                return "Assigned spot #" + probeIndex + " (" + (probes - 1) + " probes)";
            }
        }

        return "Parking Full!";
    }

    // Exit vehicle
    public String exitVehicle(String licensePlate) {
        int index = hash(licensePlate);

        for (int i = 0; i < capacity; i++) {
            int probeIndex = (index + i) % capacity;

            if (table[probeIndex].status == Status.EMPTY) {
                return "Vehicle not found!";
            }

            if (table[probeIndex].status == Status.OCCUPIED &&
                    table[probeIndex].licensePlate.equals(licensePlate)) {

                long exitTime = System.currentTimeMillis();
                long durationMillis = exitTime - table[probeIndex].entryTime;

                double hours = durationMillis / (1000.0 * 60 * 60);
                double fee = hours * 5; // $5 per hour

                table[probeIndex].status = Status.DELETED;
                size--;

                return "Spot #" + probeIndex + " freed, Duration: "
                        + String.format("%.2f", hours)
                        + "h, Fee: $" + String.format("%.2f", fee);
            }
        }

        return "Vehicle not found!";
    }

    // Get statistics
    public String getStatistics() {
        double occupancy = (size * 100.0) / capacity;
        double avgProbes = totalOperations == 0 ? 0 : (double) totalProbes / totalOperations;

        int peakHour = -1, max = 0;
        for (Map.Entry<Integer, Integer> entry : hourlyTraffic.entrySet()) {
            if (entry.getValue() > max) {
                max = entry.getValue();
                peakHour = entry.getKey();
            }
        }

        return "Occupancy: " + String.format("%.2f", occupancy) + "%, " +
                "Avg Probes: " + String.format("%.2f", avgProbes) + ", " +
                "Peak Hour: " + peakHour + ":00-" + (peakHour + 1) + ":00";
    }

    // Test
    public static void main(String[] args) {
        ParkingLotManagementwithOpenAddressing system =
                new ParkingLotManagementwithOpenAddressing();

        System.out.println(system.parkVehicle("ABC-1234"));
        System.out.println(system.parkVehicle("ABC-1235"));
        System.out.println(system.parkVehicle("XYZ-9999"));

        try { Thread.sleep(2000); } catch (Exception e) {}

        System.out.println(system.exitVehicle("ABC-1234"));
        System.out.println(system.getStatistics());
    }
}
