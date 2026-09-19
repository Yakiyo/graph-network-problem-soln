# DroneNet Opt 🚁

**Autonomous Drone Network - Operations Center & Optimization Visualizer**

DroneNet Opt is a JavaFX application designed to simulate and visualize optimal drone deployment routes for disaster response scenarios. It serves as an interactive learning tool for complex network algorithms.

## Features

- **Max Flow Optimization (Edmonds-Karp)**: Calculates the maximum number of drones that can be deployed simultaneously from the dispatch center to the disaster area without bottlenecking flight corridors. Features a step-by-step UI animation of the algorithm finding and traversing augmenting paths in real-time.
- **First Responder Deployment (Shortest Path)**: Uses Breadth-First Search (BFS) to trace the absolute shortest physical route for a single elite drone, animating the path edge-by-edge on screen.
- **Drone Fleet Management (Greedy Algorithm)**: Automatically generates a random fleet of drones on startup and sorts them dynamically using a greedy algorithm (prioritizing highest Payload Capacity, then Battery Life) to select the optimal drones for any mission.

## Requirements

To build and run this project, you need the following installed on your system:
- **Java Development Kit (JDK) 17** (or higher)
- **Apache Maven** (for dependency management and running)

## How to Build and Run

This project uses the `javafx-maven-plugin` which automatically manages the JavaFX dependencies so you don't have to configure any module paths manually.

1. Open your terminal or command prompt.
2. Navigate to the root directory of the project (where the `pom.xml` file is located).
3. **Compile the project**:
   ```bash
   mvn clean compile
   ```
4. **Run the application**:
   ```bash
   mvn javafx:run
   ```

---

**Disclaimer:** *This project and its source code were generated with the assistance of Artificial Intelligence (AI) during an interactive pair-programming session.*
