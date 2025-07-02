import java.util.List;
import java.util.PriorityQueue;
import java.util.Comparator;

public class GraphUtils {
    /**
     * محاسبه هزینه بین دو دانشگاه بر اساس فاصله اقلیدسی
     */
    private static int calculateCost(Universities u1, Universities u2) {
        int dx = u1.getX() - u2.getX();
        int dy = u1.getY() - u2.getY();
        return (int) Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * افزودن پویا دانشگاه جدید با استفاده از PriorityQueue برای یافتن حداقل هزینه
     */
    static void updateGraphAfterAddingUniversity(Universities newUni, List<Universities> allUnis, List<UniPaths> paths) {
        // ساخت یک صف اولویت‌دار بر اساس کمترین هزینه
        PriorityQueue<UniPaths> pq = new PriorityQueue<>(Comparator.comparingInt(UniPaths::getCost));

        // قرار دادن تمام یال‌های ممکن به صف اولویت
        for (Universities other : allUnis) {
            if (!other.getUniversityName().equals(newUni.getUniversityName())) {
                int cost = calculateCost(newUni, other);
                UniPaths edge = new UniPaths(
                        0,
                        0,
                        cost,
                        0,
                        newUni.getUniversityName(),
                        other.getUniversityName(),
                        false
                );
                pq.offer(edge);
            }
        }

        // انتخاب یال با کمترین هزینه و افزودن به گراف
        if (!pq.isEmpty()) {
            UniPaths bestEdge = pq.poll();
            // علامت‌گذاری به عنوان یال خودکار
            bestEdge.setRandom(true);
            paths.add(bestEdge);
        }
    }
}
