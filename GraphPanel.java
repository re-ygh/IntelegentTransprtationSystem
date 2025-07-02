// GraphPanel.java
import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * این کلاس نمای گرافیکی از گراف دانشگاهی را رسم می‌کند و دکمه‌های
 * نمایش MST، بررسی ارتباط دو دانشگاه و پیشنهاد مسیر را در بالا نمایش می‌دهد.
 */
public class GraphPanel extends JPanel {

    private List<UniPaths> paths;                       // لیست یال‌های گراف
    private Map<String, Point> universityPositions;     // مختصات دانشگاه‌ها
    private List<Universities> universities;            // لیست نودها
    private List<UniPaths> mstEdges = null;             // یال‌های MST پس از محاسبه
    private boolean mstButtonClicked = false;
    public GraphPanel(List<UniPaths> paths,
                      Map<String, Point> positions,
                      List<Universities> universities) {
        this.paths = paths;
        this.universityPositions = positions;
        this.universities = universities;
        setupTopButtons();
    }

    /** ساخت دکمه‌های عملیاتی در بالای گراف */
    private void setupTopButtons() {
        JButton reachButton = new JButton("بررسی ارتباط دو دانشگاه (حداکثر ۲ گام)");
        reachButton.addActionListener(e -> showReachabilityDialog());

        JButton mstButton = new JButton("نمایش MST");
        mstButton.addActionListener(e -> {
            this.mstEdges = MSTCalculator.computeMST(universities, paths);
            mstButtonClicked = true;
            repaint();
        });

        JButton suggestButton = new JButton("پیشنهاد مسیر و رزرو هوشمند");
        suggestButton.addActionListener(e -> showSuggestionDialog());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        topPanel.setBackground(new Color(117, 166, 121));
        topPanel.add(reachButton);
        topPanel.add(mstButton);
        topPanel.add(suggestButton);

        this.setLayout(new BorderLayout());
        this.add(topPanel, BorderLayout.NORTH);
    }

    /** دیالوگ پیشنهاد مسیر و رزرو هوشمند */
    private void showSuggestionDialog() {
        JTextField studentField = new JTextField(12);
        String[] uniNames = universities.stream()
                .map(Universities::getUniversityName)
                .toArray(String[]::new);
        JComboBox<String> originCombo = new JComboBox<>(uniNames);
        JComboBox<String> destCombo   = new JComboBox<>(uniNames);

        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        panel.add(new JLabel("نام دانشجو:"));
        panel.add(studentField);
        panel.add(new JLabel("دانشگاه مبدا:"));
        panel.add(originCombo);
        panel.add(new JLabel("دانشگاه مقصد:"));
        panel.add(destCombo);

        int result = JOptionPane.showConfirmDialog(
                this, panel,
                "پیشنهاد مسیر و رزرو هوشمند",
                JOptionPane.OK_CANCEL_OPTION
        );
        if (result == JOptionPane.OK_OPTION) {
            String student = studentField.getText().trim();
            String origin  = (String) originCombo.getSelectedItem();
            String dest    = (String) destCombo.getSelectedItem();

            if (student.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this, "لطفاً نام دانشجو را وارد کنید.",
                        "خطا", JOptionPane.ERROR_MESSAGE
                );
            } else if (origin.equals(dest)) {
                JOptionPane.showMessageDialog(
                        this, "دانشگاه مبدا و مقصد نمی‌توانند یکسان باشند.",
                        "خطا", JOptionPane.ERROR_MESSAGE
                );
            } else {
                if (UniPaths.DijkstraShortestPath(paths, origin, dest)) {
                // TODO: اضافه کردن منطق محاسبه مسیر و رزرو هوشمند
                JOptionPane.showMessageDialog(
                        this,
                        "درخواست ثبت شد:\n" +
                                "دانشجو: " + student + "\n" +
                                "مبدا: "    + origin  + "\n" +
                                "مقصد: "    + dest,
                        "ثبت پیشنهاد مسیر", JOptionPane.INFORMATION_MESSAGE
                );
                repaint();
                }else {
                    JOptionPane.showMessageDialog(
                            this, "بین این دو دانشگاه مسیر خالی ای وجود ندارد.",
                            "خطا", JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }
    }

    /** پنجره بررسی اتصال دو دانشگاه در حداکثر دو گام */
    private void showReachabilityDialog() {
        String[] names = universities.stream()
                .map(Universities::getUniversityName)
                .toArray(String[]::new);
        JComboBox<String> startCombo  = new JComboBox<>(names);
        JComboBox<String> targetCombo = new JComboBox<>(names);

        JPanel panel = new JPanel(new GridLayout(2,2,5,5));
        panel.add(new JLabel("دانشگاه مبدأ:"));
        panel.add(startCombo);
        panel.add(new JLabel("دانشگاه مقصد:"));
        panel.add(targetCombo);

        int res = JOptionPane.showConfirmDialog(
                this, panel,
                "بررسی اتصال با حداکثر ۲ گام",
                JOptionPane.OK_CANCEL_OPTION
        );
        if (res == JOptionPane.OK_OPTION) {
            boolean ok = BFSDepth2Checker
                    .isReachableWithin2Steps(
                            (String)startCombo.getSelectedItem(),
                            (String)targetCombo.getSelectedItem(),
                            paths
                    );
            JOptionPane.showMessageDialog(
                    this,
                    ok ? "اتصال برقرار است (۲ گام یا کمتر)"
                            : "اتصال وجود ندارد یا بیش از ۲ گام نیاز است",
                    "نتیجه",
                    ok ? JOptionPane.INFORMATION_MESSAGE
                            : JOptionPane.WARNING_MESSAGE
            );
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(2));

        // رسم یال‌ها
        for (UniPaths p : paths) {
            Point a = universityPositions.get(p.getStartLocation());
            Point b = universityPositions.get(p.getEndLocation());
            if (a == null || b == null) continue;

            if (mstButtonClicked){
                if (mstEdges != null && mstEdges.contains(p)) {
                    g2.setColor(Color.BLUE);
                } else if (p.isRandom()) {
                    g2.setColor(Color.LIGHT_GRAY);
                } else {
                    g2.setColor(Color.BLACK);
                }
            } else {
                if (p.isRandom()) {
                    g2.setColor(Color.LIGHT_GRAY);
                } else {
                    if(p.isHighlighted()){
                        g2.setColor(Color.RED);
                    }else {
                        g2.setColor(Color.BLACK);
                    }
                }
            }
            g2.drawLine(a.x, a.y, b.x, b.y);
            g2.setColor(Color.BLACK);
            g2.drawString(
                    String.valueOf(p.getCost()),
                    (a.x + b.x) / 2,
                    (a.y + b.y) / 2
            );
        }

        // رسم نودها
        Map<String, Color> colors = Map.of(
                "شمال", new Color(252, 61, 3),
                "جنوب", new Color(252, 152, 3),
                "شرق",  new Color(177, 3, 252),
                "غرب",  new Color(252, 3, 136),
                "مرکز", new Color(7, 169, 250)
        );
        for (Universities u : universities) {
            Point pt = universityPositions.get(u.getUniversityName());
            if (pt == null) continue;
            Color c = colors.getOrDefault(u.getUniversityLocation(), Color.GREEN);
            g2.setColor(c);
            g2.fillOval(pt.x - 10, pt.y - 10, 20, 20);
            g2.drawString(u.getUniversityName(), pt.x + 12, pt.y);
        }
    }
}
