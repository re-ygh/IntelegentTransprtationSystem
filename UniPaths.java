public class UniPaths {
    private int startTime;
    private int endTime;
    private int cost;
    private int capacity;
    private String startLocation;
    private String endLocation;
    private boolean isInMST ; // برای رنگ زرد

    UniPaths(int startTime, int endTime, int cost, int capacity, String startLocation, String endLocation) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.cost = cost;
        this.capacity = capacity;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        isInMST = false;
    }

    public int getStartTime() {
        return startTime;
    }

    public boolean isInMST() {
        return isInMST;
    }

    public void setInMST(boolean inMST) {
        isInMST = inMST;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public String getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(String endLocation) {
        this.endLocation = endLocation;
    }

    public String getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(String startLocation) {
        this.startLocation = startLocation;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getCost() {
        return cost;
    }


    public void setCost(int cost) {
        this.cost = cost;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }
}
