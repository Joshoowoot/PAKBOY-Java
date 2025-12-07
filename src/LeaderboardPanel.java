import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;

public class LeaderboardPanel extends JPanel implements KeyListener {
    private JFrame parent;
    private int width;
    private int height;
    private javax.sound.sampled.Clip menuClip;

    public LeaderboardPanel(JFrame parent, int width, int height, javax.sound.sampled.Clip menuClip) {
        this.parent = parent;
        this.width = width;
        this.height = height;
        this.menuClip = menuClip;
        setPreferredSize(new Dimension(width, height));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        
        if (menuClip != null && !menuClip.isRunning()) {
            try {
                menuClip.setFramePosition(0);
                menuClip.loop(javax.sound.sampled.Clip.LOOP_CONTINUOUSLY);
                menuClip.start();
            } catch (Exception ex) {}
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawLeaderboard(g);
    }

    private void drawLeaderboard(Graphics g) {
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Crackman", Font.PLAIN, 70));
        String title = "LEADERBOARD";
        int titleWidth = g.getFontMetrics().stringWidth(title);
        g.drawString(title, (width - titleWidth) / 2, height / 6);

        List<Leaderboard.Entry> entries = Leaderboard.load();
        g.setFont(new Font("ArcadeClassic", Font.PLAIN, 30));
        
        if (entries.isEmpty()) {
            g.setColor(Color.WHITE);
            String noScores = "NO  SCORES  YET";
            int nsWidth = g.getFontMetrics().stringWidth(noScores);
            g.drawString(noScores, (width - nsWidth) / 2, height / 2);
        } else {
            int startY = (int)(height * 0.35);
            int lineHeight = 40;
            
            for (int i = 0; i < entries.size(); i++) {
                Leaderboard.Entry entry = entries.get(i);
                
                if (i == 0) {
                    g.setColor(new Color(255, 215, 0)); // Gold
                } else if (i == 1) {
                    g.setColor(new Color(192, 192, 192)); // Silver
                } else if (i == 2) {
                    g.setColor(new Color(205, 127, 50)); // Bronze
                } else {
                    g.setColor(Color.WHITE);
                }
                
                String rank = String.format("Rank%d", i + 1);
                String name = entry.name;
                String score = String.format("%d", entry.score);
                
                int y = startY + (i * lineHeight);
                int rankX = width / 6;
                int nameX = width / 2 - g.getFontMetrics().stringWidth(name) / 2;
                int scoreX = (int)(width * 0.75);
                
                g.drawString(rank, rankX, y);
                g.drawString(name, nameX, y);
                g.drawString(score, scoreX, y);
            }
        }

        g.setColor(Color.YELLOW);
        g.setFont(new Font("ArcadeClassic", Font.PLAIN, 20));
        String prompt = "PRESS   ANY   KEY   TO   RETURN";
        int pWidth = g.getFontMetrics().stringWidth(prompt);
        g.drawString(prompt, (width - pWidth) / 2, (int)(height * 0.9));
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {
        returnToMenu();
    }

    private void returnToMenu() {
        if (menuClip != null) {
            try {
                menuClip.setFramePosition(0);
                menuClip.loop(javax.sound.sampled.Clip.LOOP_CONTINUOUSLY);
                menuClip.start();
            } catch (Exception ex) {}
        }
        MenuPanel menu = new MenuPanel(parent, width, height, menuClip);
        parent.getContentPane().removeAll();
        parent.add(menu);
        parent.revalidate();
        parent.repaint();
        menu.requestFocusInWindow();
    }
}

// JOSHOOWOOT 12/07/2025