package se.oru.coordination.coordination_oru.vehicles;

import java.util.HashMap;

/**
 * Singleton class to maintain a mapping of vehicle IDs to their respective vehicles.
 */
public class VehiclesHashMap {

    private static volatile VehiclesHashMap instance;
    private static final HashMap<Integer, AbstractVehicle> list = new HashMap<>();
    private static final Object lock = new Object();
    private static final HashMap<Integer, Double> vehicleMaxVelocity = new HashMap<>();
    private static final HashMap<Integer, Double> vehicleMaxAcceleration = new HashMap<>();
    private static final HashMap<Integer, Integer> vehicleTrackingPeriod = new HashMap<>();

    /**
     * Private constructor to prevent direct instantiation.
     */
    private VehiclesHashMap() {}

    /**
     * Retrieves the singleton instance of VehiclesHashMap.
     *
     * @return The singleton instance of VehiclesHashMap.
     */
    public static VehiclesHashMap getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new VehiclesHashMap();
                }
            }
        }
        return instance;
    }

    /**
     * Retrieves a vehicle by its ID.
     *
     * @param key The ID of the vehicle.
     * @return The vehicle associated with the given ID.
     */
    public static AbstractVehicle getVehicle(int key) {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new VehiclesHashMap();
                }
            }
        }
        return getList().get(key);
    }

    /**
     * Retrieves the list of all vehicles.
     *
     * @return A hashmap containing all vehicle IDs and their associated vehicles.
     */
    public synchronized static HashMap<Integer, AbstractVehicle> getList() {
        return list;
    }

    /**
     * Retrieves a mapping of vehicle IDs to their max velocities.
     *
     * @return A hashmap where each key is a vehicle ID and its value is the corresponding vehicle's max velocity.
     */
    public static HashMap<Integer, Double> getVehicleMaxVelocities() {

        for (Integer vehicleID : list.keySet()) {
            AbstractVehicle vehicle = list.get(vehicleID);
            vehicleMaxVelocity.put(vehicleID, vehicle.getMaxVelocity());
        }
        return vehicleMaxVelocity;
    }

    /**
     * Retrieves a mapping of vehicle IDs to their max accelerations.
     *
     * @return A hashmap where each key is a vehicle ID and its value is the corresponding vehicle's max acceleration.
     */
    public static HashMap<Integer, Double> getVehicleMaxAccelerations() {

        for (Integer vehicleID : list.keySet()) {
            AbstractVehicle vehicle = list.get(vehicleID);
            vehicleMaxAcceleration.put(vehicleID, vehicle.getMaxAcceleration());
        }
        return vehicleMaxAcceleration;
    }

    /**
     * Retrieves a mapping of vehicle IDs to their tracking periods.
     *
     * @return A hashmap where each key is a vehicle ID and its value is the corresponding vehicle's tracking period.
     */
    public static HashMap<Integer, Integer> getVehicleTrackingPeriod() {

        for (Integer vehicleID : list.keySet()) {
            AbstractVehicle vehicle = list.get(vehicleID);
            vehicleTrackingPeriod.put(vehicleID, vehicle.getTrackingPeriod());
        }
        return vehicleTrackingPeriod;
    }

    public static void removeVehicle(int ID) {
        list.remove(ID);
    }

}
