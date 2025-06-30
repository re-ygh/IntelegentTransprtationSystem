import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphPanel extends JPanel {
    private List<UniPaths> edgeList;
    private Map<String, Point> universityPositions;
    private List<Universities> universities;
    private Map<Universities, Point> normalizedPositions = new HashMap<>();


    public GraphPanel(List<UniPaths> edgeList, Map<String, Point> universityPositions, List<Universities> universities) {
        this.edgeList = edgeList;
        this.universityPositions = universityPositions;
        this.universities = universities;
        setBackground(new Color(147, 196, 151)); // بک‌گراند سیاه
    }



    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(4));

        // رسم یال‌ها
        for (UniPaths edge : edgeList) {
            Point p1 = universityPositions.get(edge.getStartLocation());
            Point p2 = universityPositions.get(edge.getEndLocation());

            if (p1 == null || p2 == null) continue;

            if (edge.isInMST()) {
                g2.setColor(new Color(50, 30, 220)); // یال‌های MST
            } else if (edge.isRandom()){
                g2.setColor(Color.gray); // یال‌های معمولی

            } else {
                g2.setColor(Color.BLACK); // یال‌های معمولی
            }

            g2.drawLine(p1.x, p1.y, p2.x, p2.y);
            int x2 = ((p2.x + p1.x) / 2) + 15;
            int y2 = (p2.y + p1.y) / 2 - 10;
            String cost = Integer.toString(edge.getCost());
            g.setFont(new Font("", Font.PLAIN, 15));
            g2.drawString(cost, x2, y2);

        }


        // رسم دانشگاه‌ها
        for (String uni : universityPositions.keySet()) {
            Point p = universityPositions.get(uni);
            for (Universities university : universities) {
                if (university.getUniversityName().equals(uni)) {
                    if (university.getUniversityLocation().equals("شمال")) {
                        g2.setColor(new Color(252, 61, 3));
                    } else if (university.getUniversityLocation().equals("جنوب")) {
                        g2.setColor(new Color(252, 152, 3));
                    } else if (university.getUniversityLocation().equals("شرق")) {
                        g2.setColor(new Color(177, 3, 252));
                    } else if (university.getUniversityLocation().equals("غرب")) {
                        g2.setColor(new Color(252, 3, 136));
                    }else if (university.getUniversityLocation().equals("مرکز")) {
                        g2.setColor(new Color(7, 169, 250));
                    }
                }
            }
            g2.fillOval(p.x - 5, p.y - 5, 35, 35);
            g2.drawString(uni, p.x + 30, p.y);
        }
    }
    // اضافه کردن متد جدید:
    public void setMSTEdges(List<UniPaths> mstEdges) {
        for (UniPaths edge : edgeList) {
            edge.setInMST(false);
        }
        for (UniPaths edge : mstEdges) {
            edge.setInMST(true);
        }
        repaint();
    }

}
