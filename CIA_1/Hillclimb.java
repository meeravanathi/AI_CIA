package Algorithms;
import org.jgrapht.Graph;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class Hillclimb {

    static Map<String, Map<String, Integer>> d = new HashMap<>();
    static Map<String, Map<String, Double>> d_h = new HashMap<>();
    static Graph<String, DefaultEdge> g;
    static GraphPanel panel;
    static JTextArea resultArea;  // Text area to display search results
    static List<List<String>> l = new ArrayList<>();

    public static void main(String[] args) {
        // Initialize graph data
        d.put("S", Map.of("A", 3, "B", 5));
        d.put("A", Map.of("S", 3, "B", 4, "D", 3));
        d.put("B", Map.of("S", 5, "A", 4, "C", 4));
        d.put("C", Map.of("B", 4, "E", 6));
        d.put("E", Map.of("C", 6));
        d.put("D", Map.of("A", 3, "G", 5));
        d.put("G", Map.of("D", 5));

        d_h.put("S", Map.of("A", 7.38, "B", 6.0));
        d_h.put("A", Map.of("S", Double.POSITIVE_INFINITY, "B", 6.0, "D", 5.0));
        d_h.put("B", Map.of("S", Double.POSITIVE_INFINITY, "A", 7.38, "C", 7.58));
        d_h.put("C", Map.of("B", 6.0, "E", Double.POSITIVE_INFINITY));
        d_h.put("E", Map.of("C", 7.58));
        d_h.put("D", Map.of("A", 7.38, "G", 0.0));
        d_h.put("G", Map.of("D", 5.0));

        // Visualize the graph before performing hill climbing
        g = formGraph(d);
        visualize(g);

        // Create a result window to display the exploration steps
        createResultWindow();

        // Perform hill climbing and visualize the graph
        List<String> FR = hillClimb(d_h, "S", "G");
    }

    // Hill Climb implementation
    public static List<String> hillClimb(Map<String, Map<String, Double>> d_heur, String start, String end) {
        Queue<List<String>> queue = new LinkedList<>();
        List<String> path = new ArrayList<>();
        queue.add(new ArrayList<>(List.of(start)));

        appendResult("Starting Hill Climb from node: " + start + "\n");

        while (!queue.isEmpty()) {
            List<String> cp = queue.poll();
            String cn = cp.get(cp.size() - 1);

            appendResult("Exploring node: " + cn + "\n");
            appendResult("Current path: " + cp + "\n");

            if (cn.equals(end)) {
                appendResult("Goal found: " + cp + "\n");
                path = cp; // Save the path found
                break;
            } else {
                List<List<String>> sl = new ArrayList<>();
                for (String neighbor : d_heur.get(cn).keySet()) {
                    if (cp.contains(neighbor)) continue;
                    List<String> el = new ArrayList<>(cp);
                    el.add(neighbor);
                    sl.add(el);
                }

                // Sort by heuristic (smallest first)
                sl.sort((a, b) -> Double.compare(d_heur.get(cn).get(a.get(a.size() - 1)), d_heur.get(cn).get(b.get(b.size() - 1))));

                for (List<String> k : sl) {
                    queue.add(k);
                    l.add(k); // Store paths for visualization
                    updateVisualization(g, k); // Visualize the current path
                    try {
                        Thread.sleep(1000); // Pause for visualization
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return path;
    }

    // Convert paths to graph
    public static Graph<String, DefaultEdge> formGraph(Map<String, Map<String, Integer>> d) {
        g = new SimpleGraph<>(DefaultEdge.class);
        for (String node : d.keySet()) {
            g.addVertex(node);
        }

        for (String node : d.keySet()) {
            for (String neighbor : d.get(node).keySet()) {
                g.addEdge(node, neighbor);
            }
        }
        return g;
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

    // Create a window to display the exploration steps
    public static void createResultWindow() {
        JFrame resultFrame = new JFrame("Hill Climbing Search Results");
        resultFrame.setSize(400, 600);
        resultFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        resultArea = new JTextArea();
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);

        resultFrame.add(scrollPane);
        resultFrame.setVisible(true);
    }

    // Append text to the result area
    public static void appendResult(String text) {
        resultArea.append(text);
    }

    // Update the graph visualization based on the current path being explored
    public static void updateVisualization(Graph<String, DefaultEdge> g, List<String> path) {
        panel.setCurrentPath(path);
        panel.repaint();
    }

    // Swing panel for graph rendering
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
