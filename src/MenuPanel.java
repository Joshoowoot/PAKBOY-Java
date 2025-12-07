import java.awt.*;
import java.awt.event.*;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;

public class MenuPanel extends JPanel implements KeyListener {
    private JFrame parent;
    private int width;
    private int height;
    private Clip menuClip;

    public MenuPanel(JFrame parent, int width, int height) {
        this(parent, width, height, null);
    }

    public MenuPanel(JFrame parent, int width, int height, Clip existingClip) {
        this.parent = parent;
        this.width = width;
        this.height = height;
        this.menuClip = existingClip;
        setPreferredSize(new Dimension(width, height));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        if (menuClip == null) {
            initMenuAudio();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawMenu(g);
    }

    private void drawMenu(Graphics g) {
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Crackman", Font.PLAIN,130));
        String title = "PAKBOY";
        int titleWidth = g.getFontMetrics().stringWidth(title);
        g.drawString(title, (width - titleWidth) / 2, height / 2);

        g.setFont(new Font("ArcadeClassic", Font.PLAIN, 20));
        String subtitle = "GROUP   3   PRESENTS";
        int subWidth = g.getFontMetrics().stringWidth(subtitle);
        g.drawString(subtitle, (width - subWidth) / 2, height / 6);

        String prompt = "PRESS   ENTER   TO   PLAY";
        int pWidth = g.getFontMetrics().stringWidth(prompt);
        g.drawString(prompt, (width - pWidth) / 2, (int)(height * 0.75));

        String leaderboard = "PRESS   SPACE   FOR   LEADERBOARD";
        int lWidth = g.getFontMetrics().stringWidth(leaderboard);
        g.drawString(leaderboard, (width - lWidth) / 2, (int)(height * 0.83));
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            CharacterSelectionPanel charSelection = new CharacterSelectionPanel(parent, width, height, menuClip);
            parent.getContentPane().removeAll();
            parent.add(charSelection);
            parent.revalidate();
            parent.repaint();
            charSelection.requestFocusInWindow();
        } else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            showLeaderboard();
        }
    }

    private void showLeaderboard() {
        if (menuClip != null) {
            try { menuClip.stop(); } catch (Exception ex) {}
        }
        LeaderboardPanel leaderboardPanel = new LeaderboardPanel(parent, width, height, menuClip);
        parent.getContentPane().removeAll();
        parent.add(leaderboardPanel);
        parent.revalidate();
        parent.repaint();
        leaderboardPanel.requestFocusInWindow();
    }

    private void initMenuAudio() {
        menuClip = loadClip("sounds/menu.wav", true);
    }

    private Clip loadClip(String resourcePath, boolean loop) {
        try {
            AudioInputStream in = AudioSystem.getAudioInputStream(getClass().getResource(resourcePath));
            AudioFormat baseFormat = in.getFormat();

            AudioInputStream din = in;
            if (baseFormat.getEncoding() != AudioFormat.Encoding.PCM_SIGNED || baseFormat.getSampleSizeInBits() != 16) {
                AudioFormat targetFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    baseFormat.getSampleRate(),
                    16,
                    baseFormat.getChannels(),
                    baseFormat.getChannels() * 2,
                    baseFormat.getSampleRate(),
                    false
                );
                din = AudioSystem.getAudioInputStream(targetFormat, in);
            }

            Clip clip = AudioSystem.getClip();
            clip.open(din);
            if (loop) {
                clip.loop(Clip.LOOP_CONTINUOUSLY);
                clip.start();
            }
            return clip;
        } catch (Exception e) {
            System.out.println("Could not load menu music (" + resourcePath + "): " + e.getMessage());
            return null;
        }
    }
}

// JOSHOOWOOT 12/07/2025