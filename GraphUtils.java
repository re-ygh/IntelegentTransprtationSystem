import java.util.List;
import java.util.Random;

public class GraphUtils {
    static void updateGraphAfterAddingUniversity(Universities newUni, List<Universities> allUnis, List<UniPaths> paths) {
        // الگویی ساده برای اتصال به نزدیک‌ترین دانشگاه از نظر هزینه مسیر
        Random rand = new Random();
        int minCost = Integer.MAX_VALUE;
        Universities bestMatch = null;
        for (Universities other : allUnis) {
            if (!other.getUniversityName().equals(newUni.getUniversityName())) {
                int simulatedCost = rand.nextInt(30) + 10; // شبیه‌سازی هزینه
                if (simulatedCost < minCost) {
                    minCost = simulatedCost;
                    bestMatch = other;
                }
            }
        }
        if (bestMatch != null) {
            UniPaths autoPath = new UniPaths(0, 0, minCost, 0, newUni.getUniversityName(), bestMatch.getUniversityName(), true);
            paths.add(autoPath);
        }
    }

}
