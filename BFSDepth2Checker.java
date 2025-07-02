import java.util.*;

public class BFSDepth2Checker {

    /**
     * بررسی اینکه آیا دو دانشگاه با حداکثر دو گام (depth <= 2) به هم متصل هستند
     */
    public static boolean isReachableWithin2Steps(String start, String target, List<UniPaths> paths) {
        Map<String, Set<String>> graph = new HashMap<>();

        // ساخت گراف مجاورتی بر اساس مسیرها
        for (UniPaths path : paths) {
            String u1 = path.getStartLocation();
            String u2 = path.getEndLocation();

            graph.putIfAbsent(u1, new HashSet<>());
            graph.putIfAbsent(u2, new HashSet<>());

            graph.get(u1).add(u2);
            graph.get(u2).add(u1); // چون گراف غیرجهت‌دار است
        }

        Queue<String> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();
        Map<String, Integer> depth = new HashMap<>();

        queue.offer(start);
        visited.add(start);
        depth.put(start, 0);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            int currentDepth = depth.get(current);

            if (current.equals(target)) return true;
            if (currentDepth == 2) continue;

            for (String neighbor : graph.getOrDefault(current, new HashSet<>())) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    depth.put(neighbor, currentDepth + 1);
                    queue.offer(neighbor);
                }
            }
        }

        return false;
    }
}
