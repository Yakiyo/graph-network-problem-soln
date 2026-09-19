package com.dronenetwork;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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
 * for an autonomous drone deployment network.
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

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Autonomous Drone Network - Max Flow Optimizer");

        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #e0e0e0;");

        // The pane where the graph will be drawn
        graphPane = new Pane();
        graphPane.setPrefSize(1000, 600);
        graphPane.setStyle("-fx-background-color: white; -fx-border-color: #999; -fx-border-width: 2;");

        // Draw the edges (lines + arrows + text) before nodes so they sit in the background
        drawEdges();

        // Draw the nodes (circles + text labels)
        drawNodes();

        // Control Panel UI
        Button calculateBtn = new Button("Deploy Drones (Calculate Max Flow)");
        calculateBtn.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        calculateBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-padding: 10 20; -fx-cursor: hand;");
        calculateBtn.setOnAction(e -> calculateAndDisplayMaxFlow());
        
        resultText = new Text("Total Drones Deployed: 0");
        resultText.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        resultText.setFill(Color.DARKSLATEGRAY);

        root.getChildren().addAll(graphPane, calculateBtn, resultText);

        Scene scene = new Scene(root, 1040, 750);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Draws directional lines and initial flow/capacity labels for all connected edges.
     */
    private void drawEdges() {
        for (int u = 0; u < NUM_NODES; u++) {
            for (int v = 0; v < NUM_NODES; v++) {
                if (capacityMatrix[u][v] > 0) {
                    double startX = nodeCoords[u][0];
                    double startY = nodeCoords[u][1];
                    double endX = nodeCoords[v][0];
                    double endY = nodeCoords[v][1];

                    // Draw the corridor line
                    Line line = new Line(startX, startY, endX, endY);
                    line.setStrokeWidth(2);
                    line.setStroke(Color.LIGHTGRAY);

                    // Calculate direction angle to place the arrow properly
                    double angle = Math.atan2(endY - startY, endX - startX);
                    double arrowSize = 12;
                    double arrowDist = 28; // Distance from end node center so arrow doesn't overlap circle

                    // Tip of the arrow
                    double tipX = endX - arrowDist * Math.cos(angle);
                    double tipY = endY - arrowDist * Math.sin(angle);

                    // Draw a simple polygon as an arrowhead
                    Polygon arrow = new Polygon();
                    arrow.getPoints().addAll(
                            tipX, tipY,
                            tipX - arrowSize * Math.cos(angle - Math.PI / 8),
                            tipY - arrowSize * Math.sin(angle - Math.PI / 8),
                            tipX - arrowSize * Math.cos(angle + Math.PI / 8),
                            tipY - arrowSize * Math.sin(angle + Math.PI / 8)
                    );
                    arrow.setFill(Color.LIGHTGRAY);

                    // Text for flow/capacity (Format: "Flow / Capacity")
                    Text flowText = new Text("0 / " + capacityMatrix[u][v]);
                    flowText.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                    flowText.setFill(Color.DARKGRAY);
                    
                    // Position text near the middle of the edge, offset slightly for readability
                    flowText.setX((startX + endX) / 2 - 15);
                    flowText.setY((startY + endY) / 2 - 10);

                    // Special case to prevent text overlap between A->B and B->A or similar cross routes
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

    /**
     * Draws the nodes (hubs) as circles with their respective labels.
     */
    private void drawNodes() {
        for (int i = 0; i < NUM_NODES; i++) {
            double x = nodeCoords[i][0];
            double y = nodeCoords[i][1];

            Circle circle = new Circle(x, y, 25);
            circle.setFill(Color.web("#B3E5FC"));
            circle.setStroke(Color.web("#0288D1"));
            circle.setStrokeWidth(3);

            // Special colors for Source and Sink nodes
            if (i == SOURCE) {
                circle.setFill(Color.web("#C8E6C9"));
                circle.setStroke(Color.web("#388E3C"));
            } else if (i == SINK) {
                circle.setFill(Color.web("#FFCCBC"));
                circle.setStroke(Color.web("#D84315"));
            }

            Text text = new Text(nodeNames[i]);
            text.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            // Center text above the circle
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

        // Update the result label
        resultText.setText("Total Drones Deployed (Max Flow): " + maxFlow);

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
     * Helper struct to bundle edge visual elements together.
     */
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
