import java.util.*;

/**
 * این کلاس مسئول محاسبه درخت پوشای کمینه (Minimum Spanning Tree) با استفاده از الگوریتم Prim است.
 * الگوریتم روی گراف غیرجهت‌دار با یال‌های دارای وزن اعمال می‌شود.
 */
public class MSTCalculator {

    /**
     * اجرای الگوریتم Prim برای یافتن درخت پوشای کمینه بر اساس لیست دانشگاه‌ها و مسیرها.
     * @param universities لیست گره‌ها (دانشگاه‌ها)
     * @param allPaths لیست یال‌های موجود بین دانشگاه‌ها
     * @return لیست یال‌هایی که در MST قرار گرفته‌اند
     */
    public static List<UniPaths> computeMST(List<Universities> universities, List<UniPaths> allPaths) {
        List<UniPaths> mst = new ArrayList<>(); // خروجی نهایی MST

        if (universities.isEmpty()) return mst;

        Set<String> visited = new HashSet<>(); // نگهداری دانشگاه‌هایی که در MST هستند
        PriorityQueue<UniPaths> pq = new PriorityQueue<>(Comparator.comparingInt(UniPaths::getCost));

        String start = universities.get(0).getUniversityName(); // شروع از اولین دانشگاه
        visited.add(start);

        // همه یال‌هایی که از نود شروع به سایر نودها می‌رسند را وارد صف اولویت می‌کنیم
        for (UniPaths path : allPaths) {
            if (path.getStartLocation().equals(start) || path.getEndLocation().equals(start)) {
                pq.offer(path);
            }
        }

        // اجرای الگوریتم Prim
        while (!pq.isEmpty() && visited.size() < universities.size()) {
            UniPaths path = pq.poll();
            String u = path.getStartLocation();
            String v = path.getEndLocation();

            // پیدا کردن یال‌هایی که یکی از گره‌ها در مجموعه بازدید شده است ولی دیگری نه
            if (visited.contains(u) && !visited.contains(v)) {
                visited.add(v);
                mst.add(path);
            } else if (visited.contains(v) && !visited.contains(u)) {
                visited.add(u);
                mst.add(path);
            } else {
                continue; // یال باعث ایجاد حلقه می‌شود → صرف‌نظر می‌کنیم
            }

            // افزودن یال‌های جدید از گره تازه وارد شده به صف اولویت
            for (UniPaths edge : allPaths) {
                if ((edge.getStartLocation().equals(u) && !visited.contains(edge.getEndLocation())) ||
                        (edge.getEndLocation().equals(u) && !visited.contains(edge.getStartLocation())) ||
                        (edge.getStartLocation().equals(v) && !visited.contains(edge.getEndLocation())) ||
                        (edge.getEndLocation().equals(v) && !visited.contains(edge.getStartLocation()))) {
                    pq.offer(edge);
                }
            }
        }

        return mst;
    }
}