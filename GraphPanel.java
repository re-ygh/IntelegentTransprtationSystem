import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.QuadCurve2D;
import java.util.*;
import java.util.List;

/**
 * این کلاس نمای گرافیکی از گراف دانشگاهی را رسم می‌کند،
 * دکمه‌های نمایش MST، بررسی ارتباط و پیشنهاد مسیر/رزرو هوشمند را دارد،
 * و امکان ایجاد یال با درگ موس را فراهم می‌کند.
 */
public class GraphPanel extends JPanel {
    private static final int NODE_RADIUS = 10;

    private List<UniPaths> paths;                       // لیست یال‌ها
    private Map<String, Point> universityPositions;     // مختصات نودها
    private List<Universities> universities;            // اطلاعات نودها
    private List<UniPaths> mstEdges = null;             // یال‌های MST

    // برای رسم پیش‌نمایش درگ
    private String dragStartNode = null;
    private Point  dragCurrentPoint = null;

    public GraphPanel(List<UniPaths> paths,
                      Map<String, Point> positions,
                      List<Universities> universities) {
        this.paths = paths;
        this.universityPositions = positions;
        this.universities = universities;
        setupTopButtons();
        setupMouseListeners();
    }

    /** ساخت و قرار دادن دکمه‌های بالای پنل */
    private void setupTopButtons() {
        JButton reachButton = new JButton("بررسی ارتباط دو دانشگاه (حداکثر ۲ گام)");
        reachButton.addActionListener(e -> showReachabilityDialog());

        JButton mstButton = new JButton("نمایش MST");
        mstButton.addActionListener(e -> {
            // پاک‌سازی هایلایت قبلی
            for (UniPaths p : paths) p.setHighlighted(false);
            // محاسبه و نمایش MST
            this.mstEdges = MSTCalculator.computeMST(universities, paths);
            repaint();
        });

        JButton suggestButton = new JButton("پیشنهاد مسیر و رزرو هوشمند");
        suggestButton.addActionListener(e -> showSuggestionDialog());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        topPanel.setBackground(new Color(117, 166, 121));
        topPanel.add(reachButton);
        topPanel.add(mstButton);
        topPanel.add(suggestButton);

        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
    }

    /** تنظیم ماوس برای درگ و کشیدن یال */
    private void setupMouseListeners() {
        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // اگر روی نود کلیک شد، شروع درگ را ذخیره کن
                dragStartNode = findNodeAt(e.getPoint());
                dragCurrentPoint = dragStartNode != null ? e.getPoint() : null;
            }
            @Override
            public void mouseDragged(MouseEvent e) {
                // به‌روزرسانی نقطه‌ی جاری و repaint برای پیش‌نمایش
                if (dragStartNode != null) {
                    dragCurrentPoint = e.getPoint();
                    repaint();
                }
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if (dragStartNode != null) {
                    String endNode = findNodeAt(e.getPoint());
                    // فقط اگر روی نود دوم رها شد و متفاوت بود
                    if (endNode != null && !endNode.equals(dragStartNode)) {
                        // چک تکراری بودن یال در همان جهت
                        boolean existsDirected = paths.stream().anyMatch(p ->
                                p.getStartLocation().equals(dragStartNode) &&
                                        p.getEndLocation().equals(endNode)
                        );
                        if (existsDirected) {
                            JOptionPane.showMessageDialog(GraphPanel.this,
                                    "بین این دو دانشگاه یک مسیر در همان جهت وجود دارد.",
                                    "خطا", JOptionPane.WARNING_MESSAGE);
                            clearDragAndRepaint(); return;
                        }

                        // پرس‌و‌جوی مرحله‌ای از کاربر با اعتبارسنجی آنی
                        Integer cost      = promptForInteger("هزینه یال جدید از " + dragStartNode + " به " + endNode + " را وارد کنید:");
                        if (cost == null) { clearDragAndRepaint(); return; }

                        Integer capacity  = promptForInteger("ظرفیت یال جدید را وارد کنید:");
                        if (capacity == null) { clearDragAndRepaint(); return; }

                        Integer startTime = promptForInteger("زمان شروع یال جدید (0–24):");
                        if (startTime == null) { clearDragAndRepaint(); return; }
                        if (startTime < 0 || startTime > 24) {
                            JOptionPane.showMessageDialog(GraphPanel.this,
                                    "زمان شروع باید بین ۰ تا ۲۴ باشد.",
                                    "خطا", JOptionPane.ERROR_MESSAGE);
                            clearDragAndRepaint(); return;
                        }

                        Integer endTime   = promptForInteger("زمان پایان یال جدید (0–24):");
                        if (endTime == null) { clearDragAndRepaint(); return; }
                        if (endTime < 0 || endTime > 24) {
                            JOptionPane.showMessageDialog(GraphPanel.this,
                                    "زمان پایان باید بین ۰ تا ۲۴ باشد.",
                                    "خطا", JOptionPane.ERROR_MESSAGE);
                            clearDragAndRepaint(); return;
                        }
                        if (endTime < startTime) {
                            JOptionPane.showMessageDialog(GraphPanel.this,
                                    "زمان پایان نمی‌تواند کمتر از زمان شروع باشد.",
                                    "خطا", JOptionPane.ERROR_MESSAGE);
                            clearDragAndRepaint(); return;
                        }

                        // در نهایت اضافه کردن یال جدید
                        UniPaths newPath = new UniPaths(
                                startTime, endTime, cost, capacity,
                                dragStartNode, endNode,
                                false, capacity
                        );
                        paths.add(newPath);
                    }
                    // حذف اولین مسیر پیشنهادی (random) بین این دو نود (هر جهت)
                    Iterator<UniPaths> iter = paths.iterator();
                    while (iter.hasNext()) {
                        UniPaths p = iter.next();
                        if (p.isRandom() &&
                                ((p.getStartLocation().equals(dragStartNode) && p.getEndLocation().equals(endNode)) ||
                                        (p.getStartLocation().equals(endNode) && p.getEndLocation().equals(dragStartNode)))) {
                            iter.remove();
                            break;
                        }
                    }
                }
                clearDragAndRepaint();
            }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);
    }

    /** پاک‌سازی حالت درگ و بازنقاشی */
    private void clearDragAndRepaint() {
        dragStartNode = null;
        dragCurrentPoint = null;
        repaint();
    }

    /** نمایش دیالوگ تکرارشونده برای دریافت عدد (برگشت null=انصراف) */
    private Integer promptForInteger(String message) {
        while (true) {
            String input = JOptionPane.showInputDialog(GraphPanel.this, message);
            if (input == null) return null; // کاربر انصراف زد
            try {
                return Integer.parseInt(input.trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(GraphPanel.this,
                        "لطفاً مقدار عددی وارد کنید.",
                        "خطا", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /** پیدا کردن نودی که روی آن کلیک شده است */
    private String findNodeAt(Point p) {
        for (Map.Entry<String, Point> entry : universityPositions.entrySet()) {
            if (entry.getValue().distance(p) <= NODE_RADIUS) {
                return entry.getKey();
            }
        }
        return null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(2));

        // شمارش مسیرها بین هر جفت نود (بدون جهت)
        Map<String, Integer> pairCount = new HashMap<>();
        for (UniPaths p : paths) {
            String u = p.getStartLocation(), v = p.getEndLocation();
            String key = u.compareTo(v) < 0 ? u + "|" + v : v + "|" + u;
            pairCount.put(key, pairCount.getOrDefault(key, 0) + 1);
        }

        // رسم مسیرها
        for (UniPaths p : paths) {
            Point a = universityPositions.get(p.getStartLocation());
            Point b = universityPositions.get(p.getEndLocation());
            if (a == null || b == null) continue;

            String u = p.getStartLocation(), v = p.getEndLocation();
            String key = u.compareTo(v) < 0 ? u + "|" + v : v + "|" + u;
            int count = pairCount.getOrDefault(key, 0);

            // رنگ یال
            if (p.isHighlighted()) {
                g2.setColor(Color.RED);
            } else if (mstEdges != null && mstEdges.contains(p)) {
                g2.setColor(Color.BLUE);
            } else if (p.isRandom()) {
                g2.setColor(Color.LIGHT_GRAY);
            } else {
                g2.setColor(Color.BLACK);
            }

            // اگر دو مسیر مخالف جهت وجود دارد، مسیر با u>v منحنی باشد
            if (count > 1 && u.compareTo(v) > 0) {
                double x1 = a.x, y1 = a.y, x2 = b.x, y2 = b.y;
                double mx = (x1 + x2) / 2, my = (y1 + y2) / 2;
                double dx = x2 - x1, dy = y2 - y1;
                double len = Math.hypot(dx, dy);
                if (len == 0) len = 1;
                double nx = -dy / len, ny = dx / len;
                double offset = 40;
                double cx = mx + nx * offset, cy = my + ny * offset;

                QuadCurve2D curve = new QuadCurve2D.Double(x1, y1, cx, cy, x2, y2);
                g2.draw(curve);
                g2.setColor(Color.BLACK);
                g2.drawString(String.valueOf(p.getCost()), (int) cx, (int) cy);
            } else {
                g2.drawLine(a.x, a.y, b.x, b.y);
                g2.setColor(Color.BLACK);
                g2.drawString(
                        String.valueOf(p.getCost()),
                        (a.x + b.x) / 2, (a.y + b.y) / 2
                );
            }
        }

        // رسم پیش‌نمایش یال در حال درگ
        if (dragStartNode != null && dragCurrentPoint != null) {
            Point a = universityPositions.get(dragStartNode);
            if (a != null) {
                g2.setColor(Color.GRAY);
                g2.drawLine(a.x, a.y, dragCurrentPoint.x, dragCurrentPoint.y);
            }
        }

        // رسم نودها
        Map<String, Color> colors = Map.of(
                "شمال", new Color(252, 61, 3),
                "جنوب", new Color(252, 152, 3),
                "شرق",  new Color(177, 3, 252),
                "غرب",  new Color(252, 3, 136),
                "مرکز", new Color(7, 169, 250)
        );
        for (Universities uObj : universities) {
            Point pt = universityPositions.get(uObj.getUniversityName());
            if (pt == null) continue;
            Color c = colors.getOrDefault(uObj.getUniversityLocation(), Color.GREEN);
            g2.setColor(c);
            g2.fillOval(pt.x - NODE_RADIUS, pt.y - NODE_RADIUS, NODE_RADIUS*2, NODE_RADIUS*2);
            g2.setColor(Color.BLACK);
            g2.drawString(uObj.getUniversityName(), pt.x + NODE_RADIUS + 2, pt.y);
        }
    }

    /** دیالوگ پیشنهاد مسیر و رزرو هوشمند */
    private void showSuggestionDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "پیشنهاد مسیر و رزرو هوشمند", true);
        dialog.setLayout(new BorderLayout(10, 10));

        JTextField studentField = new JTextField(12);
        String[] uniNames = universities.stream()
                .map(Universities::getUniversityName)
                .toArray(String[]::new);
        JComboBox<String> originCombo = new JComboBox<>(uniNames);
        JComboBox<String> destCombo   = new JComboBox<>(uniNames);

        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        inputPanel.add(new JLabel("نام دانشجو:"));    inputPanel.add(studentField);
        inputPanel.add(new JLabel("دانشگاه مبدا:"));  inputPanel.add(originCombo);
        inputPanel.add(new JLabel("دانشگاه مقصد:"));  inputPanel.add(destCombo);

        DefaultListModel<String> model = new DefaultListModel<>();
        for (UniPaths p : paths) {
            model.addElement(String.format(
                    "%s → %s  | هزینه: %d  | ظرفیت باقیمانده: %d",
                    p.getStartLocation(), p.getEndLocation(),
                    p.getCost(), p.getRemainingCapacity()
            ));
        }
        JList<String> list = new JList<>(model);
        list.setVisibleRowCount(10);
        JScrollPane listScroll = new JScrollPane(list);
        listScroll.setPreferredSize(new Dimension(500,200));

        dialog.add(inputPanel, BorderLayout.NORTH);
        dialog.add(listScroll, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton bestButton = new JButton("بهینه‌ترین مسیر بین مبدا و مقصد");
        JButton okButton   = new JButton("تأیید");
        JButton cancelButton = new JButton("انصراف");
        buttonPanel.add(bestButton);
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        bestButton.addActionListener(e -> {
            String origin = (String) originCombo.getSelectedItem();
            String dest   = (String) destCombo.getSelectedItem();
            if (origin == null || dest == null || origin.equals(dest)) {
                JOptionPane.showMessageDialog(dialog,
                        "دانشگاه مبدا و مقصد باید انتخاب و متفاوت باشند.",
                        "خطا", JOptionPane.ERROR_MESSAGE);
            } else {
                for (UniPaths p : paths) p.setHighlighted(false);
                boolean found = UniPaths.DijkstraShortestPath(paths, dest, origin, false);
                if (!found) {
                    JOptionPane.showMessageDialog(dialog,
                            "مسیر مناسبی یافت نشد.",
                            "خطا", JOptionPane.WARNING_MESSAGE);
                } else {
                    repaint();
                    dialog.dispose();
                }
            }
        });

        okButton.addActionListener(e -> {
            String student = studentField.getText().trim();
            String origin  = (String) originCombo.getSelectedItem();
            String dest    = (String) destCombo.getSelectedItem();
            if (student.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                        "لطفاً نام دانشجو را وارد کنید.",
                        "خطا", JOptionPane.ERROR_MESSAGE);
            } else if (origin.equals(dest)) {
                JOptionPane.showMessageDialog(dialog,
                        "دانشگاه مبدا و مقصد نمی‌توانند یکسان باشند.",
                        "خطا", JOptionPane.ERROR_MESSAGE);
            } else {
                for (UniPaths p : paths) p.setHighlighted(false);
                boolean found = UniPaths.DijkstraShortestPath(paths, dest, origin, true);
                if (!found) {
                    JOptionPane.showMessageDialog(dialog,
                            "مسیر مناسبی یافت نشد.",
                            "خطا", JOptionPane.WARNING_MESSAGE);
                } else {
                    repaint();
                    dialog.dispose();
                }
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    /** دیالوگ بررسی ارتباط با حداکثر دو گام */
    private void showReachabilityDialog() {
        String[] names = universities.stream()
                .map(Universities::getUniversityName)
                .toArray(String[]::new);
        JComboBox<String> startCombo  = new JComboBox<>(names);
        JComboBox<String> targetCombo = new JComboBox<>(names);

        JPanel panel = new JPanel(new GridLayout(2,2,5,5));
        panel.add(new JLabel("دانشگاه مبدأ:")); panel.add(startCombo);
        panel.add(new JLabel("دانشگاه مقصد:")); panel.add(targetCombo);

        int res = JOptionPane.showConfirmDialog(
                this, panel,
                "بررسی اتصال با حداکثر ۲ گام",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (res == JOptionPane.OK_OPTION) {
            boolean ok = BFSDepth2Checker.isReachableWithin2Steps(
                    (String) startCombo.getSelectedItem(),
                    (String) targetCombo.getSelectedItem(),
                    paths
            );
            JOptionPane.showMessageDialog(
                    this,
                    ok ? "اتصال برقرار است (۲ گام یا کمتر)"
                            : "اتصال وجود ندارد یا بیش از ۲ گام نیاز است",
                    "نتیجه",
                    ok ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE
            );
        }
    }
}
