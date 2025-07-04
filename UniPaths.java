import java.io.Serializable;
import java.util.*;

/**
 * این کلاس نمایانگر مسیر (یال) بین دو دانشگاه است
 * شامل اطلاعاتی مثل زمان حرکت، زمان پایان، هزینه، ظرفیت و نام دو دانشگاه متصل شده
 */
public class UniPaths implements Serializable {
 private int startTime;
 private int endTime;
 private int cost;
 private int capacity;
 private String startLocation;
 private String endLocation;
 private boolean isRandom;        // مشخص می‌کند آیا یال به‌صورت خودکار تولید شده یا دستی
 private int remainingCapacity;
 private boolean highlighted = false;  // هایلایت (قرمز) برای مسیر نهایی
private List<String> reservations = new ArrayList<>();
 public UniPaths(int startTime, int endTime, int cost, int capacity,
                 String startLocation, String endLocation, boolean isRandom, int remainingCapacity,List<String> reservations) {
  this.startTime = startTime;
  this.endTime = endTime;
  this.cost = cost;
  this.capacity = capacity;
  this.startLocation = startLocation;
  this.endLocation = endLocation;
  this.isRandom = isRandom;
  this.remainingCapacity = remainingCapacity;
  this.reservations = reservations;
 }

 // getter / setterها


 public List<String> getReservations() {
  return reservations;
 }

 public void setReservations(List<String> reservations) {
  this.reservations = reservations;
 }

 public int getStartTime() { return startTime; }
 public void setStartTime(int startTime) { this.startTime = startTime; }

 public int getEndTime() { return endTime; }
 public void setEndTime(int endTime) { this.endTime = endTime; }

 public int getCost() { return cost; }
 public void setCost(int cost) { this.cost = cost; }

 public int getCapacity() { return capacity; }
 public void setCapacity(int capacity) { this.capacity = capacity; }

 public String getStartLocation() { return startLocation; }
 public void setStartLocation(String startLocation) { this.startLocation = startLocation; }

 public String getEndLocation() { return endLocation; }
 public void setEndLocation(String endLocation) { this.endLocation = endLocation; }

 public boolean isRandom() { return isRandom; }
 public void setRandom(boolean random) { isRandom = random; }

 public int getRemainingCapacity() { return remainingCapacity; }
 public void setRemainingCapacity(int remainingCapacity) { this.remainingCapacity = remainingCapacity; }

 public boolean isHighlighted() { return highlighted; }
 public void setHighlighted(boolean highlighted) { this.highlighted = highlighted; }

 @Override
 public String toString() {
  return startLocation + " -> " + endLocation + " | Cost: " + cost +
          " | Time: " + startTime + "–" + endTime;
 }

 /**
  * محاسبه و انتخاب کوتاه‌ترین مسیر از src به dest بر اساس
  * کمترین مجموع (هزینه + زمان) و هایلایت کردن یال‌های مسیر.
  * اگر یالی ظرفیتش صفر یا منفی باشد، آن را حذف و مجدداً الگوریتم را اجرا می‌کند.
  *
  * @param allPaths      لیست همه‌ی یال‌ها
  * @param src           نام دانشگاه مبدأ
  * @param dest          نام دانشگاه مقصد
  * @param reduceCapacity اگر true باشد، پس از انتخاب مسیر، ظرفیت هر یال یک واحد کاهش می‌یابد
  * @return true اگر مسیر یافت شود، false در غیر این صورت
  */
 public static Boolean DijkstraShortestPath(List<UniPaths> allPaths, String src, String dest, boolean reduceCapacity) {
  // ۱. پاکسازی هایلایت‌های قبلی
  for (UniPaths p : allPaths) {
   p.setHighlighted(false);
  }

  // ۲. کپی لیست برای حذف موقت یال‌های بدون ظرفیت
  List<UniPaths> workingPaths = new ArrayList<>(allPaths);

  while (true) {
   // ۳. ساخت لیست مجاورت
   Map<String, List<UniPaths>> adj = new HashMap<>();
   for (UniPaths p : workingPaths) {
    if (p.getCapacity() <= 0) continue;
    adj.computeIfAbsent(p.getStartLocation(), k -> new ArrayList<>()).add(p);
   }

   // ۴. مقداردهی ساختار Dijkstra
   Map<String, Double> dist = new HashMap<>();
   Map<String, UniPaths> prevEdge = new HashMap<>();
   Set<String> visited = new HashSet<>();

   for (String node : adj.keySet()) {
    dist.put(node, Double.POSITIVE_INFINITY);
   }
   dist.put(src, 0.0);

   PriorityQueue<String> pq = new PriorityQueue<>(Comparator.comparing(dist::get));
   pq.add(src);

   // ۵. اجرای Dijkstra با وزن = cost + duration
   while (!pq.isEmpty()) {
    String u = pq.poll();
    if (visited.contains(u)) continue;
    visited.add(u);
    if (u.equals(dest)) break;

    double uDist = dist.getOrDefault(u, Double.POSITIVE_INFINITY);
    for (UniPaths edge : adj.getOrDefault(u, Collections.emptyList())) {
     String v = edge.getEndLocation();
     int duration = edge.getEndTime() - edge.getStartTime();
     if (duration <= 0) continue;  // زمان نامعتبر
     // اینجا به جای نسبت، از جمع هزینه و زمان استفاده می‌کنیم
     double weight = edge.getCost() + duration;
     double alt = uDist + weight;
     if (alt < dist.getOrDefault(v, Double.POSITIVE_INFINITY)) {
      dist.put(v, alt);
      prevEdge.put(v, edge);
      pq.add(v);
     }
    }
   }

   // ۶. بازسازی مسیر
   if (!prevEdge.containsKey(dest)) {
    return false;  // مسیری وجود ندارد
   }
   List<UniPaths> shortestPath = new ArrayList<>();
   String cur = dest;
   while (!cur.equals(src)) {
    UniPaths edge = prevEdge.get(cur);
    shortestPath.add(edge);
    cur = edge.getStartLocation();
   }
   Collections.reverse(shortestPath);

   // ۷. حذف یال‌های فاقد ظرفیت در مسیر
   boolean allHaveCapacity = true;
   for (UniPaths edge : shortestPath) {
    if (edge.remainingCapacity <= 0) {
     workingPaths.remove(edge);
     allHaveCapacity = false;
    }
   }
   if (!allHaveCapacity) {
    continue;  // دوباره تلاش کن
   }

   // ۸. هایلایت و کاهش ظرفیت (در صورت نیاز)
   for (UniPaths edge : shortestPath) {
    edge.setHighlighted(true);
    if (reduceCapacity) {
     edge.setRemainingCapacity(edge.getRemainingCapacity() - 1);
    }
   }

   break;
  }

  return true;
 }



 /**
  * یافتن کوتاه‌ترین مسیر بر اساس کمترین (هزینه + زمان)
  * **بدون** درنظر گرفتن ظرفیت؛
  * برای شناسایی این که آیا ظرفیت دارد یا خیر.
  * @return لیست یال‌های مسیر یا خالی اگر مسیری نباشد
  */
 public static List<UniPaths> findShortestPathEdges(
         List<UniPaths> allPaths, String src, String dest
 ) {
  Map<String, List<UniPaths>> adj = new HashMap<>();
  for (UniPaths p : allPaths) {
   int dur = p.getEndTime() - p.getStartTime();
   if (dur <= 0) continue;
   adj.computeIfAbsent(p.getStartLocation(), k -> new ArrayList<>())
           .add(p);
  }

  Map<String, Double> dist = new HashMap<>();
  Map<String, UniPaths> prevEdge = new HashMap<>();
  Set<String> visited = new HashSet<>();

  for (String node : adj.keySet()) {
   dist.put(node, Double.POSITIVE_INFINITY);
  }
  dist.put(src, 0.0);

  PriorityQueue<String> pq = new PriorityQueue<>(Comparator.comparing(dist::get));
  pq.add(src);

  while (!pq.isEmpty()) {
   String u = pq.poll();
   if (visited.contains(u)) continue;
   visited.add(u);
   if (u.equals(dest)) break;

   double uDist = dist.getOrDefault(u, Double.POSITIVE_INFINITY);
   for (UniPaths edge : adj.getOrDefault(u, Collections.emptyList())) {
    String v = edge.getEndLocation();
    double weight = edge.getCost() + (edge.getEndTime() - edge.getStartTime());
    double alt = uDist + weight;
    if (alt < dist.getOrDefault(v, Double.POSITIVE_INFINITY)) {
     dist.put(v, alt);
     prevEdge.put(v, edge);
     pq.add(v);
    }
   }
  }

  if (!prevEdge.containsKey(dest)) {
   return Collections.emptyList();
  }
  List<UniPaths> path = new ArrayList<>();
  String cur = dest;
  while (!cur.equals(src)) {
   UniPaths e = prevEdge.get(cur);
   path.add(e);
   cur = e.getStartLocation();
  }
  Collections.reverse(path);
  return path;
 }

}
