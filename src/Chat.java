import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Chat {

    private JPanel chatPanel;
    private int chatWidth;

    public Chat(String width, String username, ObjectOutputStream objectOutputStream, ObjectInputStream objectInputStream, JLayeredPane layeredPane) {

        if (width.equals("4")) {
            chatWidth = 800;
        } else {
            chatWidth = 1120;
        }

        // Create chat
        chatPanel = new JPanel();
        chatPanel.setLayout(null);
        chatPanel.setOpaque(true);
        chatPanel.setBounds(chatWidth - 480, 0, 350, 640);
        chatPanel.setBackground(new Color(213,216,183));

        JTextArea sendingMessage = new JTextArea();
        sendingMessage.setEditable(true);
        sendingMessage.setBackground(new Color(249,247,232));
        sendingMessage.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 5));
        sendingMessage.setFont(new Font(null, Font.PLAIN, 20));
        sendingMessage.setForeground(Color.BLACK);
        JScrollPane scrollMassageToSend = new JScrollPane(sendingMessage);
        scrollMassageToSend.setBounds(0, 540, 270, 100); // Adjusted bounds to fit the button
        chatPanel.add(scrollMassageToSend);

        JTextArea message = new JTextArea();
        message.setEditable(false);
        message.setLineWrap(true);
        message.setBackground(new Color(213,216,183));
        message.setForeground(Color.black);
        message.setFont(new Font(null, Font.PLAIN, 20));
        message.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 5));
        JScrollPane massageScrollPane = new JScrollPane(message);
        massageScrollPane.setBounds(0, 0, 350, 540);
        chatPanel.add(massageScrollPane);

        JButton send = new JButton("Send");
        send.setForeground(Color.WHITE);
        send.setBackground(Color.DARK_GRAY);
        send.setFocusable(false);
        send.setBounds(270, 540, 80, 100); // Adjusted to span the height of the text area
        send.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String textFromTextArea = sendingMessage.getText();
                String trimmedMessage = textFromTextArea.trim();
                if (!trimmedMessage.isBlank()) {
                    try {
                        objectOutputStream.writeUTF(username + ": " + trimmedMessage);
                        objectOutputStream.flush();
                        message.append(username + ": " + trimmedMessage + "\n");
                        sendingMessage.setText("");
                        chatPanel.repaint();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });

        chatPanel.add(send);
        chatPanel.revalidate();
        chatPanel.repaint();
        layeredPane.add(chatPanel);

        new Thread(() -> {
            try {
                while (true) {
                    String messageString = objectInputStream.readUTF();
                    if (!messageString.isBlank()) {
                        message.append(messageString + "\n");
                        chatPanel.repaint();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}