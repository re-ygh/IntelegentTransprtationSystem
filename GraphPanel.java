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

            if (mstEdges != null && mstEdges.contains(path)) {
                g2.setColor(Color.BLUE);
            } else {
                if (path.isRandom()){
                g2.setColor(Color.LIGHT_GRAY);
                } else {
                    g2.setColor(Color.black);
                }
            }
            g2.drawLine(p1.x, p1.y, p2.x, p2.y);

            int labelX = (p1.x + p2.x) / 2;
            int labelY = (p1.y + p2.y) / 2;
            g2.setColor(Color.BLACK);
            g2.drawString(String.valueOf(path.getCost()), labelX, labelY);
        }

        // رنگ مشخص برای هر منطقه
        Map<String, Color> locationColors = Map.of(
                "شمال", new Color(252, 61, 3),
                "جنوب", new Color(252, 152, 3),
                "شرق", new Color(177, 3, 252),
                "غرب", new Color(252, 3, 136),
                "مرکز", new Color(7, 169, 250)
        );

        for (Universities u : universities) {
            int x = u.getX();
            int y = u.getY();
            Color nodeColor = locationColors.getOrDefault(u.getUniversityLocation(), Color.GREEN);

            g2.setColor(nodeColor);
            g2.fillOval(x - 10, y - 10, 20, 20);

            g2.setColor(nodeColor);
            g2.drawString(u.getUniversityName(), x + 12, y);
        }
    }
}