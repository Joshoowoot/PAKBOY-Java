import java.awt.*;
import java.awt.event.*;
import javax.sound.sampled.Clip;
import javax.swing.*;

public class CharacterSelectionPanel extends JPanel implements KeyListener {
    private JFrame parent;
    private int width;
    private int height;
    private Clip menuClip;
    private int selectedCharacter = 0; // 0=PakBoy, 1=PakGirl
    private String[] characterNames = {"PAK BOY", "PAK GIRL"};
    private String[] characterImages = {"img/pakboyIdle.gif", "img/pakgirlIdle.gif"};
    private Image[] images;

    public CharacterSelectionPanel(JFrame parent, int width, int height) {
        this(parent, width, height, null);
    }

    public CharacterSelectionPanel(JFrame parent, int width, int height, Clip menuClip) {
        this.parent = parent;
        this.width = width;
        this.height = height;
        this.menuClip = menuClip;
        setPreferredSize(new Dimension(width, height));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        loadImages();
        SwingUtilities.invokeLater(() -> requestFocusInWindow());
    }

    private void loadImages() {
        images = new Image[characterImages.length];
        for (int i = 0; i < characterImages.length; i++) {
            try {
                java.net.URL imgURL = getClass().getResource(characterImages[i]);
                if (imgURL != null) {
                    images[i] = new ImageIcon(imgURL).getImage();
                }
            } catch (Exception e) {
                System.err.println("Could not load image: " + characterImages[i]);
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawCharacterSelection(g);
    }

    private void drawCharacterSelection(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        
        g2d.setColor(Color.YELLOW);
        g2d.setFont(new Font("Crackman", Font.PLAIN, 50));
        String title = "SELECT CHARACTER";
        FontMetrics fm = g2d.getFontMetrics();
        int titleWidth = fm.stringWidth(title);
        g2d.drawString(title, (width - titleWidth) / 2, height / 6);

        int charBoxWidth = 150;
        int charBoxHeight = 180;
        int spacing = 80;
        int totalWidth = (charBoxWidth * 2) + spacing;
        int startX = (width - totalWidth) / 2;
        int startY = height / 2 - charBoxHeight / 2;

        for (int i = 0; i < 2; i++) {
            int x = startX + (i * (charBoxWidth + spacing));
            int y = startY;

            if (i == selectedCharacter) {
                g2d.setColor(Color.YELLOW);
                g2d.setStroke(new BasicStroke(3));
                g2d.drawRect(x, y, charBoxWidth, charBoxHeight);
                g2d.fillRect(x, y, charBoxWidth, charBoxHeight);
                g2d.setColor(Color.BLACK);
            } else {
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRect(x, y, charBoxWidth, charBoxHeight);
            }

            if (images[i] != null) {
                int imgX = x + 15;
                int imgY = y + 15;
                int imgWidth = charBoxWidth - 30;
                int imgHeight = charBoxHeight - 60;
                g2d.drawImage(images[i], imgX, imgY, imgWidth, imgHeight, this);
            } else {
                g2d.setColor(getCharacterColor(i));
                g2d.fillRect(x + 15, y + 15, charBoxWidth - 30, charBoxHeight - 60);
            }

            g2d.setColor(i == selectedCharacter ? Color.BLACK : Color.YELLOW);
            g2d.setFont(new Font("ArcadeClassic", Font.PLAIN, 16));
            String name = characterNames[i];
            FontMetrics nameFm = g2d.getFontMetrics();
            int nameWidth = nameFm.stringWidth(name);
            g2d.drawString(name, x + (charBoxWidth - nameWidth) / 2, y + charBoxHeight - 20);
        }

        g2d.setColor(Color.YELLOW);
        g2d.setFont(new Font("ArcadeClassic", Font.PLAIN, 18));
        String instructions = "USE   A  AND  D   TO   SELECT";
        String instructions2 = "PRESS   ENTER   TO   CONFIRM";
        FontMetrics instrFm = g2d.getFontMetrics();
        int instrWidth = instrFm.stringWidth(instructions);
        g2d.drawString(instructions, (width - instrWidth) / 2, (int)(height * 0.88));
        int instrWidth2 = instrFm.stringWidth(instructions2);
        g2d.drawString(instructions2, (width - instrWidth2) / 2, (int)(height * 0.92));
    }

    private Color getCharacterColor(int index) {
        switch (index) {
            case 0:
                return new Color(200, 50, 50); // Red
            case 1:
                return new Color(50, 100, 200); // Blue
            case 2:
                return new Color(150, 50, 200); // Purple
            default:
                return Color.WHITE;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT) {
            selectedCharacter = (selectedCharacter - 1 + 2) % 2;
            repaint();
        } else if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT) {
            selectedCharacter = (selectedCharacter + 1) % 2;
            repaint();
        } else if (key == KeyEvent.VK_ENTER) {
            startGameWithCharacter(selectedCharacter);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    private void startGameWithCharacter(int characterIndex) {
        if (menuClip != null) {
            try { menuClip.stop(); menuClip.close(); } catch (Exception ex) {}
        }
        PakBoy game = new PakBoy(characterIndex);
        parent.getContentPane().removeAll();
        parent.add(game);
        parent.revalidate();
        parent.repaint();
        game.requestFocusInWindow();
    }
}

// JOSHOOWOOT 12/07/2025