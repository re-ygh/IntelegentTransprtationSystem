import java.util.*;

/**
 * کلاس کمکی برای عملیات گرافی مانند محاسبه هزینه بین دانشگاه‌ها و پیشنهاد اتصال جدید
 */
public class GraphUtils {

    /**
     * محاسبه فاصله اقلیدسی بین دو دانشگاه به‌عنوان هزینه یال
     * @param u1 دانشگاه اول
     * @param u2 دانشگاه دوم
     * @return فاصله بین دو مختصات به‌صورت عدد صحیح (int)
     */
    private static int calculateCost(Universities u1, Universities u2) {
        int dx = u1.getX() - u2.getX();
        int dy = u1.getY() - u2.getY();
        return (int) Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * وقتی دانشگاه جدیدی اضافه می‌شود، این متد کوتاه‌ترین مسیر بین آن و سایر دانشگاه‌ها را پیشنهاد می‌دهد
     * و آن را به‌صورت یال جدید (با پرچم isRandom=true) به گراف اضافه می‌کند.
     * از PriorityQueue برای یافتن سریع‌ترین یال استفاده می‌شود.
     *
     * @param newUni دانشگاه جدید
     * @param allUnis لیست همه دانشگاه‌های موجود
     * @param paths لیست مسیرهای گراف (که مسیر جدید به آن اضافه می‌شود)
     */
    public static void updateGraphAfterAddingUniversity(Universities newUni, List<Universities> allUnis, List<UniPaths> paths) {
        // صف اولویت‌دار برای انتخاب یال با کمترین هزینه
        PriorityQueue<UniPaths> pq = new PriorityQueue<>(Comparator.comparingInt(UniPaths::getCost));

        for (Universities other : allUnis) {
            if (!other.getUniversityName().equals(newUni.getUniversityName())) {
                int cost = calculateCost(newUni, other);
                UniPaths edge = new UniPaths(
                        1, 2, cost, 50,
                        newUni.getUniversityName(), other.getUniversityName(), true, 50);
                pq.offer(edge);
            }
        }

        // افزودن تنها کم‌هزینه‌ترین یال پیشنهادی به مسیرها
        if (!pq.isEmpty()) {
            UniPaths bestEdge = pq.poll();
            bestEdge.setRandom(true); // مشخص کردن که یال به‌صورت خودکار پیشنهاد شده
            paths.add(bestEdge);
        }
    }
}