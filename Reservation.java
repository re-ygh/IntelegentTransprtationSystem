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
    /**
     * مسیر کامل را به صورت "A->B(3)->C(2)->D(5)" برمی‌گرداند،
     * یعنی هر گام با ظرفیت remainingCapacity مشخص شده.
     */
    public String getFullPathString() {
        if (pathEdges == null || pathEdges.isEmpty()) {
            return origin + "->" + dest;
        }
        StringBuilder sb = new StringBuilder();
        // نام مبدا اولین یال
        sb.append(pathEdges.get(0).getStartLocation());
        // برای هر یال، نام مقصد و ظرفیت باقی‌مانده را اضافه کن
        for (UniPaths edge : pathEdges) {
            sb.append("->")
                    .append(edge.getEndLocation())
                    .append("(")
                    .append(edge.getRemainingCapacity())
                    .append(")");
        }
        return sb.toString();
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






















