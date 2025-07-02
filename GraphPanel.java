import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * این کلاس نمای گرافیکی از گراف دانشگاهی را رسم می‌کند و همچنین دکمه‌های عملیاتی
 * مانند نمایش MST و بررسی اتصال دو دانشگاه را در بالای صفحه نشان می‌دهد.
 */
public class GraphPanel extends JPanel {

    private List<UniPaths> paths;                       // لیست یال‌های گراف
    private Map<String, Point> universityPositions;     // مختصات دانشگاه‌ها برای رسم
    private List<Universities> universities;            // لیست دانشگاه‌ها (نودها)
    private List<UniPaths> mstEdges = null;             // لیست MST برای نمایش جداگانه

    public GraphPanel(List<UniPaths> paths, Map<String, Point> universityPositions, List<Universities> universities) {
        this.paths = paths;
        this.universityPositions = universityPositions;
        this.universities = universities;
        setBackground(new Color(147, 196, 151)); // بک‌گراند سیاه
        setupTopButtons(); // ساخت دکمه‌ها (نمایش MST و بررسی اتصال)
    }

    /**
     * ایجاد دکمه‌های عملیاتی در بالای صفحه: بررسی دو گام و نمایش MST
     */
    private void setupTopButtons() {
        JButton reachButton = new JButton("بررسی ارتباط دو دانشگاه (حداکثر ۲ گام)");
        reachButton.addActionListener(e -> showReachabilityDialog());

        JButton mstButton = new JButton("نمایش MST");
        mstButton.addActionListener(e -> {
            this.mstEdges = MSTCalculator.computeMST(universities, paths);
            repaint();
        });

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        topPanel.setBackground(new Color(117, 166, 121)); // رنگ پس‌زمینه هماهنگ با تم برنامه
        topPanel.add(reachButton);
        topPanel.add(mstButton);

        this.setLayout(new BorderLayout());
        this.add(topPanel, BorderLayout.NORTH);
    }

    /**
     * نمایش پنجره‌ای برای انتخاب دو دانشگاه و بررسی اتصال آن‌ها در حداکثر دو گام
     */
    private void showReachabilityDialog() {
        String[] uniNames = universities.stream().map(Universities::getUniversityName).toArray(String[]::new);
        JComboBox<String> startCombo = new JComboBox<>(uniNames);
        JComboBox<String> targetCombo = new JComboBox<>(uniNames);

        JPanel panel = new JPanel(new GridLayout(2, 2));
        panel.add(new JLabel("دانشگاه مبدأ:"));
        panel.add(startCombo);
        panel.add(new JLabel("دانشگاه مقصد:"));
        panel.add(targetCombo);

        int result = JOptionPane.showConfirmDialog(null, panel, "بررسی اتصال با حداکثر ۲ گام", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String start = (String) startCombo.getSelectedItem();
            String target = (String) targetCombo.getSelectedItem();
            boolean reachable = BFSDepth2Checker.isReachableWithin2Steps(start, target, paths);
            if (reachable) {
                JOptionPane.showMessageDialog(this, "اتصال برقرار است (در ۲ گام یا کمتر)", "نتیجه", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "اتصالی وجود ندارد یا بیش از ۲ گام نیاز است", "نتیجه", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    /**
     * ست کردن لیست یال‌های MST برای رسم با رنگ متفاوت
     */
    public void setMSTEdges(List<UniPaths> mstEdges) {
        this.mstEdges = mstEdges;
    }

    /**
     * رسم گراف دانشگاه‌ها و مسیرها روی صفحه
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(2));

        // رسم مسیرها (یال‌ها)
        for (UniPaths path : paths) {
            Point p1 = universityPositions.get(path.getStartLocation());
            Point p2 = universityPositions.get(path.getEndLocation());
            if (p1 == null || p2 == null) continue;

            // اگر این مسیر جزو MST است → رنگ آبی؛ در غیر این صورت خاکستری
            if (mstEdges != null && mstEdges.contains(path)) {
                g2.setColor(Color.BLUE);
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

        // رسم نودها (دانشگاه‌ها)
        for (Universities u : universities) {
            int x = u.getX();
            int y = u.getY();
            g2.setColor(Color.GREEN);
            g2.fillOval(x - 10, y - 10, 20, 20); // دایره سبز برای هر دانشگاه

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
            g2.drawString(u.getUniversityName(), x + 12, y);
        }
    }
}
