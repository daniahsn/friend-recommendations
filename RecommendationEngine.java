import java.util.*;

public class RecommendationEngine {
    private Map<String, Student> students;
    private Map<String, List<String>> collaborations;

    public RecommendationEngine(Map<String, Student> students,
                                 Map<String, List<String>> collaborations) {
        this.students = students;
        this.collaborations = collaborations;
    }

    public List<String> recommendPartners(String studentName, int maxRecommendations) {
        Student target = students.get(studentName);
        if (target == null) return Collections.emptyList();

        Map<String, Integer> scores = new HashMap<>();

        for (String candidateName : students.keySet()) {
            if (candidateName.equals(studentName)) continue;
            if (isInTeam(candidateName) || isInTeam(studentName)) continue;

            Student candidate = students.get(candidateName);
            int score = 0;

            // Shared major bonus
            if (candidate.getMajor().equalsIgnoreCase(target.getMajor())) {
                score += 10;
            }

            // Skill complementarity
            for (String skill : target.getSkills().keySet()) {
                int targetSkill = target.getSkill(skill);
                int candidateSkill = candidate.getSkill(skill);
                if (candidateSkill > targetSkill) {
                    score += (candidateSkill - targetSkill);
                }
            }

            // Collaboration penalty
            List<String> prevPartners = collaborations.getOrDefault(studentName, new ArrayList<>());
            if (prevPartners.contains(candidateName)) {
                score -= 5; // penalize reuse
            }

            scores.put(candidateName, score);
        }

        return scores.entrySet()
                     .stream()
                     .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                     .limit(maxRecommendations)
                     .map(Map.Entry::getKey)
                     .toList();
    }

    public List<String> recommendPartners(String studentName, int max, int majorWeight, int skillWeight, int collabPenalty) {
        Student target = students.get(studentName);
        if (target == null) return Collections.emptyList();
    
        Map<String, Integer> scores = new HashMap<>();
    
        for (String candidateName : students.keySet()) {
            if (candidateName.equals(studentName)) continue;
    
            Student candidate = students.get(candidateName);
            int score = 0;
    
            if (candidate.getMajor().equalsIgnoreCase(target.getMajor())) {
                score += majorWeight;
            }
    
            for (String skill : target.getSkills().keySet()) {
                int delta = candidate.getSkill(skill) - target.getSkill(skill);
                if (delta > 0) score += delta * skillWeight;
            }
    
            List<String> pastPartners = collaborations.getOrDefault(studentName, new ArrayList<>());
            if (pastPartners.contains(candidateName)) {
                score -= collabPenalty;
            }
    
            scores.put(candidateName, score);
        }
    
        return scores.entrySet().stream()
                .sorted((a, b) -> b.getValue() - a.getValue())
                .limit(max)
                .map(Map.Entry::getKey)
                .toList();
    }

    public int computeScore(String source, String target, int majorWeight, int skillWeight, int collabPenalty) {
        if (!students.containsKey(source) || !students.containsKey(target)) return 0;
        if (source.equals(target)) return 0;
    
        Student a = students.get(source);
        Student b = students.get(target);
        int score = 0;
    
        if (a.getMajor().equalsIgnoreCase(b.getMajor())) {
            score += majorWeight;
        }
    
        for (String skill : a.getSkills().keySet()) {
            int delta = b.getSkill(skill) - a.getSkill(skill);
            if (delta > 0) score += delta * skillWeight;
        }
    
        List<String> prev = collaborations.getOrDefault(source, new ArrayList<>());
        if (prev.contains(target)) score -= collabPenalty;
    
        return score;
    }

    public Map<String, List<String>> getCollaborations() {
        return collaborations;
    }

    // Optional: simulate check for existing teams
    private boolean isInTeam(String name) {
        // Placeholder for integration with future team roster
        return false;
    }
}