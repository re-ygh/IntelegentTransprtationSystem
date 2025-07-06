import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

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

    /**
     * پیدا کردن کم‌ترین یال‌های بین‌بخشی که همه‌ی ناحیه‌ها را به هم وصل کند.
     * @param regions خروجی partitionNodesByRegion (نام منطقه → لیست دانشگاه‌ها)
     * @param interEdges لیست یال‌های بین‌بخشی
     * @return لیستی از UniPaths (یال‌های بین‌بخشی) که یک MST روی خوشه‌ها می‌سازد
     */
    public static List<UniPaths> computeInterRegionMST(
            Map<String, List<Universities>> regions,
            List<UniPaths> interEdges) {


        // 1) نگاشت نام دانشگاه → منطقه
                Map<String,String> nodeRegion = new HashMap<>();
                for (Map.Entry<String, List<Universities>> entry : regions.entrySet()) {
                        String region = entry.getKey();
                        for (Universities u : entry.getValue()) {
                                nodeRegion.put(u.getUniversityName(), region);
                            }
                    }

        // 2) لیست نام مناطق و ایندکس هر کدام برای UnionFind
        List<String> regionList = new ArrayList<>(regions.keySet());
        Map<String,Integer> idx = new HashMap<>();
        for (int i = 0; i < regionList.size(); i++) {
            idx.put(regionList.get(i), i);
        }

        // 3) مرتب‌سازی یال‌های بین‌بخشی بر اساس وزن = cost + duration
        List<UniPaths> sorted = new ArrayList<>(interEdges);
        sorted.sort(Comparator.comparingDouble(p ->
                p.getCost() + (p.getEndTime() - p.getStartTime())
        ));

        // 4) ساختار Union-Find
        int n = regionList.size();
        int[] parent = new int[n];
        for (int i = 0; i < n; i++) parent[i] = i;
        Function<Integer, Integer> find = new Function<Integer, Integer>() {
            public Integer apply(Integer x) {
                if (parent[x] == x) {
                    return x;
                } else {
                    parent[x] = this.apply(parent[x]);
                    return parent[x];
                }
            }
        };
        BiConsumer<Integer,Integer> unite = (a, b) -> {
            int ra = find.apply(a), rb = find.apply(b);
            if (ra != rb) parent[rb] = ra;
        };

        // 5) Kruskal: انتخاب یال تا وصل شدن همه‌ خوشه‌ها
        List<UniPaths> connectors = new ArrayList<>();
        for (UniPaths p : sorted) {
            String r1 = nodeRegion.get(p.getStartLocation());
            String r2 = nodeRegion.get(p.getEndLocation());
            // بررسی null بودن مناطق
            if (r1 == null || r2 == null) continue;
            Integer i1 = idx.get(r1);
            Integer i2 = idx.get(r2);
            // بررسی null بودن ایندکس‌ها
            if (i1 == null || i2 == null) continue;
            if (!find.apply(i1).equals(find.apply(i2))) {
                unite.accept(i1, i2);
                connectors.add(p);
                // اگر به n-1 رسید، تمام شد
                if (connectors.size() == n - 1) break;
            }
        }
        return connectors;
    }
}