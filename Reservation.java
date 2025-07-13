import java.util.List;
import java.util.stream.Collectors;

/**
 * یک ورودی صف رزرو برای حمل‌ونقل دانشگاهی
 * ثبت و پیگیری با صف اولویت‌دار بر اساس زمان رزرو
 */
//
public class Reservation implements Comparable<Reservation> {
    private final long bookingTimestamp;      // زمان رزرو
    private String studentName;               // نام دانشجو
    private String origin;                    // مبدا
    private String dest;                      // مقصد
    private List<UniPaths> pathEdges;         // لیست یال‌های مسیر

    public Reservation(String studentName,
                       String origin,
                       String dest,
                       List<UniPaths> pathEdges) {
        this.bookingTimestamp = System.currentTimeMillis();
        this.studentName = studentName;
        this.origin = origin;
        this.dest = dest;
        this.pathEdges = pathEdges;
    }

    public String getStudentName() {
        return studentName;
    }


    public List<UniPaths> getPathEdges() {
        return pathEdges;
    }

    /**
     * مسیر کامل را با ظرفیت هر یال نشان می‌دهد:
     * مثال: "A->B(3)->C(1)->D(4)"
     */
    public String getFullPathString() {
        if (pathEdges == null || pathEdges.isEmpty()) {
            return origin + "->" + dest;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(origin); // شروع از مبدا واقعی
        
        // اضافه کردن تمام نقاط میانی
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
     * کمترین ظرفیت باقی‌مانده در طول مسیر را برمی‌گرداند
     */
    public int getRemainingCapacity() {
        return pathEdges.stream()
                .mapToInt(UniPaths::getRemainingCapacity)
                .min()
                .orElse(0);
    }

    @Override
    public int compareTo(Reservation other) {
        return Long.compare(this.bookingTimestamp, other.bookingTimestamp);
    }

    @Override
    public String toString() {
        return studentName + ": " + origin + " → " + dest;
    }
}
