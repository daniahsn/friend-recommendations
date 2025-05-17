import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

public class CollaborationGraph extends JPanel {
    private final Map<String, Student> students;
    private final Map<String, List<String>> collaborations;
    private final Map<String, Point> nodePositions = new HashMap<>();
    private String selectedNode = null;
    private Consumer<String> nodeClickListener = name -> {};

    public CollaborationGraph(Map<String, Student> students, Map<String, List<String>> collaborations) {
        this.students = students;
        this.collaborations = collaborations;
        setPreferredSize(new Dimension(500, 500));
        setBackground(Theme.PANEL_COLOR);
        layoutNodes();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                for (Map.Entry<String, Point> entry : nodePositions.entrySet()) {
                    Point p = entry.getValue();
                    if (p.distance(e.getPoint()) < 15) {
                        selectedNode = entry.getKey();
                        nodeClickListener.accept(selectedNode);
                        repaint();
                        break;
                    }
                }
            }
        });
    }

    public void setSelectedNode(String name) {
        this.selectedNode = name;
        repaint();
    }

    public void setNodeClickListener(Consumer<String> listener) {
        this.nodeClickListener = listener;
    }

    private void layoutNodes() {
        int size = students.size();
        int radius = 150;
        int panelWidth = getPreferredSize().width;
        int panelHeight = getPreferredSize().height;
        int centerX = panelWidth / 2;
        int centerY = panelHeight / 2;

        int i = 0;
        for (String name : students.keySet()) {
            double angle = 2 * Math.PI * i / size;
            int x = centerX + (int) (radius * Math.cos(angle));
            int y = centerY + (int) (radius * Math.sin(angle));
            nodePositions.put(name, new Point(x, y));
            i++;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        layoutNodes();  // Recalculate based on current panel size

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw edges
        g2.setColor(Theme.BORDER_COLOR);
        for (String a : collaborations.keySet()) {
            Point pa = nodePositions.get(a);
            for (String b : collaborations.get(a)) {
                Point pb = nodePositions.get(b);
                if (pa != null && pb != null) {
                    g2.drawLine(pa.x, pa.y, pb.x, pb.y);
                }
            }
        }

        // Draw nodes
        for (Map.Entry<String, Point> entry : nodePositions.entrySet()) {
            String name = entry.getKey();
            Point p = entry.getValue();

            if (name.equals(selectedNode)) {
                g2.setColor(Theme.PRIMARY_COLOR);
                g2.fillOval(p.x - 10, p.y - 10, 20, 20);
            } else {
                g2.setColor(Theme.SECONDARY_COLOR);
                g2.fillOval(p.x - 8, p.y - 8, 16, 16);
            }

            g2.setColor(Theme.TEXT_COLOR);
            g2.setFont(Theme.NORMAL_FONT);
            g2.drawString(name, p.x - 15, p.y - 15);
        }
    }
}