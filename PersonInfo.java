import java.io.*;
import java.util.*;

public class PersonInfo {

    // load two csv files needed for the code
    private static final String INTERESTS_FILE = "interests.csv";
    private static final String FRIENDSHIPS_FILE = "friendships.csv";
    // initialize hashmaps for  csv files and weights
    private Map<String, Map<String, String>> interests = new HashMap<>();
    private Map<String, List<String>> friendships = new HashMap<>();
    private Map<String, Integer> interestWeights = new HashMap<>();
    private int friendScoreWeight;  // friend weighting

    public static void main(String[] args) {
        // catch try to intialise the files
        try {
            PersonInfo people = new PersonInfo();
            people.loadInterests();
            people.loadFriendships();
            people.collectInterestWeights();
            people.runPrompt();
        } catch (IOException e) {
            System.out.println("There was an error reading the files: " + e.getMessage());
        }
    }

    private void collectInterestWeights() throws IOException {
        Scanner scanner = new Scanner(System.in);
        // asking user to set the weights for each metric
        System.out.println("Set weights (points based):");

        // collecting weights for each interest
        try (BufferedReader reader = new BufferedReader(new FileReader(INTERESTS_FILE))) {
            String[] headers = reader.readLine().split(",");
            // read and store the header names from the interests file
            for (int i = 1; i < headers.length; i++) {
                int weight = 0;
                boolean validWeight = false;
                while (!validWeight) {
                    System.out.print("Enter the weight for " + headers[i].trim().toLowerCase() + " (1-10): ");
                    // many points should each interest  be worth
                    try { // only integer check
                        weight = scanner.nextInt();
                        if (weight >= 1 && weight <= 10) {
                            validWeight = true;
                        } else {
                            scanner.nextLine(); // clear buffer after invalid input
                        }
                    } catch (Exception e) {
                        System.out.println("Not an integer. Please enter a valid integer.");
                        scanner.nextLine(); // discard rest of line
                    }
                    if (weight >= 1 && weight <= 10) {
                        validWeight = true;
                    } else {
                        System.out.println("Weight must be an integer between 1 and 10.");
                    }
                }
                interestWeights.put(headers[i].trim(), weight);
            }
        }

        // collecting weight for shared friends
        int friendScoreWeight = 0;
        boolean validFriendWeight = false;
        while (!validFriendWeight) {
            System.out.print("Enter the weight for each shared friend (1-10): ");
            try {
                friendScoreWeight = scanner.nextInt();
                if (friendScoreWeight >= 1 && friendScoreWeight <= 10) {
                    validFriendWeight = true;
                } else {
                    System.out.println("Weight must be an integer between 1 and 10.");
                    scanner.nextLine(); // clear buffer after invalid input
                }
            } catch (Exception e) {
                System.out.println("Not an integer. Please enter a valid integer.");
                scanner.nextLine(); // consume and discard the rest of the line
            }
        }
        this.friendScoreWeight = friendScoreWeight;
    }


    private void loadInterests() throws IOException { // getting interests
        try (BufferedReader reader = new BufferedReader(new FileReader(INTERESTS_FILE))) {
            String[] headers = reader.readLine().split(","); // read and store the header names from the
            // interests file
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length != headers.length) continue; // if some sort of data is missing from a person,
                // we skip the person
                String name = data[0].trim();
                Map<String, String> details = new HashMap<>();
                for (int i = 1; i < headers.length; i++) {
                    details.put(headers[i].trim(), data[i].trim()); // input details fo the person in thee hashmap named details
                }
                interests.put(name, details); // add to interests
            }
        }
    }

    private void loadFriendships() throws IOException { // getting friendships
        try (BufferedReader reader = new BufferedReader(new FileReader(FRIENDSHIPS_FILE))) {
            String[] names = reader.readLine().split(","); // reading the header of the file
            List<String> cleanedNames = Arrays.asList(Arrays.copyOfRange(names, 1, names.length)); // remove the first empty line
            String line;
            while ((line = reader.readLine()) != null) { // awhile there are people
                String[] data = line.split(","); // split csv
                String person = data[0].trim(); // first element is the person's name
                List<String> friends = new ArrayList<>();
                for (int i = 1; i < data.length; i++) { // skip person's name (start from 1)
                    if ("1".equals(data[i].trim())) { // if equals to 1 then friend
                        friends.add(cleanedNames.get(i - 1).trim()); // get name
                    }
                }
                friendships.put(person, friends);
            }
        }
    }


    private void chooseRecommendationType(String person, List<String> currentFriends) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Choose the type of friend recommendation:");
        System.out.println("1: Find friends based on shared friends only (Triadic Closure)");
        System.out.println("2: Find friends based on shared interests only (Focal Closure)");
        System.out.println("3: Find friends based on both shared friends and interests (Membership Closure)");
        System.out.print("Enter your choice: ");
        int choice = 0;
        try {
            choice = scanner.nextInt();
        } catch (Exception e) {
            scanner.nextLine(); // clear buffer
        }
        switch (choice) { // switch case based on the choice
            case 1:
                recommendBasedOnFriends(person, currentFriends);
                break;
            case 2:
                recommendBasedOnInterests(person, currentFriends);
                break;
            case 3:
                recommendFriends(person, currentFriends);
                break;
            default:
                System.out.println("Invalid, try again.");
                chooseRecommendationType(person, currentFriends); // asks user again
                break;
        }
    }

    private void handleFriendRecommendations(Scanner scanner) {
        System.out.print("Enter the name of the person you want recommendations for: ");
        String inputName = scanner.nextLine().trim().toLowerCase();
        String name =  inputName.substring(0, 1).toUpperCase() + inputName.substring(1); // Capitalize the first letter
        if (!interests.containsKey(name)) { // if the name is not in the csv
            System.out.println("No data available for " + name); // tells user can't be found
            return;
        }
        System.out.println("Interests of " + name + ": "); // else tells the user the interests
        System.out.println(interests.get(name));
        if (friendships.containsKey(name)) { // if the user has friends
            List<String> currentFriends = friendships.get(name); // gets the friends in a list
            System.out.println("Friends of " + name + ": ");
            System.out.println(currentFriends); // prints the friends
            chooseRecommendationType(name, currentFriends); // asks the user what recommendation they want
        } else {
            System.out.println(name + " has no recorded friends."); // tells the user they have no friends
        }
    }

    private void handleDegreeOfSeparation(Scanner scanner) {  // find the degree between 2 people (friendship degree)
        System.out.print("Enter the name of the first person: "); // first person
        String name1 = scanner.nextLine().trim().toLowerCase();
        String person1 =  name1.substring(0, 1).toUpperCase() + name1.substring(1); // Capitalize the first letter
        System.out.print("Enter the name of the second person: "); // second person
        String name2 = scanner.nextLine().trim().toLowerCase();
        String person2 =  name2.substring(0, 1).toUpperCase() + name2.substring(1); // Capitalize the first letter


        if (!friendships.containsKey(person1) || !friendships.containsKey(person2)) { // if a name isn't recorded
            System.out.println("Error: a name was not found."); // tells user there is a name
            return;
        }

        int degree = findDegreeOfSeparation(person1, person2); // finds the degree between the 2 people
        if (degree == -1) { // if non-existent (-1)
            System.out.println("There is no connection between " + person1 + " and " + person2); // no degree
        } else {
            System.out.println("The degree of separation between " + person1 + " and " + person2 + " is: " + degree); // prints degree
        }
    }

    private void runPrompt() {
        Scanner scanner = new Scanner(System.in);
        while (true) { // asks user what option they want
            System.out.println("What would you like to do?");
            System.out.println("1: Find friend recommendations");
            System.out.println("2: Find the degree of separation between two people");
            System.out.print("Enter your choice (or type 'exit' to exit): ");
            String choice = scanner.nextLine().trim();

            if ("exit".equalsIgnoreCase(choice)) {
                break;
            } else if ("1".equals(choice)) {
                handleFriendRecommendations(scanner);  // function to ask which recommendation
            } else if ("2".equals(choice)) {
                handleDegreeOfSeparation(scanner); // function to find the degree
            } else {
                System.out.println("Invalid choice, try again.");
            }
        }
        scanner.close();
    }

    private int findDegreeOfSeparation(String start, String end) { // bfs implementation
        Queue<String> queue = new LinkedList<>(); // queue
        Set<String> visited = new HashSet<>(); // visited
        Map<String, Integer> distance = new HashMap<>(); // distance between

        queue.add(start); // before we add to the queue the first person
        visited.add(start); // add the person as visited
        distance.put(start, 0); // keep track of the distance

        while (!queue.isEmpty()) { // if the queue is not empty
            String current = queue.poll(); // take the top and delete the element from the queue
            if (current.equals(end)) { // if the current element is the friend we are searching for
                return distance.get(current); // found, so return distance
            }

            for (String neighbour : friendships.get(current)) { // for each neighbour (friend)
                if (!visited.contains(neighbour)) { // we see if the neighbour was visited
                    visited.add(neighbour); // if it wasn't, add to visited set
                    queue.add(neighbour);  // add the neighbour to the queue
                    distance.put(neighbour, distance.get(current) + 1); // increment the distance to this b person to 1
                }
            }
        }

        return -1;  // return -1 if no path found
    }

    private void recommendBasedOnFriends(String person, List<String> currentFriends) {
        Map<String, List<String>> commonFriends = new HashMap<>(); // hashmap for common friends

        for (String friend : currentFriends) { // for each friend
            List<String> friendsOfFriend = friendships.get(friend);
            for (String foaf : friendsOfFriend) { // friends of friends
                if (!currentFriends.contains(foaf) && !foaf.equals(person)) { // if contains friends of friends
                    commonFriends.computeIfAbsent(foaf, k -> new ArrayList<>()).add(friend); // map function
                }
            }
        }

        List<Map.Entry<String, List<String>>> sortedFriends = new ArrayList<>(commonFriends.entrySet());
        sortedFriends.sort((a, b) -> b.getValue().size() - a.getValue().size()); // sort by the friends size

        // Display the recommended friends
        System.out.println("Top recommended friends for " + person + ":");
        int count = 1; // Start numbering from 1
        for (Map.Entry<String, List<String>> entry : sortedFriends) {
            if (count > 5) break; // Exit loop if 5 recommendations are printed
            String friendName = entry.getKey();
            List<String> commonFriendsList = entry.getValue();
            String commonFriendsStr = String.join(", ", commonFriendsList);
            System.out.println(count + ". " + friendName);
            System.out.println("   Common Friends: " + commonFriendsStr);
            System.out.println("------------------------------------------");
            count++; // Increment count for the next friend
        }

        // If no recommendations are found
        if (count == 1) {
            System.out.println("No new friend recommendations based on shared friends for " + person);
        }
    }

    private void recommendBasedOnInterests(String person, List<String> currentFriends) { // recommendations based on the interests
        Map<String, Integer> scores = new HashMap<>(); // hashmap to keep track of score
        Map<String, Map<String, String>> commonInterests = new HashMap<>(); // hashmap to keep track of common interests

        for (String potentialFriend : friendships.keySet()) { // potential friends
            if (!currentFriends.contains(potentialFriend) && !potentialFriend.equals(person)) {
                int score = 0;
                Map<String, String> tempCommonInterests = new HashMap<>();
                for (Map.Entry<String, String> entry : interests.get(person).entrySet()) { // collects the interest of each eprson
                    String interest = entry.getKey();
                    String value = entry.getValue();
                    String friendInterestValue = interests.get(potentialFriend).get(interest);
                    if (friendInterestValue != null && value.equals(friendInterestValue)) {
                        tempCommonInterests.put(interest, value);
                        score += interestWeights.getOrDefault(interest, 0); // add to the score the interestWeights (if none, then just put 0)
                    }
                }
                if (score > 0) {
                    scores.put(potentialFriend, score); // adds the score to the hashmap alongside the potential friend
                    commonInterests.put(potentialFriend, tempCommonInterests);
                }
            }
        }

        List<Map.Entry<String, Integer>> sortedFriends = new ArrayList<>(scores.entrySet());
        sortedFriends.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        System.out.println("Recommended friends for " + person + " based on shared interests:"); // display recommendations 
        int count = 1; // counter to count the recommendations
        for (Map.Entry<String, Integer> entry : sortedFriends) {
            if (count > 5) break; // display only top 5 recommendations
            String friendName = entry.getKey(); // get name of friend
            int score = entry.getValue(); // get the score
            Map<String, String> interestsMap = commonInterests.get(friendName);
            System.out.println(count + ". " + friendName);
            System.out.println("   Score: " + score);
            System.out.println("   Common Interests:");
            for (Map.Entry<String, String> interestEntry : interestsMap.entrySet()) {
                System.out.println("      - " + interestEntry.getKey() + ": " + interestEntry.getValue());
            }
            System.out.println("------------------------------------------");
            count++;
        }

        // if no recommendations are found
        if (count == 1) {
            System.out.println("No new friend recommendations based on interests for " + person);
        }
    }
    private void recommendFriends(String person, List<String> currentFriends) {
        Map<String, Integer> scores = new HashMap<>();
        Map<String, List<String>> commonFriends = new HashMap<>();
        Map<String, Map<String, String>> commonInterests = new HashMap<>();

        for (String friend : currentFriends) { // for each of the friends
            List<String> friendsOfFriend = friendships.get(friend); // get the list of friends
            for (String foaf : friendsOfFriend) { // for each friend of friend
                if (!currentFriends.contains(foaf) && !foaf.equals(person)) { // if the current friend doesnt contain the friend of a friend, and the friend of the friend isn't current person
                    commonFriends.computeIfAbsent(foaf, k -> new ArrayList<>()).add(friend); // map the friend of the new friend
                    scores.put(foaf, scores.getOrDefault(foaf, 0) + friendScoreWeight); // add to the hashmap scores the friend of the friend alongside their score
                }
            }
        }

        for (String potentialFriend : friendships.keySet()) { // for each potential friend
            if (!currentFriends.contains(potentialFriend) && !potentialFriend.equals(person)) { // if the person doesn't have the potential friend as a friend and if the person isn't the potential friend
                int interestScore = 0; // set score to 0 for interests
                Map<String, String> tempCommonInterests = new HashMap<>(); // hashmap for the common interests
                for (Map.Entry<String, String> entry : interests.get(person).entrySet()) { 
                    String interest = entry.getKey(); // get interests and score
                    String value = entry.getValue();
                    String friendInterestValue = interests.get(potentialFriend).get(interest); // get the interests of the potential friend
                    if (friendInterestValue != null && value.equals(friendInterestValue)) { // check friend interests has a value
                        tempCommonInterests.put(interest, value); // add the interests and the value
                        interestScore += interestWeights.getOrDefault(interest, 0); // adds to the score
                    }
                }
                if (interestScore > 0) { // if an interest score above 0
                    scores.put(potentialFriend, scores.getOrDefault(potentialFriend, 0) + interestScore); // add the potential friend alongside their score to the map
                    commonInterests.put(potentialFriend, tempCommonInterests); // add the common interests
                }
            }
        }

        List<Map.Entry<String, Integer>> sortedFriends = new ArrayList<>(scores.entrySet()); //create a list from the set of entries in the scores map
        sortedFriends.sort((a, b) -> b.getValue().compareTo(a.getValue())); // sort the list in descending order based on the scores

        // display recommendations
        System.out.println("Combined friend recommendations for " + person + ":");
        int count = 1;
        for (Map.Entry<String, Integer> entry : sortedFriends) {
            if (count > 5) break; // display only top 5 recommendations
            String friendName = entry.getKey();
            int totalScore = entry.getValue();
            List<String> friendCommonFriends = commonFriends.getOrDefault(friendName, Collections.emptyList()); // get the common friends
            Map<String, String> friendCommonInterests = commonInterests.getOrDefault(friendName, Collections.emptyMap());


            // printing out each recommendation
            System.out.println(count + ". " + friendName);
            System.out.println("   Total Score: " + totalScore);
            System.out.println("   Mutual Friends: " + friendCommonFriends);
            System.out.println("   Shared Interests: " + friendCommonInterests);
            System.out.println("------------------------------------------");
            count++;
        }

        // if no recommendations are found
        if (count == 1) {
            System.out.println("No new friend recommendations based on combined criteria for " + person);
        }
    }
}