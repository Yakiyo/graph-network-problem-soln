package com.dronenetwork;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

/**
 * DroneNetworkApp visualizes the Ford-Fulkerson Max-Flow algorithm
 * and shortest path for an autonomous drone deployment network.
 */
public class DroneNetworkApp extends Application {

    // Graph definition: 6 nodes (0 to 5)
    // Node 0: Dispatch/Source, Node 5: Disaster/Sink
    private final int NUM_NODES = 6;
    private final int SOURCE = 0;
    private final int SINK = 5;

    // Capacity Matrix representing maximum flight corridor capacities
    private final int[][] capacityMatrix = {
            //S, A,  B,  C,  D,  T
            {0, 16, 13, 0,  0,  0 }, // S (0)
            {0, 0,  10, 12, 0,  0 }, // A (1)
            {0, 4,  0,  0,  14, 0 }, // B (2)
            {0, 0,  9,  0,  0,  20}, // C (3)
            {0, 0,  0,  7,  0,  4 }, // D (4)
            {0, 0,  0,  0,  0,  0 }  // T (5)
    };

    // Logical physical coordinates for nodes (x, y) for UI rendering
    private final double[][] nodeCoords = {
            {100, 300}, // 0: Dispatch (Source)
            {350, 150}, // 1: A
            {350, 450}, // 2: B
            {600, 150}, // 3: C
            {600, 450}, // 4: D
            {850, 300}  // 5: Disaster (Sink)
    };

    // Node labels
    private final String[] nodeNames = {
            "Dispatch", "Node A", "Node B", "Node C", "Node D", "Disaster Area"
    };

    // List to keep track of visual edges for updating later
    private List<EdgeUI> edgeUIs = new ArrayList<>();
    private Pane graphPane;
    private Text resultText;
    private TextArea fleetStatsArea;
    private DroneFleetManager fleetManager;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Autonomous Drone Network - Operations Center");

        // Initialize fleet manager and generate random parameters
        fleetManager = new DroneFleetManager();
        fleetManager.generateRandomFleet(20); // Generate 20 random drones on startup

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #e0e0e0;");

        // The pane where the graph will be drawn
        graphPane = new Pane();
        graphPane.setPrefSize(1000, 500);
        graphPane.setStyle("-fx-background-color: white; -fx-border-color: #999; -fx-border-width: 2;");

        // Draw the edges (lines + arrows + text) before nodes so they sit in the background
        drawEdges();

        // Draw the nodes (circles + text labels)
        drawNodes();

        // Control Panel UI (Buttons)
        HBox buttonBox = new HBox(20);
        
        Button maxFlowBtn = new Button("Deploy Drones (Calculate Max Flow)");
        maxFlowBtn.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        maxFlowBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-padding: 10 20; -fx-cursor: hand;");
        maxFlowBtn.setOnAction(e -> calculateAndDisplayMaxFlow());
        
        Button shortestPathBtn = new Button("Deploy First Responder (Shortest Path)");
        shortestPathBtn.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        shortestPathBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-padding: 10 20; -fx-cursor: hand;");
        shortestPathBtn.setOnAction(e -> deployFirstResponder());

        buttonBox.getChildren().addAll(maxFlowBtn, shortestPathBtn);
        
        resultText = new Text("System Ready. Awaiting Command...");
        resultText.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        resultText.setFill(Color.DARKSLATEGRAY);

        // UI element to show generated random parameters/selected drones
        fleetStatsArea = new TextArea();
        fleetStatsArea.setEditable(false);
        fleetStatsArea.setPrefRowCount(6);
        fleetStatsArea.setFont(Font.font("Monospaced", 12));
        fleetStatsArea.setText("Fleet Manager Initialized. 20 drones available in inventory.\nReady for deployment optimization.");

        root.getChildren().addAll(graphPane, buttonBox, resultText, fleetStatsArea);

        Scene scene = new Scene(root, 1040, 800);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void drawEdges() {
        for (int u = 0; u < NUM_NODES; u++) {
            for (int v = 0; v < NUM_NODES; v++) {
                if (capacityMatrix[u][v] > 0) {
                    double startX = nodeCoords[u][0];
                    double startY = nodeCoords[u][1];
                    double endX = nodeCoords[v][0];
                    double endY = nodeCoords[v][1];

                    Line line = new Line(startX, startY, endX, endY);
                    line.setStrokeWidth(2);
                    line.setStroke(Color.LIGHTGRAY);

                    double angle = Math.atan2(endY - startY, endX - startX);
                    double arrowSize = 12;
                    double arrowDist = 28;

                    double tipX = endX - arrowDist * Math.cos(angle);
                    double tipY = endY - arrowDist * Math.sin(angle);

                    Polygon arrow = new Polygon();
                    arrow.getPoints().addAll(
                            tipX, tipY,
                            tipX - arrowSize * Math.cos(angle - Math.PI / 8),
                            tipY - arrowSize * Math.sin(angle - Math.PI / 8),
                            tipX - arrowSize * Math.cos(angle + Math.PI / 8),
                            tipY - arrowSize * Math.sin(angle + Math.PI / 8)
                    );
                    arrow.setFill(Color.LIGHTGRAY);

                    Text flowText = new Text("0 / " + capacityMatrix[u][v]);
                    flowText.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                    flowText.setFill(Color.DARKGRAY);
                    
                    flowText.setX((startX + endX) / 2 - 15);
                    flowText.setY((startY + endY) / 2 - 10);

                    if (u == 1 && v == 2) {
                        flowText.setX(flowText.getX() + 25);
                    } else if (u == 2 && v == 1) {
                        flowText.setX(flowText.getX() - 25);
                    }

                    graphPane.getChildren().addAll(line, arrow, flowText);
                    edgeUIs.add(new EdgeUI(u, v, line, arrow, flowText));
                }
            }
        }
    }

    private void drawNodes() {
        for (int i = 0; i < NUM_NODES; i++) {
            double x = nodeCoords[i][0];
            double y = nodeCoords[i][1];

            Circle circle = new Circle(x, y, 25);
            circle.setFill(Color.web("#B3E5FC"));
            circle.setStroke(Color.web("#0288D1"));
            circle.setStrokeWidth(3);

            if (i == SOURCE) {
                circle.setFill(Color.web("#C8E6C9"));
                circle.setStroke(Color.web("#388E3C"));
            } else if (i == SINK) {
                circle.setFill(Color.web("#FFCCBC"));
                circle.setStroke(Color.web("#D84315"));
            }

            Text text = new Text(nodeNames[i]);
            text.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            text.setX(x - text.getLayoutBounds().getWidth() / 2);
            text.setY(y - 35); 

            graphPane.getChildren().addAll(circle, text);
        }
    }

    /**
     * Executes the solver and updates the UI lines to reflect the calculated flow.
     */
    private void calculateAndDisplayMaxFlow() {
        MaxFlowSolver solver = new MaxFlowSolver(capacityMatrix);
        int maxFlow = solver.solveMaxFlow(SOURCE, SINK);

        resultText.setText("Max Flow Operation: Total Drones Deployed = " + maxFlow);

        // Fetch optimal fleet based on required number (maxFlow)
        List<DroneFleetManager.Drone> selectedDrones = fleetManager.getOptimalFleet(maxFlow);
        updateFleetUI("Max Flow Deployment", selectedDrones);

        // Update visual elements for each edge based on the solver's flow calculations
        for (EdgeUI edge : edgeUIs) {
            int flow = solver.getFlow(edge.u, edge.v);
            int capacity = capacityMatrix[edge.u][edge.v];

            edge.flowText.setText(flow + " / " + capacity);

            // Color coding based on capacity utilization
            if (flow == capacity) {
                // Saturated edge (bottleneck) -> Red
                edge.line.setStroke(Color.RED);
                edge.line.setStrokeWidth(3.5);
                edge.arrow.setFill(Color.RED);
                edge.flowText.setFill(Color.RED);
            } else if (flow > 0) {
                // Utilized edge (partial capacity) -> Green
                edge.line.setStroke(Color.web("#4CAF50"));
                edge.line.setStrokeWidth(3);
                edge.arrow.setFill(Color.web("#4CAF50"));
                edge.flowText.setFill(Color.web("#2E7D32"));
            } else {
                // Unused edge -> keep gray
                edge.line.setStroke(Color.LIGHTGRAY);
                edge.line.setStrokeWidth(2);
                edge.arrow.setFill(Color.LIGHTGRAY);
                edge.flowText.setFill(Color.DARKGRAY);
            }
        }
    }

    /**
     * Finds the fastest route (shortest path) and highlights it in the UI.
     */
    private void deployFirstResponder() {
        MaxFlowSolver solver = new MaxFlowSolver(capacityMatrix);
        List<Integer> shortestPath = solver.findFastestRoute(SOURCE, SINK);

        if (shortestPath.isEmpty()) {
            resultText.setText("First Responder Error: No available path to disaster area.");
            return;
        }

        resultText.setText("First Responder Deployed via Shortest Path: " + pathToString(shortestPath));

        // For a first responder, we only need 1 optimal drone (the absolute best one)
        List<DroneFleetManager.Drone> selectedDrones = fleetManager.getOptimalFleet(1);
        updateFleetUI("First Responder (Single Elite Drone)", selectedDrones);

        // Reset and update visual edges
        for (EdgeUI edge : edgeUIs) {
            edge.flowText.setText("0 / " + capacityMatrix[edge.u][edge.v]); // Reset text
            
            // Check if this edge is part of the shortest path
            boolean isPathEdge = false;
            for (int i = 0; i < shortestPath.size() - 1; i++) {
                if (shortestPath.get(i) == edge.u && shortestPath.get(i+1) == edge.v) {
                    isPathEdge = true;
                    break;
                }
            }

            if (isPathEdge) {
                // Highlight the shortest path in bold blue
                edge.line.setStroke(Color.web("#1976D2")); // Blue
                edge.line.setStrokeWidth(4);
                edge.arrow.setFill(Color.web("#1976D2"));
                edge.flowText.setFill(Color.web("#1976D2"));
                edge.flowText.setText("Path / " + capacityMatrix[edge.u][edge.v]);
            } else {
                // Reset unused edges
                edge.line.setStroke(Color.LIGHTGRAY);
                edge.line.setStrokeWidth(2);
                edge.arrow.setFill(Color.LIGHTGRAY);
                edge.flowText.setFill(Color.DARKGRAY);
            }
        }
    }

    /**
     * Updates the text area and prints to console to prove sorting works.
     */
    private void updateFleetUI(String operationName, List<DroneFleetManager.Drone> drones) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("=== %s ===\n", operationName));
        sb.append("Greedy Sorting Algorithm Executed: Selecting top drones by Payload, then Battery.\n");
        sb.append("----------------------------------------------------------------------------\n");
        
        System.out.println(sb.toString()); // Print to console per requirements

        for (int i = 0; i < drones.size(); i++) {
            DroneFleetManager.Drone d = drones.get(i);
            String droneInfo = String.format("%d. %s\n", (i+1), d.toString());
            sb.append(droneInfo);
            System.out.print(droneInfo); // Print to console
        }
        
        fleetStatsArea.setText(sb.toString());
    }
    
    private String pathToString(List<Integer> path) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < path.size(); i++) {
            sb.append(nodeNames[path.get(i)]);
            if (i < path.size() - 1) sb.append(" -> ");
        }
        return sb.toString();
    }

    private static class EdgeUI {
        int u, v;
        Line line;
        Polygon arrow;
        Text flowText;

        EdgeUI(int u, int v, Line line, Polygon arrow, Text flowText) {
            this.u = u;
            this.v = v;
            this.line = line;
            this.arrow = arrow;
            this.flowText = flowText;
        }
    }
}
