import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class Reservation {
    private String studentName;
    private String origin;
    private String dest;
    private List<UniPaths> pathEdges;

    public Reservation(String studentName, String origin, String dest, List<UniPaths> pathEdges) {
        this.studentName = studentName;
        this.origin      = origin;
        this.dest        = dest;
        this.pathEdges   = pathEdges;
    }

    public String getStudentName() { return studentName; }
    public String getOrigin()      { return origin; }
    public String getDest()        { return dest; }
    public List<UniPaths> getPathEdges() { return pathEdges; }

    /**
     * مسیر کامل را با ظرفیت هر یال نشان می‌دهد:
     * مثال: "A->B(3)->C(1)->D(4)"
     */
    public String getFullPathString() {
        if (pathEdges == null || pathEdges.isEmpty()) {
            return origin + "->" + dest;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(pathEdges.get(0).getStartLocation());
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
    public String toString() {
        return studentName + ": " + origin + " → " + dest;
    }
}