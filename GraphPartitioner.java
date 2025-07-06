import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphPartitioner {
    /**
     * تقسیم گره‌ها بر اساس universityLocation
     */
    public static Map<String, List<Universities>> partitionNodesByRegion(List<Universities> nodes) {
        Map<String, List<Universities>> regions = new HashMap<>();
        for (Universities u : nodes) {
            regions.computeIfAbsent(u.getUniversityLocation(), k -> new ArrayList<>()).add(u);
        }
        return regions;
    }

    /**
     * تقسیم یال‌ها به دو دسته: درون‌بخشی (intra) و بین‌بخشی (inter)
     */
    public static Map<String, List<UniPaths>> partitionEdgesByRegion(
            Map<String, List<Universities>> regions,
            List<UniPaths> allPaths) {
        // نگاشت هر نود به منطقه
        Map<String, String> nodeRegion = new HashMap<>();
        for (Map.Entry<String, List<Universities>> e : regions.entrySet()) {
            for (Universities u : e.getValue()) {
                nodeRegion.put(u.getUniversityName(), e.getKey());
            }
        }
        List<UniPaths> intra = new ArrayList<>();
        List<UniPaths> inter = new ArrayList<>();
        for (UniPaths p : allPaths) {
            String r1 = nodeRegion.get(p.getStartLocation());
            String r2 = nodeRegion.get(p.getEndLocation());
            if (r1 != null && r1.equals(r2)) intra.add(p);
            else inter.add(p);
        }
        Map<String, List<UniPaths>> result = new HashMap<>();
        result.put("intra", intra);
        result.put("inter", inter);
        return result;
    }
}