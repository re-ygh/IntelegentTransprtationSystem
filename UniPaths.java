import java.io.Serializable;
import java.util.*;

/**
 * این کلاس نمایانگر مسیر (یال) بین دو دانشگاه است
 * شامل اطلاعاتی مثل زمان حرکت، زمان پایان، هزینه، ظرفیت و نام دو دانشگاه متصل شده
 */
public class UniPaths implements Serializable {
    private int endTime;
    private int cost;
    private int capacity;
    private String startLocation;
    private String endLocation;
    private boolean isFull;
    private boolean isRandom; // مشخص می‌کند آیا یال به‌صورت خودکار تولید شده یا دستی

    public UniPaths(int startTime, int endTime, int cost, int capacity,
                    String startLocation, String endLocation, boolean isRandom, boolean isFull) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.cost = cost;
        this.capacity = capacity;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.isRandom = isRandom;
        this.isFull = isFull;
    }

    public boolean isFull() {
        return isFull;
    }

    public void setFull(boolean full) {
        isFull = full;
    }
    // متدهای دسترسی به فیلدها (getter و setter)

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(String startLocation) {
        this.startLocation = startLocation;
    }

    public String getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(String endLocation) {
        this.endLocation = endLocation;
    }

    public boolean isRandom() {
        return isRandom;
    }

    public void setRandom(boolean random) {
        isRandom = random;
    }

    @Override
    public String toString() {
        return startLocation + " -> " + endLocation + " | Cost: " + cost;
    }
  * کمترین نسبت (cost / (endTime - startTime))
  * و هایلایت (قرمز) کردن یال‌های مسیر نهایی.
  * اگر یالی ظرفیتش صفر یا منفی باشد، آن را موقّتاً حذف کرده
  * و دوباره الگوریتم را اجرا می‌کند تا مسیری با ظرفیت کافی بیابد.
  *
  * @param allPaths لیست تمام یال‌های موجود در گراف
  * @param src      نام دانشگاه مبدا
  * @param dest     نام دانشگاه مقصد
  */
 public static Boolean DijkstraShortestPath(List<UniPaths> allPaths, String src, String dest) {
  // ۱. پاکسازی هایلایت‌های قبلی
  for (UniPaths p : allPaths) {
   p.setHighlighted(false);
  }

  // ۲. لیست کاری برای حذف یال‌های پر (capacity <= 0)
  List<UniPaths> workingPaths = new ArrayList<>(allPaths);

  while (true) {
   // ۳. ساخت نقشه مجاورت (Adjacency List)
   Map<String, List<UniPaths>> adj = new HashMap<>();
   for (UniPaths p : workingPaths) {
    if (p.getCapacity() <= 0) continue;               // نادیده گرفتن یال‌های پر
    adj.computeIfAbsent(p.getStartLocation(), k -> new ArrayList<>()).add(p);
   }

   // ۴. آماده‌سازی ساختارهای Dijkstra
   Map<String, Double> dist = new HashMap<>();           // فاصله‌ی تجمعی
   Map<String, UniPaths> prevEdge = new HashMap<>();     // یال قبلی برای بازسازی مسیر
   Set<String> visited = new HashSet<>();

   // مقداردهی اولیه فاصله‌ها
   for (String node : adj.keySet()) {
    dist.put(node, Double.POSITIVE_INFINITY);
   }
   dist.put(src, 0.0);

   // صف اولویت بر اساس dist
   PriorityQueue<String> pq = new PriorityQueue<>(Comparator.comparing(dist::get));
   pq.add(src);

   // ۵. اجرای الگوریتم Dijkstra با وزن هر یال = cost / duration
   while (!pq.isEmpty()) {
    String u = pq.poll();
    if (visited.contains(u)) continue;
    visited.add(u);
    if (u.equals(dest)) break;  // اگر به مقصد رسیدیم، متوقف می‌شویم

    double uDist = dist.getOrDefault(u, Double.POSITIVE_INFINITY);
    List<UniPaths> edges = adj.getOrDefault(u, Collections.emptyList());
    for (UniPaths edge : edges) {
     String v = edge.getEndLocation();
     int duration = edge.getEndTime() - edge.getStartTime();
     if (duration <= 0) continue;  // نادیده گرفتن یال‌های بی‌معنی
     double weight = edge.getCost() / (double) duration;
     double alt = uDist + weight;
     if (alt < dist.getOrDefault(v, Double.POSITIVE_INFINITY)) {
      dist.put(v, alt);
      prevEdge.put(v, edge);
      pq.add(v);
     }
    }
   }

   // ۶. بازسازی مسیر از dest به src
   if (!prevEdge.containsKey(dest)) {
    // مسیر شناخته‌شده‌ای وجود ندارد
    return false;
   }
   List<UniPaths> shortestPath = new ArrayList<>();
   String cur = dest;
   while (!cur.equals(src)) {
    UniPaths edge = prevEdge.get(cur);
    shortestPath.add(edge);
    cur = edge.getStartLocation();
   }
   Collections.reverse(shortestPath);

   // ۷. بررسی ظرفیت یال‌ها در مسیر
   boolean allHaveCapacity = true;
   for (UniPaths edge : shortestPath) {
    if (edge.remainingCapacity <= 0) {
     // حذف یال فاقد ظرفیت از گراف کاری و تکرار الگوریتم
     workingPaths.remove(edge);
     allHaveCapacity = false;
    }
   }
   if (!allHaveCapacity) {
    continue;
   }

   // ۸. هایلایت (قرمز) کردن یال‌های مسیر نهایی
   for (UniPaths edge : shortestPath) {
    edge.setHighlighted(true);
    edge.setRemainingCapacity(edge.getRemainingCapacity() - 1);
   }
   break;  // مسیر نهایی پیدا و هایلایت شد → خروج
  }
  return true;
 }
}
