import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Universities {
    private String universityName;
    private String universityLocation;
    private int x, y;
    Universities(String universityName, String universityLocation , int x, int y) {
        this.universityName = universityName;
        this.universityLocation = universityLocation;
        assignPosition();
    }

    private void assignPosition() {
        Random rand = new Random();
        switch (universityLocation) {
            case "شمال" -> {
                x = 300 + rand.nextInt(300);  // بازه x: 300 تا 600
                y = 50 + rand.nextInt(100);   // بازه y: 50 تا 150
            }
            case "جنوب" -> {
                x = 300 + rand.nextInt(300);
                y = 550 + rand.nextInt(100);
            }
            case "شرق" -> {
                x = 600 + rand.nextInt(150);
                y = 250 + rand.nextInt(200);
            }
            case "غرب" -> {
                x = 50 + rand.nextInt(150);
                y = 250 + rand.nextInt(200);
            }
            default -> {
                x = 400;
                y = 350;
            }
        }
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