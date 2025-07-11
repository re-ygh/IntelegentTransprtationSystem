import java.util.*;
import java.util.OptionalInt;

/**
 * کلاس کمکی برای عملیات گرافی مانند محاسبه هزینه بین دانشگاه‌ها،
 * پیشنهاد اتصال جدید و مدیریت ظرفیت مسیرها.
 */
//
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
                        new ArrayList<>()    // reservations خالی
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
        int newCap = edge.getRemainingCapacity() + 1;
        if (newCap > edge.getCapacity()) {
            newCap = edge.getCapacity();
        }
        edge.setRemainingCapacity(newCap);
    }

    /**
     * ساخت ماتریس هزینه بین دانشگاه‌های انتخاب‌شده برای مسئله TSP
     *
     * @param selectedUnis لیست دانشگاه‌های انتخاب‌شده توسط کاربر
     * @param allPaths لیست تمام مسیرهای موجود در سیستم
     * @param timeWeight وزن زمان در محاسبه هزینه (بین 0 تا 1)
     * @param costWeight وزن هزینه مالی در محاسبه هزینه (بین 0 تا 1)
     * @return ماتریس هزینه با ابعاد [n][n] که n تعداد دانشگاه‌های انتخابی است
     * @throws IllegalArgumentException اگر مجموع وزن‌ها برابر با 1 نباشد
     */
    public static double[][] buildCostMatrix(List<Universities> selectedUnis,
                                             List<UniPaths> allPaths,
                                             double timeWeight,
                                             double costWeight) {
        // اعتبارسنجی پارامترها
        if (Math.abs((timeWeight + costWeight) - 1.0) > 0.0001) {
            throw new IllegalArgumentException("مجموع وزن‌های زمان و هزینه باید برابر با 1 باشد");
        }

        int n = selectedUnis.size();
        double[][] matrix = new double[n][n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    matrix[i][j] = 0; // هزینه سفر از یک دانشگاه به خودش صفر است
                    continue;
                }

                String from = selectedUnis.get(i).getUniversityName();
                String to = selectedUnis.get(j).getUniversityName();

                // یافتن کوتاه‌ترین مسیر با الگوریتم دایجسترا
                boolean pathExists = UniPaths.DijkstraShortestPath(allPaths, from, to, false);

                if (!pathExists) {
                    matrix[i][j] = Double.POSITIVE_INFINITY; // عدم دسترسی
                } else {
                    // محاسبه هزینه ترکیبی با وزن‌دهی به زمان و هزینه
                    matrix[i][j] = UniPaths.DijkstraPaths.stream()
                            .mapToDouble(p -> (costWeight * p.getCost()) +
                                    (timeWeight * (p.getEndTime() - p.getStartTime())))
                            .sum();
                }
            }
        }
        return matrix;
    }

    /**
     * نسخه ساده‌تر با وزن‌های پیش‌فرض (50% زمان، 50% هزینه)
     */
    public static double[][] buildCostMatrix(List<Universities> selectedUnis,
                                             List<UniPaths> allPaths) {
        return buildCostMatrix(selectedUnis, allPaths, 0.5, 0.5);
    }

}
