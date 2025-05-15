import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import javax.swing.*;

public class SpaceInvaders extends JPanel implements ActionListener, KeyListener {
    private Game game;
    private int tileSize;
    private int rows, cols;
    private int borderWidth;
    private Timer gameLoop;

    private Block ship;
    private int shipVelocityX;
    private boolean moveLeft, moveRight, shootBullet;

    private ArrayList<Block> alienArray;
    private ArrayList<Block> bulletArray;

    private int alienWidth, alienHeight;
    private int alienX, alienY;
    private int alienVelocityX = 2;
    private int alienCols = 3;
    private int alienRows = 2;

    int bulletVelocityY = -10;
    private long lastShotTime = 0;
    private int bulletCD = 100;

    private HashMap<String, Image> sprites = new HashMap<>();
    private ArrayList<String> spriteKeys;

    private int score = 0;
    private int scoreInc = 10;
    private boolean gameOver = false;

    SpaceInvaders(Game game) {
        this.game = game;
        tileSize = game.tileSize;
        rows = game.rows;
        cols = game.cols;
        borderWidth = tileSize * cols;
        shipVelocityX = tileSize / 2;

        alienWidth = tileSize*2;
        alienHeight = tileSize;
        alienX = tileSize;
        alienY = tileSize;

        setPreferredSize(new Dimension(game.width, game.height));
        setBackground(Color.black);
        setFocusable(true);
        addKeyListener(this);

        setDoubleBuffered(true);

        loadSprites();
        spriteKeys = new ArrayList<>(sprites.keySet());
        spriteKeys.removeIf(key -> key.contains("ship"));

        ship = new Block(
            tileSize * cols / 2 - tileSize,
            tileSize * rows - tileSize * 2,
            tileSize * 2,
            tileSize,
            sprites.get("ship")
        );

        bulletArray = new ArrayList<>();
        alienArray = new ArrayList<>();

        createAliens();

        gameLoop = new Timer(1000 / 60, this);
        gameLoop.start();
    }

    private void loadSprites() {
        sprites.put("ship", new ImageIcon(getClass().getResource("/img/ship.png")).getImage());
        sprites.put("alien-white", new ImageIcon(getClass().getResource("/img/alien-white.png")).getImage());
        sprites.put("alien-cyan", new ImageIcon(getClass().getResource("/img/alien-cyan.png")).getImage());
        sprites.put("alien-magenta", new ImageIcon(getClass().getResource("/img/alien-magenta.png")).getImage());
        sprites.put("alien-yellow", new ImageIcon(getClass().getResource("/img/alien-yellow.png")).getImage());
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    private void draw(Graphics g) {
        g.drawImage(ship.img, ship.x, ship.y, ship.width, ship.height, null);

        Block alien;
        for (int i = 0; i < alienArray.size(); i++) {
            alien = alienArray.get(i);
            g.drawImage(alien.img, alien.x, alien.y, alien.width, alien.height, null);
        }

        g.setColor(Color.white);

        Block bullet;
        for (int i = 0; i < bulletArray.size(); i++) {
            bullet = bulletArray.get(i);
            g.drawRect(bullet.x, bullet.y, bullet.width, bullet.height);
        }

        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.PLAIN, 32));
        if (!gameOver) {
            g.drawString(String.valueOf(score), 10, 30);
        }
        else {            
            g.drawString("Score: " + String.valueOf(score), 10, 30);
            
            String message;
            FontMetrics fm;
            int x, y;

            message = "Game Over";
            fm = g.getFontMetrics();
            x = (getWidth() - fm.stringWidth(message)) / 2;
            y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g.drawString(message, x, y - 32);

            message = "Press the space key to restart";
            x = (getWidth() - fm.stringWidth(message)) / 2;
            g.drawString(message, x, y);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameOver) {
            gameLoop.stop();
            return;
        }

        if (moveLeft && ship.x - shipVelocityX >= 0) {
            ship.x -= shipVelocityX;
        }
        else if (moveRight && ship.x + ship.width + shipVelocityX <= borderWidth) {
            ship.x += shipVelocityX;
        }

        long currentTime = System.currentTimeMillis();

        if (shootBullet && currentTime - lastShotTime >= bulletCD) {
            Block bullet = new Block(ship.x + ship.width*15/32, ship.y, tileSize/8, tileSize/2, null);
            bulletArray.add(bullet);
                
            lastShotTime = currentTime;
        }

        moveAlien();
        moveBullet();
        repaint();
        Toolkit.getDefaultToolkit().sync();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (gameOver && key == KeyEvent.VK_SPACE) {            
            resetGame();
            gameLoop.start();
        }

        if (key == KeyEvent.VK_A) {
            moveLeft = true;
        }

        if (key == KeyEvent.VK_D) {
            moveRight = true;
        }

        if (key == KeyEvent.VK_ENTER) {
            shootBullet = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_A) {
            moveLeft = false;
        }

        if (key == KeyEvent.VK_D) {
            moveRight = false;
        }

        if (key == KeyEvent.VK_ENTER) {
            shootBullet = false;
        }
    }
    
    @Override
    public void keyTyped(KeyEvent e) {}


    private void moveAlien() {
        Block alien;

        for (int i = 0; i < alienArray.size(); i++) {
            alien = alienArray.get(i);
            alien.x += alienVelocityX;

            if (alien.x <= 0 || alien.x + alien.width >= borderWidth) {
                alienVelocityX *= -1;
                alien.x += alienVelocityX * 2;
            }
        }

        if (alienArray.size() == 0) {
            alienCols += 1;
            alienRows += 1;
            alienVelocityX = 2;

            bulletArray.clear(); 

            System.out.println(alienCols + " * " + alienWidth + " = " + alienCols*alienWidth);

            if (alienCols * alienWidth >= game.width) {
                gameOver = true;
                return;
            }
            
            createAliens();
        }
    }

    private void moveBullet() {
        Block alien;
        Block bullet;
        
        for (int i = 0; i < bulletArray.size(); i++) {
            bullet = bulletArray.get(i);
            bullet.y += bulletVelocityY;

            for (int j = 0; j < alienArray.size(); j++) {
                alien = alienArray.get(j);
                if (detectCollision(bullet, alien)) {
                    bulletArray.remove(i);
                    alienArray.remove(j);

                    score += scoreInc;
                }
            }
        }

        while (bulletArray.size() > 0) {
            bullet = bulletArray.get(0);
            if (bullet.y <= 0) {
                bulletArray.remove(0);
            }
            else {
                break;
            }
        }
    }

    private void createAliens() {
        Random random = new Random();
        Block alien;
        int distance = 0;

        for (int row = 0; row < alienRows; row++) {
            
            for (int col = 0; col < alienCols; col++) {
                alien = new Block(
                    alienX + col * alienWidth,
                    (alienY + distance) + row * alienHeight,
                    alienWidth,
                    alienHeight,
                    sprites.get(spriteKeys.get(random.nextInt(spriteKeys.size())))
                );

                alienArray.add(alien);
            }

            distance += 10;
        }
    }

    private boolean detectCollision(Block a, Block b) {
        return (a.x <= b.x + b.width && a.x >= b.x) && (a.y <= b.y + b.height && a.y >= b.y );   
    }

    private void resetGame() {
        gameOver = false;
        score = 0;
        alienCols = 3;
        alienRows = 2;
        alienVelocityX = 2;
        
        createAliens();
    }


    class Block {
        int x, y, width, height;
        Image img;

        Block(int x, int y, int width, int height, Image img) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.img = img;
        }
    }
}
