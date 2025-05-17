import java.io.*;
import java.util.*;

public class StudentDataLoader {
    private Map<String, Student> students = new HashMap<>();
    private Map<String, List<String>> collaborations = new HashMap<>();

    public StudentDataLoader(String studentFile, String skillFile, String collaborationFile) throws IOException {
        Map<String, String> majors = loadMajors(studentFile);
        Map<String, Map<String, Integer>> skills = loadSkills(skillFile);
        for (String name : majors.keySet()) {
            if (skills.containsKey(name)) {
                students.put(name, new Student(name, majors.get(name), skills.get(name)));
            }
        }
        loadCollaborations(collaborationFile);
    }

    private Map<String, String> loadMajors(String path) throws IOException {
        Map<String, String> majors = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String[] headers = reader.readLine().split(",");
            int majorIdx = Arrays.asList(headers).indexOf("Major");

            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length > majorIdx) {
                    majors.put(data[0].trim(), data[majorIdx].trim());
                }
            }
        }
        return majors;
    }

    private Map<String, Map<String, Integer>> loadSkills(String path) throws IOException {
        Map<String, Map<String, Integer>> allSkills = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String[] headers = reader.readLine().split(",");
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                String name = data[0].trim();
                Map<String, Integer> skillMap = new HashMap<>();
                for (int i = 1; i < data.length; i++) {
                    try {
                        skillMap.put(headers[i].trim(), Integer.parseInt(data[i].trim()));
                    } catch (NumberFormatException e) {
                        skillMap.put(headers[i].trim(), 1); // default
                    }
                }
                allSkills.put(name, skillMap);
            }
        }
        return allSkills;
    }

    private void loadCollaborations(String path) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String[] headers = reader.readLine().split(",");
            List<String> names = Arrays.asList(Arrays.copyOfRange(headers, 1, headers.length));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                String name = data[0].trim();
                List<String> partners = new ArrayList<>();
                for (int i = 1; i < data.length; i++) {
                    if ("1".equals(data[i].trim())) {
                        partners.add(names.get(i - 1).trim());
                    }
                }
                collaborations.put(name, partners);
            }
        }
    }

    public Map<String, Student> getStudents() {
        return students;
    }

    public Map<String, List<String>> getCollaborations() {
        return collaborations;
    }
}