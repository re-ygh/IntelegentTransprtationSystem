import java.util.*;
import java.util.OptionalInt;

/**
 * کلاس کمکی برای عملیات گرافی مانند محاسبه هزینه بین دانشگاه‌ها،
 * پیشنهاد اتصال جدید و مدیریت ظرفیت مسیرها.
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
     * وقتی دانشگاه جدیدی اضافه می‌شود، این متد کوتاه‌ترین مسیر بین آن و سایر دانشگاه‌ها را پیشنهاد
     * می‌دهد و آن را به‌صورت یال جدید (با پرچم isRandom=true) به گراف اضافه می‌کند.
     * از PriorityQueue برای یافتن سریع‌ترین یال استفاده می‌شود.
     *
     * @param newUni دانشگاه جدید
     * @param allUnis لیست همه دانشگاه‌های موجود
     * @param paths لیست مسیرهای گراف (که مسیر جدید به آن اضافه می‌شود)
     */
    public static void updateGraphAfterAddingUniversity(Universities newUni,
                                                        List<Universities> allUnis,
                                                        List<UniPaths> paths) {
        PriorityQueue<UniPaths> pq = new PriorityQueue<>(Comparator.comparingInt(UniPaths::getCost));

        for (Universities other : allUnis) {
            if (!other.getUniversityName().equals(newUni.getUniversityName())) {
                int cost = calculateCost(newUni, other);
                UniPaths edge = new UniPaths(
                        1,      // id یال
                        6,      // جنس یال (مثلاً 6 معادل نوع خاصی از مسیر)
                        cost,   // وزن یا هزینه
                        20,     // ظرفیت اولیه
                        newUni.getUniversityName(),
                        other.getUniversityName(),
                        true,   // isRandom
                        20,     // remainingCapacity اولیه
                        null    // info اضافی
                );
                pq.offer(edge);
            }
        }

        // افزودن تنها کم‌هزینه‌ترین یال پیشنهادی به مسیرها
        if (!pq.isEmpty()) {
            UniPaths bestEdge = pq.poll();
            bestEdge.setRandom(true);
            paths.add(bestEdge);
        }
    }

    /**
     * کمینهٔ ظرفیت باقی‌مانده در طول یک لیست از یال‌ها (UniPaths) را برمی‌گرداند
     * @param edges لیست یال‌ها
     * @return کمترین مقدار remainingCapacity یا 0 اگر لیست خالی باشد
     */
    public static int getMinCapacityAlong(List<UniPaths> edges) {
        OptionalInt min = edges.stream()
                .mapToInt(UniPaths::getRemainingCapacity)
                .min();
        return min.orElse(0);
    }

    /**
     * پس از حرکت یک دانشجو، ۱ واحد ظرفیت یال را آزاد (increase) می‌کند
     * @param edge یالی که ظرفیتش قرار است افزایش یابد
     */
    public static void incrementCapacity(UniPaths edge) {
        edge.setRemainingCapacity(edge.getRemainingCapacity() + 1);
    }
}
