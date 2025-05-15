import javax.swing.JFrame;

public class App {
    private static Game game = new Game(32, 16, 16);
    
    public static void main(String[] args) {
        JFrame frame = new JFrame("Space Invaders");
        frame.setSize(game.width, game.height);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setAlwaysOnTop(true);
        
        SpaceInvaders spaceInvaders = new SpaceInvaders(game);
        spaceInvaders.requestFocus();
        
        frame.add(spaceInvaders);
        frame.pack();

        frame.setVisible(true);
    }
}