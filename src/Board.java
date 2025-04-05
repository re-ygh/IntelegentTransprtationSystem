import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public interface Board {
    Object[][] Gametiles1 = new Object[8][4];
    Object[][] Gametiles2 = new Object[8][8];
    void gameBoard(String gameTiles, ObjectInputStream objectInputStream, ObjectOutputStream objectOutputStream, ServerSocket serverSocket, Socket socket);
}
