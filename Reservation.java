import java.util.List;
import java.util.stream.Collectors;

/**
 * یک ورودی صف رزرو برای حمل‌ونقل دانشگاهی
 */
public class Reservation {
    private String studentName;          // نام دانشجو
    private String origin;               // مبدا
    private String dest;                 // مقصد
    private List<UniPaths> pathEdges;    // لیست یال‌های مسیر (UniPaths)

    public Reservation(String studentName,
                       String origin,
                       String dest,
                       List<UniPaths> pathEdges) {
        this.studentName = studentName;
        this.origin      = origin;
        this.dest        = dest;
        this.pathEdges   = pathEdges;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDest() {
        return dest;
    }

    public List<UniPaths> getPathEdges() {
        return pathEdges;
    }

    /**
     * مسیر کامل را به صورت "A->B->C->D" برمی‌گرداند
     */
    public String getFullPathString() {
        if (pathEdges.isEmpty()) {
            return origin + "->" + dest;
        }
        // ابتدا همه‌ی شروع‌های یال‌ها را می‌چسبانیم،
        // سپس انتهای آخرین یال را اضافه می‌کنیم.
        String prefix = pathEdges.stream()
                .map(UniPaths::getStartLocation)
                .collect(Collectors.joining("->"));
        String lastEnd = pathEdges.get(pathEdges.size() - 1)
                .getEndLocation();
        return prefix + "->" + lastEnd;
    }

    /**
     * کمینهٔ ظرفیت باقی‌مانده در طول مسیر را برمی‌گرداند
     */
    public int getRemainingCapacity() {
        return pathEdges.stream()
                .mapToInt(UniPaths::getRemainingCapacity)
                .min()
                .orElse(0);
    }

    @Override
    public String toString() {
        // نمایش خلاصه: نام دانشجو و مسیر کلی
        return studentName + ": " + origin + " → " + dest;
    }
}






















