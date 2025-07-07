import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.io.*;

/**
 * کلاس اصلی اجرای برنامه: «سامانه هوشمند حمل و نقل دانشگاهی»
 */
public class main {
    static List<Universities> universities = new ArrayList<>();
    static List<UniPaths> paths = new ArrayList<>();
    static Map<String, Point> universityPositions = new HashMap<>();
    static GraphPanel graphPanel = new GraphPanel(paths, universityPositions, universities);

    public static JPanel mainPanel;
    public static CardLayout cardLayout;
    public static TSPPage tspPage;

    public static void main(String[] args) {

        // تولید داده‌های رندوم برای تست
        // برای غیرفعال کردن تولید داده‌های رندوم، خط زیر را کامنت کنید:
        generateRandomUniversitiesAndPaths();

        JFrame frame = new JFrame("سامانه هوشمند حمل و نقل دانشگاهی");




        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1100, 700);
        frame.setLocationRelativeTo(null);

        tspPage = new TSPPage(graphPanel, universities, paths);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(createMainMenu(), "menu");
        mainPanel.add(createBuildGraphPage(), "page1");
        mainPanel.add(createPage("صفحه سفر چندمقصدی (TSP)"), "page5");
        mainPanel.add(tspPage, "page5");

        JScrollPane scrollPane = new JScrollPane(mainPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        frame.getContentPane().add(scrollPane);
        frame.setVisible(true);
    }

    private static JPanel createMainMenu() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(147, 196, 151));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        String[] titles = {
                "۱. ساخت گراف دانشگاه‌ها",
                "۲. سفر چندمقصدی (TSP)",
                "۳. ذخیره دانشگاه‌ها و مسیرها",
                "۴. بارگذاری دانشگاه‌ها و مسیرها",
                "۵. خروج"
        };
        String[] pageIds = {"page1", "page5", "save", "load", "exit"};

        for (int i = 0; i < titles.length; i++) {
            JButton btn = new JButton(titles[i]);
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setMaximumSize(new Dimension(300, 40));
            panel.add(btn);
            panel.add(Box.createRigidArea(new Dimension(0, 10)));

            final int idx = i;
            btn.addActionListener(e -> {
                if (pageIds[idx].equals("exit")) {
                    System.exit(0);
                } else if (pageIds[idx].equals("save")) {
                    saveUniversitiesAndPaths();
                } else if (pageIds[idx].equals("load")) {
                    loadUniversitiesAndPaths();
                } else {
                    cardLayout.show(mainPanel, pageIds[idx]);
                }
            });
        }
        return panel;
    }

    /**
     * تولید دانشگاه‌ها و مسیرهای رندوم برای تست
     * این متد را می‌توان کامنت کرد تا داده‌های رندوم تولید نشوند
     */
    private static void generateRandomUniversitiesAndPaths() {
        Random rand = new Random();
        int panelWidth  = 750;
        int panelHeight = 700;

        // 1) تولید ۱۵ دانشگاه با توزیع تصادفی، به‌گونه‌ای که هر منطقه حداقل ۲ دانشگاه داشته باشد
        String[] regions = {"شمال", "جنوب", "شرق", "غرب", "مرکز"};
        int minPerRegion = 2;
        int totalUniversities = 15;

        // آماده‌سازی لیست مناطق (حداقل ۲ بار هر منطقه)
        List<String> regionPool = new ArrayList<>();
        for (String r : regions) {
            for (int i = 0; i < minPerRegion; i++) {
                regionPool.add(r);
            }
        }
        // پرکردن باقی‌مانده تا ۱۵ با مناطق تصادفی
        while (regionPool.size() < totalUniversities) {
            regionPool.add(regions[rand.nextInt(regions.length)]);
        }
        Collections.shuffle(regionPool, rand);

        // نام‌گذاری داینامیک
        Map<String, Integer> nameCount = new HashMap<>();
        for (String region : regionPool) {
            int cnt = nameCount.getOrDefault(region, 0) + 1;
            nameCount.put(region, cnt);
            String uniName = "دانشگاه " + region + " " + cnt;
            Universities u = Universities.generateNewUniversity(
                    uniName, region, 0, 0, universities, panelWidth, panelHeight
            );
            universities.add(u);
            universityPositions.put(u.getUniversityName(), new Point(u.getX(), u.getY()));
        }

        // 2) مرحله‌ی اتصال اولیه: برای هر منطقه یک «درخت پوشا» (chain) بسازیم
        Set<String> usedPairs = new HashSet<>();
        Map<String, List<Integer>> regionIndices = new HashMap<>();
        for (int i = 0; i < universities.size(); i++) {
            String region = universities.get(i).getUniversityLocation();
            regionIndices.computeIfAbsent(region, k -> new ArrayList<>()).add(i);
        }
        for (List<Integer> idxs : regionIndices.values()) {
            Collections.shuffle(idxs, rand);
            for (int i = 1; i < idxs.size(); i++) {
                int fromIdx = idxs.get(i - 1);
                int toIdx   = idxs.get(i);
                String key  = fromIdx + "->" + toIdx;
                // اگر هنوز این جهت استفاده نشده، اضافه کن
                if (!usedPairs.contains(key)) {
                    usedPairs.add(key);
                    Universities from = universities.get(fromIdx);
                    Universities to   = universities.get(toIdx);
                    // ساخت یال با ویژگی‌های تصادفی
                    UniPaths p = new UniPaths(
                            rand.nextInt(24),                      // startTime
                            rand.nextInt(24 - rand.nextInt(24))    // endTime (بزرگ‌تر از startTime)
                                    + rand.nextInt(24) + 1,
                            rand.nextInt(391) + 10,                // cost [10,400]
                            rand.nextInt(5) + 1,                   // cap [1,5]
                            from.getUniversityName(),
                            to.getUniversityName(),
                            rand.nextBoolean(),
                            20,                                   // remainingCapacity = capacity
                            new ArrayList<>()
                    );
                    paths.add(p);
                }
            }
        }

        // 3) اضافه‌کردن یال‌های تصادفی تا رسیدن به مثلاً ۳۰ یال
        int edgesToAdd = 30;
        while (paths.size() < edgesToAdd) {
            int fromIdx = rand.nextInt(universities.size());
            int toIdx;
            do { toIdx = rand.nextInt(universities.size()); }
            while (toIdx == fromIdx);

            String forwardKey = fromIdx + "->" + toIdx;
            String reverseKey = toIdx + "->" + fromIdx;
            // اگر این جهت قبلاً ایجاد شده، رد کن
            if (usedPairs.contains(forwardKey)) continue;
            // اگر هر دو جهت قبلاً استفاده شده (یعنی already two edges)، رد کن
            if (usedPairs.contains(forwardKey) && usedPairs.contains(reverseKey)) continue;

            usedPairs.add(forwardKey);
            Universities from = universities.get(fromIdx);
            Universities to   = universities.get(toIdx);

            int cost      = rand.nextInt(391) + 10;
            int cap       = rand.nextInt(5)   + 1;
            int startTime = rand.nextInt(24);
            int endTime   = rand.nextInt(24 - startTime) + startTime + 1;
            boolean isRandom = rand.nextBoolean();

            UniPaths p = new UniPaths(
                    startTime,
                    endTime,
                    cost,
                    cap,
                    from.getUniversityName(),
                    to.getUniversityName(),
                    isRandom,
                    cap,
                    new ArrayList<>()
            );
            paths.add(p);
        }
        // تمام! اکنون:
        // • هر منطقه یک زیرگراف متصل دارد.
        // • بین هر جفت دانشگاه حداکثر دو یال (یک در هر جهت) وجود دارد.
        // پایان رندوم شیت
    }

    /**
     * ذخیره دانشگاه‌ها و مسیرها در فایل
     */
    private static void saveUniversitiesAndPaths() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("انتخاب محل ذخیره فایل");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("فایل‌های متنی (*.txt)", "txt"));
        
        int result = fileChooser.showSaveDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".txt")) {
                file = new File(file.getAbsolutePath() + ".txt");
            }
            
            try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"))) {
                // ذخیره دانشگاه‌ها
                writer.println("=== دانشگاه‌ها ===");
                for (Universities uni : universities) {
                    writer.println(uni.getUniversityName() + "," + 
                                 uni.getUniversityLocation() + "," + 
                                 uni.getX() + "," + uni.getY());
                }
                
                // ذخیره مسیرها
                writer.println("\n=== مسیرها ===");
                for (UniPaths path : paths) {
                    writer.println(path.getStartLocation() + "," + 
                                 path.getEndLocation() + "," + 
                                 path.getCost() + "," + 
                                 path.getStartTime() + "," + 
                                 path.getEndTime() + "," + 
                                 path.getCapacity() + "," + 
                                 path.isRandom());
                }
                
                JOptionPane.showMessageDialog(null,
                    "دانشگاه‌ها و مسیرها با موفقیت در فایل ذخیره شدند:\n" + file.getName(),
                    "ذخیره موفق", JOptionPane.INFORMATION_MESSAGE);
                    
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null,
                    "خطا در ذخیره فایل:\n" + e.getMessage(),
                    "خطا", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * بارگذاری دانشگاه‌ها و مسیرها از فایل
     */
    private static void loadUniversitiesAndPaths() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("انتخاب فایل برای بارگذاری");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("فایل‌های متنی (*.txt)", "txt"));
        
        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
                // پاک کردن داده‌های فعلی
                universities.clear();
                paths.clear();
                universityPositions.clear();
                
                String line;
                boolean readingUniversities = false;
                boolean readingPaths = false;
                
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;
                    
                    if (line.equals("=== دانشگاه‌ها ===")) {
                        readingUniversities = true;
                        readingPaths = false;
                        continue;
                    } else if (line.equals("=== مسیرها ===")) {
                        readingUniversities = false;
                        readingPaths = true;
                        continue;
                    }
                    
                    if (readingUniversities) {
                        String[] parts = line.split(",");
                        if (parts.length >= 4) {
                            String name = parts[0];
                            String location = parts[1];
                            int x = Integer.parseInt(parts[2]);
                            int y = Integer.parseInt(parts[3]);
                            
                            Universities uni = new Universities(name, location, 0, 0, x, y);
                            universities.add(uni);
                            universityPositions.put(name, new Point(x, y));
                        }
                    } else if (readingPaths) {
                        String[] parts = line.split(",");
                        if (parts.length >= 7) {
                            String startLoc = parts[0];
                            String endLoc = parts[1];
                            int cost = Integer.parseInt(parts[2]);
                            int startTime = Integer.parseInt(parts[3]);
                            int endTime = Integer.parseInt(parts[4]);
                            int capacity = Integer.parseInt(parts[5]);
                            boolean isRandom = Boolean.parseBoolean(parts[6]);
                            
                            UniPaths path = new UniPaths(startTime, endTime, cost, capacity,
                                                        startLoc, endLoc, isRandom, capacity, new ArrayList<>());
                            paths.add(path);
                        }
                    }
                }
                
                // به‌روزرسانی کامل سیستم پس از بارگذاری
                updateSystemAfterDataChange();
                
                JOptionPane.showMessageDialog(null,
                    "دانشگاه‌ها و مسیرها با موفقیت از فایل بارگذاری شدند:\n" + file.getName(),
                    "بارگذاری موفق", JOptionPane.INFORMATION_MESSAGE);
                    
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null,
                    "خطا در بارگذاری فایل:\n" + e.getMessage(),
                    "خطا", JOptionPane.ERROR_MESSAGE);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null,
                    "خطا در فرمت فایل:\n" + e.getMessage(),
                    "خطا", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * به‌روزرسانی کامل سیستم پس از تغییر داده‌ها (بارگذاری، حذف، افزودن)
     */
    private static void updateSystemAfterDataChange() {
        // 1. به‌روزرسانی گراف پنل
        graphPanel = new GraphPanel(paths, universityPositions, universities);
        
        // 2. به‌روزرسانی TSP صفحه
        if (tspPage != null) {
            tspPage.updateUniversityList();
        }
        
        // 3. به‌روزرسانی صفحه اصلی گراف (اگر باز باشد)
        if (mainPanel != null) {
            // حذف صفحه قدیمی گراف
            mainPanel.removeAll();
            
            // اضافه کردن مجدد صفحات
            mainPanel.add(createMainMenu(), "menu");
            mainPanel.add(createBuildGraphPage(), "page1");
            mainPanel.add(createPage("صفحه سفر چندمقصدی (TSP)"), "page5");
            mainPanel.add(tspPage, "page5");
            
            // بازنقاشی
            mainPanel.revalidate();
            mainPanel.repaint();
        }
        
        // 4. پاک کردن هایلایت‌های قبلی
        for (UniPaths path : paths) {
            path.setHighlighted(false);
        }
        
        // 5. پاک کردن مسیرهای Dijkstra
        UniPaths.DijkstraPaths.clear();
        
        // 6. بازنقاشی گراف
        if (graphPanel != null) {
            graphPanel.repaint();
        }
    }

    private static JPanel createPage(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(144, 238, 144));

        JLabel label = new JLabel(title, SwingConstants.CENTER);
        label.setFont(new Font("Tahoma", Font.BOLD, 18));
        panel.add(label, BorderLayout.CENTER);

        JButton backButton = new JButton("بازگشت به منوی اصلی");
        backButton.addActionListener(e -> cardLayout.show(mainPanel, "menu"));
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom.setOpaque(false);
        bottom.add(backButton);
        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    private static JPanel createBuildGraphPage() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(144, 238, 144));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        JTextField nameField         = new JTextField(12);
        String[] regions             = {"شمال", "جنوب", "شرق", "غرب", "مرکز"};
        JComboBox<String> regionField = new JComboBox<>(regions);
        JComboBox<Universities> fromBox = new JComboBox<>();
        JComboBox<Universities> toBox   = new JComboBox<>();
        JComboBox<Universities> removeUniBox = new JComboBox<>();
        
        // متد کمکی برای به‌روزرسانی ComboBox ها
        Runnable updateComboBoxes = () -> {
            fromBox.removeAllItems();
            toBox.removeAllItems();
            removeUniBox.removeAllItems();
            for (Universities uni : universities) {
                fromBox.addItem(uni);
                toBox.addItem(uni);
                removeUniBox.addItem(uni);
            }
        };
        
        // اضافه کردن دانشگاه‌های موجود به ComboBox ها
        updateComboBoxes.run();
        JTextField costField         = new JTextField(10);
        JTextField startTimeField    = new JTextField(10);
        JTextField endTimeField      = new JTextField(10);
        JTextField capacityField     = new JTextField(10);

        contentPanel.add(new JLabel("نام دانشگاه جدید:"));
        contentPanel.add(nameField);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        contentPanel.add(new JLabel("منطقه:"));
        contentPanel.add(regionField);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        JButton addUniBtn = new JButton("افزودن دانشگاه");
        addUniBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(addUniBtn);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        contentPanel.add(new JLabel("از:"));
        contentPanel.add(fromBox);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        contentPanel.add(new JLabel("به:"));
        contentPanel.add(toBox);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        contentPanel.add(new JLabel("هزینه مسیر:"));
        contentPanel.add(costField);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        contentPanel.add(new JLabel("زمان شروع (0–24):"));
        contentPanel.add(startTimeField);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        contentPanel.add(new JLabel("زمان پایان (0–24):"));
        contentPanel.add(endTimeField);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        contentPanel.add(new JLabel("ظرفیت:"));
        contentPanel.add(capacityField);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        JButton addPathBtn = new JButton("افزودن مسیر");
        addPathBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(addPathBtn);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // بخش حذف مسیر
        contentPanel.add(new JLabel("حذف مسیر:"));
        contentPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        contentPanel.add(new JLabel("از:"));
        contentPanel.add(fromBox);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        contentPanel.add(new JLabel("به:"));
        contentPanel.add(toBox);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        JButton removePathBtn = new JButton("حذف مسیر");
        removePathBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(removePathBtn);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // بخش حذف دانشگاه
        contentPanel.add(new JLabel("حذف دانشگاه:"));
        contentPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        contentPanel.add(new JLabel("دانشگاه:"));
        contentPanel.add(removeUniBox);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        JButton removeUniBtn = new JButton("حذف دانشگاه");
        removeUniBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(removeUniBtn);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JButton backButton = new JButton("بازگشت به منوی اصلی");
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        backButton.addActionListener(e -> cardLayout.show(mainPanel, "menu"));
        contentPanel.add(backButton);

        JScrollPane scrollPanel = new JScrollPane(contentPanel);
        scrollPanel.setPreferredSize(new Dimension(260, 700));
        panel.add(scrollPanel, BorderLayout.EAST);

        panel.add(graphPanel, BorderLayout.CENTER);

        // اضافه کردن دانشگاه
        addUniBtn.addActionListener(e -> {
            String name   = nameField.getText().trim();
            String region = (String) regionField.getSelectedItem();

            boolean exists = universities.stream()
                    .anyMatch(u -> u.getUniversityName().equalsIgnoreCase(name));
            if (exists) {
                JOptionPane.showMessageDialog(panel,
                        "دانشگاهی با این نام قبلاً اضافه شده است.",
                        "خطا", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(panel,
                        "لطفاً نام دانشگاه را وارد کنید.",
                        "خطا", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Universities u = Universities.generateNewUniversity(
                    name, region, 0, 0, universities, 750, 700
            );
            universities.add(u);
            universityPositions.put(name, new Point(u.getX(), u.getY()));
            GraphUtils.updateGraphAfterAddingUniversity(u, universities, paths);

            if (universities.size() > 1) {
                JOptionPane.showMessageDialog(panel,
                        "دانشگاه جدید افزوده شد و مسیر پیشنهادی اضافه گردید.",
                        "موفقیت", JOptionPane.INFORMATION_MESSAGE);
            }

            nameField.setText("");
            updateComboBoxes.run();
            graphPanel.repaint();
        });

        // اضافه کردن مسیر دستی بین دو دانشگاه (رفع ConcurrentModificationException)
        addPathBtn.addActionListener(e -> {
            Universities from = (Universities) fromBox.getSelectedItem();
            Universities to   = (Universities) toBox.getSelectedItem();
            try {
                int cost      = Integer.parseInt(costField.getText().trim());
                int startTime = Integer.parseInt(startTimeField.getText().trim());
                int endTime   = Integer.parseInt(endTimeField.getText().trim());
                int capacity  = Integer.parseInt(capacityField.getText().trim());

                // اعتبارسنجی انتخاب مبدا/مقصد
                if (from == null || to == null || from.equals(to)) {
                    JOptionPane.showMessageDialog(panel,
                            "دانشگاه مبدا و مقصد نمی‌توانند خالی یا یکسان باشند.",
                            "خطا", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                // اعتبارسنجی بازه زمانی 0–24
                if (startTime < 0 || startTime > 24 || endTime < 0 || endTime > 24) {
                    JOptionPane.showMessageDialog(panel,
                            "زمان شروع و پایان باید عددی بین ۰ تا ۲۴ باشند.",
                            "خطا", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                //زمان شروع و پایان نمیتوانند یکی باشند
                if (startTime == endTime) {
                    JOptionPane.showMessageDialog(panel,
                            "زمان شروع و پایان باید زمان شروع و پایان نمیتوانند یکی باشند.",
                            "خطا", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                // اعتبارسنجی ترتیب زمانی
                if (endTime < startTime) {
                    JOptionPane.showMessageDialog(panel,
                            "زمان پایان نمی‌تواند کمتر از زمان شروع باشد.",
                            "خطا", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // ساخت آبجکت مسیر جدید
                UniPaths newPath = new UniPaths(
                        startTime, endTime, cost, capacity,
                        from.getUniversityName(), to.getUniversityName(),
                        false, capacity, new ArrayList<>()
                );

                // چک تکراری بودن مسیر در همان جهت (فقط برای یال‌های غیر رندوم)
                boolean existsNonRandomPath = paths.stream().anyMatch(p ->
                        p.getStartLocation().equals(newPath.getStartLocation()) &&
                                p.getEndLocation().equals(newPath.getEndLocation()) &&
                                !p.isRandom()
                );

                if (existsNonRandomPath) {
                    JOptionPane.showMessageDialog(panel,
                            "بین این دو دانشگاه یک مسیر غیر رندوم قبلی وجود دارد.",
                            "خطا", JOptionPane.WARNING_MESSAGE);
                } else {
                    // حذف یال رندوم موجود در همان جهت (اگر وجود داشته باشد)
                    paths.removeIf(p ->
                            p.getStartLocation().equals(newPath.getStartLocation()) &&
                                    p.getEndLocation().equals(newPath.getEndLocation()) &&
                                    p.isRandom()
                    );
                    
                    // اضافه کردن مسیر دستی
                    paths.add(newPath);
                }

                // پاک‌سازی فیلدها و بازنقاشی
                costField.setText("");
                startTimeField.setText("");
                endTimeField.setText("");
                capacityField.setText("");
                updateComboBoxes.run();
                graphPanel.repaint();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel,
                        "لطفاً مقادیر عددی را به درستی وارد کنید.",
                        "خطا", JOptionPane.ERROR_MESSAGE);
            }
        });

        // حذف مسیر
        removePathBtn.addActionListener(e -> {
            Universities from = (Universities) fromBox.getSelectedItem();
            Universities to   = (Universities) toBox.getSelectedItem();

            if (from == null || to == null || from.equals(to)) {
                JOptionPane.showMessageDialog(panel,
                        "دانشگاه مبدا و مقصد نمی‌توانند خالی یا یکسان باشند.",
                        "خطا", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // حذف مسیر در هر دو جهت
            final int[] removedCount = {0};
            paths.removeIf(p -> {
                boolean shouldRemove = (p.getStartLocation().equals(from.getUniversityName()) &&
                        p.getEndLocation().equals(to.getUniversityName())) ||
                        (p.getStartLocation().equals(to.getUniversityName()) &&
                                p.getEndLocation().equals(from.getUniversityName()));
                if (shouldRemove) {
                    removedCount[0]++;
                }
                return shouldRemove;
            });

            if (removedCount[0] > 0) {
                JOptionPane.showMessageDialog(panel,
                        removedCount[0] + " مسیر حذف شد.",
                        "موفقیت", JOptionPane.INFORMATION_MESSAGE);
                updateComboBoxes.run();
                graphPanel.repaint();
            } else {
                JOptionPane.showMessageDialog(panel,
                        "مسیری بین این دو دانشگاه یافت نشد.",
                        "خطا", JOptionPane.WARNING_MESSAGE);
            }
        });

        // حذف دانشگاه
        removeUniBtn.addActionListener(e -> {
            Universities uniToRemove = (Universities) removeUniBox.getSelectedItem();

            if (uniToRemove == null) {
                JOptionPane.showMessageDialog(panel,
                        "لطفاً دانشگاهی را برای حذف انتخاب کنید.",
                        "خطا", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // تأیید حذف
            int result = JOptionPane.showConfirmDialog(panel,
                    "آیا از حذف دانشگاه '" + uniToRemove.getUniversityName() + "' و تمام مسیرهای مربوط به آن اطمینان دارید؟",
                    "تأیید حذف", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (result == JOptionPane.YES_OPTION) {
                String uniName = uniToRemove.getUniversityName();

                // حذف تمام مسیرهای مربوط به این دانشگاه
                final int[] removedPaths = {0};
                paths.removeIf(p -> {
                    boolean shouldRemove = p.getStartLocation().equals(uniName) || p.getEndLocation().equals(uniName);
                    if (shouldRemove) {
                        removedPaths[0]++;
                    }
                    return shouldRemove;
                });

                // حذف دانشگاه از لیست
                universities.remove(uniToRemove);

                // حذف موقعیت دانشگاه
                universityPositions.remove(uniName);

                JOptionPane.showMessageDialog(panel,
                        "دانشگاه '" + uniName + "' و " + removedPaths[0] + " مسیر مربوط به آن حذف شد.",
                        "موفقیت", JOptionPane.INFORMATION_MESSAGE);

                updateComboBoxes.run();
                graphPanel.repaint();
            }
        });

        return panel;
    }
}
