package Algorithms;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class Beam {

    static Map<String, Map<String, Integer>> d = new HashMap<>();
    static Graph<String, DefaultEdge> g;
    static GraphPanel panel;

    public static void main(String[] args) {
        // Initialize graph data
        d.put("S", Map.of("A", 3, "B", 5));
        d.put("A", Map.of("S", 3, "B", 4, "D", 3));
        d.put("B", Map.of("S", 5, "A", 4, "C", 4));
        d.put("C", Map.of("B", 4, "E", 6));
        d.put("E", Map.of("C", 6));
        d.put("D", Map.of("A", 3, "G", 5));
        d.put("G", Map.of("D", 5));

        // Create the graph and visualize it
        g = formGraph(d);
        visualize(g);

        // Perform the Beam Search and visualize each step
        List<List<String>> paths = beamSearch(d, "S", "G", 2);
        for (List<String> path : paths) {
            System.out.println("Exploring path: " + path);
            updateVisualization(g, path);
            try {
                Thread.sleep(1000); // Pause to simulate the search process
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // Beam Search implementation
    public static List<List<String>> beamSearch(Map<String, Map<String, Integer>> graph, String start, String end, int beamWidth) {
        List<List<String>> foundPaths = new ArrayList<>();
        Queue<List<String>> queue = new LinkedList<>();
        queue.add(new ArrayList<>(List.of(start)));

        while (!queue.isEmpty()) {
            List<String> currentPath = queue.poll();
            String currentNode = currentPath.get(currentPath.size() - 1);

            if (currentNode.equals(end)) {
                foundPaths.add(currentPath);
                break; // Stop after finding the first path to the goal
            }

            List<List<String>> successors = new ArrayList<>();
            for (String neighbor : graph.getOrDefault(currentNode, Collections.emptyMap()).keySet()) {
                if (!currentPath.contains(neighbor)) {
                    List<String> newPath = new ArrayList<>(currentPath);
                    newPath.add(neighbor);
                    successors.add(newPath);
                }
            }

            // Sort successors by path length (can change criteria based on heuristic)
            successors.sort(Comparator.comparingInt(path -> graph.get(currentNode).get(path.get(path.size() - 1))));

            // Add the top beamWidth successors to the queue
            queue.addAll(successors.subList(0, Math.min(beamWidth, successors.size())));
            foundPaths.addAll(successors);
        }

        return foundPaths;
    }

    // Convert paths to graph
    public static Graph<String, DefaultEdge> formGraph(Map<String, Map<String, Integer>> d) {
        Graph<String, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        for (String node : d.keySet()) {
            graph.addVertex(node);
            for (String neighbor : d.get(node).keySet()) {
                graph.addVertex(neighbor);
                graph.addEdge(node, neighbor);
            }
        }
        return graph;
    }

    // Visualization using Java Swing
    public static void visualize(Graph<String, DefaultEdge> g) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        panel = new GraphPanel(g);
        frame.add(panel);
        frame.setVisible(true);
    }

    // Update the graph visualization based on the current path being explored
    public static void updateVisualization(Graph<String, DefaultEdge> g, List<String> path) {
        panel.setCurrentPath(path);
        panel.repaint();
    }

    // Swing panel for graph rendering and displaying current path
    static class GraphPanel extends JPanel {
        Graph<String, DefaultEdge> graph;
        Map<String, Point> positions;
        List<String> currentPath;

        public GraphPanel(Graph<String, DefaultEdge> graph) {
            this.graph = graph;
            positions = new HashMap<>();
            currentPath = new ArrayList<>();
            setRandomPositions(graph);
        }

        private void setRandomPositions(Graph<String, DefaultEdge> graph) {
            Random random = new Random();
            for (String vertex : graph.vertexSet()) {
                positions.put(vertex, new Point(50 + random.nextInt(700), 50 + random.nextInt(500)));
            }
        }

        public void setCurrentPath(List<String> path) {
            this.currentPath = path;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            // Draw edges
            for (DefaultEdge edge : graph.edgeSet()) {
                String source = graph.getEdgeSource(edge);
                String target = graph.getEdgeTarget(edge);
                Point sourcePoint = positions.get(source);
                Point targetPoint = positions.get(target);
                if (isEdgeInCurrentPath(source, target)) {
                    g2d.setColor(Color.ORANGE); // Current path
                } else {
                    g2d.setColor(Color.BLUE); // Explored edges
                }
                g2d.drawLine(sourcePoint.x, sourcePoint.y, targetPoint.x, targetPoint.y);
            }

            // Draw vertices
            for (String vertex : graph.vertexSet()) {
                Point p = positions.get(vertex);
                g2d.setColor(Color.BLACK);
                g2d.fillOval(p.x - 15, p.y - 15, 30, 30);
                g2d.setColor(Color.WHITE);
                g2d.drawString(vertex, p.x - 5, p.y + 5);
            }

            // Display the current path
            g2d.setColor(Color.BLUE);
            g2d.drawString("Current Path: " + String.join(" -> ", currentPath), 20, 550);
        }

        private boolean isEdgeInCurrentPath(String source, String target) {
            for (int i = 0; i < currentPath.size() - 1; i++) {
                String pathSource = currentPath.get(i);
                String pathTarget = currentPath.get(i + 1);
                if ((source.equals(pathSource) && target.equals(pathTarget)) ||
                        (source.equals(pathTarget) && target.equals(pathSource))) {
                    return true;
                }
            }
            return false;
        }
    }
}
