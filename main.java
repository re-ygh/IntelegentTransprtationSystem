import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Scanner;
import java.util.*;
public class main  {


    static List<Universities> universities = new ArrayList<>();
    static List<UniPaths> paths = new ArrayList<>();
    static Map<String, Point> universityPositions = new HashMap<>();
    static GraphPanel graphPanel = new GraphPanel(paths, universityPositions,  universities);


    Scanner sc = new Scanner(System.in);
    int UniNumber = 0;

    private static JPanel mainPanel; // نگهدارنده کل پنل
    private static CardLayout cardLayout; // مدیریت صفحات مختلف


    public static void main(String[] args) {
        JFrame frame = new JFrame("سامانه هوشمند حمل و نقل دانشگاهی");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        JPanel menuPanel = createMainMenu();
        mainPanel.add(menuPanel, "menu");
        mainPanel.add(createBuildGraphPage(), "page1");
        mainPanel.add(createPage("صفحه نمایش گراف و زیرساخت"), "page2");
        mainPanel.add(createPage("صفحه الگوریتم‌ها و تحلیل‌ها"), "page3");
        mainPanel.add(createPage("صفحه پیشنهاد مسیر و رزرو هوشمند"), "page4");
        mainPanel.add(createPage("صفحه سفر چندمقصدی (TSP)"), "page5");
        mainPanel.add(createPage("صفحه مقیاس‌بندی و خوشه‌بندی"), "page6");

        // تنظیم اندازه ترجیحی برای mainPanel (کوچکتر از اندازه کامل مانیتور)
        mainPanel.setPreferredSize(new Dimension(1100, 700));

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        frame.getContentPane().add(scrollPane);

        // استفاده از اندازه متوسط و بدون MAXIMIZED
        frame.setSize(1100, 700); // یا مثلاً 900×600 بسته به نیاز
        frame.setLocationRelativeTo(null); // وسط صفحه باز شود
        frame.setVisible(true);
    }


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
                if (index == 6) {
                    System.exit(0);
                } else {
                    cardLayout.show(mainPanel, "page" + (index + 1));
                }
            });
        }
        return panel;
    }

    private static JPanel createPage(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(144, 238, 144));

        JLabel label = new JLabel(title, SwingConstants.CENTER);
        label.setFont(new Font("Tahoma", Font.BOLD, 18));
        panel.add(label, BorderLayout.CENTER);

        if (title.contains("الگوریتم‌ها")) {
            JButton showMSTButton = new JButton("نمایش MST");
            showMSTButton.addActionListener(e -> {
                List<UniPaths> mst = MSTCalculator.computeMST(universities, paths);
                graphPanel.setMSTEdges(mst);
                graphPanel.repaint();

            });
            JPanel topPanel = new JPanel();
            topPanel.setOpaque(false);
            topPanel.add(showMSTButton);
            panel.add(topPanel, BorderLayout.NORTH);
        }

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.setOpaque(false);
        JButton backButton = new JButton("بازگشت به منوی اصلی");
        backButton.addActionListener(e -> cardLayout.show(mainPanel, "menu"));
        bottomPanel.add(backButton);

        panel.add(bottomPanel, BorderLayout.SOUTH);
        return panel;
    }

    private static JPanel createBuildGraphPage() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(144, 238, 144));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        JTextField nameField = new JTextField(12);
        String[] regions = {"شمال", "جنوب", "شرق", "غرب", "مرکز"};
        JComboBox<String> regionField = new JComboBox<>(regions);
        JComboBox<Universities> fromBox = new JComboBox<>();
        JComboBox<Universities> toBox = new JComboBox<>();
        JTextField costField = new JTextField(10);
        JTextField startTimeField = new JTextField(10);
        JTextField endTimeField = new JTextField(10);
        JTextField capacityField = new JTextField(10);

        contentPanel.add(new JLabel("نام دانشگاه جدید:"));
        contentPanel.add(nameField);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        contentPanel.add(new JLabel("منطقه:"));
        contentPanel.add(regionField);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        JButton addUniBtn = new JButton("افزودن دانشگاه");
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
        contentPanel.add(addPathBtn);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        JButton backButton = new JButton("بازگشت به منوی اصلی");
        backButton.addActionListener(e -> cardLayout.show(mainPanel, "menu"));
        contentPanel.add(backButton);

        JScrollPane scrollPanel = new JScrollPane(contentPanel);
        scrollPanel.setPreferredSize(new Dimension(260, 700));

        panel.add(scrollPanel, BorderLayout.EAST);
        panel.add(graphPanel, BorderLayout.CENTER);

        addUniBtn.addActionListener(e -> {

            String name = nameField.getText();
            String region = (String) regionField.getSelectedItem();
            if (!name.isEmpty()) {
                Universities u = new Universities(name, region, 0, 0);
                universities.add(u);
                universityPositions.put(name, new Point(u.getX(), u.getY())); // ⬅ این خط ضروریه
                fromBox.addItem(u);
                toBox.addItem(u);
                nameField.setText("");
                graphPanel.repaint();
            }
        });

        addPathBtn.addActionListener(e -> {
            Universities from = (Universities) fromBox.getSelectedItem();
            Universities to = (Universities) toBox.getSelectedItem();
            try {
                int cost = Integer.parseInt(costField.getText());
                int startTime = Integer.parseInt(startTimeField.getText());
                int endTime = Integer.parseInt(endTimeField.getText());
                int capacity = Integer.parseInt(capacityField.getText());
                if (from != null && to != null && !from.equals(to)) {

                    UniPaths path = new UniPaths(startTime, endTime, cost, capacity, from.getUniversityName(), to.getUniversityName());
                    int flag = 0;
                    for (UniPaths p : paths){
                        if (p.getStartLocation().equals(path.getStartLocation()) && p.getEndLocation().equals(path.getEndLocation())) {
                            flag = 1;
                        }
                    }
                    if (flag == 0){
                        paths.add(path);
                    } else {
                        JOptionPane.showMessageDialog(panel, "بین این دو دانشگاه یک مسیر قبلی وجود دارد. میتوانید آن را ادیت بزنید");
                    }
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