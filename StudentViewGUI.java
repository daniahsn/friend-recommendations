import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import javax.swing.border.TitledBorder;


public class StudentViewGUI {
    private JFrame frame;
    private JComboBox<String> studentSelector;
    private WrappedJTextPane outputArea;
    private RecommendationEngine engine;
    private Map<String, Student> students;
    private CollaborationGraph graph;
    private JSlider majorWeightSlider;
    private JSlider skillWeightSlider;
    private JSlider collabPenaltySlider;
    private JPanel settingsPanel;

    public StudentViewGUI(String studentCSV, String skillCSV, String collabCSV) {
        try {
            StudentDataLoader loader = new StudentDataLoader(studentCSV, skillCSV, collabCSV);
            this.students = loader.getStudents();
            this.engine = new RecommendationEngine(students, loader.getCollaborations());
            SwingUtilities.invokeLater(this::buildGUI);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error loading data: " + e.getMessage());
        }
    }

    private static class WrappedJTextPane extends JTextPane {
        @Override
        public boolean getScrollableTracksViewportWidth() {
            return getUI().getPreferredSize(this).width <= getParent().getSize().width;
        }
    }

    private void buildGUI() {
        frame = new JFrame("Student Project Partner Selector");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);
        frame.setLayout(new BorderLayout(10, 10));
        frame.getContentPane().setBackground(Theme.BACKGROUND_COLOR);
    
        // Title Panel with gradient
        JPanel titlePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth();
                int h = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, Theme.PRIMARY_COLOR, w, h, new Color(0, 86, 179));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        titlePanel.setPreferredSize(new Dimension(frame.getWidth(), 60));
        JLabel titleLabel = new JLabel("Student Project Partner Selector");
        titleLabel.setFont(Theme.TITLE_FONT);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titlePanel.add(titleLabel);
        frame.add(titlePanel, BorderLayout.NORTH);
    
        // === Settings Panel (Left) ===
        settingsPanel = new JPanel();
        settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));
        settingsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        settingsPanel.setBackground(Theme.BACKGROUND_COLOR);
    
        // Student Selection Panel
        JPanel studentPanel = createStyledPanel("Student Selection");
        studentSelector = new JComboBox<>(students.keySet().toArray(new String[0]));
        studentSelector.setFont(Theme.NORMAL_FONT);
        studentSelector.setBackground(Theme.PANEL_COLOR);
        studentSelector.setForeground(Theme.TEXT_COLOR);
        studentSelector.setPreferredSize(new Dimension(180, 30)); // narrower
        studentSelector.setAlignmentX(Component.CENTER_ALIGNMENT);
        studentSelector.addActionListener(e -> {
            String selected = (String) studentSelector.getSelectedItem();
            if (selected != null) {
                graph.setSelectedNode(selected);
                outputArea.setText("<html><b>Select 'Recommend Partners' to see suggestions.</b></html>");
            } else {
                outputArea.setText("<html><b>Select a student to see recommendations.</b></html>");
            }
        });
    
        studentPanel.add(studentSelector);
        settingsPanel.add(studentPanel);
    
        // Recommendation Weights Panel
        JPanel weightPanel = createStyledPanel("Recommendation Weights");
        weightPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
    
        JPanel labelWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        labelWrapper.setBackground(Theme.PANEL_COLOR); // match background
        JLabel weightDescLabel = new JLabel("<html><div style='text-align: center;'>Adjust the importance of different factors<br>in partner recommendations</div></html>");
        weightDescLabel.setFont(Theme.NORMAL_FONT);
        weightDescLabel.setForeground(Theme.TEXT_COLOR);
        labelWrapper.add(weightDescLabel);
        weightPanel.add(labelWrapper);
        weightPanel.add(Box.createVerticalStrut(5));
    
        JPanel majorSliderPanel = createStyledSlider("Major Weight", 1, 5, 3);
        JPanel skillSliderPanel = createStyledSlider("Skill Weight", 1, 5, 3);
        JPanel collabSliderPanel = createStyledSlider("Collaboration Penalty", 1, 5, 2);
    
        majorWeightSlider = (JSlider) majorSliderPanel.getComponent(2);
        skillWeightSlider = (JSlider) skillSliderPanel.getComponent(2);
        collabPenaltySlider = (JSlider) collabSliderPanel.getComponent(2);
    
        weightPanel.add(majorSliderPanel);
        weightPanel.add(Box.createVerticalStrut(10));
        weightPanel.add(skillSliderPanel);
        weightPanel.add(Box.createVerticalStrut(10));
        weightPanel.add(collabSliderPanel);
        settingsPanel.add(weightPanel);
    
        // Actions Panel
        JPanel buttonPanel = createStyledPanel("Actions");
        JButton recommendButton = createStyledButton("Recommend Partners");
        JButton degreeButton = createStyledButton("Find Connection Degree");
    
        recommendButton.addActionListener(e -> handleRecommendation());
        degreeButton.addActionListener(e -> showDegreeDialog());
    
        recommendButton.setToolTipText("Suggest top 5 project partners for the selected student");
        degreeButton.setToolTipText("Show how many connections away two students are");
    
        buttonPanel.setLayout(new GridLayout(2, 1, 0, 10));
        buttonPanel.add(recommendButton);
        buttonPanel.add(degreeButton);
        settingsPanel.add(buttonPanel);
    
        // Wrap settings in scroll pane
        JScrollPane settingsScrollPane = new JScrollPane(settingsPanel);
        settingsScrollPane.getViewport().setBackground(Theme.BACKGROUND_COLOR);
        settingsScrollPane.setBackground(Theme.BACKGROUND_COLOR);
    
    
        // === Main Content Panel (Right) ===
        outputArea = new WrappedJTextPane();
        outputArea.setContentType("text/html");
        outputArea.setEditable(false);
        outputArea.setFont(Theme.NORMAL_FONT);
        outputArea.setBackground(Theme.PANEL_COLOR);
        outputArea.setForeground(Theme.TEXT_COLOR);
        

        
        JScrollPane outputScrollPane = new JScrollPane(outputArea);
        outputScrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER_COLOR),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        outputScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    
        graph = new CollaborationGraph(students, engine.getCollaborations());
        graph.setPreferredSize(new Dimension(400, 500));
        graph.setBackground(Theme.PANEL_COLOR);
        graph.setNodeClickListener(name -> {
            studentSelector.setSelectedItem(name);
            displayStudentInfo(name);
        });
    
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(graph), outputScrollPane);
        mainSplitPane.setDividerLocation(550);
        mainSplitPane.setBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR));
        
    
      
        JSplitPane settingsAndMainSplit = new JSplitPane(
        JSplitPane.HORIZONTAL_SPLIT,
        settingsScrollPane,
        mainSplitPane
        );
        settingsAndMainSplit.setResizeWeight(0.25);
        settingsAndMainSplit.setDividerSize(10);
        settingsAndMainSplit.setOneTouchExpandable(true);
        mainSplitPane.setResizeWeight(0.5);
        settingsAndMainSplit.setDividerLocation(300); // adjust as needed

        frame.add(settingsAndMainSplit, BorderLayout.CENTER);

        // === Configure outputArea styling and wrapping ===
        outputArea.setContentType("text/html");
        outputArea.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
        outputArea.setFont(Theme.NORMAL_FONT);
        outputArea.setBackground(Theme.PANEL_COLOR);
        outputArea.setForeground(Theme.TEXT_COLOR);

        outputArea.setEditorKit(new javax.swing.text.html.HTMLEditorKit() {
            @Override
            public javax.swing.text.ViewFactory getViewFactory() {
                return new javax.swing.text.html.HTMLEditorKit.HTMLFactory() {
                    @Override
                    public javax.swing.text.View create(javax.swing.text.Element elem) {
                        javax.swing.text.View view = super.create(elem);
                        if (view instanceof javax.swing.text.html.InlineView) {
                            return new javax.swing.text.html.InlineView(elem) {
                                @Override
                                public int getBreakWeight(int axis, float pos, float len) {
                                    return GoodBreakWeight;
                                }
        
                                @Override
                                public javax.swing.text.View breakView(int axis, int p0, float pos, float len) {
                                    return this;
                                }
                            };
                        }
                        return view;
                    }
                };
            }
        });

        // ✅ Initial message
        outputArea.setText("""
        <html>
        <head>
        <style>
        body {
            font-family: 'Segoe UI', sans-serif;
            color: #323741;
            padding: 20px;
            font-size: 14px;
        }
        .notice {
            background-color: #F5F7FA;
            padding: 20px;
            border: 1px solid #E1E4EB;
            border-radius: 6px;
            color: #3A3F58;
        }
        </style>
        </head>
        <body>
        <div class='notice'>
        📌 <b>Select a student</b> from the left panel and click <b>'Recommend Partners'</b> to view suggestions here.
        </div>
        </body>
        </html>
        """);
            

        
    
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                frame.revalidate();
            }
        });
    }
    private JPanel createStyledPanel(String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Theme.PANEL_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Theme.BORDER_COLOR),
                title,
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                Theme.HEADER_FONT,
                Theme.PANEL_TITLE_COLOR
            ),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        // Set preferred dimensions for the panels to ensure they're sized properly
        if (title.equals("Student Selection")) {
            panel.setPreferredSize(new Dimension(220, 80));
        } else if (title.equals("Recommendation Weights")) {
            panel.setPreferredSize(new Dimension(300, 300)); // slightly taller to avoid cut-off
        } else if (title.equals("Actions")) {
            panel.setPreferredSize(new Dimension(280, 150));
            panel.setMinimumSize(new Dimension(280, 150));
        }

        TitledBorder border = BorderFactory.createTitledBorder(
        BorderFactory.createLineBorder(Theme.BORDER_COLOR),
        title,
        TitledBorder.LEFT,
        TitledBorder.TOP,
        Theme.HEADER_FONT,
        Theme.PANEL_TITLE_COLOR
    );
panel.setBorder(BorderFactory.createCompoundBorder(border, BorderFactory.createEmptyBorder(15, 15, 15, 15)));
        
        return panel;
    }

    private JPanel createStyledSlider(String label, int min, int max, int value) {
        JPanel sliderPanel = new JPanel();
        sliderPanel.setLayout(new BoxLayout(sliderPanel, BoxLayout.Y_AXIS));
        sliderPanel.setBackground(Theme.PANEL_COLOR);
        // sliderPanel.setMaximumSize(new Dimension(280, 80));
        
        JLabel sliderLabel = new JLabel(label + ": " + value);
        sliderLabel.setFont(Theme.NORMAL_FONT);
        sliderLabel.setForeground(Theme.TEXT_COLOR);
        sliderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JSlider slider = new JSlider(min, max, value);
        slider.setFont(Theme.NORMAL_FONT);
        slider.setBackground(Theme.PANEL_COLOR);
        slider.setForeground(Theme.TEXT_COLOR);
        slider.setMajorTickSpacing(1);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setSnapToTicks(true);
        
        slider.addChangeListener(e -> {
            sliderLabel.setText(label + ": " + slider.getValue());
        });
        
        sliderPanel.add(sliderLabel);
        sliderPanel.add(Box.createVerticalStrut(5));
        sliderPanel.add(slider);
        
        return sliderPanel;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(Theme.BUTTON_FONT);
        button.setForeground(Color.WHITE);
        button.setBackground(Theme.PRIMARY_COLOR);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(Theme.PRIMARY_HOVER_COLOR);
            }
    
            public void mouseExited(MouseEvent e) {
                button.setBackground(Theme.PRIMARY_COLOR);
            }
        });
    
        return button;
    }

    private void handleRecommendation() {
        String selectedName = (String) studentSelector.getSelectedItem();
        if (selectedName != null) {
            int majorWeight = majorWeightSlider.getValue();
            int skillWeight = skillWeightSlider.getValue();
            int collabPenalty = collabPenaltySlider.getValue();
    
            List<String> recommendations = engine.recommendPartners(selectedName, 5,
                    majorWeight, skillWeight, collabPenalty);
    
            StringBuilder html = new StringBuilder();
            html.append("<html><head><style>")
                .append("body { font-family: 'Segoe UI', sans-serif; color: #323741; padding: 15px; }")
                .append("h2 { color: #3A3F58; font-size: 20px; margin-bottom: 15px; }")
                .append(".rec { margin-bottom: 20px; padding: 12px 15px; background-color: #F5F7FA; border: 1px solid #E1E4EB; border-radius: 6px; }")
                .append(".name { font-weight: bold; font-size: 15px; margin-bottom: 6px; display: block; }")
                .append(".info { font-size: 13px; line-height: 1.5; margin-bottom: 4px; }")
                .append(".score { color: #6078E6; font-weight: bold; font-size: 13px; }")
                .append(".skill-bar { height: 10px; background-color: #e1e4eb; border-radius: 5px; overflow: hidden; }")
                .append(".skill-fill { height: 10px; background-color: #6078E6; border-radius: 5px; }")
                .append("</style></head><body>");
    
            html.append("<h2>Top Recommendations for ").append(selectedName).append(":</h2>");
    
            for (String name : recommendations) {
                Student s = students.get(name);
                int score = engine.computeScore(selectedName, name, majorWeight, skillWeight, collabPenalty);
                Map<String, Integer> skills = s.getSkills();
    
                html.append("<div class='rec'>")
                    .append("<span class='name'>").append(name).append("</span>")
                    .append("<div class='info'><b>Major:</b> ").append(s.getMajor()).append("</div>")
                    .append("<div class='info'><b>Skills:</b></div>");
    
                for (String skill : skills.keySet()) {
                    int level = skills.get(skill);
                    int percent = level * 20;
    
                    String icon = switch (skill.toLowerCase()) {
                        case "design" -> "🎨";
                        case "backend" -> "🛠️";
                        case "frontend" -> "💻";
                        default -> "🔧";
                    };
    
                    html.append("<div class='info'>")
                        .append(icon).append(" <b>").append(skill).append(":</b>")
                        .append("<div class='skill-bar'><div class='skill-fill' style='width: ")
                        .append(percent).append("%;'></div></div>")
                        .append("</div>");
                }
    
                html.append("<div class='score'>Similarity Score: ").append(score).append("</div>")
                    .append("</div>");
            }
    
            html.append("</body></html>");
            outputArea.setText(html.toString());
            outputArea.setCaretPosition(0);
        }
    }

    private void displayStudentInfo(String name) {
        Student s = students.get(name);
        Map<String, Integer> skills = s.getSkills();
    
        StringBuilder html = new StringBuilder();
        html.append("<html><head><style>")
            .append("body { font-family: 'Segoe UI', sans-serif; color: #323741; padding: 10px; }")
            .append(".card { background-color: #F5F7FA; padding: 15px; border-radius: 6px; border: 1px solid #E1E4EB; }")
            .append(".title { font-size: 18px; font-weight: bold; color: #3A3F58; margin-bottom: 10px; }")
            .append(".info { font-size: 14px; line-height: 1.5; margin-bottom: 8px; }")
            .append(".skill-bar { height: 10px; background-color: #e1e4eb; border-radius: 5px; overflow: hidden; }")
            .append(".skill-fill { height: 10px; background-color: #6078E6; border-radius: 5px; }")
            .append("</style></head><body>");
    
        html.append("<div class='card'>")
            .append("<div class='title'>").append(name).append("</div>")
            .append("<div class='info'><b>Major:</b> ").append(s.getMajor()).append("</div>")
            .append("<div class='info'><b>Skills:</b></div>");
    
            for (String skill : skills.keySet()) {
                int level = skills.get(skill);
                int percent = level * 20;
            
                String icon = switch (skill.toLowerCase()) {
                    case "design" -> "🎨";
                    case "backend" -> "🛠️";
                    case "frontend" -> "💻";
                    default -> "🔧";
                };
            
                html.append("<div class='info'>")
                    .append("<div>").append(icon).append(" <b>").append(skill).append(":</b></div>")
                    .append("<div class='skill-bar'><div class='skill-fill' style='width: ")
                    .append(percent).append("%;'></div></div>")
                    .append("</div>");
            }
    
        html.append("</div></body></html>");
    
        outputArea.setText(html.toString());
        outputArea.setCaretPosition(0); // Scroll to top
    }
    private void showDegreeDialog() {
        JDialog degreeDialog = new JDialog(frame, "Find Connection Degree", true);
        degreeDialog.setLayout(new GridBagLayout());
        degreeDialog.getContentPane().setBackground(Theme.PANEL_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel person1Label = new JLabel("First person:");
        degreeDialog.add(person1Label, gbc);

        gbc.gridx = 1;
        JComboBox<String> person1Selector = new JComboBox<>(students.keySet().toArray(new String[0]));
        degreeDialog.add(person1Selector, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel person2Label = new JLabel("Second person:");
        degreeDialog.add(person2Label, gbc);

        gbc.gridx = 1;
        JComboBox<String> person2Selector = new JComboBox<>(students.keySet().toArray(new String[0]));
        degreeDialog.add(person2Selector, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        JLabel resultLabel = new JLabel(" ");
        degreeDialog.add(resultLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        JButton findButton = new JButton("Find Degree");
        findButton.addActionListener(e -> {
            String person1 = (String) person1Selector.getSelectedItem();
            String person2 = (String) person2Selector.getSelectedItem();

            if (person1 != null && person2 != null) {
                int degree = findDegreeOfSeparation(person1, person2);
                if (degree == -1) {
                    resultLabel.setText("No connection between " + person1 + " and " + person2);
                } else {
                    resultLabel.setText("The degree of separation is: " + degree);
                }
            }
        });
        degreeDialog.add(findButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> degreeDialog.dispose());
        degreeDialog.add(closeButton, gbc);

        degreeDialog.setPreferredSize(new Dimension(350, 220));
        degreeDialog.pack();
        degreeDialog.setLocationRelativeTo(frame);
        degreeDialog.setVisible(true);
    }

    private int findDegreeOfSeparation(String start, String end) {
        Map<String, List<String>> graph = engine.getCollaborations();
        Queue<String> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();
        Map<String, Integer> distance = new HashMap<>();

        queue.add(start);
        visited.add(start);
        distance.put(start, 0);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            if (current.equals(end)) {
                return distance.get(current);
            }
            for (String neighbor : graph.getOrDefault(current, Collections.emptyList())) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                    distance.put(neighbor, distance.get(current) + 1);
                }
            }
        }
        return -1;
    }

    public static void main(String[] args) {
        UIManager.put("ToolTip.background", new Color(255, 255, 255)); // white
        UIManager.put("ToolTip.foreground", Theme.TEXT_COLOR);         // your dark text
        UIManager.put("ToolTip.font", Theme.NORMAL_FONT);
        UIManager.put("ToolTip.border", BorderFactory.createLineBorder(Theme.BORDER_COLOR));
        new StudentViewGUI("students.csv", "skills.csv", "collaborations.csv");
    }
}