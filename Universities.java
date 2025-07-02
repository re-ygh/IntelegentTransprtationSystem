import java.awt.*;
import java.util.List;
import java.util.Random;

/**
 * کلاس دانشگاه‌ها - نگهداری اطلاعات پایه‌ای هر دانشگاه به همراه موقعیت مکانی آن
 * شامل: نام، منطقه مکانی، زمان‌های شروع و پایان، مختصات گرافیکی (x, y)
 */
public class Universities {
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
     * تولید دانشگاه جدید با موقعیت تقریبی بر اساس منطقه انتخاب‌شده توسط کاربر
     * منطق جلوگیری از هم‌پوشانی دانشگاه‌ها و ایجاد پراکندگی نسبی رعایت شده است
     */
    public static Universities generateNewUniversity(String name, String universityLocation, int startTime, int FinishTime, List<Universities> existing, int panelWidth, int panelHeight) {
        Random rand = new Random();
        int minDistance = 80; // حداقل فاصله قابل قبول بین دو دانشگاه

        Rectangle zone;
        int centerX = panelWidth / 2;
        int centerY = panelHeight / 2;

        // انتخاب ناحیه مستطیلی برای منطقه انتخاب‌شده کاربر
        switch (universityLocation) {
            case "شمال":
                zone = new Rectangle(panelWidth / 3, 0, panelWidth / 3, panelHeight / 4);
                break;
            case "جنوب":
                zone = new Rectangle(panelWidth / 3, 3 * panelHeight / 4, panelWidth / 3, panelHeight / 4);
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
                zone = new Rectangle(50, 50, panelWidth - 100, panelHeight - 100); // ناحیه کلی پیش‌فرض
        }

        int maxAttempts = 1000;
        int x = 50, y = 50;
        boolean tooClose;

        // تلاش برای یافتن موقعیتی بدون تداخل با دانشگاه‌های موجود
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

        // در صورت نبود فضای کافی، تولید مختصات رندوم در همان منطقه بدون بررسی فاصله
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
