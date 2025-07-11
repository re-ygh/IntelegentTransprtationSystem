import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.awt.geom.QuadCurve2D;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * این کلاس نمای گرافیکی از گراف دانشگاهی را رسم می‌کند،
 * با قابلیت نمایش نمودار حرارتی (heatmap) برای میزان استفاده از مسیرها،
 * دکمه‌های نمایشی، پیشنهاد مسیر/رزرو هوشمند،
 * نمایش لیست رزرو با صف اولویت‌دار و حرکت دانشجویان.
 */
public class GraphPanel extends JPanel {
    private static final int NODE_RADIUS = 10;
    private static final int HEATMAP_MARGIN = 20;  // فاصله حاشیه برای دیالوگ Heatmap

    private List<UniPaths> paths;
    private Map<String, Point> universityPositions;
    private List<Universities> universities;
    private List<UniPaths> mstEdges = null;
    private List<AnimatedStudent> animations = new ArrayList<>();

    // صف اولویت‌دار رزروها بر اساس زمان رزرو
    private PriorityQueue<Reservation> reservations = new PriorityQueue<>();

    // برای درگ موس
    private String dragStartNode = null;
    private Point  dragCurrentPoint = null;

    // برای heatmap: شمارش استفاده از مسیرها
    private final Map<UniPaths, Integer> usageCount = new HashMap<>();
    private int maxUsage = 1;

    // برای نگهداری دیالوگ و پنل Heatmap
    private JDialog heatmapDialog;
    private JPanel heatPanel;

    private JDialog partitionDialog;
    private JPanel previewPanel;
    private List<UniPaths> previewEdges;
    enum PreviewMode { REGION, GLOBAL }
    private PreviewMode previewMode;

    private boolean showTopPanel = true;

    public GraphPanel(List<UniPaths> paths,
                      Map<String, Point> positions,
                      List<Universities> universities) {
        this(paths, positions, universities, true);
    }

    public GraphPanel(List<UniPaths> paths,
                      Map<String, Point> positions,
                      List<Universities> universities,
                      boolean showTopPanel) {
        this.paths = paths;
        this.universityPositions = positions;
        this.universities = universities;
        this.showTopPanel = showTopPanel;
        if (showTopPanel) setupTopButtons();
        setupMouseListeners();
        new Timer(1000, e -> repaint()).start();
    }

    /**
     * ثبت یک استفاده از مسیر جهت نمایش در Heatmap
     */
    public void recordUsage(UniPaths path) {
        int count = usageCount.getOrDefault(path, 0) + 1;
        usageCount.put(path, count);
        maxUsage = Math.max(maxUsage, count);
    }


    private void setupTopButtons() {
        JButton reachButton = new JButton("بررسی ارتباط دو دانشگاه (حداکثر ۲ گام)");
        reachButton.addActionListener(e -> showReachabilityDialog());

        JButton mstButton = new JButton("نمایش MST");
        mstButton.addActionListener(e -> {
            for (UniPaths p : paths) p.setHighlighted(false);
            this.mstEdges = MSTCalculator.computeMST(universities, paths);
            repaint();
        });

        JButton suggestButton = new JButton("پیشنهاد مسیر و رزرو هوشمند");
        suggestButton.addActionListener(e -> showSuggestionDialog());

        JButton reservationButton = new JButton("لیست رزرو");
        reservationButton.addActionListener(e -> showReservationDialog());

        // دکمه جدید برای نمایش پنل Heatmap
        JButton heatmapButton = new JButton("نمایش Heatmap جداگانه");
        heatmapButton.addActionListener(e -> showHeatmapDialog());

        JButton PartitionedMSTbtn = new JButton("نمایش Partitioned MST");
        PartitionedMSTbtn.addActionListener(e -> computeAndShowPartitionedMST());

        JButton normalGraphButton = new JButton("نمایش گراف عادی");
        normalGraphButton.addActionListener(e -> {
            // پاک کردن همه highlight ها و MST
            for (UniPaths p : paths) p.setHighlighted(false);
            this.mstEdges = null;
            // پاک کردن usage count برای برگشت به رنگ‌های عادی
            usageCount.clear();
            repaint();
            // به‌روزرسانی heatmap اگر باز باشد
            if (heatmapDialog != null && heatmapDialog.isVisible() && heatPanel != null) {
                heatPanel.repaint();
            }
        });

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        topPanel.setBackground(new Color(117, 166, 121));
        topPanel.add(reachButton);
        topPanel.add(mstButton);
        topPanel.add(suggestButton);
        topPanel.add(reservationButton);
        topPanel.add(heatmapButton);
        topPanel.add(PartitionedMSTbtn);
        topPanel.add(normalGraphButton);
        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
    }

    /** یکبار بساز و هر بار فقط نمایش بده، با repaint خودکار */
    private void showHeatmapDialog() {
        if (heatmapDialog == null) {
            // calculate bounds
            int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
            for (Point pt : universityPositions.values()) {
                minX = Math.min(minX, pt.x);
                minY = Math.min(minY, pt.y);
                maxX = Math.max(maxX, pt.x);
                maxY = Math.max(maxY, pt.y);
            }
            int width  = Math.max(1000, maxX - minX + NODE_RADIUS*2 + HEATMAP_MARGIN*2);
            int height = Math.max(800, maxY - minY + NODE_RADIUS*2 + HEATMAP_MARGIN*2);

            heatmapDialog = new JDialog(
                    (Frame) SwingUtilities.getWindowAncestor(this),
                    "Heatmap جداگانه", false
            );
            int finalMinX = minX, finalMinY = minY;

            heatPanel = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.translate(HEATMAP_MARGIN - finalMinX,
                            HEATMAP_MARGIN - finalMinY);

                    // count duplicate edges
                    Map<String, Integer> pairCount = new HashMap<>();
                    Map<String, Integer> edgeIndex = new HashMap<>();
                    for (UniPaths p : paths) {
                        String u = p.getStartLocation(), v = p.getEndLocation();
                        String key = u.compareTo(v) < 0 ? u + "|" + v : v + "|" + u;
                        pairCount.put(key, pairCount.getOrDefault(key,0)+1);
                    }

                    for (UniPaths p : paths) {
                        Point a = universityPositions.get(p.getStartLocation());
                        Point b = universityPositions.get(p.getEndLocation());
                        if (a == null || b == null) continue;

                        int usage = usageCount.getOrDefault(p, 0);
                        // تعیین رنگ پایه یال
                        Color baseColor = p.isRandom() ? Color.LIGHT_GRAY : Color.BLACK;
                        int totalCapacity = p.getCapacity();
                        int remainingCapacity = p.getRemainingCapacity();
                        int usedCapacity = totalCapacity - remainingCapacity;
                        float ratio = totalCapacity > 0 ? Math.min(1f, (float) usedCapacity / totalCapacity) : 0f;
                        Color heatColor;
                        if (ratio == 0f) {
                            heatColor = baseColor;
                        } else if (ratio < 0.25f) {
                            // interpolate baseColor to light red
                            heatColor = interpolateColor(baseColor, new Color(255,200,200), ratio/0.25f);
                        } else if (ratio < 0.5f) {
                            // interpolate light red to medium red
                            heatColor = interpolateColor(new Color(255,200,200), new Color(255,100,100), (ratio-0.25f)/0.25f);
                        } else if (ratio < 0.75f) {
                            // interpolate medium red to dark red
                            heatColor = interpolateColor(new Color(255,100,100), new Color(255,50,50), (ratio-0.5f)/0.25f);
                        } else {
                            // interpolate dark red to brown
                            heatColor = interpolateColor(new Color(255,50,50), new Color(139,0,0), (ratio-0.75f)/0.25f);
                        }
                        g2.setColor(heatColor);

                        String u = p.getStartLocation(), v = p.getEndLocation();
                        String key = u.compareTo(v) < 0 ? u + "|" + v : v + "|" + u;
                        // شمارش یال‌های مشابه برای تصمیم‌گیری منحنی
                        int currentIndex = edgeIndex.getOrDefault(key, 0);
                        edgeIndex.put(key, currentIndex + 1);
                        
                        if (pairCount.getOrDefault(key,0) > 1 && currentIndex > 0) {
                            // استفاده از تابع createCurve برای منحنی
                            QuadCurve2D.Double curve = createCurve(a, b);
                            g2.draw(curve);
                            drawArrowOnCurve(g2, curve);
                        } else {
                            // straight directed edge
                            drawArrow(g2, a.x, a.y, b.x, b.y);
                        }
                    }

                    // draw nodes
                    for (Map.Entry<String, Point> e : universityPositions.entrySet()) {
                        Point p = e.getValue();
                        g2.setColor(Color.BLUE);
                        g2.fillOval(p.x-NODE_RADIUS, p.y-NODE_RADIUS,
                                NODE_RADIUS*2, NODE_RADIUS*2);
                        g2.setColor(Color.BLACK);
                        g2.drawString(e.getKey(),
                                p.x+NODE_RADIUS, p.y-NODE_RADIUS);
                    }
                }
            };
            heatPanel.setPreferredSize(new Dimension(width, height));
            heatmapDialog.add(new JScrollPane(heatPanel));
            heatmapDialog.pack();
            heatmapDialog.setLocationRelativeTo(this);
        }

        // show (or bring to front) the existing dialog
        heatmapDialog.setVisible(true);
        heatmapDialog.toFront();
    }


    private void setupMouseListeners() {
        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragStartNode = findNodeAt(e.getPoint());
                dragCurrentPoint = dragStartNode != null ? e.getPoint() : null;
            }
            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragStartNode != null) {
                    dragCurrentPoint = e.getPoint();
                    repaint();
                }
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if (dragStartNode != null) {
                    String endNode = findNodeAt(e.getPoint());
                    if (endNode != null && !endNode.equals(dragStartNode)) {
                        boolean existsDirected = paths.stream().anyMatch(p ->
                                p.getStartLocation().equals(dragStartNode) &&
                                        p.getEndLocation().equals(endNode) &&
                                        !p.isRandom()
                        );
                        if (existsDirected) {
                            JOptionPane.showMessageDialog(GraphPanel.this,
                                    "بین این دو دانشگاه در همان جهت مسیر وجود دارد.",
                                    "خطا", JOptionPane.WARNING_MESSAGE);
                            clearDragAndRepaint();
                            return;
                        }
                        Integer cost = promptForInteger(
                                "هزینه یال جدید از " + dragStartNode + " به " + endNode + " را وارد کنید:");
                        if (cost == null) { clearDragAndRepaint(); return; }

                        Integer capacity = promptForInteger("ظرفیت یال جدید را وارد کنید:");
                        if (capacity == null) { clearDragAndRepaint(); return; }

                        Integer startTime = promptForInteger("زمان شروع یال جدید (0–24):");
                        if (startTime == null) { clearDragAndRepaint(); return; }
                        if (startTime < 0 || startTime > 24) {
                            JOptionPane.showMessageDialog(GraphPanel.this,
                                    "زمان شروع باید بین ۰ تا ۲۴ باشد.",
                                    "خطا", JOptionPane.ERROR_MESSAGE);
                            clearDragAndRepaint();
                            return;
                        }

                        Integer endTime = promptForInteger("زمان پایان یال جدید (0–24):");
                        if (endTime == null) { clearDragAndRepaint(); return; }
                        if (endTime < 0 || endTime > 24) {
                            JOptionPane.showMessageDialog(GraphPanel.this,
                                    "زمان پایان باید بین ۰ تا ۲۴ باشد.",
                                    "خطا", JOptionPane.ERROR_MESSAGE);
                            clearDragAndRepaint();
                            return;
                        }
                        if (startTime == endTime) {
                            JOptionPane.showMessageDialog(GraphPanel.this,
                                    "زمان شروع و پایان نمیتوانند یکی باشند.",
                                    "خطا", JOptionPane.ERROR_MESSAGE);
                            clearDragAndRepaint();
                            return;
                        }
                        if (endTime < startTime) {
                            JOptionPane.showMessageDialog(GraphPanel.this,
                                    "زمان پایان نمی‌تواند کمتر از زمان شروع باشد.",
                                    "خطا", JOptionPane.ERROR_MESSAGE);
                            clearDragAndRepaint();
                            return;
                        }

                        Iterator<UniPaths> iter = paths.iterator();
                        while (iter.hasNext()) {
                            UniPaths p = iter.next();
                            if (p.isRandom() &&
                                    p.getStartLocation().equals(dragStartNode) &&
                                    p.getEndLocation().equals(endNode)) {
                                iter.remove();
                                break;
                            }
                        }

                        UniPaths newPath = new UniPaths(
                                startTime, endTime, cost, capacity,
                                dragStartNode, endNode,
                                false, capacity, new ArrayList<>()
                        );
                        paths.add(newPath);
                    }
                }
                clearDragAndRepaint();
            }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);
    }

    private void clearDragAndRepaint() {
        dragStartNode = null;
        dragCurrentPoint = null;
        repaint();
    }

    private Integer promptForInteger(String message) {
        while (true) {
            String input = JOptionPane.showInputDialog(GraphPanel.this, message);
            if (input == null) return null;
            try {
                return Integer.parseInt(input.trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(GraphPanel.this,
                        "لطفاً مقدار عددی وارد کنید.",
                        "خطا", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

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
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // رسم یال‌ها با توجه به heatmap
        Map<String, Integer> pairCount = new HashMap<>();
        Map<String, Integer> edgeIndex = new HashMap<>();
        for (UniPaths p : paths) {
            String u = p.getStartLocation(), v = p.getEndLocation();
            // کلید بدون جهت برای تشخیص یال‌های موازی
            String key = u.compareTo(v) < 0 ? u + "|" + v : v + "|" + u;
            pairCount.put(key, pairCount.getOrDefault(key, 0) + 1);
        }
        for (UniPaths p : paths) {
            Point a = universityPositions.get(p.getStartLocation());
            Point b = universityPositions.get(p.getEndLocation());
            if (a == null || b == null) continue;
                g2.setStroke(new BasicStroke(2));
                if (p.isHighlighted()) g2.setColor(Color.RED);
                else if (mstEdges != null && mstEdges.contains(p)) g2.setColor(Color.BLUE);
                else if (p.isRandom()) g2.setColor(Color.LIGHT_GRAY);
                else g2.setColor(Color.BLACK);

            String u = p.getStartLocation(), v = p.getEndLocation();
            String key = u.compareTo(v) < 0 ? u + "|" + v : v + "|" + u;
            // شمارش یال‌های مشابه برای تصمیم‌گیری منحنی
            int currentIndex = edgeIndex.getOrDefault(key, 0);
            edgeIndex.put(key, currentIndex + 1);
            
            // رسم منحنی یا خط مستقیم
            if (pairCount.getOrDefault(key, 0) > 1 && currentIndex > 0) {
                // استفاده از تابع createCurve برای منحنی
                QuadCurve2D.Double curve = createCurve(a, b);
                g2.draw(curve);
                drawArrowOnCurve(g2, curve);
                // نمایش هزینه روی منحنی
                Point mid = controlPoint(a, b);
                g2.setColor(Color.BLACK);
                g2.drawString(String.valueOf(p.getCost()), mid.x, mid.y);
            } else {
                drawArrow(g2, a.x, a.y, b.x, b.y);
                g2.setColor(Color.BLACK);
                g2.drawString(String.valueOf(p.getCost()),
                        (a.x + b.x)/2, (a.y + b.y)/2);
            }
        }
        if (dragStartNode != null && dragCurrentPoint != null) {
            Point a = universityPositions.get(dragStartNode);
            if (a != null) {
                g2.setColor(Color.GRAY);
                g2.drawLine(a.x, a.y, dragCurrentPoint.x, dragCurrentPoint.y);
            }
        }

        // ترسیم نودها
        Map<String, Color> colors = new HashMap<>();
        colors.put("شمال", new Color(252, 61, 3));
        colors.put("جنوب", new Color(252, 152, 3));
        colors.put("شرق", new Color(177, 3, 252));
        colors.put("غرب", new Color(252, 3, 136));
        colors.put("مرکز", new Color(7, 169, 250));
        for (Universities uObj : universities) {
            Point pt = universityPositions.get(uObj.getUniversityName());
            if (pt == null) continue;
            Color c = colors.getOrDefault(uObj.getUniversityLocation(), Color.GREEN);
            g2.setColor(c);
            g2.fillOval(pt.x - NODE_RADIUS, pt.y - NODE_RADIUS,
                    NODE_RADIUS * 2, NODE_RADIUS * 2);
            g2.setColor(Color.BLACK);
            g2.drawString(uObj.getUniversityName(), pt.x + NODE_RADIUS + 2, pt.y);
        }

        // رسم آدمک‌ها (انیمیشن دانشجو)
        g2.setColor(Color.MAGENTA);
        for (AnimatedStudent anim : animations) {
            Point p = anim.getCurrentPosition();
            int r = NODE_RADIUS;
            g2.fillOval(p.x - r, p.y - r, 2 * r, 2 * r);
        }
    }

    /** دیالوگ پیشنهاد مسیر و رزرو هوشمند */
// در همان کلاسی که متدهای UI را نگه می‌دارد (مثلاً GraphPanel یا MainFrame)
    private void showSuggestionDialog() {
        Window window = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(
                window instanceof Frame ? (Frame) window : null,
                "پیشنهاد مسیر و رزرو هوشمند",
                true
        );
        dialog.setLayout(new BorderLayout(10, 10));

        // --- پنل ورودی ---
        JTextField studentField = new JTextField(12);
        String[] uniNames = universities.stream()
                .map(Universities::getUniversityName)
                .toArray(String[]::new);
        JComboBox<String> originCombo = new JComboBox<>(uniNames);
        JComboBox<String> destCombo   = new JComboBox<>(uniNames);
        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        inputPanel.add(new JLabel("نام دانشجو:"));   inputPanel.add(studentField);
        inputPanel.add(new JLabel("دانشگاه مبدا:")); inputPanel.add(originCombo);
        inputPanel.add(new JLabel("دانشگاه مقصد:")); inputPanel.add(destCombo);

        // --- لیست مسیرها با مدل از نوع UniPaths ---
        DefaultListModel<UniPaths> model = new DefaultListModel<>();
        for (UniPaths p : paths) {
            model.addElement(p);
        }
        JList<UniPaths> list = new JList<>(model);
        list.setVisibleRowCount(10);
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list,
                                                          Object value,
                                                          int index,
                                                          boolean isSelected,
                                                          boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                UniPaths p = (UniPaths) value;
                String text = String.format(
                        "%s → %s  | هزینه: %d  | ظرفیت: %d  | زمان: %02d–%02d",
                        p.getStartLocation(),
                        p.getEndLocation(),
                        p.getCost(),
                        p.getRemainingCapacity(),
                        p.getStartTime(),
                        p.getEndTime()
                );
                label.setText(text);
                // قرمز کردن اگر ظرفیت صفر است
                if (p.getRemainingCapacity() <= 0) {
                    label.setForeground(Color.RED);
                } else {
                    label.setForeground(isSelected
                            ? list.getSelectionForeground()
                            : list.getForeground());
                }
                return label;
            }
        });
        JScrollPane listScroll = new JScrollPane(list);
        listScroll.setPreferredSize(new Dimension(500, 200));

        // --- چینش و دکمه‌ها ---
        dialog.add(inputPanel, BorderLayout.NORTH);
        dialog.add(listScroll, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton bestButton   = new JButton("بهینه‌ترین مسیر");
        JButton okButton     = new JButton("تأیید");
        JButton cancelButton = new JButton("انصراف");
        buttonPanel.add(bestButton);
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        // بهینه‌ترین مسیر (بدون کاهش ظرفیت)
        bestButton.addActionListener(e -> {
            String origin = (String) originCombo.getSelectedItem();
            String dest   = (String) destCombo.getSelectedItem();
            if (origin == null || dest == null || origin.equals(dest)) {
                JOptionPane.showMessageDialog(dialog,
                        "دانشگاه مبدا و مقصد باید متفاوت باشند.",
                        "خطا", JOptionPane.ERROR_MESSAGE);
                return;
            }
            for (UniPaths p : paths) p.setHighlighted(false);
            boolean found = UniPaths.DijkstraShortestPath(paths, origin, dest, false);
            if (!found) {
                JOptionPane.showMessageDialog(dialog,
                        "مسیر مناسبی یافت نشد.",
                        "خطا", JOptionPane.WARNING_MESSAGE);
            } else {
                repaint();
                dialog.dispose();
            }
        });

        // تأیید نهایی (کاهش ظرفیت یا ورود به صف رزرو)
        okButton.addActionListener(e -> {
            String student = studentField.getText().trim();
            String origin  = (String) originCombo.getSelectedItem();
            String dest    = (String) destCombo.getSelectedItem();
            if (student.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                        "لطفاً نام دانشجو را وارد کنید.",
                        "خطا", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (origin.equals(dest)) {
                JOptionPane.showMessageDialog(dialog,
                        "دانشگاه مبدا و مقصد نمی‌توانند یکسان باشند.",
                        "خطا", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // ۱) مسیر بر اساس هزینه+زمان بی‌درنظر ظرفیت بیاب (برای چک ظرفیت)
            List<UniPaths> bestPath = UniPaths.findShortestPathEdges(paths, origin, dest);
            if (bestPath.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                        "مسیر مناسبی یافت نشد.",
                        "خطا", JOptionPane.WARNING_MESSAGE);
                return;
            }
            // ۲) اگر هر کدام از یال‌ها ظرفیت صفر دارند
            boolean anyFull = bestPath.stream()
                    .anyMatch(ep -> ep.getRemainingCapacity() <= 0);
            if (anyFull) {
                int choice = JOptionPane.showOptionDialog(dialog,
                        "مسیر انتخاب‌شده ظرفیت ندارد.\n" +
                                "آیا می‌خواهید وارد صف رزرو این مسیر شوید یا مسیر دیگری انتخاب کنید؟",
                        "ظرفیت تکمیل است",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        new String[]{"رزرو", "تغییر مسیر"},
                        "رزرو");
                if (choice == 0) {
                    for (UniPaths e2 : bestPath) {
                        // حتی اگر ظرفیت <=0 باشد، منفی هم بشود
                        e2.setRemainingCapacity(e2.getRemainingCapacity() - 1);
                    }
                    // رزرو
                    reservations.add(new Reservation(student, origin, dest, bestPath));
                    showReservationDialog();
                    if (heatPanel != null && heatPanel.isShowing()) {
                        heatPanel.repaint();
                    }
                    dialog.dispose();
                } else {
                    // تغییر مسیر: حالا مسیر بعدی را با کاهش ظرفیت واقعی پیدا کن
                    for (UniPaths p : paths) p.setHighlighted(false);
                    boolean found = UniPaths.DijkstraShortestPath(paths, origin, dest, true);
                    if (!found) {
                        JOptionPane.showMessageDialog(dialog,
                                "مسیر دیگری یافت نشد.",
                                "خطا", JOptionPane.WARNING_MESSAGE);
                    } else {
                        List<UniPaths> bestPath2 = UniPaths.findShortestPathEdges(paths, origin, dest);

                        reservations.add(new Reservation(student, origin, dest, bestPath2));
                        repaint();
                        if (heatPanel != null && heatPanel.isShowing()) {
                            heatPanel.repaint();
                        }
                        dialog.dispose();
                    }
                }
            } else {
                // ظرفیت دارد: مستقیم رزرو (کاهش ۱ واحد ظرفیت)
                for (UniPaths p : paths) p.setHighlighted(false);
                boolean found = UniPaths.DijkstraShortestPath(paths, origin, dest, true);
                if (!found) {
                    JOptionPane.showMessageDialog(dialog,
                            "خطا در رزرو مسیر.",
                            "خطا", JOptionPane.ERROR_MESSAGE);
                } else {
                    List<UniPaths> bestPath3 = UniPaths.findShortestPathEdges(paths, origin, dest);
                    reservations.add(new Reservation(student, origin, dest, bestPath3));
                    repaint();
                    if (heatPanel != null && heatPanel.isShowing()) {
                        heatPanel.repaint();
                    }
                    dialog.dispose();
                }
            }
            for (UniPaths edge : bestPath) {
               recordUsage(edge);
            }
            repaint();

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

    /** دیالوگ نمایش لیست رزروها با صف اولویت‌دار */
    private void showReservationDialog() {
        JDialog dialog = new JDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                "لیست رزرو", true
        );
        DefaultListModel<Reservation> model = new DefaultListModel<>();

        // کپی و مرتب‌سازی بر اساس زمان رزرو
        List<Reservation> sorted = new ArrayList<>(reservations);
        Collections.sort(sorted);
        for (Reservation r : sorted) {
            model.addElement(r);
        }

        JList<Reservation> list = new JList<>(model);
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list,
                                                          Object value,
                                                          int index,
                                                          boolean isSelected,
                                                          boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                Reservation r = (Reservation) value;
                String fullPath = r.getFullPathString();
                int minCap = r.getRemainingCapacity();
                label.setText(r.getStudentName() +
                        " | مسیر: " + fullPath +
                        " | کمترین ظرفیت: " + minCap);

                // ➞ حالا رنگِ متن را بسته به قابلیت رفتنِ مسیر تنظیم می‌کنیم
                if (minCap > 0) {
                    label.setForeground(isSelected
                            ? list.getSelectionForeground()
                            : Color.BLACK);
                } else {
                    label.setForeground(isSelected
                            ? list.getSelectionForeground()
                            : Color.RED);
                }

                return label;
            }
        });


        JScrollPane scroll = new JScrollPane(list);
        scroll.setPreferredSize(new Dimension(400, 200));

        JPanel btnPanel = new JPanel();
        JButton moveBtn = new JButton("حرکت دانشجو");
        moveBtn.addActionListener(e -> moveNextStudent(model));
        JButton closeBtn = new JButton("بستن");
        closeBtn.addActionListener(e -> {
            // Reset all edge capacities and usageCount
            for (UniPaths p : paths) {
                p.setRemainingCapacity(p.getCapacity());
            }
            usageCount.clear();
            repaint();
            if (heatPanel != null) heatPanel.repaint();
            dialog.dispose();
        });
        btnPanel.add(moveBtn);
        btnPanel.add(closeBtn);

        dialog.setLayout(new BorderLayout(5, 5));
        dialog.add(scroll, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    /** حرکت یک رزرو از صف اولویت‌دار */
    private void moveNextStudent(DefaultListModel<Reservation> model) {
        Reservation next = reservations.poll();
        if (next == null) return;

        List<UniPaths> pathEdges = next.getPathEdges();
        // ثبت استفاده برای heatmap
        for (UniPaths edge : pathEdges) {
            recordUsage(edge);
        }

        List<Point> pathPts = buildPathPoints(pathEdges);
        Collections.reverse(pathPts);

        AnimatedStudent anim = new AnimatedStudent(
                pathPts,
                50,
                () -> {
                    // وقتی رسید به مقصد، ظرفیت‌ها آزاد می‌شن
                    for (UniPaths edge : pathEdges) {
                        GraphUtils.incrementCapacity(edge);
                    }
                    SwingUtilities.invokeLater(() -> {
                        updateReservationModel(model);
                    });
                    // حتماً repaint کنید تا heatmap و گراف اصلی آپدیت بشه
                    repaint();
                }
        );
        animations.add(anim);
        anim.start();
        if (heatPanel != null && heatPanel.isShowing()) {
            heatPanel.repaint();
        }
    }


    /** به‌روز کردن مدل لیست بعد از هر حرکت */
    private void updateReservationModel(DefaultListModel<Reservation> model) {
        model.clear();
        List<Reservation> sorted = new ArrayList<>(reservations);
        Collections.sort(sorted);
        for (Reservation r : sorted) {
            model.addElement(r);
        }
    }


    private class AnimatedStudent {
        List<Point> pathPoints;    // کل نقاط مسیر (شامل نقاط واسط)
        int currentIndex = 0;      // نقطه‌ای که الان قراره رسم بشه
        Timer timer;               // Swing Timer برای حرکت گام‌به‌گام

        AnimatedStudent(List<Point> pts, int delayMs, Runnable onComplete) {
            this.pathPoints = pts;
            timer = new Timer(delayMs, e -> {
                currentIndex++;
                if (currentIndex >= pathPoints.size()) {
                    timer.stop();
                    onComplete.run();            // ظرفیت‌ها رو آزاد کن
                    animations.remove(this);     // حذف انیمیشن
                }
                repaint();
                // به‌روزرسانی heatmap اگر باز باشد
                if (heatmapDialog != null && heatmapDialog.isVisible() && heatPanel != null) {
                    heatPanel.repaint();
                }
            });
        }
        void start() { timer.start(); }
        Point getCurrentPosition() {
            return pathPoints.get(Math.min(currentIndex, pathPoints.size()-1));
        }
    }

    private List<Point> buildPathPoints(List<UniPaths> edges) {
        List<Point> pts = new ArrayList<>();
        // شمارش یال‌های موازی مشابه paintComponent
        Map<String, Integer> pairCount = new HashMap<>();
        Map<String, Integer> edgeIndex = new HashMap<>();
        for (UniPaths p : paths) {
            String u = p.getStartLocation(), v = p.getEndLocation();
            String key = u.compareTo(v) < 0 ? u + "|" + v : v + "|" + u;
            pairCount.put(key, pairCount.getOrDefault(key, 0) + 1);
        }
        for (UniPaths e : edges) {
            Point a = universityPositions.get(e.getStartLocation());
            Point b = universityPositions.get(e.getEndLocation());
            String u = e.getStartLocation(), v = e.getEndLocation();
            String key = u.compareTo(v) < 0 ? u + "|" + v : v + "|" + u;
            int currentIndex = edgeIndex.getOrDefault(key, 0);
            edgeIndex.put(key, currentIndex + 1);
            int steps = 20;
            if (pairCount.getOrDefault(key, 0) > 1 && currentIndex > 0) {
                // منحنی: نقاط روی منحنی Bézier درجه ۲
                QuadCurve2D.Double curve = createCurve(a, b);
                for (int i = 0; i < steps; i++) {
                    double t = i / (double)(steps-1);
                    double x = Math.pow(1-t,2)*curve.getX1() + 2*(1-t)*t*curve.getCtrlX() + Math.pow(t,2)*curve.getX2();
                    double y = Math.pow(1-t,2)*curve.getY1() + 2*(1-t)*t*curve.getCtrlY() + Math.pow(t,2)*curve.getY2();
                    pts.add(new Point((int)x, (int)y));
                }
            } else {
                // خط مستقیم
                for (int i = 0; i < steps; i++) {
                    double t = i / (double)(steps-1);
                    int x = (int)(a.x + t*(b.x - a.x));
                    int y = (int)(a.y + t*(b.y - a.y));
                    pts.add(new Point(x, y));
                }
            }
        }
        return pts;
    }

    /**
     * رسم یال جهت‌دار با سر پیکان در انتها
     */
    private void drawArrow(Graphics2D g2, double x1, double y1, double x2, double y2) {
        // خودِ خط
        g2.draw(new Line2D.Double(x1, y1, x2, y2));

        double phi = Math.toRadians(25);
        double barb = 15;
        double theta = Math.atan2(y2 - y1, x2 - x1);

        for (int i = 0; i < 2; i++) {
            double rho = theta + (i == 0 ? phi : -phi);
            double xx  = x2 - barb * Math.cos(rho);
            double yy  = y2 - barb * Math.sin(rho);
            g2.draw(new Line2D.Double(x2, y2, xx, yy));
        }
    }

    /**
     * متد کمکی برای رسم سر پیکان روی منحنی QuadCurve2D
     */
    private void drawArrowOnCurve(Graphics2D g2, QuadCurve2D q) {
        // t نزدیک 1 برای پیدا کردن جهتِ منحنی
        double t = 0.9;
        double x1 = q.getX1(), y1 = q.getY1();
        double cx = q.getCtrlX(), cy = q.getCtrlY();
        double x2 = q.getX2(), y2 = q.getY2();

        // نقطه روی منحنی
        double xt = Math.pow(1 - t, 2) * x1 + 2 * (1 - t) * t * cx + t * t * x2;
        double yt = Math.pow(1 - t, 2) * y1 + 2 * (1 - t) * t * cy + t * t * y2;
        // مشتق برای زاویه
        double dx = 2 * (1 - t) * (cx - x1) + 2 * t * (x2 - cx);
        double dy = 2 * (1 - t) * (cy - y1) + 2 * t * (y2 - cy);
        double theta = Math.atan2(dy, dx);

        double phi = Math.toRadians(25);
        double barb = 15;
        for (int i = 0; i < 2; i++) {
            double rho = theta + (i == 0 ? phi : -phi);
            double xx = x2 - barb * Math.cos(rho);
            double yy = y2 - barb * Math.sin(rho);
            g2.draw(new Line2D.Double(x2, y2, xx, yy));
        }
    }

    private void computeAndShowPartitionedMST() {
        if (partitionDialog == null) {
            partitionDialog = new JDialog(
                    (Frame) SwingUtilities.getWindowAncestor(this),
                    "نمایش MST مقیاس‌پذیر", false
            );
            partitionDialog.setLayout(new BorderLayout());

            // 1) پنل بالایی
            JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
            String[] regs = GraphPartitioner.partitionNodesByRegion(universities)
                    .keySet().toArray(new String[0]);
            JComboBox<String> combo = new JComboBox<>(regs);
            combo.setSelectedIndex(-1);
            JButton btnRegion = new JButton("نمایش MST منطقه‌ای");
            JButton btnGlobal = new JButton("نمایش MST کل");
            top.add(new JLabel("انتخاب منطقه:"));
            top.add(combo);
            top.add(btnRegion);
            top.add(btnGlobal);
            partitionDialog.add(top, BorderLayout.NORTH);

            // 2) پنل پیش‌نمایش گراف
            previewPanel = new JPanel() {

                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
                    // شمارش یال‌های موازی (کلید بدون جهت)
                    Map<String, Integer> dup = new HashMap<>();
                    Map<String, Integer> edgeIndex = new HashMap<>();
                    for (UniPaths p : paths) {
                        String aName = p.getStartLocation(), bName = p.getEndLocation();
                        String key = aName.compareTo(bName) < 0 ? aName + "|" + bName : bName + "|" + aName;
                        dup.put(key, dup.getOrDefault(key, 0) + 1);
                    }

                    // نگاشت نام دانشگاه → نام منطقه
                    Map<String,String> nodeRegion = new HashMap<>();
                    Map<String, List<Universities>> regions = GraphPartitioner.partitionNodesByRegion(universities);
                    for (Map.Entry<String, List<Universities>> e : regions.entrySet()) {
                        String region = e.getKey();
                        for (Universities u : e.getValue()) {
                            nodeRegion.put(u.getUniversityName(), region);
                        }
                    }

                    // رسم همه یال‌ها
                    for (UniPaths p : paths) {
                        Point a = universityPositions.get(p.getStartLocation());
                        Point b = universityPositions.get(p.getEndLocation());
                        if (a == null || b == null) continue;
                        
                        // رنگ پیش‌فرض: مشکی
                        g2.setColor(Color.BLACK);
                        g2.setStroke(new BasicStroke(1));
                        
                        // اگر یال در previewEdges باشد، رنگش را تغییر بده
                        if (previewEdges != null && previewEdges.contains(p)) {
                            if (previewMode == PreviewMode.REGION) {
                                g2.setColor(Color.RED);
                                g2.setStroke(new BasicStroke(3));
                            } else { // GLOBAL
                                String region = nodeRegion.get(p.getStartLocation());
                                // تخصیص رنگ بر اساس منطقه
                                Color c;
                                switch(region) {
                                    case "شمال": c = Color.CYAN; break;
                                    case "جنوب": c = Color.MAGENTA; break;
                                    case "شرق": c = Color.ORANGE; break;
                                    case "غرب": c = Color.PINK; break;
                                    case "مرکز": c = Color.YELLOW; break;
                                    default: c = Color.LIGHT_GRAY; break;
                                }
                                g2.setColor(c);
                                g2.setStroke(new BasicStroke(3));
                            }
                        }
                        
                        // رسم یال (مستقیم یا منحنی)
                        String aName = p.getStartLocation(), bName = p.getEndLocation();
                        String key = aName.compareTo(bName) < 0 ? aName + "|" + bName : bName + "|" + aName;
                        int currentIndex = edgeIndex.getOrDefault(key, 0);
                        edgeIndex.put(key, currentIndex + 1);
                        
                        if (dup.getOrDefault(key, 0) > 1 && currentIndex > 0) {
                            QuadCurve2D.Double curve = createCurve(a, b);
                            g2.draw(curve);
                            drawArrowOnCurve(g2, curve);
                            Point mid = controlPoint(a, b);
                            g2.drawString(p.getCost() + "(" + p.getRemainingCapacity() + ")", mid.x, mid.y);
                        } else {
                            drawArrow(g2, a.x, a.y, b.x, b.y);
                            int mx = (a.x + b.x) / 2, my = (a.y + b.y) / 2;
                            g2.drawString(p.getCost() + "(" + p.getRemainingCapacity() + ")", mx, my);
                        }
                    }
                    
                    // رسم نودها
                    for (Universities u : universities) {
                        Point pt = universityPositions.get(u.getUniversityName());
                        if (pt == null) continue;
                        
                        // رنگ نود بر اساس منطقه
                        Color nodeColor;
                        switch (u.getUniversityLocation()) {
                            case "شمال": nodeColor = Color.CYAN; break;
                            case "جنوب": nodeColor = Color.MAGENTA; break;
                            case "شرق": nodeColor = Color.ORANGE; break;
                            case "غرب": nodeColor = Color.PINK; break;
                            case "مرکز": nodeColor = Color.YELLOW; break;
                            default: nodeColor = Color.GRAY; break;
                        }
                        g2.setColor(nodeColor);
                        g2.fillOval(pt.x - 8, pt.y - 8, 16, 16);
                        g2.setColor(Color.BLACK);
                        g2.drawString(u.getUniversityName(), pt.x + 10, pt.y);
                    }
                }
            };

            // 3) تعریف اکشن دکمه‌ها
            btnRegion.addActionListener(e -> {
                String sel = (String) combo.getSelectedItem();
                if (sel == null) {
                    JOptionPane.showMessageDialog(partitionDialog,
                            "لطفاً یک منطقه انتخاب کنید.", "خطا", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                Map<String, List<Universities>> regs2 = GraphPartitioner.partitionNodesByRegion(universities);
                List<Universities> regionUniversities = regs2.get(sel);
                Set<String> names = regionUniversities.stream()
                        .map(Universities::getUniversityName).collect(Collectors.toSet());
                List<UniPaths> regionEdges = paths.stream()
                        .filter(p -> names.contains(p.getStartLocation()) && names.contains(p.getEndLocation()))
                        .collect(Collectors.toList());
                previewEdges = MSTCalculator.computeMST(regionUniversities, regionEdges);
                previewMode = PreviewMode.REGION;
                previewPanel.repaint();
            });

            btnGlobal.addActionListener(e -> {
                Map<String, List<Universities>> regions = GraphPartitioner.partitionNodesByRegion(universities);
                Map<String, List<UniPaths>> parts = GraphPartitioner.partitionEdgesByRegion(regions, paths);
                List<UniPaths> mstAll = new ArrayList<>();
                // 1. MST هر ناحیه
                for (String region : regions.keySet()) {
                    List<Universities> regionNodes = regions.get(region);
                    Set<String> names = regionNodes.stream().map(Universities::getUniversityName).collect(Collectors.toSet());
                    List<UniPaths> regionEdges = parts.get("intra").stream()
                        .filter(p -> names.contains(p.getStartLocation()) && names.contains(p.getEndLocation()))
                        .collect(Collectors.toList());
                    mstAll.addAll(MSTCalculator.computeMST(regionNodes, regionEdges));
                }
                // 2. MST بین ناحیه‌ای (بین مناطق)
                mstAll.addAll(GraphPartitioner.computeInterRegionMST(regions, parts.get("inter")));
                previewEdges = mstAll;
                previewMode = PreviewMode.GLOBAL;
                previewPanel.repaint();
            });

            /////////

            previewPanel.setPreferredSize(new Dimension(getWidth(), getHeight() - 80));
            partitionDialog.add(new JScrollPane(previewPanel), BorderLayout.CENTER);

            partitionDialog.pack();
            partitionDialog.setLocationRelativeTo(this);
        }
        partitionDialog.setVisible(true);
    }

    /**
     * ایجاد یک منحنی QuadCurve2D بین دو نقطه (برای رسم یال‌های موازی)
     */
    private QuadCurve2D.Double createCurve(Point a, Point b) {
        double x1 = a.x, y1 = a.y, x2 = b.x, y2 = b.y;
        double mx = (x1 + x2) / 2.0, my = (y1 + y2) / 2.0;
        double dx = x2 - x1, dy = y2 - y1;
        double len = Math.hypot(dx, dy);
        if (len == 0) len = 1;
        double nx = -dy / len, ny = dx / len;
        double offset = 60;  // میزان انحنای منحنی (افزایش یافته برای وضوح بیشتر)
        return new QuadCurve2D.Double(
                x1, y1,
                mx + nx*offset, my + ny*offset,
                x2, y2
        );
    }

    /**
     * محاسبه نقطه میانی منحنی برای رسم برچسب هزینه
     */
    private Point controlPoint(Point a, Point b) {
        QuadCurve2D.Double curve = createCurve(a, b);
        // t=0.5 برای وسط منحنی
        double t = 0.5;
        double x = Math.pow(1 - t, 2) * curve.getX1()
                + 2 * (1 - t) * t * curve.getCtrlX()
                + t * t * curve.getX2();
        double y = Math.pow(1 - t, 2) * curve.getY1()
                + 2 * (1 - t) * t * curve.getCtrlY()
                + t * t * curve.getY2();
        return new Point((int)x, (int)y);
    }

    // --- utility method for color interpolation ---
    private static Color interpolateColor(Color c1, Color c2, float t) {
        t = Math.max(0, Math.min(1, t));
        int r = (int)(c1.getRed()   + t * (c2.getRed()   - c1.getRed()));
        int g = (int)(c1.getGreen() + t * (c2.getGreen() - c1.getGreen()));
        int b = (int)(c1.getBlue()  + t * (c2.getBlue()  - c1.getBlue()));
        return new Color(r, g, b);
    }

}