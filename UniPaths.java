import java.io.Serializable;

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
}
