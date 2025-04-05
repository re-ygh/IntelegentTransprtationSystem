import javax.swing.*;
import java.awt.*;
import java.io.Serializable;

public class Peace implements Serializable {
    private int power;
    private int moveCapacity;
    private String peaceTag;
    private int x;
    private int y;
    private int whichside; // 0 for Christian, 1 for Muslim
    private int assetIndex;
    private ImageIcon imageIcon = new ImageIcon();
    private JLabel peaceLable = new JLabel();


    public Peace(int power, int moveCapacity, String peaceTag, int x, int y, int whichside) {
        this.power = power;
        this.moveCapacity = moveCapacity;
        this.peaceTag = peaceTag;
        this.x = x;
        this.y = y;
        this.whichside = whichside;
    }

    public int getAssetIndex() {
        return assetIndex;
    }

    public void setAssetIndex(int assetIndex) {
        this.assetIndex = assetIndex;
    }

    public ImageIcon getImageIcon() {
        return imageIcon;
    }

    public void setImageIcon(ImageIcon imageIcon) {
        this.imageIcon = imageIcon;
    }
    public JLabel getPeaceLable() {
        return peaceLable;
    }

    public void setPeaceLable(JLabel peaceLable) {
        this.peaceLable = peaceLable;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public int getWhichside() {
        return whichside;
    }



    public int getMoveCapacity() {
        return moveCapacity;
    }

    public void setMoveCapacity(int moveCapacity) {
        this.moveCapacity = moveCapacity;
    }

    public String getPeaceTag() {
        return peaceTag;
    }

    public void setPeaceTag(String peaceTag) {
        this.peaceTag = peaceTag;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setWhichside(int whichside) {
        this.whichside = whichside;
    }
    private static void increasePowerForAdjacentSarbaz(Object[][] gameBoard, int i, int j) {

        int[][] directions = {{-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1}};

        for (int[] dir : directions) {
            int newRow = i + dir[0];
            int newCol = j + dir[1];
            if (isValidPosition(gameBoard, newRow, newCol) && gameBoard[newRow][newCol] instanceof Sarbaz) {
                Sarbaz current = (Sarbaz) gameBoard[i][j];
                Sarbaz adjacent = (Sarbaz) gameBoard[newRow][newCol];
                if (current.getWhichside() == adjacent.getWhichside()) {
                    adjacent.setPower(adjacent.getPower() + 1);
                }
            }
        }
    }

    private static void increasePowerForAdjacentPeace(Object[][] gameBoard, int i, int j) {
        int[][] directions = {{-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1}};

        for (int[] dir : directions) {
            int newRow = i + dir[0];
            int newCol = j + dir[1];
            if (isValidPosition(gameBoard, newRow, newCol) && gameBoard[newRow][newCol] instanceof Peace) {
                Peace current = (Peace) gameBoard[i][j];
                Peace adjacent = (Peace) gameBoard[newRow][newCol];
                if (current.getWhichside() == adjacent.getWhichside()) {
                    adjacent.setPower(adjacent.getPower() + 1);
                }
            }
        }
    }

    private static void reducePowerForEnemyAdjacent(Object[][] gameBoard, int i, int j) {
        int[][] directions = {{-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1}};

        for (int[] dir : directions) {
            int newRow = i + dir[0];
            int newCol = j + dir[1];
            if (isValidPosition(gameBoard, newRow, newCol) && gameBoard[newRow][newCol] instanceof Peace) {
                Peace current = (Peace) gameBoard[i][j];
                Peace adjacent = (Peace) gameBoard[newRow][newCol];
                if (current.getWhichside() != adjacent.getWhichside()) {
                    if (adjacent.getPower() == 1) {
                        adjacent.setPower(0);
                    } else {
                        adjacent.setPower(adjacent.getPower() - 2);
                    }

                }
            }
        }
    }

    public static boolean isValidPosition(Object[][] gameBoard, int i, int j) {
        return i >= 0 && i < gameBoard.length && j >= 0 && j < gameBoard[0].length;
    }
    //move method
    public static void move(Object[][] gameBoard, Peace clickedPeace, int rowToMoveTo, int colToMoveTo,JLayeredPane layeredPane,HostOrJoin hostOrJoin) {
        if (isValidMove(clickedPeace, rowToMoveTo, colToMoveTo)) {

//            new MovementGUI(clickedPeace.getImageIcon(),layeredPane).movePiece(colToMoveTo,rowToMoveTo);


            performMove(gameBoard, clickedPeace, rowToMoveTo, colToMoveTo,layeredPane);
        } else {
            Peace peaceToAttack = (Peace) gameBoard[rowToMoveTo][colToMoveTo];
            if (peaceToAttack.getWhichside() != clickedPeace.getWhichside() && peaceToAttack.getPower() <= clickedPeace.getPower()) {

//                new MovementGUI(clickedPeace.getImageIcon(),layeredPane).movePiece(colToMoveTo,rowToMoveTo);

                performAttack(gameBoard, clickedPeace, rowToMoveTo, colToMoveTo,layeredPane);
            }
        }

    }

    public static boolean isValidMove(Peace clickedPeace, int rowToMoveTo, int colToMoveTo) {
        int x = clickedPeace.getX() / 80;
        int y = clickedPeace.getY() / 80;
        if (clickedPeace instanceof Knight) {
            return Math.abs(x - colToMoveTo) <= 2 && Math.abs(y - rowToMoveTo) <= 2;
        } else if (clickedPeace instanceof Sarbaz) {
            if (clickedPeace.getWhichside() == 0) {
                return Math.abs(x - colToMoveTo) <= 1 && (rowToMoveTo - y) == 1;
            } else {
                return Math.abs(x - colToMoveTo) <= 1 && (y - rowToMoveTo) == 1;
            }
        } else {
            return Math.abs(x - colToMoveTo) <= 1 && Math.abs(y - rowToMoveTo) <= 1;
        }
    }

    private static void performMove(Object[][] gameBoard, Peace clickedPeace, int rowToMoveTo, int colToMoveTo,JLayeredPane layeredPane) {
        int x = clickedPeace.getX() / 80;
        int y = clickedPeace.getY() / 80;
        clickedPeace.setY(rowToMoveTo * 80);
        clickedPeace.setX(colToMoveTo * 80);
        clickedPeace.getPeaceLable().setBounds(colToMoveTo * 80, rowToMoveTo * 80, 80, 80);
        gameBoard[rowToMoveTo][colToMoveTo] = clickedPeace;
        gameBoard[y][x] = null;
        Peace.ability(gameBoard);
        layeredPane.repaint();
    }


    private static void performAttack(Object[][] gameBoard, Peace clickedPeace, int rowToMoveTo, int colToMoveTo,JLayeredPane layeredPane) {
        Peace peaceToAttack = (Peace) gameBoard[rowToMoveTo][colToMoveTo];
        if (peaceToAttack instanceof Castle) {
            performMove(gameBoard, clickedPeace, rowToMoveTo, colToMoveTo,layeredPane);
            JLabel winlabel = new JLabel();
            if (peaceToAttack.getWhichside()==0){
                winlabel.setText("muslims win");
            }else {
                winlabel.setText("cri win");
            }

            winlabel.setFont(new Font(null, Font.PLAIN, 20));
            winlabel.setBackground(Color.darkGray);
            winlabel.setBorder(BorderFactory.createRaisedBevelBorder());
            winlabel.setBounds(33, 300, 250, 50);
            winlabel.setOpaque(true);
            layeredPane.add(winlabel);
            for (int i = 0; i < 1000; i++) {
                System.out.println("you win");
            }
            System.exit(0);
        } else {
            performMove(gameBoard, clickedPeace, rowToMoveTo, colToMoveTo,layeredPane);
        }
    }

    public static void ability(Object[][] gameBoard) {
        int rows = gameBoard.length;
        int cols = gameBoard[0].length;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (gameBoard[i][j] instanceof Sarbaz) {
                    ((Sarbaz) gameBoard[i][j]).setPower(1);
                } else if (gameBoard[i][j] instanceof Castle) {
                    ((Castle) gameBoard[i][j]).setPower(1);
                } else if (gameBoard[i][j] instanceof Catapult) {
                    ((Catapult) gameBoard[i][j]).setPower(0);
                } else if (gameBoard[i][j] instanceof Knight) {
                    ((Knight) gameBoard[i][j]).setPower(1);
                } else if (gameBoard[i][j] instanceof Archer) {
                    ((Archer) gameBoard[i][j]).setPower(2);
                }
            }
        }

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (gameBoard[i][j] instanceof Sarbaz) {
                    increasePowerForAdjacentSarbaz(gameBoard, i, j);
                } else if (gameBoard[i][j] instanceof Castle) {
                    increasePowerForAdjacentPeace(gameBoard, i, j);
                } else if (gameBoard[i][j] instanceof Catapult) {
                    reducePowerForEnemyAdjacent(gameBoard, i, j);
                }
            }
        }
    }
}
