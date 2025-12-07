import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JFrame;


public class App {
    public static void main(String[] args) throws Exception {
        int rowCount = 23;
        int columnCount = 19;
        int tileSize = 32;
        int boardWidth = columnCount * tileSize;
        int boardHeight = rowCount * tileSize;

        JFrame frame = new JFrame("Pak Boy");
        try {
            java.net.URL iconURL = App.class.getResource("img/pakboyHead.png");
            if (iconURL != null) {
                Image icon = new ImageIcon(iconURL).getImage();
                frame.setIconImage(icon);
            }
        } catch (Exception e) {
            System.err.println("Could not set window icon: " + e.getMessage());
        }

        frame.setSize(boardWidth, boardHeight);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        MenuPanel menu = new MenuPanel(frame, boardWidth, boardHeight);
        frame.add(menu);
        frame.pack();
        menu.requestFocusInWindow();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

// JOSHOOWOOT 12/07/2025