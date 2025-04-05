import javax.swing.*;
import java.io.Serializable;

public class Knight extends Peace implements Serializable {


    Knight(int power, int moveCapacity, String peaceTag, int x, int y,int whichside) {
        super(power, moveCapacity, peaceTag, x, y,whichside);
        if (whichside == 0) {
            setAssetIndex(17);
        } else if (whichside == 1) {
            setAssetIndex(18);
        }
    }

    @Override
    public int getAssetIndex() {
        return super.getAssetIndex();
    }

    @Override
    public void setAssetIndex(int assetIndex) {
        super.setAssetIndex(assetIndex);
    }

    @Override
    public ImageIcon getImageIcon() {
        return super.getImageIcon();
    }

    @Override
    public void setImageIcon(ImageIcon imageIcon) {
        super.setImageIcon(imageIcon);
    }

    public int getWhichside() {
        return super.getWhichside();
    }

    public void setWhichside(int whichside) {
        super.setWhichside(whichside);
    }

    @Override
    public JLabel getPeaceLable() {
        return super.getPeaceLable();
    }

    @Override
    public void setPeaceLable(JLabel peaceLable) {
        super.setPeaceLable(peaceLable);
    }

    @Override
    public int getPower() {
        return super.getPower();
    }

    @Override
    public void setPower(int power) {
        super.setPower(power);
    }

    @Override
    public int getMoveCapacity() {
        return super.getMoveCapacity();
    }

    @Override
    public void setMoveCapacity(int moveCapacity) {
        super.setMoveCapacity(moveCapacity);
    }

    @Override
    public String getPeaceTag() {
        return super.getPeaceTag();
    }

    @Override
    public void setPeaceTag(String peaceTag) {
        super.setPeaceTag(peaceTag);
    }

    @Override
    public int getX() {
        return super.getX();
    }

    @Override
    public void setX(int x) {
        super.setX(x);
    }

    @Override
    public int getY() {
        return super.getY();
    }

    @Override
    public void setY(int y) {
        super.setY(y);
    }
}
