import java.io.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.List;  // Explicit import for List
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class PersonInfo {
    // File paths
    private static final String INTERESTS_FILE = "interests.csv";
    private static final String FRIENDSHIPS_FILE = "friendships.csv";
    private static final String LOCATIONS_FILE = "locations.csv"; // New file for geographical data
    
    // Data structures
    private Map<String, Map<String, String>> interests = new HashMap<>();
    private Map<String, List<String>> friendships = new HashMap<>();
    private Map<String, Integer> interestWeights = new HashMap<>();
    private Map<String, Location> locations = new HashMap<>(); // Geographic locations
    private Map<String, List<String>> negativeRelations = new HashMap<>(); // People who shouldn't be recommended
    private int friendScoreWeight;
    
    // GUI components
    private JFrame mainFrame;
    private JPanel mainPanel;
    private JComboBox<String> personSelector;
    private NetworkGraph networkGraph; // Custom component for visualizing the network
    
    // Inner class for geographic location
    private static class Location {
        double latitude;
        double longitude;
        
        public Location(double lat, double lon) {
            this.latitude = lat;
            this.longitude = lon;
        }
        
        // Calculate distance in kilometers using Haversine formula
        public double distanceTo(Location other) {
            final int R = 6371; // Earth's radius in kilometers
            double latDistance = Math.toRadians(other.latitude - this.latitude);
            double lonDistance = Math.toRadians(other.longitude - this.longitude);
            double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                     + Math.cos(Math.toRadians(this.latitude)) * Math.cos(Math.toRadians(other.latitude))
                     * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            return R * c;
        }
    }
    
    // Inner class for network visualization
    private class NetworkGraph extends JPanel {
        private Map<String, Point> nodePositions = new HashMap<>();
        private String selectedPerson;
        private Map<String, Integer> recommendationScores;
        
        public NetworkGraph() {
            setPreferredSize(new Dimension(600, 400));
            setBackground(Color.WHITE);
            
            // Layout nodes in a force-directed way (simplified here)
            layoutNodes();
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    for (Map.Entry<String, Point> entry : nodePositions.entrySet()) {
                        Point p = entry.getValue();
                        if (distance(e.getX(), e.getY(), p.x, p.y) < 15) {
                            String clickedPerson = entry.getKey();
                            personSelector.setSelectedItem(clickedPerson);
                            break;
                        }
                    }
                }
            });
            
        }
        
        private double distance(int x1, int y1, int x2, int y2) {
            return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
        }
        
        public void setSelectedPerson(String person, Map<String, Integer> recScores) {
            this.selectedPerson = person;
            this.recommendationScores = recScores;
            repaint();
        }
        
        private void layoutNodes() {
            // Simple circular layout - in a real implementation, use a force-directed algorithm
            int radius = 150;
            int centerX = 300;
            int centerY = 200;
            
            int i = 0;
            for (String person : interests.keySet()) {
                double angle = 2 * Math.PI * i / interests.size();
                int x = centerX + (int)(radius * Math.cos(angle));
                int y = centerY + (int)(radius * Math.sin(angle));
                nodePositions.put(person, new Point(x, y));
                i++;
            }
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Draw edges (relationships)
            g2d.setStroke(new BasicStroke(1.0f));
            for (Map.Entry<String, List<String>> entry : friendships.entrySet()) {
                String person = entry.getKey();
                Point p1 = nodePositions.get(person);
                if (p1 == null) continue;
                
                for (String friend : entry.getValue()) {
                    Point p2 = nodePositions.get(friend);
                    if (p2 == null) continue;
                    
                    // Draw thicker lines for strong recommendations
                    if (selectedPerson != null && person.equals(selectedPerson) && 
                        recommendationScores != null && recommendationScores.containsKey(friend)) {
                        int score = recommendationScores.get(friend);
                        float thickness = 1.0f + (score * 0.1f);
                        g2d.setStroke(new BasicStroke(thickness));
                        g2d.setColor(new Color(0, 100, 0)); // Dark green
                    } else {
                        g2d.setStroke(new BasicStroke(1.0f));
                        g2d.setColor(Color.LIGHT_GRAY);
                    }
                    
                    g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
                }
            }
            
            // Draw recommendation links
            if (selectedPerson != null && recommendationScores != null) {
                Point p1 = nodePositions.get(selectedPerson);
                if (p1 != null) {
                    g2d.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 
                                             0, new float[]{5}, 0));
                    g2d.setColor(new Color(200, 0, 0));
                    
                    for (Map.Entry<String, Integer> entry : recommendationScores.entrySet()) {
                        String recommendedPerson = entry.getKey();
                        int score = entry.getValue();
                        
                        // Only draw if not already friends
                        if (!friendships.getOrDefault(selectedPerson, Collections.emptyList()).contains(recommendedPerson)) {
                            Point p2 = nodePositions.get(recommendedPerson);
                            if (p2 != null) {
                                // Make line thickness proportional to recommendation strength
                                float thickness = 1.0f + (score * 0.05f);
                                g2d.setStroke(new BasicStroke(thickness, BasicStroke.CAP_ROUND, 
                                                          BasicStroke.JOIN_ROUND, 0, new float[]{5}, 0));
                                g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
                            }
                        }
                    }
                }
            }
            
            // Draw nodes (people)
            for (Map.Entry<String, Point> entry : nodePositions.entrySet()) {
                String person = entry.getKey();
                Point p = entry.getValue();
                
                // Highlight selected person
                if (selectedPerson != null && person.equals(selectedPerson)) {
                    g2d.setColor(new Color(100, 100, 255));
                    g2d.fillOval(p.x - 10, p.y - 10, 20, 20);
                } 
                // Highlight recommended people
                else if (selectedPerson != null && recommendationScores != null && 
                         recommendationScores.containsKey(person)) {
                    int score = recommendationScores.get(person);
                    // Color based on recommendation strength
                    int green = Math.min(255, 100 + score * 10);
                    g2d.setColor(new Color(255, green, 100));
                    g2d.fillOval(p.x - 8, p.y - 8, 16, 16);
                } 
                // Regular nodes
                else {
                    g2d.setColor(new Color(200, 200, 200));
                    g2d.fillOval(p.x - 6, p.y - 6, 12, 12);
                }
                
                // Draw node border
                g2d.setColor(Color.BLACK);
                g2d.drawOval(p.x - 6, p.y - 6, 12, 12);
                
                // Draw name labels
                g2d.setColor(Color.BLACK);
                g2d.drawString(person, p.x - 10, p.y - 12);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                PersonInfo app = new PersonInfo();
                app.loadAllData();
                app.createAndShowGUI();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, 
                    "Error loading data: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        });
    }
    
    private void loadAllData() throws IOException {
        loadInterests();
        loadFriendships();
        
        // Try to load optional files, but don't fail if they don't exist
    
        
        try {
            loadLocations();
        } catch (IOException e) {
            System.out.println("No locations file found. Using default locations.");
            // Create sample locations for everyone
            Random random = new Random(123); // Fixed seed for reproducibility
            for (String person : interests.keySet()) {
                // Generate random locations in a reasonable range
                double lat = 40.0 + random.nextDouble() * 10.0;
                double lon = -75.0 + random.nextDouble() * 10.0;
                locations.put(person, new Location(lat, lon));
            }
            
            // Save these locations to the file
            saveLocations();
        }
    }
    
    private void saveLocations() throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(LOCATIONS_FILE))) {
            writer.println("Name,Latitude,Longitude");
            for (Map.Entry<String, Location> entry : locations.entrySet()) {
                writer.println(entry.getKey() + "," + 
                              entry.getValue().latitude + "," + 
                              entry.getValue().longitude);
            }
        }
    }
    
    private void loadInterests() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(INTERESTS_FILE))) {
            String[] headers = reader.readLine().split(",");
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length != headers.length) continue;
                
                String name = data[0].trim();
                Map<String, String> details = new HashMap<>();
                for (int i = 1; i < headers.length; i++) {
                    details.put(headers[i].trim(), data[i].trim());
                }
                interests.put(name, details);
            }
        }
    }
    
    private void loadFriendships() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(FRIENDSHIPS_FILE))) {
            String[] names = reader.readLine().split(",");
            List<String> cleanedNames = Arrays.asList(Arrays.copyOfRange(names, 1, names.length));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                String person = data[0].trim();
                List<String> friends = new ArrayList<>();
                
                for (int i = 1; i < data.length; i++) {
                    if ("1".equals(data[i].trim())) {
                        friends.add(cleanedNames.get(i - 1).trim());
                    }
                }
                friendships.put(person, friends);
            }
        }
    }
    

    
    private void loadLocations() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(LOCATIONS_FILE))) {
            String line = reader.readLine(); // Skip header
            
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length < 3) continue;
                
                String name = data[0].trim();
                double latitude = Double.parseDouble(data[1].trim());
                double longitude = Double.parseDouble(data[2].trim());
                
                locations.put(name, new Location(latitude, longitude));
            }
        }
    }
    
    
    private void createAndShowGUI() {
        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Create main window
        mainFrame = new JFrame("Friend Recommendation System");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(1000, 700);
        
        // Create main panel with border layout
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Create top control panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        // Person selector
        JLabel selectLabel = new JLabel("Select a person: ");
        personSelector = new JComboBox<>(interests.keySet().toArray(new String[0]));
        personSelector.addActionListener(e -> updateSelectedPerson());
        
        // Buttons for different recommendation types
        JButton friendsButton = new JButton("Shared Friends");
        friendsButton.addActionListener(e -> showRecommendations(1));
        
        JButton interestsButton = new JButton("Shared Interests");
        interestsButton.addActionListener(e -> showRecommendations(2));
        
        JButton combinedButton = new JButton("Combined");
        combinedButton.addActionListener(e -> showRecommendations(3));
        
        JButton degreeButton = new JButton("Find Connection Degree");
        degreeButton.addActionListener(e -> showDegreeDialog());
        
        controlPanel.add(selectLabel);
        controlPanel.add(personSelector);
        controlPanel.add(friendsButton);
        controlPanel.add(interestsButton);
        controlPanel.add(combinedButton);
        controlPanel.add(degreeButton);
        
        // Create the network visualization
        networkGraph = new NetworkGraph();
        
        // Create bottom panel for displaying recommendation details
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBorder(BorderFactory.createTitledBorder("Recommendation Details"));
        detailsPanel.setPreferredSize(new Dimension(800, 200));
        
        // Add components to main panel
        mainPanel.add(controlPanel, BorderLayout.NORTH);
        mainPanel.add(networkGraph, BorderLayout.CENTER);
        mainPanel.add(detailsPanel, BorderLayout.SOUTH);
        
        // Add to frame and show
        mainFrame.add(mainPanel);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
        
        // Collect weights on first run
        collectInterestWeights();
    }
    
    private void updateSelectedPerson() {
        String selectedPerson = (String) personSelector.getSelectedItem();
        if (selectedPerson != null) {
            // Update the network visualization
            Map<String, Integer> emptyScores = new HashMap<>();
            networkGraph.setSelectedPerson(selectedPerson, emptyScores);
            
            // Show person's details in the details panel
            JPanel detailsPanel = (JPanel) mainPanel.getComponent(2);
            detailsPanel.removeAll();
            
            // Add person's interests
            JPanel interestsPanel = new JPanel(new GridLayout(0, 3, 5, 5));
            interestsPanel.setBorder(BorderFactory.createTitledBorder("Interests"));
            
            Map<String, String> personInterests = interests.get(selectedPerson);
            if (personInterests != null) {
                for (Map.Entry<String, String> entry : personInterests.entrySet()) {
                    interestsPanel.add(new JLabel(entry.getKey() + ": " + entry.getValue()));
                }
            }
            
            // Add person's friends
            JPanel friendsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            friendsPanel.setBorder(BorderFactory.createTitledBorder("Current Friends"));
            
            List<String> personFriends = friendships.getOrDefault(selectedPerson, Collections.emptyList());
            for (String friend : personFriends) {
                JButton friendButton = new JButton(friend);
                friendButton.addActionListener(e -> personSelector.setSelectedItem(friend));
                friendsPanel.add(friendButton);
            }
            
            detailsPanel.add(interestsPanel);
            detailsPanel.add(friendsPanel);
            
            detailsPanel.revalidate();
            detailsPanel.repaint();
        }
    }
    
    private void collectInterestWeights() {
        // Get headers from interests file
        String[] interestCategories = interests.isEmpty() ? new String[0] : 
                                     interests.values().iterator().next().keySet().toArray(new String[0]);
        
        // Create a dialog for collecting weights
        JDialog weightDialog = new JDialog(mainFrame, "Set Recommendation Weights", true);
        weightDialog.setLayout(new BorderLayout());
        
        JPanel weightsPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        weightsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        Map<String, JSlider> sliders = new HashMap<>();
        
        for (String category : interestCategories) {
            JLabel label = new JLabel("Weight for " + category + ":");
            JSlider slider = new JSlider(1, 10, 5);
            slider.setMajorTickSpacing(1);
            slider.setPaintTicks(true);
            slider.setPaintLabels(true);
            
            weightsPanel.add(label);
            weightsPanel.add(slider);
            
            sliders.put(category, slider);
        }
        
        // Add slider for friend score weight
        JLabel friendLabel = new JLabel("Weight for shared friends:");
        JSlider friendSlider = new JSlider(1, 10, 5);
        friendSlider.setMajorTickSpacing(1);
        friendSlider.setPaintTicks(true);
        friendSlider.setPaintLabels(true);
        
        weightsPanel.add(friendLabel);
        weightsPanel.add(friendSlider);
        
        // Add a "geographical proximity" weight slider
        JLabel proximityLabel = new JLabel("Weight for geographical proximity:");
        JSlider proximitySlider = new JSlider(1, 10, 5);
        proximitySlider.setMajorTickSpacing(1);
        proximitySlider.setPaintTicks(true);
        proximitySlider.setPaintLabels(true);
        
        weightsPanel.add(proximityLabel);
        weightsPanel.add(proximitySlider);

        // Add OK button
        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> {
            // Save the weights
            for (Map.Entry<String, JSlider> entry : sliders.entrySet()) {
                interestWeights.put(entry.getKey(), entry.getValue().getValue());
            }
            
            friendScoreWeight = friendSlider.getValue();
            
            // Close dialog
            weightDialog.dispose();
        });
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(okButton);
        
        weightDialog.add(weightsPanel, BorderLayout.CENTER);
        weightDialog.add(buttonPanel, BorderLayout.SOUTH);
        weightDialog.pack();
        weightDialog.setLocationRelativeTo(mainFrame);
        weightDialog.setVisible(true);
    }
    
    private void showDegreeDialog() {
        JDialog degreeDialog = new JDialog(mainFrame, "Find Connection Degree", true);
        degreeDialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Person 1 selector
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel person1Label = new JLabel("First person:");
        degreeDialog.add(person1Label, gbc);
        
        gbc.gridx = 1;
        JComboBox<String> person1Selector = new JComboBox<>(interests.keySet().toArray(new String[0]));
        degreeDialog.add(person1Selector, gbc);
        
        // Person 2 selector
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel person2Label = new JLabel("Second person:");
        degreeDialog.add(person2Label, gbc);
        
        gbc.gridx = 1;
        JComboBox<String> person2Selector = new JComboBox<>(interests.keySet().toArray(new String[0]));
        degreeDialog.add(person2Selector, gbc);
        
        // Result label
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        JLabel resultLabel = new JLabel(" ");
        degreeDialog.add(resultLabel, gbc);
        
        // Find button
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
                    resultLabel.setText("There is no connection between " + person1 + " and " + person2);
                } else {
                    resultLabel.setText("The degree of separation is: " + degree);
                    
                    // Visualize the shortest path
                    List<String> path = findShortestPath(person1, person2);
                    JPanel pathPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                    
                    for (int i = 0; i < path.size(); i++) {
                        pathPanel.add(new JLabel(path.get(i)));
                        
                        if (i < path.size() - 1) {
                            pathPanel.add(new JLabel(" → "));
                        }
                    }
                    
                    gbc.gridx = 0;
                    gbc.gridy = 4;
                    gbc.gridwidth = 2;
                    degreeDialog.add(pathPanel, gbc);
                    degreeDialog.pack();
                }
            }
        });
        degreeDialog.add(findButton, gbc);
        
        // Close button
        gbc.gridx = 0;
        gbc.gridy = 5;
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> degreeDialog.dispose());
        degreeDialog.add(closeButton, gbc);
        
        degreeDialog.pack();
        degreeDialog.setLocationRelativeTo(mainFrame);
        degreeDialog.setVisible(true);
    }
    
    private int findDegreeOfSeparation(String start, String end) {
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
            
            for (String neighbor : friendships.getOrDefault(current, Collections.emptyList())) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                    distance.put(neighbor, distance.get(current) + 1);
                }
            }
        }
        
        return -1;
    }
    
    private List<String> findShortestPath(String start, String end) {
        Queue<String> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();
        Map<String, String> predecessor = new HashMap<>();
        
        queue.add(start);
        visited.add(start);
        
        while (!queue.isEmpty()) {
            String current = queue.poll();
            
            if (current.equals(end)) {
                // Reconstruct path
                List<String> path = new ArrayList<>();
                String at = end;
                while (at != null) {
                    path.add(0, at);
                    at = predecessor.get(at);
                }
                return path;
            }
            
            for (String neighbor : friendships.getOrDefault(current, Collections.emptyList())) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                    predecessor.put(neighbor, current);
                }
            }
        }
        
        return Collections.emptyList();
    }
    
    private void showRecommendations(int type) {
        String selectedPerson = (String) personSelector.getSelectedItem();
        if (selectedPerson == null) {
            JOptionPane.showMessageDialog(mainFrame, 
                "Please select a person first.", 
                "No Person Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        List<String> currentFriends = friendships.getOrDefault(selectedPerson, Collections.emptyList());
        Map<String, Integer> scores = new HashMap<>();
        
        switch (type) {
            case 1:
                // Shared friends
                recommendBasedOnFriends(selectedPerson, currentFriends, scores);
                break;
            case 2:
                // Shared interests
                recommendBasedOnInterests(selectedPerson, currentFriends, scores);
                break;
            case 3:
                // Combined recommendation
                recommendCombined(selectedPerson, currentFriends, scores);
                break;
            default:
                break;
        }

        // Sort and update visualization
        Map<String, Integer> sortedScores = new TreeMap<>(
            (a, b) -> scores.get(b) - scores.get(a)
        );
        sortedScores.putAll(scores);

        networkGraph.setSelectedPerson(selectedPerson, sortedScores);
        updateRecommendationDetails(sortedScores);
    }

    private void recommendBasedOnFriends(String selectedPerson, List<String> currentFriends, Map<String, Integer> scores) {
        for (String friend : currentFriends) {
            for (String friendOfFriend : friendships.getOrDefault(friend, Collections.emptyList())) {
                if (!friendOfFriend.equals(selectedPerson) && !currentFriends.contains(friendOfFriend)) {
                    scores.put(friendOfFriend, scores.getOrDefault(friendOfFriend, 0) + friendScoreWeight);
                }
            }
        }
    }

    private void recommendBasedOnInterests(String selectedPerson, List<String> currentFriends, Map<String, Integer> scores) {
        Map<String, String> personInterests = interests.get(selectedPerson);

        for (String other : interests.keySet()) {
            if (other.equals(selectedPerson) || currentFriends.contains(other)) continue;

            int totalScore = 0;
            Map<String, String> otherInterests = interests.get(other);

            for (String category : personInterests.keySet()) {
                if (personInterests.get(category).equalsIgnoreCase(otherInterests.get(category))) {
                    totalScore += interestWeights.getOrDefault(category, 1);
                }
            }
            scores.put(other, totalScore);
        }
    }

    private void recommendCombined(String selectedPerson, List<String> currentFriends, Map<String, Integer> scores) {
        recommendBasedOnFriends(selectedPerson, currentFriends, scores);
        recommendBasedOnInterests(selectedPerson, currentFriends, scores);

        // Optionally include proximity and recency
        Location selectedLocation = locations.get(selectedPerson);

        for (String other : interests.keySet()) {
            if (other.equals(selectedPerson) || currentFriends.contains(other)) continue;

            int currentScore = scores.getOrDefault(other, 0);

            // Proximity factor
            Location otherLocation = locations.get(other);
            if (selectedLocation != null && otherLocation != null) {
                double distance = selectedLocation.distanceTo(otherLocation);
                int proximityScore = Math.max(0, 10 - (int)(distance / 10));
                currentScore += proximityScore;
            }


            scores.put(other, currentScore);
        }
    }

    private void updateRecommendationDetails(Map<String, Integer> sortedScores) {
        JPanel detailsPanel = (JPanel) mainPanel.getComponent(2);
        detailsPanel.removeAll();

        JPanel recommendationsPanel = new JPanel(new GridLayout(0, 1));
        recommendationsPanel.setBorder(BorderFactory.createTitledBorder("Top Recommendations"));

        int count = 0;
        for (Map.Entry<String, Integer> entry : sortedScores.entrySet()) {
            if (count++ >= 10) break;
            recommendationsPanel.add(new JLabel(entry.getKey() + " (Score: " + entry.getValue() + ")"));
        }

        detailsPanel.add(recommendationsPanel);
        detailsPanel.revalidate();
        detailsPanel.repaint();
    }
}