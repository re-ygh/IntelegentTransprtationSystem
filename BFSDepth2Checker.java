import java.util.*;

/**
 * این کلاس شامل متدی برای بررسی ارتباط بین دو دانشگاه در گراف با حداکثر دو گام است.
 * از الگوریتم BFS با محدودیت عمق استفاده می‌کند.
 */
//
public class BFSDepth2Checker {

    /**
     * بررسی اینکه آیا بین دو دانشگاه حداکثر با دو گام (depth <= 2) ارتباط وجود دارد یا نه
     * @param start نام دانشگاه مبدأ
     * @param target نام دانشگاه مقصد
     * @param paths لیست کل مسیرهای گراف
     * @return true اگر ارتباط در حداکثر دو گام وجود داشته باشد، در غیر این صورت false
     */
    public static boolean isReachableWithin2Steps(String start, String target, List<UniPaths> paths) {
        Map<String, Set<String>> graph = new HashMap<>(); // گراف بدون جهت

        // ساخت گراف مجاورتی از روی لیست مسیرها
        for (UniPaths path : paths) {
            String u1 = path.getStartLocation();
            String u2 = path.getEndLocation();

            graph.putIfAbsent(u1, new HashSet<>());
            graph.putIfAbsent(u2, new HashSet<>());

            graph.get(u1).add(u2);
            graph.get(u2).add(u1); // چون گراف غیرجهت‌دار است
        }

        Queue<String> queue = new LinkedList<>(); // صف BFS
        Set<String> visited = new HashSet<>();    // گره‌های بازدیدشده
        Map<String, Integer> depth = new HashMap<>(); // نگهداری عمق هر گره از مبدأ

        queue.offer(start);
        visited.add(start);
        depth.put(start, 0);

        // اجرای BFS با محدودیت عمق ۲
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