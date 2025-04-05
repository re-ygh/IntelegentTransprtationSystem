import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MovementGUI extends JPanel implements ActionListener {
    private int panel_width;
    private int panel_height;
    private ImageIcon piece;
    private JLayeredPane layeredPane;
    private Timer timer;
    private int xVelocity;
    private int yVelocity;
    private int x = 0;
    private int y = 0;
    private JLabel pieceLabel;

    MovementGUI(ImageIcon piece, JLayeredPane layeredPane) {
        this.piece = piece;
        this.layeredPane = layeredPane;
        this.panel_width = layeredPane.getWidth();
        this.panel_height = layeredPane.getHeight();
        this.pieceLabel = new JLabel(piece);
        this.pieceLabel.setBounds(x, y, piece.getIconWidth(), piece.getIconHeight());
        this.layeredPane.add(pieceLabel, JLayeredPane.DEFAULT_LAYER);
        this.timer = new Timer(100, this);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(piece.getImage(), x, y, this);
    }

    public void movePiece(int targetX, int targetY) {
        xVelocity = (targetX - x) / 10; // Divide by 10 for smooth movement
        yVelocity = (targetY - y) / 10; // Divide by 10 for smooth movement
        timer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (Math.abs(x - pieceLabel.getX()) < Math.abs(xVelocity) && Math.abs(y - pieceLabel.getY()) < Math.abs(yVelocity)) {
            timer.stop();
            x = pieceLabel.getX();
            y = pieceLabel.getY();
        } else {
            x += xVelocity;
            y += yVelocity;
            pieceLabel.setLocation(x, y);
            layeredPane.repaint();
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(800, 600));
        frame.add(layeredPane);

        ImageIcon pieceIcon = new ImageIcon("path/to/your/image.png");
        MovementGUI movementGUI = new MovementGUI(pieceIcon, layeredPane);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        // Test movement
        movementGUI.movePiece(400, 300);
    }
}
