import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.awt.geom.QuadCurve2D;
import java.util.*;
import java.util.List;

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

    public GraphPanel(List<UniPaths> paths,
                      Map<String, Point> positions,
                      List<Universities> universities) {
        this.paths = paths;
        this.universityPositions = positions;
        this.universities = universities;
        setupTopButtons();
        setupMouseListeners();
        // Timer برای بروزرسانی نمایش Heatmap
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

    /**
     * آپدیت heatmap وقتی ظرفیت تغییر می‌کند
     */
    public void updateHeatmap() {
        if (heatPanel != null && heatPanel.isShowing()) {
            heatPanel.repaint();
        }
        repaint();
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

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        topPanel.setBackground(new Color(117, 166, 121));
        topPanel.add(reachButton);
        topPanel.add(mstButton);
        topPanel.add(suggestButton);
        topPanel.add(reservationButton);
        topPanel.add(heatmapButton);
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
            int width  = maxX - minX + NODE_RADIUS*2 + HEATMAP_MARGIN*2;
            int height = maxY - minY + NODE_RADIUS*2 + HEATMAP_MARGIN*2;

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
                    for (UniPaths p : paths) {
                        String a = p.getStartLocation(), b = p.getEndLocation();
                        String key = a.compareTo(b) < 0 ? a+"|"+b : b+"|"+a;
                        pairCount.put(key, pairCount.getOrDefault(key,0)+1);
                    }

                    for (UniPaths p : paths) {
                        Point a = universityPositions.get(p.getStartLocation());
                        Point b = universityPositions.get(p.getEndLocation());
                        if (a == null || b == null) continue;

                        int usage = usageCount.getOrDefault(p, 0);
                        // constant stroke
                        g2.setStroke(new BasicStroke(2));

                        // بررسی ظرفیت برای یال‌های منحنی
                        String u = p.getStartLocation(), v = p.getEndLocation();
                        String key = u.compareTo(v) < 0 ? u+"|"+v : v+"|"+u;
                        boolean isDuplicate = pairCount.getOrDefault(key,0) > 1;

                        if (usage > 0) {
                            // فرض می‌کنیم در UniPaths متدی دارید به نام getCapacity()
                            int capacity = p.getCapacity();
                            // جلوگیری از تقسیم بر صفر
                            float ratio = capacity > 0
                                    ? Math.min(1f, (float) usage / capacity)
                                    : 0f;
                            // colour gradient: white → light-red → dark-red → brown
                            Color heatColor;
                            if (ratio < 0.5f) {
                                // white → red
                                float t = ratio / 0.5f;
                                heatColor = new Color(
                                        1f,
                                        1f - t,
                                        1f - t
                                );
                            } else {
                                // red → brown
                                float t = (ratio - 0.5f) / 0.5f;
                                // interpolates red (1,0,0) → brown (0.6,0.2,0)
                                heatColor = new Color(
                                        1f - 0.4f*t,
                                        0f + 0.2f*t,
                                        0f
                                );
                            }
                            g2.setColor(heatColor);
                        } else {
                            // no usage: fallback to highlight / MST / random / black
                            if (p.isHighlighted())      g2.setColor(Color.RED);
                            else if (mstEdges != null &&
                                    mstEdges.contains(p)) g2.setColor(Color.BLUE);
                            else if (p.isRandom())      g2.setColor(Color.LIGHT_GRAY);
                            else                        g2.setColor(Color.BLACK);
                        }

                        if (isDuplicate && u.compareTo(v) > 0) {
                            // curved duplicate edge
                            double x1=a.x, y1=a.y, x2=b.x, y2=b.y;
                            double mx=(x1+x2)/2, my=(y1+y2)/2;
                            double dx=x2-x1, dy=y2-y1;
                            double len=Math.hypot(dx,dy); if(len==0) len=1;
                            double nx=-dy/len, ny=dx/len;
                            double offset=40;
                            QuadCurve2D curve = new QuadCurve2D.Double(
                                    x2,y2, mx+nx*offset, my+ny*offset, x1,y1
                            );
                            g2.draw(curve);
                            drawArrowOnCurve(g2, curve);
                        } else {
                            // straight directed edge
                            drawArrow(g2, b.x, b.y, a.x, a.y);
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
                                false, capacity, null
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
        for (UniPaths p : paths) {
            String u = p.getStartLocation(), v = p.getEndLocation();
            String key = u.compareTo(v) < 0 ? u + "|" + v : v + "|" + u;
            pairCount.put(key, pairCount.getOrDefault(key, 0) + 1);
        }
        for (UniPaths p : paths) {
            Point a = universityPositions.get(p.getStartLocation());
            Point b = universityPositions.get(p.getEndLocation());
            if (a == null || b == null) continue;

            g2.setStroke(new BasicStroke(2));

            // بررسی ظرفیت برای یال‌های منحنی
            String u = p.getStartLocation(), v = p.getEndLocation();
            String key = u.compareTo(v) < 0 ? u + "|" + v : v + "|" + u;
            boolean isDuplicate = pairCount.getOrDefault(key, 0) > 1;

            // انتخاب رنگ بر اساس وضعیت یال
            if (p.isHighlighted()) g2.setColor(Color.RED);
            else if (mstEdges != null && mstEdges.contains(p)) g2.setColor(Color.BLUE);
            else if (p.isRandom()) g2.setColor(Color.LIGHT_GRAY);
            else g2.setColor(Color.BLACK);

            // رسم منحنی یا خط مستقیم
            if (isDuplicate && u.compareTo(v) > 0) {
                double x1 = a.x, y1 = a.y, x2 = b.x, y2 = b.y;
                double mx = (x1 + x2) / 2, my = (y1 + y2) / 2;
                double dx = x2 - x1, dy = y2 - y1;
                double len = Math.hypot(dx, dy);
                if (len == 0) len = 1;
                double nx = -dy / len, ny = dx / len;
                double offset = 40;
                double cx = mx + nx * offset, cy = my + ny * offset;
                QuadCurve2D curve = new QuadCurve2D.Double(x2, y2, cx, cy, x1, y1);
                g2.draw(curve);
                drawArrowOnCurve(g2, curve);

                // نمایش هزینه و ظرفیت روی منحنی
                g2.setColor(Color.BLACK);
                String costText = p.getCost() + "(" + p.getRemainingCapacity() + ")";
                g2.drawString(costText, (int) cx, (int) cy);
            } else {
                drawArrow(g2, b.x, b.y, a.x, a.y);

                // نمایش هزینه و ظرفیت روی خط مستقیم
                g2.setColor(Color.BLACK);
                String costText = p.getCost() + "(" + p.getRemainingCapacity() + ")";
                g2.drawString(costText, (a.x + b.x)/2, (a.y + b.y)/2);
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
        Map<String, Color> colors = Map.of(
                "شمال", new Color(252, 61, 3),
                "جنوب", new Color(252, 152, 3),
                "شرق", new Color(177, 3, 252),
                "غرب", new Color(252, 3, 136),
                "مرکز", new Color(7, 169, 250)
        );
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
        JDialog dialog = new JDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
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
            if (!UniPaths.DijkstraShortestPath(paths, dest, origin, false)) {
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
            if (!UniPaths.DijkstraShortestPath(paths, dest, origin, false)) {
                JOptionPane.showMessageDialog(dialog,
                        "مسیر مناسبی یافت نشد.",
                        "خطا", JOptionPane.WARNING_MESSAGE);
                return;
            }
            // ۲) اگر هر کدام از یال‌ها ظرفیت صفر دارند
            boolean anyFull = UniPaths.DijkstraPaths.stream()
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
                    for (UniPaths e2 : UniPaths.DijkstraPaths) {
                        // حتی اگر ظرفیت <=0 باشد، منفی هم بشود
                        e2.setRemainingCapacity(e2.getRemainingCapacity() - 1);
                    }
                    // رزرو
                    reservations.add(new Reservation(student, origin, dest, UniPaths.DijkstraPaths));
                    showReservationDialog();
                    updateHeatmap();
                    dialog.dispose();
                } else {
                    // تغییر مسیر: حالا مسیر بعدی را با کاهش ظرفیت واقعی پیدا کن
                    for (UniPaths p : paths) p.setHighlighted(false);
                    boolean found = UniPaths.DijkstraShortestPath(paths, dest, origin, true);
                    if (!found) {
                        JOptionPane.showMessageDialog(dialog,
                                "مسیر دیگری یافت نشد.",
                                "خطا", JOptionPane.WARNING_MESSAGE);
                    } else {
                        reservations.add(new Reservation(student, origin, dest, UniPaths.DijkstraPaths));
                        updateHeatmap();
                        dialog.dispose();
                    }
                }
            } else {
                // ظرفیت دارد: مستقیم رزرو (کاهش ۱ واحد ظرفیت)
                for (UniPaths p : paths) p.setHighlighted(false);
                boolean found = UniPaths.DijkstraShortestPath(paths, dest, origin, true);
                if (!found) {
                    JOptionPane.showMessageDialog(dialog,
                            "خطا در رزرو مسیر.",
                            "خطا", JOptionPane.ERROR_MESSAGE);
                } else {
                    reservations.add(new Reservation(student, origin, dest, UniPaths.DijkstraPaths));
                }

                updateHeatmap();
                dialog.dispose();
            }
            for (UniPaths edge : UniPaths.DijkstraPaths) {
               recordUsage(edge);
            }
            updateHeatmap();

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
        closeBtn.addActionListener(e -> dialog.dispose());
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
                    updateHeatmap();
                }
        );
        animations.add(anim);
        anim.start();

        // فوری heatmap را آپدیت کن
        repaint();
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
                // آپدیت heatmap در هر فریم انیمیشن
                if (heatPanel != null && heatPanel.isShowing()) {
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
        for (UniPaths e : edges) {
            Point a = universityPositions.get(e.getStartLocation());
            Point b = universityPositions.get(e.getEndLocation());
            int steps = 20;
            for (int i = 0; i < steps; i++) {
                double t = i / (double)(steps-1);
                int x = (int)(a.x + t*(b.x - a.x));
                int y = (int)(a.y + t*(b.y - a.y));
                pts.add(new Point(x, y));
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

}