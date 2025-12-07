import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Random;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;

public class PakBoy extends JPanel implements ActionListener, KeyListener {
    private boolean paused = false;
    private boolean muted = false;
    private boolean countdownWasRunning = false;
    private boolean countdownStartsGameSaved = false;
    private int selectedPauseButton = 0; // 0=Continue, 1=Restart, 2=Mute, 3=Menu, 4=Exit
    private Rectangle continueBtn, restartBtn, muteBtn, menuBtn, exitBtn;
    class Block {
        int x;
        int y;
        int width;
        int height;
        Image image;

        int startX;
        int startY;
        char direction = 'U';
        int velocityX = 0;
        int velocityY = 0;

        Block(Image image, int x, int y, int width, int height) {
            this.image = image;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.startX = x;
            this.startY = y;
        }
        void updateDirection(char direction) {
            char prevDirection = this.direction;
            this.direction = direction;
            updateVelocity();
            this.x += this.velocityX;
            this.y += this.velocityY;
            for(Block wall : walls) {
                if (collision(this, wall)) {
                    this.x -= this.velocityX;
                    this.y -= this.velocityY;
                    this.direction = prevDirection; 
                    updateVelocity();
                }
            }
        }

        // MOVEMENT SPEED OF THE SPRITES
        // U = UP, D = DOWN, L = LEFT, R = RIGHT
        void updateVelocity() {
            if (this.direction == 'U') {
                this.velocityX = 0;
                this.velocityY = -tileSize/4;
            }
            else if(this.direction == 'D') {
                this.velocityX = 0;
                this.velocityY = tileSize/4;
            }
            else if(this.direction == 'L') {
                this.velocityX = -tileSize/4;
                this.velocityY = 0;
            }
            else if(this.direction == 'R') {
                this.velocityX = tileSize/4;
                this.velocityY = 0;
            }
        }
        void reset() {
            this.x = this.startX;
            this.y = this.startY;
        }
    }

    // MAZE MAP DIMENSIONS
    private int rowCount = 21;
    private int columnCount = 19;
    private int tileSize = 32;
    private int boardWidth = columnCount * tileSize;
    private int boardHeight = rowCount * tileSize;

    // IMAGES
    private Image wallImage;
    private Image vampireOneImage;
    private Image vampireTwoImage;
    private Image vampireThreeImage;
    private Image slimeImage;

    private Image pakboyRightImage;
    private Image pakboyLeftImage;
    private Image pakboyUpImage;
    private Image pakboyDownImage;
    private Image pakboyHeadImage;
    private Image pakboyDeathImage;

    private Image pakgirlRightImage;
    private Image pakgirlLeftImage;
    private Image pakgirlUpImage;
    private Image pakgirlDownImage;
    private Image pakgirlDeathImage;

    private int selectedCharacter = 0; // 0 = PakBoy, 1 = PakGirl

    //X = wall, O = skip, P = pakboy, ' ' = food
    //enemies: b = vampire2, o = slime, p = vampire3, r = vampire1
    private String[] tileMap = {
        //MAP 1
        "XXXXXXXXXXXXXXXXXXX",
        "X        X        X",
        "X XX XXX X XXX XX X",
        "X                 X",
        "X XX X XXXXX X XX X",
        "X    X       X    X",
        "XXXX XXXX XXXX XXXX",
        "OOOX X       X XOOO",
        "XXXX X XXrXX X XXXX",
        "X       bpo       X",
        "XXXX X XXXXX X XXXX",
        "OOOX X       X XOOO",
        "XXXX X XXXXX X XXXX",
        "X        X        X",
        "X XX XXX X XXX XX X",
        "X  X     P     X  X",
        "XX X X XXXXX X X XX",
        "X    X   X   X    X",
        "X XXXXXX X XXXXXX X",
        "X                 X",
        "XXXXXXXXXXXXXXXXXXX" 

        //MAP 2
        // "XXXXXXXXXXXXXXXXXXX",
        // "X        X        X",
        // "X XX XXX X XXX XX X",
        // "X            X    X",
        // "X XX X XXXXX X XX X",
        // "X    X       X    X",
        // "X XX XXXX XXXX XX X",
        // "X    X       X    X",
        // "XXXX X XXrXX X XXXX",
        // "X       bpo       X",
        // "X XX X XXXXX X XX X",
        // "X  X     X     X  X",
        // "X XX X X X X X XX X",
        // "X        X        X",
        // "X XX XXX X XXX XX X",
        // "X  X     P     X  X",
        // "XX X X XXXXX X X XX",
        // "X    X       X    X",
        // "X XXXXXX X XXXXXX X",
        // "X        X        X",
        // "XXXXXXXXXXXXXXXXXXX" 
    };

    HashSet<Block> walls;
    HashSet<Block> enemys;
    HashSet<Block> foods;
    Block pakboy;

    // AUDIO
    private Clip bgClip;
    private Clip eatClip;
    private Clip gameoverClip;
    private Clip clearClip;
    private Clip collideClip;
    private Clip countdownClip;

    private int countdownRemaining = 0;
    private Timer countdownTimer;

    // GAME OVER ZOOM EFFECT
    private boolean gameOverZooming = false;
    private float gameOverScale = 1.0f;
    private final float gameOverScaleIncrement = 0.08f;
    private final float gameOverTargetScale = 3.0f;

    private boolean awaitingReturnKey = false;

    Timer gameLoop;
    char [] directions = {'U', 'D', 'L', 'R'};
    Random random = new Random();
    int score = 0;
    int lives = 1;
    boolean gameOver = false;
    private int highscore = 0;
    private boolean scoreRecorded = false;

    PakBoy() {
        this(0);
    }

    PakBoy(int character) {
        this.selectedCharacter = character;
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.BLACK);
        addKeyListener(this);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (paused) {
                    handlePauseMenuClick(e.getX(), e.getY());
                }
            }
        });
        setFocusable(true);

        // LOAD IMAGES
        wallImage = new ImageIcon(getClass().getResource("img/wall.png")).getImage();
        vampireOneImage = new ImageIcon(getClass().getResource("img/vampire1.gif")).getImage();
        vampireTwoImage = new ImageIcon(getClass().getResource("img/vampire2.gif")).getImage();
        vampireThreeImage = new ImageIcon(getClass().getResource("img/vampire3.gif")).getImage();
        slimeImage = new ImageIcon(getClass().getResource("img/slime.gif")).getImage();

        // LOAD CHARACTER IMAGES BASED ON SELECTION WHEREIN 1 = PAKGIRL AND 0 = PAKBOY
        if (character == 1) {
            pakgirlUpImage = new ImageIcon(getClass().getResource("img/pakgirlUp.gif")).getImage();
            pakgirlDownImage = new ImageIcon(getClass().getResource("img/pakgirlDown.gif")).getImage();
            pakgirlLeftImage = new ImageIcon(getClass().getResource("img/pakgirlLeft.gif")).getImage();
            pakgirlRightImage = new ImageIcon(getClass().getResource("img/pakgirlRight.gif")).getImage();
            pakboyHeadImage = new ImageIcon(getClass().getResource("img/pakgirlDown.gif")).getImage();

            java.net.URL girlHurtUrl = getClass().getResource("img/pakgirlHurt.gif");
            if (girlHurtUrl != null) {
                pakgirlDeathImage = new ImageIcon(girlHurtUrl).getImage();
            } else {
                java.net.URL girlDeadUrl = getClass().getResource("img/pakgirlDead.gif");
                pakgirlDeathImage = new ImageIcon(girlDeadUrl != null ? girlDeadUrl : getClass().getResource("img/pakgirlDown.gif")).getImage();
            }
        } else {
            pakboyUpImage = new ImageIcon(getClass().getResource("img/pakboyUp.gif")).getImage();
            pakboyDownImage = new ImageIcon(getClass().getResource("img/pakboyDown.gif")).getImage();
            pakboyLeftImage = new ImageIcon(getClass().getResource("img/pakboyLeft.gif")).getImage();
            pakboyRightImage = new ImageIcon(getClass().getResource("img/pakboyRight.gif")).getImage();
            pakboyHeadImage = new ImageIcon(getClass().getResource("img/pakboyHead.png")).getImage();
            pakboyDeathImage = new ImageIcon(getClass().getResource("img/pakboyDead.gif")).getImage();
        }

        // LOAD MAP
        loadMap();
        for (Block enemy : enemys) {
            char newDirection = directions[random.nextInt(4)];
            enemy.updateDirection(newDirection);
        }
        initAudio();
        highscore = Leaderboard.getHighscore();
        gameLoop = new Timer(50, this);
        startCountdown();
    }

    public void  loadMap(){
        walls = new HashSet<Block>();
        foods = new HashSet<Block>();
        enemys = new HashSet<Block>();

        //LOOP FOR THE MAP
        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < columnCount; c++) {
                String row = tileMap[r];
                char tileMapChar = row.charAt(c);

                int x = c*tileSize;
                int y = r*tileSize;

                if (tileMapChar == 'X') {
                    Block wall = new Block(wallImage, x, y, tileSize, tileSize);
                    walls.add(wall);
                }
                else if (tileMapChar == 'r') {
                    Block enemy = new Block(vampireOneImage, x, y, tileSize, tileSize);
                    enemys.add(enemy);
                }
                else if (tileMapChar == 'b') {
                    Block enemy = new Block(vampireTwoImage, x, y, tileSize, tileSize);
                    enemys.add(enemy);
                }
                else if (tileMapChar == 'p') {
                    Block enemy = new Block(vampireThreeImage, x, y, tileSize, tileSize);
                    enemys.add(enemy);
                }
                else if (tileMapChar == 'o') {
                    Block enemy = new Block(slimeImage, x, y, tileSize, tileSize);
                    enemys.add(enemy);
                }
                else if (tileMapChar == 'P') {
                    Image initialImage = (selectedCharacter == 1) ? pakgirlRightImage : pakboyRightImage;
                    pakboy = new Block(initialImage, x, y, tileSize, tileSize);
                }
                else if (tileMapChar == ' ') {
                    Block food = new Block(null, x + 14, y + 14, 4, 4);
                    foods.add(food);
                }
            }
        }
    }

    // PAINT COMPONENT
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        for(Block enemy : enemys) {
            g.drawImage(enemy.image, enemy.x, enemy.y, enemy.width, enemy.height, null);
        }
        for(Block wall : walls) {
            g.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);
        }

        g.setColor(Color.YELLOW);
        for(Block food : foods) {
            g.fillOval(food.x, food.y, food.width, food.height);
        }

        if (!gameOver) {
            g.drawImage(pakboy.image, pakboy.x, pakboy.y, pakboy.width, pakboy.height, null);
        }
        if (countdownRemaining > 0) {
            Font big = new Font("ArcadeClassic", Font.BOLD, 130);
            g.setFont(big);
            FontMetrics fm = g.getFontMetrics(big);
            String text = String.valueOf(countdownRemaining);
            int sw = fm.stringWidth(text);
            int sh = fm.getAscent();
            Color overlay = new Color(0, 0, 0, 170);
            g.setColor(overlay);
            g.fillRect(0, boardHeight/2 - 120, boardWidth, 240);
            g.setColor(Color.WHITE);
            g.drawString(text, boardWidth/2 - sw/2, boardHeight/2 + sh/2);
        }

        g.setFont(new Font("Arial", Font.PLAIN, 18));
        int lifeIconX = 10;
        int lifeIconY = boardHeight + 15;
        if (!gameOver) {
            if (pakboyHeadImage != null) {
                g.drawImage(pakboyHeadImage, lifeIconX, lifeIconY, 30, 30, null);
            } else if (pakboy != null && pakboy.image != null) {
                g.drawImage(pakboy.image, lifeIconX, lifeIconY, 30, 30, null);
            }
        }

        int textX = lifeIconX + 30;
        int textY = boardHeight + 38;
        if (!gameOver) {
            g.drawString("x" + String.valueOf(lives) + " Score: " + String.valueOf(score) + "                                                            Highscore: " + String.valueOf(highscore), textX, textY);
        }

        // GAME OVER ZOOM EFFECT
        if (gameOver) {
            float scaleToUse = Math.max(gameOverScale, 1.0f);
            scaleToUse = Math.min(scaleToUse, gameOverTargetScale);
            int centerX = pakboy.x + pakboy.width/2;
            int centerY = pakboy.y + pakboy.height/2;
            int w = (int)(pakboy.width * scaleToUse);
            int h = (int)(pakboy.height * scaleToUse);
            int drawX = centerX - w/2;
            int drawY = centerY - h/2;
            g.drawImage(pakboy.image, drawX, drawY, w, h, null);
            
            if (awaitingReturnKey && !gameOverZooming) {
                drawGameOverOverlay(g);
            }
            else if (awaitingReturnKey && gameOverZooming) {
                g.setFont(new Font("ArcadeClassic", Font.BOLD, 28));
                g.setColor(Color.WHITE);
                String prompt = "PRESS  SPACE  TO  SKIP";
                FontMetrics promptFm = g.getFontMetrics();
                int promptX = boardWidth/2 - promptFm.stringWidth(prompt)/2;
                g.drawString(prompt, promptX, boardHeight - 30);
            }
        }
        
        if (paused) {
            drawPauseMenu(g);
        }
    }
    
    private void drawPauseMenu(Graphics g) {
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(0, 0, boardWidth, boardHeight);
        g.setFont(new Font("ArcadeClassic", Font.BOLD, 48));
        g.setColor(Color.YELLOW);
        String title = "PAUSED";
        FontMetrics titleFm = g.getFontMetrics();
        int titleX = boardWidth/2 - titleFm.stringWidth(title)/2;
        g.drawString(title, titleX, 120);
        
        int btnWidth = 280;
        int btnHeight = 60;
        int btnX = boardWidth/2 - btnWidth/2;
        int startY = 180;
        int spacing = 70;
        
        continueBtn = new Rectangle(btnX, startY, btnWidth, btnHeight);
        restartBtn = new Rectangle(btnX, startY + spacing, btnWidth, btnHeight);
        muteBtn = new Rectangle(btnX, startY + spacing*2, btnWidth, btnHeight);
        menuBtn = new Rectangle(btnX, startY + spacing*3, btnWidth, btnHeight);
        exitBtn = new Rectangle(btnX, startY + spacing*4, btnWidth, btnHeight);
        
        Rectangle[] buttons = {continueBtn, restartBtn, muteBtn, menuBtn, exitBtn};
        String[] labels = {"CONTINUE", "RESTART", muted ? "UNMUTE" : "MUTE", "MENU", "EXIT"};
        
        g.setFont(new Font("ArcadeClassic", Font.BOLD, 24));
        for (int i = 0; i < buttons.length; i++) {
            Rectangle btn = buttons[i];
            boolean selected = (i == selectedPauseButton);
            
            if (selected) {
                g.setColor(Color.YELLOW);
                g.fillRect(btn.x, btn.y, btn.width, btn.height);
                g.setColor(Color.BLACK);
            } else {
                g.setColor(new Color(80, 80, 80));
                g.fillRect(btn.x, btn.y, btn.width, btn.height);
                g.setColor(Color.WHITE);
            }
            
            g.drawRect(btn.x, btn.y, btn.width, btn.height);
            
            FontMetrics fm = g.getFontMetrics();
            int textX = btn.x + btn.width/2 - fm.stringWidth(labels[i])/2;
            int textY = btn.y + btn.height/2 + fm.getAscent()/2 - 2;
            g.drawString(labels[i], textX, textY);
        }
        
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        g.setColor(Color.LIGHT_GRAY);
        String hint = "Use UP/DOWN arrows or W/S to navigate, ENTER to select, ESC to resume";
        FontMetrics hintFm = g.getFontMetrics();
        int hintX = boardWidth/2 - hintFm.stringWidth(hint)/2;
        g.drawString(hint, hintX, boardHeight - 40);
    }
    
    // GAME OVER OVERLAY
    private void drawGameOverOverlay(Graphics g) {
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(0, 0, boardWidth, boardHeight);
        
        g.setFont(new Font("ArcadeClassic", Font.BOLD, 72));
        g.setColor(Color.RED);
        String gameOverText = "GAME OVER";
        FontMetrics titleFm = g.getFontMetrics();
        int titleX = boardWidth/2 - titleFm.stringWidth(gameOverText)/2;
        g.drawString(gameOverText, titleX, 200);
        
        g.setFont(new Font("ArcadeClassic", Font.BOLD, 48));
        g.setColor(Color.YELLOW);
        String scoreText = "FINAL SCORE   " + score;
        FontMetrics scoreFm = g.getFontMetrics();
        int scoreX = boardWidth/2 - scoreFm.stringWidth(scoreText)/2;
        g.drawString(scoreText, scoreX, 300);
        
        g.setFont(new Font("ArcadeClassic", Font.BOLD, 36));
        g.setColor(Color.CYAN);
        String highscoreText = "HIGHSCORE   " + highscore;
        FontMetrics highscoreFm = g.getFontMetrics();
        int highscoreX = boardWidth/2 - highscoreFm.stringWidth(highscoreText)/2;
        g.drawString(highscoreText, highscoreX, 370);
        
        g.setFont(new Font("ArcadeClassic", Font.BOLD, 28));
        g.setColor(Color.WHITE);
        String prompt = "PRESS   ANY  KEY   TO   CONTINUE";
        FontMetrics promptFm = g.getFontMetrics();
        int promptX = boardWidth/2 - promptFm.stringWidth(prompt)/2;
        g.drawString(prompt, promptX, boardHeight - 100);
    }

    public void move() {
        if (countdownRemaining > 0) {
            return;
        }
        pakboy.x += pakboy.velocityX;
        pakboy.y += pakboy.velocityY;

        // TO CHECK THE COLLISIONS WITH WALLS
        for (Block wall : walls) {
            if (collision(pakboy, wall)) {
                pakboy.x -= pakboy.velocityX;
                pakboy.y -= pakboy.velocityY;
                break;
            }
        }

        // TO CHECK THE COLLISIONS WITH ENEMIES
        for (Block enemy : enemys) {
            if (collision(enemy , pakboy)) {
                playCollideSound();
                lives -= 1;
                if (lives == 0) {
                    triggerGameOver();
                    return;
                }
                resetPositions();
                startReviveCountdown();
            }

            // ENEMY SPECIAL MOVEMENT FOR IT NOT TO BE STUCK
            if (enemy.y == tileSize*9 && enemy.direction != 'U' && enemy.direction != 'D') {
                enemy.updateDirection('U');
            }
            enemy.x += enemy.velocityX;
            enemy.y += enemy.velocityY;
            for (Block wall : walls) {
                if (collision(enemy, wall) || enemy.x <= 0 || enemy.x + enemy.width >= boardWidth) {
                    enemy.x -= enemy.velocityX;
                    enemy.y -= enemy.velocityY;
                    char newDirection = directions[random.nextInt(4)];
                    enemy.updateDirection(newDirection);
                }
            }
        }

        // TO CHECK THE FOOD EATEN
        Block foodEaten = null;
        for (Block food : foods) {
            if (collision(pakboy, food)) {
                foodEaten = food;
                score += 10;
                playEatSound();
            }
        }
        foods.remove(foodEaten);

        if (foods.isEmpty()) {
            playClearSound();
            loadMap();
            resetPositions();
        }
    }

    // AUDIO INITIALIZATION
    private void initAudio() {
        bgClip = loadClip("sounds/bg.wav", false);
        eatClip = loadClip("sounds/eat.wav", false);
        gameoverClip = loadClip("sounds/gameover.wav", false);
        clearClip = loadClip("sounds/clear.wav", false);
        // Use character-specific hurt sound for collisions
        String hurtSound = (selectedCharacter == 1) ? "sounds/pakgirlHurt.wav" : "sounds/pakboyHurt.wav";
        collideClip = loadClip(hurtSound, false);
        countdownClip = loadClip("sounds/countdown.wav", false);
    }

    private void playClearSound() {
        try {
            if (muted) return;
            if (clearClip != null) {
                if (clearClip.isRunning()) {
                    clearClip.stop();
                }
                clearClip.setFramePosition(0);
                clearClip.start();
            }
        } catch (Exception e) {
        }
    }

    private void playCollideSound() {
        try {
            if (muted) return;
            if (collideClip != null) {
                if (collideClip.isRunning()) {
                    collideClip.stop();
                }
                collideClip.setFramePosition(0);
                collideClip.start();
            }
        } catch (Exception e) {}
    }

    private void startReviveCountdown() {
        countdownRemaining = 4;
        if (countdownTimer != null && countdownTimer.isRunning()) {
            countdownTimer.stop();
        }
        countdownTimer = new Timer(1050, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                repaint();
                countdownRemaining--;
                if (countdownRemaining > 1) {
                    playCountdownSound();
                }
                if (countdownRemaining <= 0) {
                    countdownTimer.stop();
                    countdownRemaining = -1;
                }
            }
        });
        countdownTimer.setInitialDelay(0);
        countdownTimer.start();
    }

    private void startGame() {
        if (bgClip != null) {
            try {
                bgClip.setFramePosition(0);
                bgClip.loop(Clip.LOOP_CONTINUOUSLY);
                bgClip.start();
            } catch (Exception ex) {}
        }
        gameLoop.start();
    }

    private void startCountdown() {
        countdownRemaining = 4;
        if (countdownTimer != null && countdownTimer.isRunning()) {
            countdownTimer.stop();
        }
        countdownTimer = new Timer(1050, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                repaint();
                countdownRemaining--;
                if (countdownRemaining > 1) {
                    playCountdownSound();
                }
                if (countdownRemaining <= 0) {
                    countdownTimer.stop();
                    startGame();
                }
            }
        });
        countdownTimer.setInitialDelay(0);
        countdownTimer.start();
    }

    private void playCountdownSound() {
        try {
            if (muted) return;
            if (countdownClip != null) {
                if (countdownClip.isRunning()) {
                    countdownClip.stop();
                }
                countdownClip.setFramePosition(0);
                countdownClip.start();
            }
        } catch (Exception e) {
        }
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
            System.out.println("Could not load " + resourcePath + ": " + e.getMessage());
            return null;
        }
    }

    private void playEatSound() {
        try {
            if (muted) return;
            if (eatClip != null) {
                if (eatClip.isRunning()) {
                    eatClip.stop();
                }
                eatClip.setFramePosition(0);
                eatClip.start();
            }
        } catch (Exception e) {
        }
    }

    private void playGameOverSound() {
        try {
            if (muted) return;
            if (gameoverClip != null) {
                if (gameoverClip.isRunning()) {
                    gameoverClip.stop();
                }
                gameoverClip.setFramePosition(0);
                gameoverClip.start();
            }
        } catch (Exception e) {
        }
    }

    private void triggerGameOver() {
        if (gameOver) {
            promptAndSaveScore();
            return;
        }
        gameOver = true;
        if (selectedCharacter == 1) {
            if (pakgirlDeathImage != null) {
                pakboy.image = pakgirlDeathImage;
            }
        } else {
            if (pakboyDeathImage != null) {
                pakboy.image = pakboyDeathImage;
            }
        }
        gameOverZooming = true;
        gameOverScale = 1.0f;
        if (bgClip != null) {
            try { bgClip.stop(); } catch (Exception ex) {}
        }
        promptAndSaveScore();
        playGameOverSound();
    }

    private void promptAndSaveScore() {
        if (scoreRecorded) {
            return;
        }
        scoreRecorded = true;
        String input = JOptionPane.showInputDialog(
            this,
            "Enter 3 letters for the leaderboard:",
            "Save Score",
            JOptionPane.PLAIN_MESSAGE
        );
        if (input == null) {
            input = "???";
        }
        input = input.trim().toUpperCase().replaceAll("[^A-Z0-9]", "");
        if (input.isEmpty()) {
            input = "???";
        }
        if (input.length() < 3) {
            StringBuilder sb = new StringBuilder(input);
            while (sb.length() < 3) {
                sb.append('?');
            }
            input = sb.toString();
        }
        if (input.length() > 3) {
            input = input.substring(0, 3);
        }

        Leaderboard.recordScore(input, score);
        highscore = Leaderboard.getHighscore();
        awaitingReturnKey = true;
    }

    private void stopAllClips() {
        try {
            if (bgClip != null && bgClip.isRunning()) bgClip.stop();
            if (eatClip != null && eatClip.isRunning()) eatClip.stop();
            if (gameoverClip != null && gameoverClip.isRunning()) gameoverClip.stop();
            if (clearClip != null && clearClip.isRunning()) clearClip.stop();
            if (collideClip != null && collideClip.isRunning()) collideClip.stop();
            if (countdownClip != null && countdownClip.isRunning()) countdownClip.stop();
        } catch (Exception ex) {}
    }

    private void pauseGame() {
        if (paused) return;
        paused = true;
        if (gameLoop != null && gameLoop.isRunning()) {
            gameLoop.stop();
        }
        countdownWasRunning = (countdownTimer != null && countdownTimer.isRunning());
        countdownStartsGameSaved = false;
        if (countdownWasRunning) {
            countdownStartsGameSaved = !gameLoop.isRunning();
            countdownTimer.stop();
        }
        stopAllClips();
    }

    private void resumeGame() {
        if (!paused) return;
        paused = false;
        if (countdownWasRunning && countdownRemaining > 0) {
            startCountdownFromCurrent(countdownStartsGameSaved);
            countdownWasRunning = false;
        } else {
            if (gameLoop != null && !gameLoop.isRunning() && !gameOver) {
                gameLoop.start();
            }
            if (!muted && bgClip != null) {
                try { bgClip.loop(Clip.LOOP_CONTINUOUSLY); bgClip.start(); } catch (Exception ex) {}
            }
        }
    }

    private void startCountdownFromCurrent(boolean startGameWhenDone) {
        if (countdownTimer != null && countdownTimer.isRunning()) countdownTimer.stop();
        countdownTimer = new Timer(1050, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                repaint();
                countdownRemaining--;
                if (countdownRemaining > 1) {
                    playCountdownSound();
                }
                if (countdownRemaining <= 0) {
                    countdownTimer.stop();
                    if (startGameWhenDone) {
                        startGame();
                    } else {
                        countdownRemaining = -1;
                        if (gameLoop != null && !gameLoop.isRunning() && !gameOver) {
                            gameLoop.start();
                        }
                    }
                }
            }
        });
        countdownTimer.setInitialDelay(0);
        countdownTimer.start();
    }

    private void restartGame() {
        if (gameLoop != null && gameLoop.isRunning()) gameLoop.stop();
        stopAllClips();
        score = 0;
        lives = 3;
        gameOver = false;
        scoreRecorded = false;
        awaitingReturnKey = false;
        highscore = Leaderboard.getHighscore();
        loadMap();
        resetPositions();
        startCountdown();
    }

    private void toggleMute() {
        muted = !muted;
        if (muted) {
            stopAllClips();
        } else {
            if (!paused && !gameOver && bgClip != null) {
                try { bgClip.setFramePosition(0); bgClip.loop(Clip.LOOP_CONTINUOUSLY); bgClip.start(); } catch (Exception ex) {}
            }
        }
    }
    
    private void executePauseMenuAction(int action) {
        switch (action) {
            case 0: // Continue
                resumeGame();
                break;
            case 1: // Restart
                paused = false;
                restartGame();
                break;
            case 2: // Mute/Unmute
                toggleMute();
                repaint();
                break;
            case 3: // Menu
                paused = false;
                stopAllClips();
                if (gameLoop != null && gameLoop.isRunning()) gameLoop.stop();
                if (countdownTimer != null && countdownTimer.isRunning()) countdownTimer.stop();
                returnToMenu();
                break;
            case 4: // Exit
                System.exit(0);
                break;
        }
    }
    
    private void handlePauseMenuClick(int x, int y) {
        if (continueBtn != null && continueBtn.contains(x, y)) {
            executePauseMenuAction(0);
        } else if (restartBtn != null && restartBtn.contains(x, y)) {
            executePauseMenuAction(1);
        } else if (muteBtn != null && muteBtn.contains(x, y)) {
            executePauseMenuAction(2);
        } else if (menuBtn != null && menuBtn.contains(x, y)) {
            executePauseMenuAction(3);
        } else if (exitBtn != null && exitBtn.contains(x, y)) {
            executePauseMenuAction(4);
        }
    }

    public boolean collision(Block a, Block b) {
        return  a.x < b.x + b.width &&
                a.x + a.width > b.x &&
                a.y < b.y + b.height &&
                a.y + a.height > b.y;
    }


    public void resetPositions() {
        pakboy.reset();
        pakboy.velocityX = 0;
        pakboy.velocityY = 0;
        for (Block enemy : enemys) {
            enemy.reset();
            char newDirection = directions[random.nextInt(4)];
            enemy.updateDirection(newDirection);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            move();
        }

        if (gameOver && gameOverZooming) {
            gameOverScale += gameOverScaleIncrement;
            if (gameOverScale >= gameOverTargetScale) {
                gameOverZooming = true;
                gameLoop.stop();
                if (bgClip != null) {
                    try { bgClip.stop(); } catch (Exception ex) {}
                }
                awaitingReturnKey = true;
            }
        }

        repaint();
    }

    private void returnToMenu() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                java.awt.Window w = SwingUtilities.getWindowAncestor(PakBoy.this);
                if (w instanceof JFrame) {
                    JFrame frame = (JFrame) w;
                    frame.getContentPane().removeAll();
                    MenuPanel menu = new MenuPanel(frame, boardWidth, boardHeight);
                    frame.add(menu);
                    frame.revalidate();
                    frame.repaint();
                    menu.requestFocusInWindow();
                }
            }
        });
    }


    // CONTROLLERS 
    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {
        if (gameOver) {
            if (awaitingReturnKey && gameOverZooming) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    gameOverZooming = false;
                    repaint();
                }
                return;
            }
            if (!gameOverZooming && awaitingReturnKey) {
                awaitingReturnKey = false;
                returnToMenu();
                return;
            }
            return;
        }

        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            if (!paused) {
                pauseGame();
                selectedPauseButton = 0;
                repaint();
            } else {
                resumeGame();
            }
            return;
        }
        
        if (paused) {
            if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W) {
                selectedPauseButton = (selectedPauseButton - 1 + 5) % 5;
                repaint();
                return;
            }
            if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_S) {
                selectedPauseButton = (selectedPauseButton + 1) % 5;
                repaint();
                return;
            }
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                executePauseMenuAction(selectedPauseButton);
                return;
            }
            return;
        }

        if (countdownRemaining > 0) {
            return;
        }

        // WASD CONTROLS
        if (e.getKeyCode() == KeyEvent.VK_W) {
            pakboy.updateDirection('U');
        }
        if (e.getKeyCode() == KeyEvent.VK_S) {
            pakboy.updateDirection('D');
        }
        if (e.getKeyCode() == KeyEvent.VK_A) {
            pakboy.updateDirection('L');
        }
        if (e.getKeyCode() == KeyEvent.VK_D) {
            pakboy.updateDirection('R');
        }

        if (pakboy.direction == 'U') {
            pakboy.image = (selectedCharacter == 1) ? pakgirlUpImage : pakboyUpImage;
        }
        else if (pakboy.direction == 'D') {
            pakboy.image = (selectedCharacter == 1) ? pakgirlDownImage : pakboyDownImage;
        }
        else if (pakboy.direction == 'L') {
            pakboy.image = (selectedCharacter == 1) ? pakgirlLeftImage : pakboyLeftImage;
        }
        else if (pakboy.direction == 'R') {
            pakboy.image = (selectedCharacter == 1) ? pakgirlRightImage : pakboyRightImage;
    }
}
}

// JOSHOOWOOT 12/07/2025