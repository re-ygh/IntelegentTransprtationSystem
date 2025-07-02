import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * کلاس اصلی اجرای برنامه: «سامانه هوشمند حمل و نقل دانشگاهی»
 * در این کلاس پنجره گرافیکی اصلی ایجاد شده و صفحات مختلف (CardLayout) مدیریت می‌شوند.
 */
public class main {

    // لیست کلی دانشگاه‌ها (نودها)
    static List<Universities> universities = new ArrayList<>();

    // لیست مسیرها (یال‌ها بین نودها)
    static List<UniPaths> paths = new ArrayList<>();

    // نگاشت نام دانشگاه به مختصات گرافیکی آن (برای رسم گراف)
    static Map<String, Point> universityPositions = new HashMap<>();

    // پنل مرکزی رسم گراف با سه منبع داده: مسیرها، مختصات، دانشگاه‌ها
    static GraphPanel graphPanel = new GraphPanel(paths, universityPositions, universities);

    private static JPanel mainPanel;
    private static CardLayout cardLayout;

    public static void main(String[] args) {
        JFrame frame = new JFrame("سامانه هوشمند حمل و نقل دانشگاهی");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1100, 700);
        frame.setLocationRelativeTo(null);  // نمایش در مرکز صفحه

        // تنظیم صفحات مختلف برنامه با استفاده از CardLayout
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // صفحه‌ها بر اساس انتخاب منو (page1 تا page6)
        mainPanel.add(createMainMenu(), "menu");
        mainPanel.add(createBuildGraphPage(), "page1");
        mainPanel.add(createPage("صفحه نمایش گراف و زیرساخت"), "page2");
        mainPanel.add(createPage("صفحه الگوریتم‌ها و تحلیل‌ها"), "page3");
        mainPanel.add(createPage("صفحه پیشنهاد مسیر و رزرو هوشمند"), "page4");
        mainPanel.add(createPage("صفحه سفر چندمقصدی (TSP)"), "page5");
        mainPanel.add(createPage("صفحه مقیاس‌بندی و خوشه‌بندی"), "page6");

        // نمایش اسکرول در صورت بزرگ بودن محتوا
        mainPanel.setPreferredSize(new Dimension(1100, 700));
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        frame.getContentPane().add(scrollPane);
        frame.setVisible(true);
    }

    /**
     * ساخت منوی اصلی برنامه شامل ۶ گزینه عملیاتی + دکمه خروج
     */
    private static JPanel createMainMenu() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(147, 196, 151));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        String[] titles = {
                "۱. ساخت گراف دانشگاه‌ها",
                "۲. نمایش گراف و زیرساخت",
                "۳. الگوریتم‌ها و تحلیل‌ها",
                "۴. پیشنهاد مسیر و رزرو هوشمند",
                "۵. سفر چندمقصدی (TSP)",
                "۶. مقیاس‌بندی و خوشه‌بندی",
                "۷. خروج"
        };

        for (int i = 0; i < titles.length; i++) {
            JButton btn = new JButton(titles[i]);
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setMaximumSize(new Dimension(300, 40));
            panel.add(btn);
            panel.add(Box.createRigidArea(new Dimension(0, 10)));

            int index = i;
            btn.addActionListener(e -> {
                if (index == 6) { // گزینه ۷: خروج
                    System.exit(0);
                } else {
                    cardLayout.show(mainPanel, "page" + (index + 1));
                }
            });
        }

        return panel;
    }

    /**
     * ساخت صفحات عمومی با تیتر مشخص برای گزینه‌های منو (غیراز page1)
     */
    private static JPanel createPage(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(144, 238, 144));

        JLabel label = new JLabel(title, SwingConstants.CENTER);
        label.setFont(new Font("Tahoma", Font.BOLD, 18));
        panel.add(label, BorderLayout.CENTER);

        // دکمه بازگشت به منوی اصلی
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.setOpaque(false);
        JButton backButton = new JButton("بازگشت به منوی اصلی");
        backButton.addActionListener(e -> cardLayout.show(mainPanel, "menu"));
        bottomPanel.add(backButton);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * ساخت صفحه مخصوص ایجاد دانشگاه‌ها و مسیرهای گراف (page1)
     */
    private static JPanel createBuildGraphPage() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(144, 238, 144));

        // فرم ورودی در سمت راست
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        // فیلدهای ورودی دانشگاه و مسیر
        JTextField nameField       = new JTextField(12);
        String[] regions           = {"شمال", "جنوب", "شرق", "غرب", "مرکز"};
        JComboBox<String> regionField = new JComboBox<>(regions);
        JComboBox<Universities> fromBox = new JComboBox<>();
        JComboBox<Universities> toBox   = new JComboBox<>();
        JTextField costField       = new JTextField(10);
        JTextField startTimeField  = new JTextField(10);
        JTextField endTimeField    = new JTextField(10);
        JTextField capacityField   = new JTextField(10);

        // افزودن فیلدها به پنل ورودی
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

        // افزودن مسیر بین دانشگاه‌ها
        contentPanel.add(new JLabel("از:"));
        contentPanel.add(fromBox);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        contentPanel.add(new JLabel("به:"));
        contentPanel.add(toBox);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        contentPanel.add(new JLabel("هزینه مسیر:"));
        contentPanel.add(costField);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        contentPanel.add(new JLabel("زمان شروع:"));
        contentPanel.add(startTimeField);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        contentPanel.add(new JLabel("زمان پایان:"));
        contentPanel.add(endTimeField);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        contentPanel.add(new JLabel("ظرفیت:"));
        contentPanel.add(capacityField);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        JButton addPathBtn = new JButton("افزودن مسیر");
        addPathBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(addPathBtn);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JButton backButton = new JButton("بازگشت به منوی اصلی");
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        backButton.addActionListener(e -> cardLayout.show(mainPanel, "menu"));
        contentPanel.add(backButton);

        JScrollPane scrollPanel = new JScrollPane(contentPanel);
        scrollPanel.setPreferredSize(new Dimension(260, 700));
        panel.add(scrollPanel, BorderLayout.EAST);

        // نمایش گراف در وسط صفحه
        panel.add(graphPanel, BorderLayout.CENTER);

        // عملکرد دکمه افزودن دانشگاه
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

            // ساخت و اضافه کردن دانشگاه جدید به همراه موقعیت گرافیکی مناسب
            Universities u = Universities.generateNewUniversity(
                    name, region, 0, 0, universities, 750, 700
            );
            universities.add(u);
            universityPositions.put(name, new Point(u.getX(), u.getY()));
            fromBox.addItem(u);
            toBox.addItem(u);

            // پیشنهاد اتصال خودکار با کمترین هزینه (فاز اول)
            GraphUtils.updateGraphAfterAddingUniversity(u, universities, paths);

            if (universities.size() != 1) {
                JOptionPane.showMessageDialog(panel,
                        "دانشگاه جدید افزوده شد و مسیر پیشنهادی اضافه گردید.",
                        "موفقیت", JOptionPane.INFORMATION_MESSAGE);
            }

            nameField.setText("");
            graphPanel.repaint();
        });

        // عملکرد دکمه افزودن مسیر دستی بین دو دانشگاه
        addPathBtn.addActionListener(e -> {
            Universities from = (Universities) fromBox.getSelectedItem();
            Universities to   = (Universities) toBox.getSelectedItem();
            try {
                int cost      = Integer.parseInt(costField.getText());
                int startTime = Integer.parseInt(startTimeField.getText());
                int endTime   = Integer.parseInt(endTimeField.getText());
                int capacity  = Integer.parseInt(capacityField.getText());

                if (from != null && to != null && !from.equals(to)) {
                    // حذف مسیر پیشنهادی (طوسی) قبلی بین این دو
                    Iterator<UniPaths> iter = paths.iterator();
                    while (iter.hasNext()) {
                        UniPaths p = iter.next();
                        if (p.isRandom() &&
                                (
                                        (p.getStartLocation().equals(from.getUniversityName()) &&
                                                p.getEndLocation().equals(to.getUniversityName()))
                                                || (p.getStartLocation().equals(to.getUniversityName()) &&
                                                p.getEndLocation().equals(from.getUniversityName()))
                                )) {
                            iter.remove();
                            break;
                        }
                    }

                    // ایجاد مسیر دستی جدید (مشکی)
                    UniPaths path = new UniPaths(
                            startTime, endTime, cost, capacity,
                            from.getUniversityName(), to.getUniversityName(), false
                    );

                    boolean existsPath = paths.stream().anyMatch(p ->
                            p.getStartLocation().equals(path.getStartLocation()) &&
                                    p.getEndLocation().equals(path.getEndLocation())
                    );

                    if (!existsPath) {
                        paths.add(path);
                    } else {
                        JOptionPane.showMessageDialog(panel,
                                "بین این دو دانشگاه یک مسیر قبلی وجود دارد.");
                    }

                    // پاکسازی فیلدها و بازنقش کردن گراف
                    costField.setText("");
                    startTimeField.setText("");
                    endTimeField.setText("");
                    capacityField.setText("");
                    graphPanel.repaint();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel, "مقادیر عددی را درست وارد کنید");
            }
        });

        return panel;
    }
}
