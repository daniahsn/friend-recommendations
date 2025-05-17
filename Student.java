import java.util.Map;

public class Student {
    private String name;
    private String major;
    private Map<String, Integer> skills;

    public Student(String name, String major, Map<String, Integer> skills) {
        this.name = name;
        this.major = major;
        this.skills = skills;
    }

    public String getName() {
        return name;
    }

    public String getMajor() {
        return major;
    }

    public Map<String, Integer> getSkills() {
        return skills;
    }

    public int getSkill(String skill) {
        return skills.getOrDefault(skill, 0);
    }
}