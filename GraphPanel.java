import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.QuadCurve2D;
import java.util.*;
import java.util.List;

/**
 * این کلاس نمای گرافیکی از گراف دانشگاهی را رسم می‌کند،
 * دکمه‌های نمایشی، پیشنهاد مسیر/رزرو هوشمند،
 * نمایش لیست رزرو با صف اولویت‌دار و حرکت دانشجویان را دارد.
 */
public class GraphPanel extends JPanel {
    private static final int NODE_RADIUS = 10;

    private List<UniPaths> paths;
    private Map<String, Point> universityPositions;
    private List<Universities> universities;
    private List<UniPaths> mstEdges = null;

    // صف اولویت‌دار رزروها بر اساس زمان رزرو
    private PriorityQueue<Reservation> reservations = new PriorityQueue<>();

    // برای درگ موس
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

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        topPanel.setBackground(new Color(117, 166, 121));
        topPanel.add(reachButton);
        topPanel.add(mstButton);
        topPanel.add(suggestButton);
        topPanel.add(reservationButton);
        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
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
        g2.setStroke(new BasicStroke(2));
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
            String u = p.getStartLocation(), v = p.getEndLocation();
            String key = u.compareTo(v) < 0 ? u + "|" + v : v + "|" + u;
            int count = pairCount.getOrDefault(key, 0);
            if (p.isHighlighted()) {
                g2.setColor(Color.RED);
            } else if (mstEdges != null && mstEdges.contains(p)) {
                g2.setColor(Color.BLUE);
            } else if (p.isRandom()) {
                g2.setColor(Color.LIGHT_GRAY);
            } else {
                g2.setColor(Color.BLACK);
            }
            if (count > 1 && u.compareTo(v) > 0) {
                double x1 = a.x, y1 = a.y, x2 = b.x, y2 = b.y;
                double mx = (x1 + x2) / 2, my = (y1 + y2) / 2;
                double dx = x2 - x1, dy = y2 - y1;
                double len = Math.hypot(dx, dy); if (len == 0) len = 1;
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
                g2.drawString(String.valueOf(p.getCost()), (a.x + b.x)/2, (a.y + b.y)/2);
            }
        }
        if (dragStartNode != null && dragCurrentPoint != null) {
            Point a = universityPositions.get(dragStartNode);
            if (a != null) {
                g2.setColor(Color.GRAY);
                g2.drawLine(a.x, a.y, dragCurrentPoint.x, dragCurrentPoint.y);
            }
        }
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
// در همان کلاسی که متدهای UI را نگه می‌دارد (مثلاً GraphPanel یا MainFrame)
    private void showSuggestionDialog() {
        JDialog dialog = new JDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                "پیشنهاد مسیر و رزرو هوشمند",
                true
        );
        dialog.setLayout(new BorderLayout(10, 10));

        // --- پانل ورودی ---
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
            boolean found = UniPaths.DijkstraShortestPath(paths, dest, origin, false);
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
            List<UniPaths> bestPath = UniPaths.findShortestPathEdges(paths, dest, origin);
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
                    // رزرو
                    reservations.add(new Reservation(student, origin, dest, bestPath));
                    showReservationDialog();
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
                        repaint();
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

    // اصلاح showReservationDialog برای نمایش ظرفیت‌های مسیر
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
                if (minCap <= 0) {
                    label.setForeground(Color.RED);
                } else {
                    label.setForeground(isSelected
                            ? list.getSelectionForeground()
                            : list.getForeground());
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
        for (UniPaths edge : next.getPathEdges()) {
            GraphUtils.incrementCapacity(edge);
        }
        updateReservationModel(model);
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
}