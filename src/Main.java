import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;


public class Main extends JFrame implements Board {

    JLayeredPane layeredPane;

    JButton width4;
    JButton width8;
    ServerSocket serverSocket;
    Socket socket;
    ServerSocket serverSocketChat;
    Socket socketChat;
    HostOrJoin hostOrJoin;
    String userName;
    public static JLabel previousGrayBackgroundLabel = null;
    public static Peace lastClickedPeace;
    int turn = 0;

    public static void main(String[] args) {
        new Main();
    }

    Main() {

        super("Chess Crusader");
        setResizable(false);
        getContentPane().setPreferredSize(new Dimension(320, 640));
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        layeredPane = new JLayeredPane();
        layeredPane.setSize(new Dimension(320, 640));
        layeredPane.setOpaque(true);
        layeredPane.setVisible(true);
        layeredPane.setBackground(Color.BLACK);

        hostAndJoin();

        add(layeredPane);
        layeredPane.repaint();

    }

    public void Gameplay(Object[][] gameboard, ObjectInputStream objectInputStream, ObjectOutputStream objectOutputStream, ServerSocket serverSocket, Socket socket) {


                                    setupMouseListeners(gameboard);

//        // Thread to handle incoming data
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while (true) {
//                    if (isMovedMethod()) {
//                        try {
//                            Object[][] receivedGameboard = (Object[][]) objectInputStream.readObject();
//                            int receivedTurn = objectInputStream.readInt();
//
//                            // Update UI on the Event Dispatch Thread
//                            SwingUtilities.invokeLater(new Runnable() {
//                                @Override
//                                public void run() {
//                                    setupMouseListeners(receivedGameboard);
//                                }
//                            });
//
//                        } catch (IOException | ClassNotFoundException e) {
//                            e.printStackTrace();
//                            break;
//                        }
//                    }
//                }
//            }
//        }).start();
//
//        // Main loop to handle outgoing data
//        while (true) {
//            try {
//                if (isMovedMethod()) {
//                    objectOutputStream.writeObject(gameboard);
//                    objectOutputStream.flush();
//
//                    objectOutputStream.writeInt(turn);
//                    objectOutputStream.flush();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//                break;
//            }
//        }
    }

    private boolean isMovedMethod() {
        if ((hostOrJoin == HostOrJoin.host && turn % 2 == 1) || (hostOrJoin == HostOrJoin.join && turn % 2 == 0)) {
            return true;
        } else {
            return false;
        }
    }

    private void setupMouseListeners(Object[][] gameboard) {
        Component[] components = layeredPane.getComponentsInLayer(0);
        for (Component component : components) {
            Rectangle bounds = component.getBounds();
            component.addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if ((turn % 2 == 0 && hostOrJoin == HostOrJoin.host) || (turn % 2 == 1 && hostOrJoin == HostOrJoin.join)) {
                        Peace.move(gameboard, lastClickedPeace, (bounds.y / 80), (bounds.x / 80), layeredPane, hostOrJoin);

                        // Combine the removal and repaint into one loop to optimize
                        for (Component comp : layeredPane.getComponentsInLayer(20)) {
                            layeredPane.remove(comp);
                        }
                        for (Component comp : layeredPane.getComponentsInLayer(10)) {
                            layeredPane.remove(comp);
                            previousGrayBackgroundLabel = null;
                            lastClickedPeace = null;
                        }
                        //fix
                        Peace.ability(gameboard);
                        setPowerLable(gameboard);
                        layeredPane.repaint();
//                        turn++;
                    }
                }

                @Override
                public void mousePressed(MouseEvent e) {
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                }

                @Override
                public void mouseExited(MouseEvent e) {
                }
            });
        }
    }

    @Override
    public void gameBoard(String gameTiles, ObjectInputStream objectInputStream, ObjectOutputStream objectOutputStream, ServerSocket serverSocket, Socket socket) {

        switch (gameTiles) {
            case "1":

                getContentPane().setPreferredSize(new Dimension(800, 640));
                layeredPane.setSize(new Dimension(800, 640));
                pack();

                // Fill screen with tiles
                int x = 0;
                int y = 0;
                int width = 80;
                int height = 80;
                int numberOfTiles = 320 * 640 / (width * height);

                for (int i = 0; i < numberOfTiles; i++) {

                    // Add ground tiles
                    int row = y / height;
                    int col = x / width;
                    JLabel tileLabel = new JLabel();
                    if (row == 7) {

                        String assetindex = switch (col) {
                            case 0 -> "02";
                            case 1 -> "10";
                            case 2 -> "11";
                            case 3 -> "12";
                            default -> throw new IllegalStateException("Unexpected value: " + col);
                        };
                        ImageIcon tileImage = new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("assets/" + assetindex + "_Chess Crusader.png")));
                        tileImage.setImage(tileImage.getImage().getScaledInstance(80, 80, 0));
                        tileLabel.setIcon(tileImage);
                        tileLabel.setBounds(x, y, width, height);

                        layeredPane.add(tileLabel, 0);
                    } else if (col == 0) {
                        ImageIcon tileImage = new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("assets/" + "0" + String.valueOf(9 - row) + "_Chess Crusader.png")));
                        tileImage.setImage(tileImage.getImage().getScaledInstance(80, 80, 0));
                        tileLabel.setIcon(tileImage);
                        tileLabel.setBounds(x, y, width, height);

                        layeredPane.add(tileLabel, 0);
                    } else {
                        String assetindex;
                        if ((row % 2 == 0) == (col % 2 == 0)) {
                            assetindex = "01";
                        } else {
                            assetindex = "00";
                        }
                        ImageIcon tileImage = new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("assets/" + assetindex + "_Chess Crusader.png")));
                        tileImage.setImage(tileImage.getImage().getScaledInstance(80, 80, 0));

                        tileLabel.setIcon(tileImage);
                        tileLabel.setBounds(x, y, width, height);

                        layeredPane.add(tileLabel, 0);
                    }

                    layeredPane.repaint(new Rectangle(x, y, width, height));

                    x += width;
                    if (x >= 320) {
                        x = 0;
                        y += height;
                    }
                }


                // Loop to add pieces to the board
                for (int i = 0; i < Gametiles1.length; i++) {
                    for (int j = 0; j < Gametiles1[i].length; j++) {
                        if (i == 0 && j == 0) {
                            addPieceToBoard(new Archer(2, 1, "cristian Archer", 0, 0, 0), j, i, 4);
                        } else if (i == 0 && j == 1) {
                            addPieceToBoard(new Castle(1, 1, "cristian Castle", 80, 0, 0), j, i, 4);
                        } else if (i == 0 && j == 2) {
                            addPieceToBoard(new Catapult(0, 1, "cristian Catapult", 160, 0, 0), j, i, 4);
                        } else if (i == 0 && j == 3) {
                            addPieceToBoard(new Knight(1, 2, "cristian Knight", 240, 0, 0), j, i, 4);
                        } else if (i == 1 && j == 0) {
                            addPieceToBoard(new Sarbaz(1, 1, "cristian Soldier", 0, 80, 0), j, i, 4);
                        } else if (i == 1 && j == 1) {
                            addPieceToBoard(new Sarbaz(1, 1, "cristian Soldier", 80, 80, 0), j, i, 4);
                        } else if (i == 1 && j == 2) {
                            addPieceToBoard(new Sarbaz(1, 1, "cristian Soldier", 160, 80, 0), j, i, 4);
                        } else if (i == 1 && j == 3) {
                            addPieceToBoard(new Sarbaz(1, 1, "cristian Soldier", 240, 80, 0), j, i, 4);
                        } else if (i == 6 && j == 0) {
                            addPieceToBoard(new Sarbaz(1, 1, "muslim Soldier", 0, 480, 1), j, i, 4);
                        } else if (i == 6 && j == 1) {
                            addPieceToBoard(new Sarbaz(1, 1, "muslim Soldier", 80, 480, 1), j, i, 4);
                        } else if (i == 6 && j == 2) {
                            addPieceToBoard(new Sarbaz(1, 1, "muslim Soldier", 160, 480, 1), j, i, 4);
                        } else if (i == 6 && j == 3) {
                            addPieceToBoard(new Sarbaz(1, 1, "muslim Soldier", 240, 480, 1), j, i, 4);
                        } else if (i == 7 && j == 0) {
                            addPieceToBoard(new Knight(1, 2, "muslim Knight", 0, 560, 1), j, i, 4);
                        } else if (i == 7 && j == 1) {
                            addPieceToBoard(new Catapult(0, 1, "muslim Catapult", 80, 560, 1), j, i, 4);
                        } else if (i == 7 && j == 2) {
                            addPieceToBoard(new Castle(1, 1, "muslim Castle", 160, 560, 1), j, i, 4);
                        } else if (i == 7 && j == 3) {
                            addPieceToBoard(new Archer(2, 1, "muslim Archer", 240, 560, 1), j, i, 4);
                        }
                    }
                }


                Peace.ability(Gametiles1);


                // adding peaces powers to the panel
                setPowerLable(Gametiles1);
                layeredPane.repaint();

                Gameplay(Gametiles1, objectInputStream, objectOutputStream, serverSocket, socket);

                break;
            case "2":

                getContentPane().setPreferredSize(new Dimension(1120, 640));
                pack();
                layeredPane.setSize(new Dimension(1120, 640));


                // Fill screen with tiles
                int x2 = 0;
                int y2 = 0;
                int width2 = 80;
                int height2 = 80;
                int numberOfTiles2 = 640 * 640 / (width2 * height2);

                for (int i2 = 0; i2 < numberOfTiles2; i2++) {

                    // Add ground tiles
                    int row2 = y2 / height2;
                    int col2 = x2 / width2;
                    JLabel tileLabel2;

                    if (row2 == 7) {

                        String assetindex2 = switch (col2) {
                            case 0 -> "02";
                            case 1 -> "10";
                            case 2 -> "11";
                            case 3 -> "12";
                            case 4 -> "36";
                            case 5 -> "37";
                            case 6 -> "38";
                            case 7 -> "39";
                            default -> throw new IllegalStateException("Unexpected value: " + col2);
                        };

                        ImageIcon tileImage2 = null;

                        if (col2 < 4) {
                            tileImage2 = new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("assets/" + assetindex2 + "_Chess Crusader.png")));
                        } else {
                            tileImage2 = new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("assets/" + assetindex2 + "_Chess Crusader.jpg")));
                        }

                        tileImage2.setImage(tileImage2.getImage().getScaledInstance(width2, height2, 0));
                        tileLabel2 = new JLabel(tileImage2);
                        tileLabel2.setBounds(x2, y2, width2, height2);

                        layeredPane.add(tileLabel2, 0);
                    } else if (col2 == 0) {
                        ImageIcon tileImage2 = new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("assets/" + "0" + String.valueOf(9 - row2) + "_Chess Crusader.png")));
                        tileImage2.setImage(tileImage2.getImage().getScaledInstance(width2, height2, 0));
                        tileLabel2 = new JLabel(tileImage2);
                        tileLabel2.setBounds(x2, y2, width2, height2);

                        layeredPane.add(tileLabel2, 0);
                    } else {
                        String assetindex2;
                        if ((row2 % 2 == 0) == (col2 % 2 == 0)) {
                            assetindex2 = "01";
                        } else {
                            assetindex2 = "00";
                        }
                        ImageIcon tileImage2 = new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("assets/" + assetindex2 + "_Chess Crusader.png")));
                        tileImage2.setImage(tileImage2.getImage().getScaledInstance(width2, height2, 0));
                        tileLabel2 = new JLabel(tileImage2);
                        tileLabel2.setBounds(x2, y2, width2, height2);

                        layeredPane.add(tileLabel2, 0);
                    }


                    layeredPane.repaint(new Rectangle(x2, y2, width2, height2));
                    x2 += width2;
                    if (x2 >= 640) {
                        x2 = 0;
                        y2 += height2;
                    }
                }


                for (int i = 0; i < Gametiles2.length; i++) {
                    for (int j = 0; j < Gametiles2[i].length; j++) {
                        if (i == 0 && j == 0) {
                            addPieceToBoard(new Archer(2, 1, "cristian Archer", 0, 0, 0), j, i, 8);
                        } else if (i == 0 && j == 1) {
                            addPieceToBoard(new Knight(1, 2, "cristian Knight", 80, 0, 0), j, i, 8);
                        } else if (i == 0 && j == 2) {
                            addPieceToBoard(new Archer(2, 1, "cristian Archer", 160, 0, 0), j, i, 8);
                        } else if (i == 0 && j == 3) {
                            addPieceToBoard(new Castle(1, 1, "cristian Castle", 240, 0, 0), j, i, 8);
                        } else if (i == 0 && j == 4) {
                            addPieceToBoard(new Catapult(0, 1, "cristian Catapult", 320, 0, 0), j, i, 8);
                        } else if (i == 0 && j == 5) {
                            addPieceToBoard(new Archer(2, 1, "cristian Archer", 400, 0, 0), j, i, 8);
                        } else if (i == 0 && j == 6) {
                            addPieceToBoard(new Knight(1, 2, "cristian Knight", 480, 0, 0), j, i, 8);
                        } else if (i == 0 && j == 7) {
                            addPieceToBoard(new Archer(2, 1, "cristian Archer", 560, 0, 0), j, i, 8);
                        } else if (i == 1 && j == 0) {
                            addPieceToBoard(new Sarbaz(1, 1, "cristian Soldier", 0, 80, 0), j, i, 8);
                        } else if (i == 1 && j == 1) {
                            addPieceToBoard(new Sarbaz(1, 1, "cristian Soldier", 80, 80, 0), j, i, 8);
                        } else if (i == 1 && j == 2) {
                            addPieceToBoard(new Sarbaz(1, 1, "cristian Soldier", 160, 80, 0), j, i, 8);
                        } else if (i == 1 && j == 3) {
                            addPieceToBoard(new Sarbaz(1, 1, "cristian Soldier", 240, 80, 0), j, i, 8);
                        } else if (i == 1 && j == 4) {
                            addPieceToBoard(new Sarbaz(1, 1, "cristian Soldier", 320, 80, 0), j, i, 8);
                        } else if (i == 1 && j == 5) {
                            addPieceToBoard(new Sarbaz(1, 1, "cristian Soldier", 400, 80, 0), j, i, 8);
                        } else if (i == 1 && j == 6) {
                            addPieceToBoard(new Sarbaz(1, 1, "cristian Soldier", 480, 80, 0), j, i, 8);
                        } else if (i == 1 && j == 7) {
                            addPieceToBoard(new Sarbaz(1, 1, "cristian Soldier", 560, 80, 0), j, i, 8);
                        } else if (i == 6 && j == 0) {
                            addPieceToBoard(new Sarbaz(1, 1, "muslim Soldier", 0, 480, 1), j, i, 8);
                        } else if (i == 6 && j == 1) {
                            addPieceToBoard(new Sarbaz(1, 1, "muslim Soldier", 80, 480, 1), j, i, 8);
                        } else if (i == 6 && j == 2) {
                            addPieceToBoard(new Sarbaz(1, 1, "muslim Soldier", 160, 480, 1), j, i, 8);
                        } else if (i == 6 && j == 3) {
                            addPieceToBoard(new Sarbaz(1, 1, "muslim Soldier", 240, 480, 1), j, i, 8);
                        } else if (i == 6 && j == 4) {
                            addPieceToBoard(new Sarbaz(1, 1, "muslim Soldier", 320, 480, 1), j, i, 8);
                        } else if (i == 6 && j == 5) {
                            addPieceToBoard(new Sarbaz(1, 1, "muslim Soldier", 400, 480, 1), j, i, 8);
                        } else if (i == 6 && j == 6) {
                            addPieceToBoard(new Sarbaz(1, 1, "muslim Soldier", 480, 480, 1), j, i, 8);
                        } else if (i == 6 && j == 7) {
                            addPieceToBoard(new Sarbaz(1, 1, "muslim Soldier", 560, 480, 1), j, i, 8);
                        } else if (i == 7 && j == 0) {
                            addPieceToBoard(new Archer(2, 1, "muslim Archer", 0, 560, 1), j, i, 8);
                        } else if (i == 7 && j == 1) {
                            addPieceToBoard(new Knight(1, 2, "muslim Knight", 80, 560, 1), j, i, 8);
                        } else if (i == 7 && j == 2) {
                            addPieceToBoard(new Archer(2, 1, "muslim Archer", 160, 560, 1), j, i, 8);
                        } else if (i == 7 && j == 3) {
                            addPieceToBoard(new Catapult(0, 1, "muslim Catapult", 240, 560, 1), j, i, 8);
                        } else if (i == 7 && j == 4) {
                            addPieceToBoard(new Castle(1, 1, "muslim Castle", 320, 560, 1), j, i, 8);
                        } else if (i == 7 && j == 5) {
                            addPieceToBoard(new Archer(2, 1, "muslim Archer", 400, 560, 1), j, i, 8);
                        } else if (i == 7 && j == 6) {
                            addPieceToBoard(new Knight(1, 2, "muslim Knight", 480, 560, 1), j, i, 8);
                        } else if (i == 7 && j == 7) {
                            addPieceToBoard(new Archer(2, 1, "muslim Archer", 560, 560, 1), j, i, 8);
                        }
                    }
                }

                Peace.ability(Gametiles2);

                // adding peaces powers to the panel
                setPowerLable(Gametiles2);
                layeredPane.repaint();

                Gameplay(Gametiles2, objectInputStream, objectOutputStream, serverSocket, socket);

                break;
        }

    }

    public void setPowerLable(Object[][] gameboard) {

        Component[] components = layeredPane.getComponentsInLayer(50);
        for (Component component : components) {
            layeredPane.remove(component);
        }
        layeredPane.repaint();

        for (int i = 0; i < gameboard.length; i++) {
            for (int j = 0; j < gameboard[i].length; j++) {
                if (gameboard[i][j] instanceof Peace) {
                    int power = ((Peace) gameboard[i][j]).getPower();
                    if (power == 0) {

                        ImageIcon powerImage = new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("assets/" + 26 + "_Chess Crusader.png")));
                        powerImage.getImage().getScaledInstance(80, 80, 0);
                        JLabel powerLable = new JLabel();
                        powerLable.setIcon(powerImage);
                        powerLable.setBounds((j * 80), (i * 80), 80, 80);

                        layeredPane.add(powerLable, Integer.valueOf(50));
                        layeredPane.repaint();

                    } else if (power == 1) {

                        ImageIcon powerImage = new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("assets/" + 27 + "_Chess Crusader.png")));
                        powerImage.getImage().getScaledInstance(80, 80, 0);
                        JLabel powerLable = new JLabel();
                        powerLable.setIcon(powerImage);
                        powerLable.setBounds((j * 80), (i * 80), 80, 80);

                        layeredPane.add(powerLable, Integer.valueOf(50));
                        layeredPane.repaint();

                    } else if (power == 2) {

                        ImageIcon powerImage = new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("assets/" + 28 + "_Chess Crusader.png")));
                        powerImage.getImage().getScaledInstance(80, 80, 0);
                        JLabel powerLable = new JLabel();
                        powerLable.setIcon(powerImage);
                        powerLable.setBounds((j * 80), (i * 80), 80, 80);

                        layeredPane.add(powerLable, Integer.valueOf(50));
                        layeredPane.repaint();

                    } else if (power == 3) {

                        ImageIcon powerImage = new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("assets/" + 29 + "_Chess Crusader.png")));
                        powerImage.getImage().getScaledInstance(80, 80, 0);
                        JLabel powerLable = new JLabel();
                        powerLable.setIcon(powerImage);
                        powerLable.setBounds((j * 80), (i * 80), 80, 80);

                        layeredPane.add(powerLable, Integer.valueOf(50));
                        layeredPane.repaint();

                    } else if (power == 4) {

                        ImageIcon powerImage = new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("assets/" + 30 + "_Chess Crusader.png")));
                        powerImage.getImage().getScaledInstance(80, 80, 0);
                        JLabel powerLable = new JLabel();
                        powerLable.setIcon(powerImage);
                        powerLable.setBounds((j * 80), (i * 80), 80, 80);

                        layeredPane.add(powerLable, Integer.valueOf(50));
                        layeredPane.repaint();

                    } else if (power == 5) {

                        ImageIcon powerImage = new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("assets/" + 31 + "_Chess Crusader.png")));
                        powerImage.getImage().getScaledInstance(80, 80, 0);
                        JLabel powerLable = new JLabel();
                        powerLable.setIcon(powerImage);
                        powerLable.setBounds((j * 80), (i * 80), 80, 80);

                        layeredPane.add(powerLable, Integer.valueOf(50));
                        layeredPane.repaint();

                    } else if (power == 6) {

                        ImageIcon powerImage = new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("assets/" + 32 + "_Chess Crusader.png")));
                        powerImage.getImage().getScaledInstance(80, 80, 0);
                        JLabel powerLable = new JLabel();
                        powerLable.setIcon(powerImage);
                        powerLable.setBounds((j * 80), (i * 80), 80, 80);

                        layeredPane.add(powerLable, Integer.valueOf(50));
                        layeredPane.repaint();

                    } else if (power == 7) {

                        ImageIcon powerImage = new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("assets/" + 33 + "_Chess Crusader.png")));
                        powerImage.getImage().getScaledInstance(80, 80, 0);
                        JLabel powerLable = new JLabel();
                        powerLable.setIcon(powerImage);
                        powerLable.setBounds((j * 80), (i * 80), 80, 80);

                        layeredPane.add(powerLable, Integer.valueOf(50));
                        layeredPane.repaint();

                    } else if (power == 8) {

                        ImageIcon powerImage = new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("assets/" + 34 + "_Chess Crusader.png")));
                        powerImage.getImage().getScaledInstance(80, 80, 0);
                        JLabel powerLable = new JLabel();
                        powerLable.setIcon(powerImage);
                        powerLable.setBounds((j * 80), (i * 80), 80, 80);

                        layeredPane.add(powerLable, Integer.valueOf(50));
                        layeredPane.repaint();

                    } else if (power == 9) {

                        ImageIcon powerImage = new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("assets/" + 35 + "_Chess Crusader.png")));
                        powerImage.getImage().getScaledInstance(80, 80, 0);
                        JLabel powerLable = new JLabel();
                        powerLable.setIcon(powerImage);
                        powerLable.setBounds((j * 80), (i * 80), 80, 80);

                        layeredPane.add(powerLable, Integer.valueOf(50));
                        layeredPane.repaint();

                    }

                }
            }
        }

    }

    public void hostAndJoin() {
        JTextField namefield = new JTextField("UserName:");
        namefield.setFont(new Font(null, Font.PLAIN, 20));
        namefield.setBackground(Color.lightGray);
        namefield.setBorder(BorderFactory.createRaisedBevelBorder());
        namefield.setBounds(60, 320, 200, 40);

        JButton Host = new JButton("Host");
        Host.setFocusable(false); // Disable focus on the button
        Host.setFont(new Font(null, Font.PLAIN, 20));
        Host.setBackground(Color.lightGray);
        Host.setBorder(BorderFactory.createRaisedBevelBorder());
        Host.setBounds(60, 200, 200, 40);
        Host.setEnabled(false); // Initially disabled
        Host.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (serverSocket == null || serverSocket.isClosed()) {
                    try {
                        serverSocket = new ServerSocket(8080);
                        serverSocketChat = new ServerSocket(1010);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Socket socket = serverSocket.accept();
                                    Socket socketChat = serverSocketChat.accept();
                                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                                    ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                                    ObjectOutputStream objectOutputStreamChat = new ObjectOutputStream(socketChat.getOutputStream());
                                    ObjectInputStream objectInputStreamChat = new ObjectInputStream(socketChat.getInputStream());

                                    // Update UI
                                    SwingUtilities.invokeLater(() -> {
                                        layeredPane.removeAll();
                                        chooseWidth(objectInputStream, objectOutputStream, objectInputStreamChat, objectOutputStreamChat);
                                        layeredPane.repaint();
                                    });

                                    hostOrJoin = HostOrJoin.host;
                                    userName = namefield.getText();
                                    Host.setEnabled(false);
                                } catch (IOException ex) {
                                    JOptionPane.showMessageDialog(null, "Failed to start the server. Please try again.");
                                    ex.printStackTrace();
                                }
                            }
                        }).start();
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(null, "Failed to start the server. Please try again.");
                        ex.printStackTrace();
                    }
                }
            }
        });

        JButton Join = new JButton("Join");
        Join.setFocusable(false); // Disable focus on the button
        Join.setFont(new Font(null, Font.PLAIN, 20));
        Join.setBackground(Color.lightGray);
        Join.setBorder(BorderFactory.createRaisedBevelBorder());
        Join.setBounds(60, 260, 200, 40);
        Join.setEnabled(false); // Initially disabled
        Join.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            socket = new Socket("localhost", 8080);
                            socketChat = new Socket("localhost", 1010);
                            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                            ObjectInputStream objectInputStreamChat = new ObjectInputStream(socketChat.getInputStream());
                            ObjectOutputStream objectOutputStreamChat = new ObjectOutputStream(socketChat.getOutputStream());

                            SwingUtilities.invokeLater(() -> {
                                layeredPane.removeAll();
                                JLabel waitLabel = new JLabel("Wait for the host to choose");
                                waitLabel.setFont(new Font(null, Font.PLAIN, 20));
                                waitLabel.setBackground(Color.lightGray);
                                waitLabel.setBorder(BorderFactory.createRaisedBevelBorder());
                                waitLabel.setBounds(33, 300, 250, 50);
                                waitLabel.setOpaque(true);
                                layeredPane.add(waitLabel);
                                layeredPane.repaint();
                            });

                            String width = objectInputStream.readUTF();
                            String finalWidth = width;
                            SwingUtilities.invokeLater(() -> {
                                layeredPane.removeAll();
                                layeredPane.repaint();
                                gameBoard(finalWidth, objectInputStream, objectOutputStream, serverSocket, socket);
                            });

                            hostOrJoin = HostOrJoin.join;
                            userName = namefield.getText();
                            if (width.equals("1")) {
                                width = "4";
                            } else {
                                width = "8";
                            }
                            new Chat(width, userName, objectOutputStreamChat, objectInputStreamChat, layeredPane);


                            JPanel panel = new JPanel();
                            panel.setBackground(Color.gray);
                            if (width.equals("4")) {
                                panel.setBounds(640, 0, 160, 640);
                            } else {
                                panel.setBounds(960, 0, 240, 640);
                            }
                            layeredPane.add(panel);

                            AudioControl audioControl = new AudioControl();
                            audioControl.backMusic(panel,hostOrJoin);

                        } catch (IOException e) {
                            JOptionPane.showMessageDialog(null, "Failed to connect to server. Please try again.");
                            e.printStackTrace();
                        }
                    }

                }).start();
            }
        });

        namefield.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (namefield.getText().equals("UserName:")) {
                    if (Character.isLetterOrDigit(e.getKeyChar())) {
                        namefield.setText("");
                        // Enable buttons after user starts typing
                        Host.setEnabled(true);
                        Join.setEnabled(true);
                    } else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE ||
                            e.getKeyCode() == KeyEvent.VK_LEFT ||
                            e.getKeyCode() == KeyEvent.VK_RIGHT) {
                        // Allow backspace, left arrow, and right arrow keys
                    } else {
                        e.consume();
                    }
                } else {
                    if (!Character.isLetterOrDigit(e.getKeyChar())) {
                        if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE ||
                                e.getKeyCode() == KeyEvent.VK_LEFT ||
                                e.getKeyCode() == KeyEvent.VK_RIGHT) {
                            // Allow backspace, left arrow, and right arrow keys
                        } else {
                            e.consume();
                        }
                    }
                }
            }
        });

        layeredPane.add(namefield);
        layeredPane.add(Host);
        layeredPane.add(Join);
        layeredPane.repaint();
    }

    public void chooseWidth(ObjectInputStream objectInputStream, ObjectOutputStream objectOutputStream, ObjectInputStream objectInputStreamChat, ObjectOutputStream objectOutputStreamChat) {
        width4 = new JButton("start with width = 4");
        width4.setFont(new Font(null, Font.PLAIN, 20));
        width4.setBackground(Color.lightGray);
        width4.setFocusable(false);
        width4.setBorder(BorderFactory.createRaisedBevelBorder());
        width4.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                layeredPane.removeAll();
                try {
                    objectOutputStream.writeUTF("1");
                    objectOutputStream.flush();
                } catch (IOException e3) {
                    e3.printStackTrace();
                }
                new Chat("4", userName, objectOutputStreamChat, objectInputStreamChat, layeredPane);
                gameBoard("1", objectInputStream, objectOutputStream, serverSocket, socket);

                JPanel panel = new JPanel();
                panel.setBackground(Color.gray);
                panel.setBounds(640, 0, 160, 640);
                layeredPane.add(panel);

                AudioControl audioControl = new AudioControl();
                audioControl.backMusic(panel,hostOrJoin);
            }
        });
        width4.setBounds(60, 200, 200, 40);

        width8 = new JButton("start with width = 8");
        width8.setFont(new Font(null, Font.PLAIN, 20));
        width8.setBackground(Color.lightGray);
        width8.setFocusable(false);
        width8.setBorder(BorderFactory.createRaisedBevelBorder());
        width8.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                layeredPane.removeAll();
                try {
                    objectOutputStream.writeUTF("2");
                    objectOutputStream.flush();
                } catch (IOException e3) {
                    e3.printStackTrace();
                }
                new Chat("8", userName, objectOutputStreamChat, objectInputStreamChat, layeredPane);
                gameBoard("2", objectInputStream, objectOutputStream, serverSocket, socket);

                JPanel panel = new JPanel();
                panel.setBackground(Color.gray);
                panel.setBounds(960, 0, 240, 640);
                layeredPane.add(panel);

                AudioControl audioControl = new AudioControl();
                audioControl.backMusic(panel,hostOrJoin);
            }
        });
        width8.setBounds(60, 260, 200, 40);

        layeredPane.add(width4);
        layeredPane.add(width8);
    }


    // Method to add mouse listener
    private void addPieceMouseListener(Peace piece, int x, int y, Object[][] gameboard) {
        piece.getPeaceLable().addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Handle mouse click event
                handleMouseClick(x, y, piece, gameboard);
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
            // Other overridden methods here (mousePressed, mouseReleased, mouseEntered, mouseExited)
        });
    }

    // Method to handle mouse click events
    private void handleMouseClick(int x, int y, Peace mainPeice, Object[][] gameboard) {

        if (lastClickedPeace == null) {
            if ((mainPeice.getWhichside() == 0 && hostOrJoin == HostOrJoin.host) || (mainPeice.getWhichside() == 1 && hostOrJoin == HostOrJoin.join)) {

                //adding gray background to the clicked tile
                ImageIcon grayBackgroundIcon = new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("assets/" + 13 + "_Chess Crusader.png")));
                grayBackgroundIcon.getImage().getScaledInstance(80, 80, 0);
                JLabel grayBackgroundLable = new JLabel();
                grayBackgroundLable.setIcon(grayBackgroundIcon);
                grayBackgroundLable.setBounds((x * 80), (y * 80), 80, 80);


                // Remove the previous gray background if it exists
                if (previousGrayBackgroundLabel == null) {
                    // Add the new gray background to the layeredPane
                    layeredPane.add(grayBackgroundLable, Integer.valueOf(10));
                    layeredPane.repaint();

                    // Update the previousGrayBackgroundLabel to the current one
                    previousGrayBackgroundLabel = grayBackgroundLable;
                } else if (previousGrayBackgroundLabel != null && previousGrayBackgroundLabel.getBounds().equals(grayBackgroundLable.getBounds())) {

                    layeredPane.remove(previousGrayBackgroundLabel);
                    previousGrayBackgroundLabel = null;
                    layeredPane.repaint();
                } else if (previousGrayBackgroundLabel != null && !previousGrayBackgroundLabel.getBounds().equals(grayBackgroundLable.getBounds())) {


                    layeredPane.remove(previousGrayBackgroundLabel);
                    // Add the new gray background to the layeredPane
                    layeredPane.add(grayBackgroundLable, Integer.valueOf(10));
                    layeredPane.repaint();

                    // Update the previousGrayBackgroundLabel to the current one
                    previousGrayBackgroundLabel = grayBackgroundLable;

                }


                //checking the near tiles to see if there is any instances of Peace class
                int[][] directions = {{-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1}};
                int[][] directions2 = {{-2, -2}, {-2, 0}, {-2, 2}, {-1, -1}, {-1, 0}, {-1, 1}, {0, -2}, {0, -1}, {0, 0}, {0, 1}, {0, 2}, {1, -1}, {1, 0}, {1, 1}, {2, -2}, {2, 0}, {2, 2}};


                //removing all attacking gray or red dot in the back ground
                Component[] components = layeredPane.getComponentsInLayer(20);
                for (Component component : components) {
                    layeredPane.remove(component);
                }
                layeredPane.repaint();

                int finalI = y;
                int finalJ = x;
                //peace object for checking the side of the peaces
                Peace peace = (Peace) gameboard[finalI][finalJ];
                for (int[] dir : directions) {
                    if (peace.getPeaceTag().equals("cristian Soldier")) {
                        if (dir[0] != 1) {
                            continue;
                        }
                    }
                    if (peace.getPeaceTag().equals("muslim Soldier")) {
                        if (dir[0] != -1) {
                            continue;
                        }
                    }

                    int newRow = finalI + dir[0];
                    int newCol = finalJ + dir[1];

                    if (Peace.isValidPosition(gameboard, newRow, newCol)) {

                        ImageIcon grayAttackBackgroundIcon = new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("assets/" + 14 + "_Chess Crusader.png")));
                        grayAttackBackgroundIcon.getImage().getScaledInstance(40, 40, 0);
                        JLabel grayAttackBackgroundLabel = new JLabel();
                        grayAttackBackgroundLabel.setIcon(grayAttackBackgroundIcon);
                        grayAttackBackgroundLabel.setBounds((newCol * 80), (newRow * 80), 80, 80);

                        //red background if power is not enough to attack
                        ImageIcon redAttackBackgroundIcon = new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("assets/" + 40 + "_Chess Crusader.jpg")));
                        redAttackBackgroundIcon.getImage().getScaledInstance(40, 40, 0);
                        JLabel redAttackBackgroundLabel = new JLabel();
                        redAttackBackgroundLabel.setIcon(redAttackBackgroundIcon);
                        redAttackBackgroundLabel.setBounds((newCol * 80), (newRow * 80), 80, 80);

                        //enemy peaces to attack
                        if (!(peace instanceof Knight)) {

                            if (gameboard[newRow][newCol] instanceof Peace) {

                                if (((Peace) gameboard[newRow][newCol]).getWhichside() != peace.getWhichside()) {

                                    if (((Peace) gameboard[newRow][newCol]).getPower() <= peace.getPower()) {

                                        if (!peace.getPeaceTag().equals("cristian Castle") || !peace.getPeaceTag().equals("muslim Castle")) {

                                            layeredPane.add(redAttackBackgroundLabel, Integer.valueOf(20));
                                            lastClickedPeace = peace;
                                        }

                                    } else {
                                        lastClickedPeace = peace;
                                    }
                                }
                            } else {
                                layeredPane.add(grayAttackBackgroundLabel, Integer.valueOf(20));
                                lastClickedPeace = peace;
                            }
                        }
                    }
                }


                //for knights because of one more extra move capacity
                if (peace instanceof Knight) {
                    for (int[] dir2 : directions2) {

                        int newRow = finalI + dir2[0];
                        int newCol = finalJ + dir2[1];

                        if (Peace.isValidPosition(gameboard, newRow, newCol)) {
                            ImageIcon grayAttackBackgroundIcon = new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("assets/" + 14 + "_Chess Crusader.png")));
                            grayAttackBackgroundIcon.getImage().getScaledInstance(40, 40, 0);
                            JLabel grayAttackBackgroundLabel = new JLabel();
                            grayAttackBackgroundLabel.setIcon(grayAttackBackgroundIcon);
                            grayAttackBackgroundLabel.setBounds((newCol * 80), (newRow * 80), 80, 80);

                            //red background if power is not enough to attack
                            ImageIcon redAttackBackgroundIcon = new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("assets/" + 40 + "_Chess Crusader.jpg")));
                            redAttackBackgroundIcon.getImage().getScaledInstance(40, 40, 0);
                            JLabel redAttackBackgroundLabel = new JLabel();
                            redAttackBackgroundLabel.setIcon(redAttackBackgroundIcon);
                            redAttackBackgroundLabel.setBounds((newCol * 80), (newRow * 80), 80, 80);


                            if (gameboard[newRow][newCol] instanceof Peace) {

                                if (((Peace) gameboard[newRow][newCol]).getWhichside() != peace.getWhichside()) {

                                    if (((Peace) gameboard[newRow][newCol]).getPower() <= peace.getPower()) {

                                        if (!peace.getPeaceTag().equals("cristian Castle") || !peace.getPeaceTag().equals("muslim Castle")) {

                                            layeredPane.add(redAttackBackgroundLabel, Integer.valueOf(20));
                                            lastClickedPeace = peace;
                                        }

                                    } else {
                                        lastClickedPeace = peace;
                                    }
                                }
                            } else {
                                layeredPane.add(grayAttackBackgroundLabel, Integer.valueOf(20));
                                lastClickedPeace = peace;
                            }
                        }
                    }
                }
                layeredPane.repaint();

                //piece clicked audio
                AudioControl AC= new AudioControl();
                AC.pieceClicked(peace);
            }
        } else {
            if (mainPeice.getWhichside() == lastClickedPeace.getWhichside()) {
                //adding gray background to the clicked tile
                ImageIcon grayBackgroundIcon = new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("assets/" + 13 + "_Chess Crusader.png")));
                grayBackgroundIcon.getImage().getScaledInstance(80, 80, 0);
                JLabel grayBackgroundLable = new JLabel();
                grayBackgroundLable.setIcon(grayBackgroundIcon);
                grayBackgroundLable.setBounds((x * 80), (y * 80), 80, 80);

                // Remove the previous gray background if it exists
                if (previousGrayBackgroundLabel.getBounds().equals(grayBackgroundLable.getBounds())) {

                    layeredPane.remove(previousGrayBackgroundLabel);
                    previousGrayBackgroundLabel = null;
                    layeredPane.repaint();

                } else if (!previousGrayBackgroundLabel.getBounds().equals(grayBackgroundLable.getBounds())) {


                    layeredPane.remove(previousGrayBackgroundLabel);
                    // Add the new gray background to the layeredPane
                    layeredPane.add(grayBackgroundLable, Integer.valueOf(10));
                    layeredPane.repaint();

                    // Update the previousGrayBackgroundLabel to the current one
                    previousGrayBackgroundLabel = grayBackgroundLable;

                }


                //checking the near tiles to see if there is any instances of Peace class
                int[][] directions = {{-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1}};
                int[][] directions2 = {{-2, -2}, {-2, 0}, {-2, 2}, {-1, -1}, {-1, 0}, {-1, 1}, {0, -2}, {0, -1}, {0, 0}, {0, 1}, {0, 2}, {1, -1}, {1, 0}, {1, 1}, {2, -2}, {2, 0}, {2, 2}};


                //removing all attacking gray or red dot in the back ground
                Component[] components = layeredPane.getComponentsInLayer(20);
                for (Component component : components) {
                    layeredPane.remove(component);
                }
                layeredPane.repaint();


                if (previousGrayBackgroundLabel == null) {

                    lastClickedPeace = null;

                } else {

                    int finalI = y;
                    int finalJ = x;
                    //peace object for checking the side of the peaces
                    Peace peace = (Peace) gameboard[finalI][finalJ];
                    for (int[] dir : directions) {
                        if (peace.getPeaceTag().equals("cristian Soldier")) {
                            if (dir[0] != 1) {
                                continue;
                            }
                        }
                        if (peace.getPeaceTag().equals("muslim Soldier")) {
                            if (dir[0] != -1) {
                                continue;
                            }
                        }

                        int newRow = finalI + dir[0];
                        int newCol = finalJ + dir[1];

                        if (Peace.isValidPosition(gameboard, newRow, newCol)) {

                            ImageIcon grayAttackBackgroundIcon = new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("assets/" + 14 + "_Chess Crusader.png")));
                            grayAttackBackgroundIcon.getImage().getScaledInstance(40, 40, 0);
                            JLabel grayAttackBackgroundLabel = new JLabel();
                            grayAttackBackgroundLabel.setIcon(grayAttackBackgroundIcon);
                            grayAttackBackgroundLabel.setBounds((newCol * 80), (newRow * 80), 80, 80);

                            //red background if power is not enough to attack
                            ImageIcon redAttackBackgroundIcon = new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("assets/" + 40 + "_Chess Crusader.jpg")));
                            redAttackBackgroundIcon.getImage().getScaledInstance(40, 40, 0);
                            JLabel redAttackBackgroundLabel = new JLabel();
                            redAttackBackgroundLabel.setIcon(redAttackBackgroundIcon);
                            redAttackBackgroundLabel.setBounds((newCol * 80), (newRow * 80), 80, 80);

                            //enemy peaces to attack
                            if (!(peace instanceof Knight)) {

                                if (gameboard[newRow][newCol] instanceof Peace) {

                                    if (((Peace) gameboard[newRow][newCol]).getWhichside() != peace.getWhichside()) {

                                        if (((Peace) gameboard[newRow][newCol]).getPower() <= peace.getPower()) {

                                            if (!peace.getPeaceTag().equals("cristian Castle") || !peace.getPeaceTag().equals("muslim Castle")) {

                                                layeredPane.add(redAttackBackgroundLabel, Integer.valueOf(20));
                                                lastClickedPeace = peace;
                                            }

                                        } else {
                                            lastClickedPeace = peace;
                                        }
                                    }
                                } else {
                                    layeredPane.add(grayAttackBackgroundLabel, Integer.valueOf(20));
                                    lastClickedPeace = peace;
                                }
                            }
                        }
                    }


                    //for knights because of one more extra move capacity
                    if (peace instanceof Knight) {
                        for (int[] dir2 : directions2) {

                            int newRow = finalI + dir2[0];
                            int newCol = finalJ + dir2[1];

                            if (Peace.isValidPosition(gameboard, newRow, newCol)) {
                                ImageIcon grayAttackBackgroundIcon = new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("assets/" + 14 + "_Chess Crusader.png")));
                                grayAttackBackgroundIcon.getImage().getScaledInstance(40, 40, 0);
                                JLabel grayAttackBackgroundLabel = new JLabel();
                                grayAttackBackgroundLabel.setIcon(grayAttackBackgroundIcon);
                                grayAttackBackgroundLabel.setBounds((newCol * 80), (newRow * 80), 80, 80);

                                //red background if power is not enough to attack
                                ImageIcon redAttackBackgroundIcon = new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("assets/" + 40 + "_Chess Crusader.jpg")));
                                redAttackBackgroundIcon.getImage().getScaledInstance(40, 40, 0);
                                JLabel redAttackBackgroundLabel = new JLabel();
                                redAttackBackgroundLabel.setIcon(redAttackBackgroundIcon);
                                redAttackBackgroundLabel.setBounds((newCol * 80), (newRow * 80), 80, 80);


                                if (gameboard[newRow][newCol] instanceof Peace) {

                                    if (((Peace) gameboard[newRow][newCol]).getWhichside() != peace.getWhichside()) {

                                        if (((Peace) gameboard[newRow][newCol]).getPower() <= peace.getPower()) {

                                            if (!peace.getPeaceTag().equals("cristian Castle") || !peace.getPeaceTag().equals("muslim Castle")) {

                                                layeredPane.add(redAttackBackgroundLabel, Integer.valueOf(20));
                                                lastClickedPeace = peace;
                                            }

                                        } else {
                                            lastClickedPeace = peace;
                                        }
                                    }
                                } else {
                                    layeredPane.add(grayAttackBackgroundLabel, Integer.valueOf(20));
                                    lastClickedPeace = peace;
                                }
                            }
                        }
                    }
                }

                layeredPane.repaint();

                //piece clicked audio
                AudioControl AC= new AudioControl();
                AC.pieceClicked(mainPeice);

            } else {
                if (Peace.isValidMove(lastClickedPeace, (y / 80), (x / 80))) {
                    if (lastClickedPeace.getPower() >= mainPeice.getPower() && !(lastClickedPeace instanceof Castle)) {

                        layeredPane.remove(previousGrayBackgroundLabel);
                        layeredPane.repaint();
                        previousGrayBackgroundLabel = null;
                        lastClickedPeace=null;

                        //calling move methode to do action
                        Peace.move(gameboard, lastClickedPeace, (y / 80), (x / 80), layeredPane, hostOrJoin);
                        turn++;

                        Component[] components2 = layeredPane.getComponentsInLayer(20);
                        for (Component component : components2) {
                            layeredPane.remove(component);
                        }
                        layeredPane.repaint();

                        Component[] components4 = layeredPane.getComponentsInLayer(10);
                        for (Component component : components4) {
                            layeredPane.remove(component);
                        }
                        layeredPane.repaint();


                        layeredPane.repaint();

                    }

                }
            }
        }
    }

    // Method to add a piece to the board
    private void addPieceToBoard(Peace piece, int x, int y, int gameWidth) {
        if (gameWidth == 4) {
            Gametiles1[y][x] = piece;
        } else {
            Gametiles2[y][x] = piece;
        }
        ImageIcon peaceImage = loadImageIcon(piece.getAssetIndex());
        piece.setImageIcon(peaceImage);
        piece.getPeaceLable().setIcon(piece.getImageIcon());
        piece.getPeaceLable().setBounds(piece.getX(), piece.getY(), 80, 80);
        layeredPane.add(piece.getPeaceLable(), Integer.valueOf(100));
        layeredPane.repaint();
        if (gameWidth == 4) {
            addPieceMouseListener(piece, x, y, Gametiles1);
        } else {
            addPieceMouseListener(piece, x, y, Gametiles2);
        }
    }

    // Method to load and scale image
    private ImageIcon loadImageIcon(int assetIndex) {
        ImageIcon peaceImage = new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("assets/" + assetIndex + "_Chess Crusader.png")));
        peaceImage.getImage().getScaledInstance(80, 80, 0);
        return peaceImage;
    }

}
