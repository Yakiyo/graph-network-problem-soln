package com.dronenetwork;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * DroneFleetManager handles the inventory and selection of drones
 * using greedy algorithms for optimal deployment.
 */
public class DroneFleetManager {

    /**
     * Drone class represents an individual autonomous drone.
     */
    public static class Drone {
        private String id;
        private double payloadCapacity; // e.g., in kg
        private double batteryLife;     // e.g., in minutes

        public Drone(String id, double payloadCapacity, double batteryLife) {
            this.id = id;
            this.payloadCapacity = payloadCapacity;
            this.batteryLife = batteryLife;
        }

        public String getId() { return id; }
        public double getPayloadCapacity() { return payloadCapacity; }
        public double getBatteryLife() { return batteryLife; }

        @Override
        public String toString() {
            return String.format("Drone %s [Payload: %.1f kg, Battery: %.1f min]", id, payloadCapacity, batteryLife);
        }
    }

    private List<Drone> availableDrones;

    public DroneFleetManager() {
        availableDrones = new ArrayList<>();
    }

    /**
     * Generates a random fleet of drones for the simulation.
     * @param count The number of drones to generate.
     */
    public void generateRandomFleet(int count) {
        Random rand = new Random();
        for (int i = 1; i <= count; i++) {
            String id = "DRN-" + String.format("%03d", i);
            // Random payload between 2.0 and 10.0 kg
            double payload = 2.0 + (8.0 * rand.nextDouble());
            // Random battery life between 20 and 60 minutes
            double battery = 20.0 + (40.0 * rand.nextDouble());
            availableDrones.add(new Drone(id, payload, battery));
        }
    }

    public List<Drone> getAvailableDrones() {
        return availableDrones;
    }

    /**
     * Greedy Algorithm to sort and select the top N optimal drones.
     * 
     * The greedy choice property here is: Always pick the drone with the 
     * highest payload capacity first (to carry the most supplies). If payload 
     * capacities are equal (or for tie-breaking), pick the one with the highest battery life.
     * 
     * @param requiredDrones The number of drones needed.
     * @return A sorted list of the top selected drones.
     */
    public List<Drone> getOptimalFleet(int requiredDrones) {
        // Create a copy of the available drones to sort
        List<Drone> sortedDrones = new ArrayList<>(availableDrones);

        // Sort using a custom comparator (Greedy strategy)
        sortedDrones.sort(new Comparator<Drone>() {
            @Override
            public int compare(Drone d1, Drone d2) {
                // 1. Primary sorting criteria: Payload Capacity (Descending)
                int payloadCompare = Double.compare(d2.getPayloadCapacity(), d1.getPayloadCapacity());
                
                // 2. Secondary sorting criteria (Tie-breaker): Battery Life (Descending)
                if (payloadCompare == 0) {
                    return Double.compare(d2.getBatteryLife(), d1.getBatteryLife());
                }
                return payloadCompare;
            }
        });

        // Return the top N drones (or fewer if we don't have enough)
        int limit = Math.min(requiredDrones, sortedDrones.size());
        return sortedDrones.subList(0, limit);
    }
}
