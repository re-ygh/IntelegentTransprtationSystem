import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

//

public class TSPPage extends JPanel {
    private final GraphPanel graphPanel;
    private final List<Universities> universities;
    private final List<UniPaths> paths;
    private List<Integer> lastOptimalOrder; // ذخیره آخرین ترتیب بهینه محاسبه شده
    private JList<Universities> universityList;
    private JTextArea resultArea;
    private JButton calculateButton;
    private JButton visualizeButton;
    private JButton showMatrixButton;

    private JButton backButton; // دکمه جدید برای بازگشت
    private JDialog graphDialog; // دیالوگ جدید برای نمایش گراف
    private JTextArea logArea; // ناحیه متن برای لاگ در پنل اصلی

    // Add a field to store the last cost matrix
    private double[][] lastCostMatrix;

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
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));

        calculateButton = new JButton("محاسبه بهترین مسیر");
        calculateButton.addActionListener(e -> calculateTSP());

        visualizeButton = new JButton("نمایش TSP");
        visualizeButton.setEnabled(false);
        visualizeButton.addActionListener(e -> visualizePath());

        showMatrixButton = new JButton("نمایش ماتریس هزینه/زمان");
        showMatrixButton.addActionListener(e -> showCostMatrixDialog());

        panel.add(calculateButton);
        panel.add(visualizeButton);
        panel.add(showMatrixButton);

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

        // بررسی اتصال دانشگاه‌های انتخاب شده
        List<Universities> connectedUniversities = getConnectedUniversities(selected);
        if (connectedUniversities.size() < 2) {
            JOptionPane.showMessageDialog(this,
                    "هشدار: دانشگاه‌های انتخاب شده به هم متصل نیستند. ممکن است نتایج ناقص باشد.",
                    "هشدار", JOptionPane.WARNING_MESSAGE);
        } else if (connectedUniversities.size() != selected.size()) {
            JOptionPane.showMessageDialog(this,
                    "هشدار: " + (selected.size() - connectedUniversities.size()) + 
                    " دانشگاه از دانشگاه‌های انتخاب شده به بقیه متصل نیستند.",
                    "هشدار", JOptionPane.WARNING_MESSAGE);
        }

        try {
            // ساخت ماتریس هزینه
            double[][] costMatrix = GraphUtils.buildCostMatrix(selected, paths);
            this.lastCostMatrix = costMatrix;

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
        if (selected == null || selected.isEmpty() || pathIndices == null || pathIndices.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "خطا: لیست دانشگاه‌ها یا مسیر بهینه خالی است.",
                    "خطا", JOptionPane.ERROR_MESSAGE);
            resultArea.setText("");
            return;
        }
        // نمایش ماتریس هزینه/زمان
        StringBuilder matrixText = new StringBuilder();
        matrixText.append("ماتریس هزینه/زمان بین دانشگاه‌های انتخاب‌شده:\n");
        matrixText.append("      ");
        for (Universities u : selected) {
            matrixText.append(String.format("%-15s", u.getUniversityName()));
        }
        matrixText.append("\n");
        for (int i = 0; i < selected.size(); i++) {
            matrixText.append(String.format("%-15s", selected.get(i).getUniversityName()));
            for (int j = 0; j < selected.size(); j++) {
                matrixText.append(String.format("%-15.0f", lastCostMatrix != null ? lastCostMatrix[i][j] : 0));
            }
            matrixText.append("\n");
        }
        matrixText.append("\n");

        // لاگ مسیر TSP با قالب‌بندی زیبا
        StringBuilder sb = new StringBuilder();
        sb.append("=== مسیر بهینه TSP ===\n\n");
        sb.append("دانشگاه‌های انتخاب‌شده (").append(selected.size()).append("):\n");
        for (int i = 0; i < pathIndices.size(); i++) {
            int idx = pathIndices.get(i);
            sb.append(selected.get(idx).getUniversityName());
            if (i < pathIndices.size() - 1) sb.append(" → ");
        }
        sb.append("\n\n");
        sb.append("——————————————\n");

        for (int i = 0; i < pathIndices.size() - 1; i++) {
            int fromIdx = pathIndices.get(i);
            int toIdx = pathIndices.get(i + 1);
            String from = selected.get(fromIdx).getUniversityName();
            String to = selected.get(toIdx).getUniversityName();
            // محاسبه مسیر بین این دو دانشگاه
            UniPaths.DijkstraShortestPath(paths, from, to, false);
            List<UniPaths> segment = UniPaths.DijkstraPaths;
            sb.append("بخش ").append(i + 1).append(": ").append(from).append(" → ").append(to).append("\n");
            sb.append("مسیر: ");
            for (int j = 0; j < segment.size(); j++) {
                sb.append(segment.get(j).getStartLocation());
                if (j < segment.size() - 1) sb.append(" → ");
            }
            if (!segment.isEmpty()) {
                sb.append(" → ").append(segment.get(segment.size() - 1).getEndLocation());
            }
            sb.append("\n");
            int cost = segment.stream().mapToInt(UniPaths::getCost).sum();
            int time = segment.stream().mapToInt(p -> p.getEndTime() - p.getStartTime()).sum();
            sb.append("هزینه: ").append(cost).append(" | زمان: ").append(time).append("\n");
            sb.append("——————————————\n");
        }
        // مسیر بازگشت
        int lastIdx = pathIndices.get(pathIndices.size() - 1);
        int firstIdx = pathIndices.get(0);
        String lastUni = selected.get(lastIdx).getUniversityName();
        String firstUni = selected.get(firstIdx).getUniversityName();
        UniPaths.DijkstraShortestPath(paths, lastUni, firstUni, false);
        List<UniPaths> returnSegment = UniPaths.DijkstraPaths;
        sb.append("مسیر بازگشت: ").append(lastUni).append(" → ").append(firstUni).append("\n");
        sb.append("مسیر: ");
        for (int j = 0; j < returnSegment.size(); j++) {
            sb.append(returnSegment.get(j).getStartLocation());
            if (j < returnSegment.size() - 1) sb.append(" → ");
        }
        if (!returnSegment.isEmpty()) {
            sb.append(" → ").append(returnSegment.get(returnSegment.size() - 1).getEndLocation());
        }
        sb.append("\n");
        int cost = returnSegment.stream().mapToInt(UniPaths::getCost).sum();
        int time = returnSegment.stream().mapToInt(p -> p.getEndTime() - p.getStartTime()).sum();
        sb.append("هزینه: ").append(cost).append(" | زمان: ").append(time).append("\n");
        sb.append("——————————————\n");

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
        
        // نمایش گراف در پنل جدید - بهبود یافته
        showGraphInNewPanel(selected, lastOptimalOrder);
    }

    /**
     * متد جدید برای اطمینان از نمایش صحیح گراف TSP
     */
    private void ensureGraphDisplay() {
        if (graphDialog != null && graphDialog.isVisible()) {
            // اطمینان از اینکه گراف درست نمایش داده می‌شود
            SwingUtilities.invokeLater(() -> {
                Component[] components = graphDialog.getContentPane().getComponents();
                for (Component comp : components) {
                    if (comp instanceof GraphPanel) {
                        comp.revalidate();
                        comp.repaint();
                        break;
                    }
                }
            });
        }
    }

    /**
     * متد کمکی برای بررسی و نمایش مسیرهای TSP
     */
    private boolean validateAndHighlightTSPPath(List<Universities> selected, List<Integer> order) {
        if (selected == null || selected.isEmpty() || order == null || order.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "خطا: لیست دانشگاه‌ها یا مسیر بهینه خالی است.",
                    "خطا", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // بررسی اینکه آیا تمام دانشگاه‌های انتخاب شده در گراف موجود هستند
        for (Universities uni : selected) {
            if (!main.universityPositions.containsKey(uni.getUniversityName())) {
                JOptionPane.showMessageDialog(this,
                        "خطا: دانشگاه '" + uni.getUniversityName() + "' در گراف موجود نیست.",
                        "خطا", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        return true;
    }

    /**
     * متد کمکی برای بررسی اتصال دانشگاه‌ها در گراف
     */
    private boolean areUniversitiesConnected(String uni1, String uni2) {
        // بررسی اینکه آیا مسیر مستقیم یا غیرمستقیم بین دو دانشگاه وجود دارد
        return UniPaths.DijkstraShortestPath(paths, uni1, uni2, false);
    }

    /**
     * متد کمکی برای فیلتر کردن دانشگاه‌های متصل
     */
    private List<Universities> getConnectedUniversities(List<Universities> selected) {
        List<Universities> connected = new ArrayList<>();
        
        for (int i = 0; i < selected.size(); i++) {
            boolean isConnected = false;
            for (int j = 0; j < selected.size(); j++) {
                if (i != j) {
                    if (areUniversitiesConnected(selected.get(i).getUniversityName(), 
                                                selected.get(j).getUniversityName())) {
                        isConnected = true;
                        break;
                    }
                }
            }
            if (isConnected) {
                connected.add(selected.get(i));
            }
        }
        
        return connected;
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
        if (selected == null || selected.isEmpty() || order == null || order.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "خطا: لیست دانشگاه‌ها یا مسیر بهینه خالی است.",
                    "خطا", JOptionPane.ERROR_MESSAGE);
            return;
        }
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
        // ابتدا مسیر را هایلایت کن
        highlightPathOnNewGraph(selected, order);

        if (graphDialog == null) {
            graphDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "گراف TSP", false);
            graphDialog.setSize(1100, 850);
            graphDialog.setLocationRelativeTo(this);
            graphDialog.setLayout(new BorderLayout());
            
            // استفاده از همان مسیرهای اصلی به جای کپی
            GraphPanel newGraphPanel = new GraphPanel(paths, main.universityPositions, universities, false);
            graphDialog.add(newGraphPanel);
            
            // اضافه کردن دکمه بستن
            JButton closeButton = new JButton("بستن");
            closeButton.addActionListener(e -> graphDialog.dispose());
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
            buttonPanel.add(closeButton);
            graphDialog.add(buttonPanel, BorderLayout.SOUTH);
            
            // اطمینان از نمایش صحیح
            SwingUtilities.invokeLater(() -> {
                newGraphPanel.revalidate();
                newGraphPanel.repaint();
            });
        } else {
            // اگر قبلاً ساخته شده، فقط repaint کن
            SwingUtilities.invokeLater(() -> {
                Component[] components = graphDialog.getContentPane().getComponents();
                for (Component comp : components) {
                    if (comp instanceof GraphPanel) {
                        comp.revalidate();
                        comp.repaint();
                        break;
                    }
                }
            });
        }
        
        graphDialog.setVisible(true);
        
        // اطمینان از نمایش صحیح پس از نمایش دیالوگ
        SwingUtilities.invokeLater(this::ensureGraphDisplay);
    }

    private void highlightPathOnNewGraph(List<Universities> selected, List<Integer> order) {
        // بررسی اعتبار داده‌ها
        if (!validateAndHighlightTSPPath(selected, order)) {
            return;
        }

        // پاکسازی تمام هایلایت‌های قبلی
        for (UniPaths path : paths) {
            path.setHighlighted(false);
        }

        System.out.println("تعداد کل مسیرها: " + paths.size());
        System.out.println("مسیرهای موجود:");
        for (UniPaths path : paths) {
            System.out.println("  " + path.getStartLocation() + " -> " + path.getEndLocation());
        }

        int highlightedPaths = 0;

        // هایلایت مسیرهای TSP با رنگ قرمز
        for (int i = 0; i < order.size() - 1; i++) {
            Universities fromUni = selected.get(order.get(i));
            Universities toUni = selected.get(order.get(i + 1));

            System.out.println("جستجوی مسیر از " + fromUni.getUniversityName() + " به " + toUni.getUniversityName());

            // محاسبه مسیر بین این دو دانشگاه
            boolean pathFound = UniPaths.DijkstraShortestPath(paths, fromUni.getUniversityName(), toUni.getUniversityName(), false);
            List<UniPaths> segment = UniPaths.DijkstraPaths;
            
            System.out.println("مسیر یافت شد: " + pathFound + ", تعداد یال‌ها: " + segment.size());
            
            if (!pathFound || segment.isEmpty()) {
                System.out.println("هشدار: مسیر بین " + fromUni.getUniversityName() + " و " + toUni.getUniversityName() + " یافت نشد");
                continue;
            }
            
            // هایلایت تمام یال‌های مسیر
            for (UniPaths path : segment) {
                path.setHighlighted(true);
                highlightedPaths++;
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
        
        if (returnPathFound && !returnSegment.isEmpty()) {
            for (UniPaths path : returnSegment) {
                path.setHighlighted(true);
                highlightedPaths++;
                System.out.println("  هایلایت بازگشت: " + path.getStartLocation() + " -> " + path.getEndLocation());
            }
        } else {
            System.out.println("هشدار: مسیر بازگشت یافت نشد");
        }

        // اعمال تغییرات روی گراف جدید - بهبود یافته
        if (graphDialog != null) {
            Component[] components = graphDialog.getContentPane().getComponents();
            for (Component comp : components) {
                if (comp instanceof GraphPanel) {
                    comp.repaint();
                    break;
                }
            }
        }
        
        System.out.println("مسیرهای TSP هایلایت شده: " + highlightedPaths);
        
        if (highlightedPaths == 0) {
            JOptionPane.showMessageDialog(this,
                    "هشدار: هیچ مسیری برای هایلایت یافت نشد. ممکن است دانشگاه‌های انتخاب شده به هم متصل نباشند.",
                    "هشدار", JOptionPane.WARNING_MESSAGE);
        }
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

    private void showCostMatrixDialog() {
        if (lastCostMatrix == null || universityList.getSelectedValuesList().size() < 2) {
            JOptionPane.showMessageDialog(this, "ابتدا مسیر را محاسبه کنید.", "خطا", JOptionPane.ERROR_MESSAGE);
            return;
        }
        List<Universities> selected = universityList.getSelectedValuesList();
        int n = selected.size();
        String[] colNames = new String[n+1];
        colNames[0] = "";
        for (int i = 0; i < n; i++) colNames[i+1] = selected.get(i).getUniversityName();
        Object[][] data = new Object[n][n+1];
        int[][] values = new int[n][n];
        int[] minRow = new int[n];
        int[] maxRow = new int[n];
        for (int i = 0; i < n; i++) {
            data[i][0] = selected.get(i).getUniversityName();
            int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
            for (int j = 0; j < n; j++) {
                int val = (lastCostMatrix != null) ? (int)lastCostMatrix[i][j] : 0;
                data[i][j+1] = val;
                values[i][j] = val;
                if (i != j && val < min) min = val;
                if (i != j && val > max) max = val;
            }
            minRow[i] = min;
            maxRow[i] = max;
        }
        JTable table = new JTable(data, colNames) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        // Custom renderer for simple 4-color scheme
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                // رنگ‌ها
                Color white = Color.WHITE;
                Color lightGray = new Color(230, 230, 230);
                Color lightGreen = new Color(180, 255, 180);
                Color darkGreen = new Color(60, 180, 60);
                if (column == 0) {
                    c.setBackground(lightGray);
                } else if (row == column-1) {
                    c.setBackground(white); // قطر اصلی
                } else {
                    int val = values[row][column-1];
                    int min = minRow[row];
                    int max = maxRow[row];
                    if (val == 0) {
                        c.setBackground(lightGray);
                    } else if (val == min) {
                        c.setBackground(darkGreen);
                    } else if (val == max) {
                        c.setBackground(lightGreen);
                    } else {
                        c.setBackground(lightGray);
                    }
                }
                if (isSelected) {
                    c.setBackground(new Color(255, 255, 0)); // زرد برای انتخاب
                }
                setHorizontalAlignment(CENTER);
                return c;
            }
        });
        table.setRowHeight(35); // Increased row height for better visibility
        table.getTableHeader().setReorderingAllowed(false);
        table.setFont(new Font("Tahoma", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Tahoma", Font.BOLD, 13));
        table.setGridColor(new Color(80, 120, 80));
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(3, 3)); // Increased spacing for better separation
        table.setBackground(new Color(147, 196, 151));
        table.getTableHeader().setBackground(new Color(147, 196, 151));

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(new Color(147, 196, 151));
        scroll.setPreferredSize(new Dimension(900, 600));

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "ماتریس هزینه/زمان", true);
        dialog.getContentPane().setBackground(new Color(147, 196, 151));
        dialog.add(scroll);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
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