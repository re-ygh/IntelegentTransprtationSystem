import java.awt.*;
import java.util.List;
import java.util.Random;
import java.io.Serializable;

/**
 * این کلاس نماینده‌ی هر دانشگاه در گراف است و شامل موقعیت، منطقه جغرافیایی و زمان‌های شروع/پایان می‌باشد.
 */
public class Universities implements Serializable {
    private String universityName;
    private String universityLocation;
    private int startTime, FinishTime;
    int x, y = 0;

    public Universities(String universityName, String universityLocation , int startTime, int FinishTime, int x, int y) {
        this.universityName = universityName;
        this.universityLocation = universityLocation;
        this.startTime = startTime;
        this.FinishTime = FinishTime;
        this.x = x;
        this.y = y;
    }

    /**
     * تولید موقعیت دانشگاه بر اساس منطقه انتخاب‌شده و جلوگیری از هم‌پوشانی با ناحیه دکمه‌های بالای صفحه
     */
    public static Universities generateNewUniversity(String name, String universityLocation, int startTime, int FinishTime, List<Universities> existing, int panelWidth, int panelHeight) {
        Random rand = new Random();
        int minDistance = 80;

        Rectangle zone;
        int centerX = panelWidth / 2;
        int centerY = panelHeight / 2;
        int topOffset = 80; // جلوگیری از رفتن زیر پنل بالا (مثلاً نوار سبز)

        switch (universityLocation) {
            case "شمال":
                zone = new Rectangle(panelWidth / 3, topOffset, panelWidth / 3, panelHeight / 4);
                break;
            case "جنوب":
                zone = new Rectangle(panelWidth / 3, 3 * panelHeight / 4, panelWidth / 3, panelHeight / 4 - 10);
                break;
            case "شرق":
                zone = new Rectangle(3 * panelWidth / 4, panelHeight / 3, panelWidth / 4 - 10, panelHeight / 3);
                break;
            case "غرب":
                zone = new Rectangle(0, panelHeight / 3, panelWidth / 4 - 10, panelHeight / 3);
                break;
            case "مرکز":
                zone = new Rectangle(panelWidth / 3, panelHeight / 3, panelWidth / 3, panelHeight / 3);
                break;
            default:
                zone = new Rectangle(50, topOffset, panelWidth - 100, panelHeight - 100);
        }

        int maxAttempts = 1000;
        int x = 50, y = 50;
        boolean tooClose;

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            x = zone.x + rand.nextInt(Math.max(1, zone.width - 50)) + 25;
            y = zone.y + rand.nextInt(Math.max(1, zone.height - 50)) + 25;

            tooClose = false;
            for (Universities u : existing) {
                int dx = x - u.getX();
                int dy = y - u.getY();
                if (Math.sqrt(dx * dx + dy * dy) < minDistance) {
                    tooClose = true;
                    break;
                }
            }

            if (!tooClose) {
                return new Universities(name, universityLocation, startTime, FinishTime, x , y);
            }
        }

        // اگر جای امن پیدا نشد، موقعیت تصادفی در همان منطقه بدون بررسی فاصله تولید می‌شود
        x = zone.x + rand.nextInt(Math.max(1, zone.width - 50)) + 25;
        y = zone.y + rand.nextInt(Math.max(1, zone.height - 50)) + 25;
        return new Universities(name, universityLocation, startTime, FinishTime, x , y);
    }

    public String getUniversityName() {
        return universityName;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setUniversityName(String universityName) {
        this.universityName = universityName;
    }

    public String getUniversityLocation() {
        return universityLocation;
    }

    public void setUniversityLocation(String universityLocation) {
        this.universityLocation = universityLocation;
    }

    @Override
    public String toString() {
        return universityName + " (" + universityLocation + ")";
    }
}
