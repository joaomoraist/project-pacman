import javax.swing.JFrame;

public class Main {
    // Declarando o tamanho do Frame
    public static void main(String[] args) {
       int rowCount = 21;
       int columnCount = 19;
       int tileSize = 32;
       int boardWidth = columnCount * tileSize;
       int boardHeight = rowCount * tileSize;

       JFrame frame = new JFrame("PACMAN");
       // frame.setVisible(true);
       frame.setSize(boardWidth, boardHeight);
       frame.setLocationRelativeTo(null);
       frame.setResizable(false);
       frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

       Pacman pacmanGame = new Pacman();
       frame.add(pacmanGame);
       frame.pack();
       pacmanGame.requestFocus();
       frame.setVisible(true);
    }
}