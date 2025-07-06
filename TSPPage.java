import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TSPPage extends JPanel {
    private final GraphPanel graphPanel;
    private final List<Universities> universities;
    private final List<UniPaths> paths;
    private List<Integer> lastOptimalOrder; // ذخیره آخرین ترتیب بهینه محاسبه شده
    private JList<Universities> universityList;
    private JTextArea resultArea;
    private JButton calculateButton;
    private JButton visualizeButton;

    private JButton backButton; // دکمه جدید برای بازگشت
    private JDialog graphDialog; // دیالوگ جدید برای نمایش گراف
    private JTextArea logArea; // ناحیه متن برای لاگ در پنل اصلی


    public TSPPage(GraphPanel graphPanel, List<Universities> universities, List<UniPaths> paths) {
        this.graphPanel = graphPanel;
        this.universities = universities;
        this.paths = paths;

        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(240, 240, 240));

        // پنل انتخاب دانشگاه‌ها
        JPanel selectionPanel = createSelectionPanel();

        // پنل نتایج
        JPanel resultPanel = createResultPanel();

        // پنل دکمه‌ها
        JPanel buttonPanel = createButtonPanel();

        // پنل بازگشت
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        backButton = new JButton("بازگشت به منوی اصلی");
        backButton.addActionListener(e -> {
            // کد بازگشت به منوی اصلی
            CardLayout cl = (CardLayout) main.mainPanel.getLayout();
            cl.show(main.mainPanel, "menu");
        });
        bottomPanel.add(backButton);

        add(selectionPanel, BorderLayout.WEST);
        add(resultPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.NORTH); // اضافه کردن پنل بازگشت به بالای صفحه
    }

    private JPanel createSelectionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("انتخاب دانشگاه‌ها"));

        universityList = new JList<>(new DefaultListModel<>());
        universityList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        universityList.setCellRenderer(new UniversityListRenderer());

        // پر کردن لیست با دانشگاه‌های موجود
        updateUniversityList();

        JScrollPane scrollPane = new JScrollPane(universityList);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // متد جدید برای آپدیت لیست دانشگاه‌ها
    public void updateUniversityList() {
        DefaultListModel<Universities> model = (DefaultListModel<Universities>) universityList.getModel();
        model.clear(); // پاک کردن لیست قبلی
        universities.forEach(model::addElement); // اضافه کردن دانشگاه‌های جدید
    }

    private JPanel createResultPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("نتایج محاسبه TSP"));

        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(resultArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        calculateButton = new JButton("محاسبه بهترین مسیر");
        calculateButton.addActionListener(e -> calculateTSP());

        visualizeButton = new JButton("نمایش TSP");
        visualizeButton.setEnabled(false);
        visualizeButton.addActionListener(e -> visualizePath());

        panel.add(calculateButton);
        panel.add(visualizeButton);

        return panel;
    }

    private void calculateTSP() {
        List<Universities> selected = universityList.getSelectedValuesList();

        if (selected.size() < 2 || selected.size() > 10) {
            JOptionPane.showMessageDialog(this,
                    "لطفاً بین ۲ تا ۱۰ دانشگاه انتخاب کنید",
                    "خطا", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // ساخت ماتریس هزینه
            double[][] costMatrix = GraphUtils.buildCostMatrix(selected, paths);

            // حل مسئله TSP
            TSPSolver solver = new TSPSolver(costMatrix);
            solver.solve();
            List<Integer> optimalPath = solver.getOptimalPath();
            double totalCost = solver.getOptimalCost();

            // ذخیره ترتیب بهینه (اضافه کردن این بخش)
            this.lastOptimalOrder = optimalPath;

            // نمایش نتایج
            displayResults(selected, optimalPath, totalCost);
            visualizeButton.setEnabled(true);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "خطا در محاسبه مسیر: " + ex.getMessage(),
                    "خطا", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void displayResults(List<Universities> selected, List<Integer> pathIndices, double totalCost) {
        StringBuilder sb = new StringBuilder();
        sb.append("بهترین ترتیب بازدید:\n");

        for (int i = 0; i < pathIndices.size(); i++) {
            int idx = pathIndices.get(i);
            sb.append(i + 1).append(". ").append(selected.get(idx).getUniversityName());

            if (i < pathIndices.size() - 1) {
                int nextIdx = pathIndices.get(i + 1);
                String from = selected.get(idx).getUniversityName();
                String to = selected.get(nextIdx).getUniversityName();

                // محاسبه جزئیات مسیر بین این دو دانشگاه
                UniPaths.DijkstraShortestPath(paths, from, to, false);
                List<UniPaths> segment = UniPaths.DijkstraPaths;

                sb.append(" → ").append(to)
                        .append(" (هزینه: ").append(segment.stream().mapToInt(UniPaths::getCost).sum())
                        .append(", زمان: ").append(segment.stream().mapToInt(p -> p.getEndTime() - p.getStartTime()).sum())
                        .append(")\n");
            }
        }

        // مسیر بازگشت به نقطه شروع
        int lastIdx = pathIndices.get(pathIndices.size() - 1);
        int firstIdx = pathIndices.get(0);
        String from = selected.get(lastIdx).getUniversityName();
        String to = selected.get(firstIdx).getUniversityName();

        UniPaths.DijkstraShortestPath(paths, from, to, false);
        List<UniPaths> returnSegment = UniPaths.DijkstraPaths;

        sb.append("\nمسیر بازگشت به نقطه شروع:\n")
                .append(from).append(" → ").append(to)
                .append(" (هزینه: ").append(returnSegment.stream().mapToInt(UniPaths::getCost).sum())
                .append(", زمان: ").append(returnSegment.stream().mapToInt(p -> p.getEndTime() - p.getStartTime()).sum())
                .append(")\n\n")
                .append("هزینه کل سفر: ").append(totalCost);

        resultArea.setText(sb.toString());
    }

    private void visualizePath() {
        System.out.println("دکمه نمایش TSP کلیک شد");
        System.out.println("lastOptimalOrder: " + lastOptimalOrder);

        if (lastOptimalOrder == null || lastOptimalOrder.isEmpty()) {
            System.out.println("خطا: مسیر بهینه محاسبه نشده است");
            JOptionPane.showMessageDialog(this,
                    "ابتدا مسیر بهینه را محاسبه کنید",
                    "خطا", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<Universities> selected = universityList.getSelectedValuesList();
        System.out.println("دانشگاه‌های انتخاب شده: " + selected.size());

        // نمایش لاگ در پنل اصلی
        showLogInMainPanel(selected, lastOptimalOrder);
        
        // نمایش گراف در پنل جدید
        showGraphInNewPanel(selected, lastOptimalOrder);
    }

    private List<Integer> extractOptimalOrder(String resultText) {
        List<Integer> optimalOrder = new ArrayList<>();

        // الگوی regex برای یافتن خطوطی مانند: "1. دانشگاه شمال → دانشگاه جنوب"
        Pattern pattern = Pattern.compile("^\\d+\\.\\s(.+?)(\\s→|$)", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(resultText);

        // استخراج نام دانشگاه‌ها از متن
        List<String> universityNames = new ArrayList<>();
        while (matcher.find()) {
            String uniName = matcher.group(1).trim();
            universityNames.add(uniName);
        }

        // تبدیل نام دانشگاه‌ها به اندیس‌های مربوطه
        DefaultListModel<Universities> model = (DefaultListModel<Universities>) universityList.getModel();
        for (String name : universityNames) {
            for (int i = 0; i < model.size(); i++) {
                if (model.getElementAt(i).getUniversityName().equals(name)) {
                    optimalOrder.add(i);
                    break;
                }
            }
        }

        return optimalOrder;
    }

    private void showLogInMainPanel(List<Universities> selected, List<Integer> order) {
        // تولید لاگ
        StringBuilder log = new StringBuilder();
        log.append("=== لاگ مسیر TSP ===\n\n");
        log.append("دانشگاه‌های انتخاب شده: ").append(selected.size()).append("\n");
        log.append("ترتیب بهینه: ");
        for (int i = 0; i < order.size(); i++) {
            log.append(selected.get(order.get(i)).getUniversityName());
            if (i < order.size() - 1) log.append(" → ");
        }
        log.append("\n\n");
        
        // جزئیات هر بخش مسیر
        for (int i = 0; i < order.size() - 1; i++) {
            Universities fromUni = selected.get(order.get(i));
            Universities toUni = selected.get(order.get(i + 1));
            
            log.append("بخش ").append(i + 1).append(": ").append(fromUni.getUniversityName())
               .append(" → ").append(toUni.getUniversityName()).append("\n");
            
            // محاسبه مسیر بین این دو دانشگاه
            UniPaths.DijkstraShortestPath(paths, fromUni.getUniversityName(), toUni.getUniversityName(), false);
            List<UniPaths> segment = UniPaths.DijkstraPaths;
            
            if (!segment.isEmpty()) {
                log.append("  مسیر: ");
                for (int j = 0; j < segment.size(); j++) {
                    UniPaths path = segment.get(j);
                    log.append(path.getStartLocation()).append(" → ").append(path.getEndLocation());
                    if (j < segment.size() - 1) log.append(" → ");
                }
                log.append("\n");
                log.append("  هزینه: ").append(segment.stream().mapToInt(UniPaths::getCost).sum()).append("\n");
                log.append("  زمان: ").append(segment.stream().mapToInt(p -> p.getEndTime() - p.getStartTime()).sum()).append("\n");
            } else {
                log.append("  خطا: مسیر مستقیم یافت نشد\n");
            }
            log.append("\n");
        }
        
        // مسیر بازگشت
        Universities lastUni = selected.get(order.get(order.size() - 1));
        Universities firstUni = selected.get(order.get(0));
        log.append("مسیر بازگشت: ").append(lastUni.getUniversityName())
           .append(" → ").append(firstUni.getUniversityName()).append("\n");
        
        UniPaths.DijkstraShortestPath(paths, lastUni.getUniversityName(), firstUni.getUniversityName(), false);
        List<UniPaths> returnSegment = UniPaths.DijkstraPaths;
        
        if (!returnSegment.isEmpty()) {
            log.append("  مسیر: ");
            for (int j = 0; j < returnSegment.size(); j++) {
                UniPaths path = returnSegment.get(j);
                log.append(path.getStartLocation()).append(" → ").append(path.getEndLocation());
                if (j < returnSegment.size() - 1) log.append(" → ");
            }
            log.append("\n");
            log.append("  هزینه: ").append(returnSegment.stream().mapToInt(UniPaths::getCost).sum()).append("\n");
            log.append("  زمان: ").append(returnSegment.stream().mapToInt(p -> p.getEndTime() - p.getStartTime()).sum()).append("\n");
        }
        
        // نمایش لاگ در ناحیه نتایج اصلی
        resultArea.setText(log.toString());
    }

    private void showGraphInNewPanel(List<Universities> selected, List<Integer> order) {
        // ایجاد پنل جدید برای نمایش گراف
        if (graphDialog == null) {
            graphDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "گراف TSP", false);
            graphDialog.setSize(800, 600);
            graphDialog.setLocationRelativeTo(this);
            
            // ایجاد یک کپی از GraphPanel برای نمایش در پنل جدید
            GraphPanel newGraphPanel = new GraphPanel(paths, main.universityPositions, universities);
            
            graphDialog.add(newGraphPanel);
        }
        
        // هایلایت مسیر روی گراف جدید (نه گراف اصلی)
        highlightPathOnNewGraph(selected, order);
        
        graphDialog.setVisible(true);
    }

    private void highlightPathOnNewGraph(List<Universities> selected, List<Integer> order) {
        // پاکسازی تمام هایلایت‌های قبلی
        for (UniPaths path : paths) {
            path.setHighlighted(false);
        }

        System.out.println("تعداد کل مسیرها: " + paths.size());
        System.out.println("مسیرهای موجود:");
        for (UniPaths path : paths) {
            System.out.println("  " + path.getStartLocation() + " -> " + path.getEndLocation());
        }

        // هایلایت مسیرهای TSP با رنگ قرمز
        for (int i = 0; i < order.size() - 1; i++) {
            Universities fromUni = selected.get(order.get(i));
            Universities toUni = selected.get(order.get(i + 1));

            System.out.println("جستجوی مسیر از " + fromUni.getUniversityName() + " به " + toUni.getUniversityName());

            // محاسبه مسیر بین این دو دانشگاه
            boolean pathFound = UniPaths.DijkstraShortestPath(paths, fromUni.getUniversityName(), toUni.getUniversityName(), false);
            List<UniPaths> segment = UniPaths.DijkstraPaths;
            
            System.out.println("مسیر یافت شد: " + pathFound + ", تعداد یال‌ها: " + segment.size());
            
            // هایلایت تمام یال‌های مسیر
            for (UniPaths path : segment) {
                path.setHighlighted(true);
                System.out.println("  هایلایت: " + path.getStartLocation() + " -> " + path.getEndLocation());
            }
        }

        // مسیر بازگشت به نقطه شروع
        Universities lastUni = selected.get(order.get(order.size() - 1));
        Universities firstUni = selected.get(order.get(0));
        
        System.out.println("جستجوی مسیر بازگشت از " + lastUni.getUniversityName() + " به " + firstUni.getUniversityName());
        
        boolean returnPathFound = UniPaths.DijkstraShortestPath(paths, lastUni.getUniversityName(), firstUni.getUniversityName(), false);
        List<UniPaths> returnSegment = UniPaths.DijkstraPaths;
        
        System.out.println("مسیر بازگشت یافت شد: " + returnPathFound + ", تعداد یال‌ها: " + returnSegment.size());
        
        for (UniPaths path : returnSegment) {
            path.setHighlighted(true);
            System.out.println("  هایلایت بازگشت: " + path.getStartLocation() + " -> " + path.getEndLocation());
        }

        // اعمال تغییرات روی گراف جدید
        if (graphDialog != null && graphDialog.isVisible()) {
            Component[] components = graphDialog.getContentPane().getComponents();
            for (Component comp : components) {
                if (comp instanceof GraphPanel) {
                    comp.repaint();
                    break;
                }
            }
        }
        
        System.out.println("مسیرهای TSP هایلایت شده: " +
                paths.stream().filter(UniPaths::isHighlighted).count());
    }

    private void highlightPathOnGraph(List<Universities> selected, List<Integer> order) {
        // 1. پاکسازی تمام هایلایت‌های قبلی
        for (UniPaths path : paths) {
            path.setHighlighted(false);
        }

        // 2. هایلایت مسیرهای TSP با رنگ قرمز
        for (int i = 0; i < order.size() - 1; i++) {
            Universities fromUni = selected.get(order.get(i));
            Universities toUni = selected.get(order.get(i + 1));

            // محاسبه مسیر بین این دو دانشگاه
            UniPaths.DijkstraShortestPath(paths, fromUni.getUniversityName(), toUni.getUniversityName(), false);
            List<UniPaths> segment = UniPaths.DijkstraPaths;
            
            // هایلایت تمام یال‌های مسیر
            for (UniPaths path : segment) {
                path.setHighlighted(true);
            }
        }

        // 3. مسیر بازگشت به نقطه شروع
        Universities lastUni = selected.get(order.get(order.size() - 1));
        Universities firstUni = selected.get(order.get(0));
        
        UniPaths.DijkstraShortestPath(paths, lastUni.getUniversityName(), firstUni.getUniversityName(), false);
        List<UniPaths> returnSegment = UniPaths.DijkstraPaths;
        
        for (UniPaths path : returnSegment) {
            path.setHighlighted(true);
        }

        // 4. اعمال تغییرات روی گراف
        graphPanel.repaint();
        System.out.println("مسیرهای TSP هایلایت شده: " +
                paths.stream().filter(UniPaths::isHighlighted).count());
    }

    // رندرر سفارشی برای نمایش دانشگاه‌ها در لیست
    private static class UniversityListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            Universities uni = (Universities) value;
            String text = String.format("%s (%s) - موقعیت: (%d,%d)",
                    uni.getUniversityName(),
                    uni.getUniversityLocation(),
                    uni.getX(),
                    uni.getY());

            return super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
        }
    }
}