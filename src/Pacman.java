import  java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import javax.swing.*;

public class Pacman extends JPanel implements ActionListener, KeyListener {

    // Blocos do mapa
    class Block {
        int x;
        int y;
        int width;
        int height;
        Image image;

        int startX;
        int startY;
        char direction = 'U'; // U (Up = Cima), D (Down = Baixo), L (Left = Esquerda), R (Right = Direita)
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
        // Direção
        void updateDirection(char direction){
            char prevDirection = this.direction;
            this.direction = direction;
            updateVelocity();
            this.x += this.velocityX;
            this.y += this.velocityY;
            for (Block wall : walls) {
                if (collision(this, wall)){
                    this.x -= this.velocityX;
                    this.y -= this.velocityY;
                    this.direction = prevDirection;
                    updateVelocity();
                }
            }
        }
        void updateVelocity(){
            if (this.direction == 'U'){
                this.velocityX = 0;
                this.velocityY = -tileSize/4;
            }
            else if (this.direction == 'D') {
                this.velocityX = 0;
                this.velocityY = +tileSize/4;
            }
            else if (this.direction == 'R') {
                this.velocityX = +tileSize/4;
                this.velocityY = 0;
            }
            else if (this.direction == 'L') {
                this.velocityX = -tileSize/4;
                this.velocityY = 0;
            }
        }
        void reset (){
            this.x = this.startX;
            this.y = this.startY;
        }
    }

    // Informando o Tamanho do Frame
    private int rowCount = 21;
    private int columnCount = 19;
    private int tileSize = 32;
    private int boardWidth = columnCount * tileSize;
    private int boardHeight = rowCount * tileSize;

    // Declarando as Imagens
    private Image wallImage;
    private Image blueGhostImage;
    private Image orangeGhostImage;
    private Image redGhostImage;
    private Image pinkGhostImage;

    private Image pacmanUpImage;
    private Image pacmanDownImage;
    private Image pacmanRightImage;
    private Image pacmanLeftImage;

    private Image cherryImage;
    private Image scaredGhostImage;

    // DEFININDO O MAPA
    //X = Parede, O = Pular, P = PACMAN, ' ' = Comida
    //Ghost: b = blue (azul), o = orange (laranja), p = pink (rosa), r = red (vermelho)
    private String[] tileMap = {
            "XXXXXXXXXXXXXXXXXXX",
            "X X      X      X X",
            "X XcXXXX X XXXX X X",
            "X                 X",
            "X XX X XXXXX X XX X",
            "X    X       X    X",
            "XXXX XXXX XXXX XXXX",
            "---X X       X X---",
            "XXXX X X r X X XXXX",
            "O00    XbpoX    00O",
            "XXXX X XXXXX X XXXX",
            "---X X       X X---",
            "XXXX X XXXXX X XXXX",
            "X        X        X",
            "X XX XXX X XXX XX X",
            "X  X     P     X  X",
            "XX X X XXXXX X X XX",
            "X    X   X   X    X",
            "X X    X X X    XcX",
            "X   XX       XX   X",
            "XXXXXXXXXXXXXXXXXXX"
    };

    // Usando o Hash
    HashSet<Block> walls;
    HashSet<Block> foods;
    HashSet<Block> ghosts;
    HashSet<Block> scaredGhosts;
    HashMap<Block, Image> ghostOriginalImages = new HashMap<>();
    HashSet<Block> cherrys;
    Block pacman;

    Timer gameLoop;
    char[] directions = {'U', 'D', 'R','L'};
    Random random = new Random();
    int score = 0;
    int lives = 3;
    boolean gameOver = false;

    Timer scaredTimer;
    boolean ghostsAreScared = false;

    // O tamanho do frame e cor do fundo.
    Pacman() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.BLACK);
        addKeyListener(this);
        setFocusable(true);

        // Carregando as Imagens
        wallImage = new ImageIcon(getClass().getResource("/resources/wall.png")).getImage();
        blueGhostImage = new ImageIcon(getClass().getResource("/resources/blueGhost.png")).getImage();
        orangeGhostImage = new ImageIcon(getClass().getResource("/resources/orangeGhost.png")).getImage();
        redGhostImage = new ImageIcon(getClass().getResource("/resources/redGhost.png")).getImage();
        pinkGhostImage = new ImageIcon(getClass().getResource("/resources/pinkGhost.png")).getImage();

        pacmanUpImage = new ImageIcon(getClass().getResource("/resources/pacmanUp.png")).getImage();
        pacmanDownImage = new ImageIcon(getClass().getResource("/resources/pacmanDown.png")).getImage();
        pacmanLeftImage = new ImageIcon(getClass().getResource("/resources/pacmanLeft.png")).getImage();
        pacmanRightImage = new ImageIcon(getClass().getResource("/resources/pacmanRight.png")).getImage();

        cherryImage = new ImageIcon(getClass().getResource("/resources/cherry.png")).getImage();
        scaredGhostImage = new ImageIcon(getClass().getResource("/resources/scaredGhost.png")).getImage();

        loadMap();
        for (Block ghost : ghosts){
            char newDirection = directions [random.nextInt(4)];
            ghost.updateDirection(newDirection);
        }
        gameLoop = new Timer(50, this);
        gameLoop.start();

        // Configuração do timer de 5 segundos
        scaredTimer = new Timer(5000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // volta os fantasmas ao normal
                ghosts.addAll(scaredGhosts);
                for (Block ghost : scaredGhosts) {
                    ghost.image = ghostOriginalImages.get(ghost);
                }
                scaredGhosts.clear();
                ghostsAreScared = false;
                scaredTimer.stop();
            }
        });
        scaredTimer.setRepeats(false);
    }


    // Definindo o mapa
    public void loadMap (){
        walls = new HashSet<Block>();
        foods = new HashSet<Block>();
        ghosts = new HashSet<Block>();
        cherrys = new HashSet<Block>();
        scaredGhosts = new HashSet<Block>();
        ghostOriginalImages.clear();

        for (int r = 0; r < rowCount; r++){
            for (int c = 0; c < columnCount; c++){
                String row = tileMap[r];
                char tileMapChar = row.charAt(c);

                int x = c*tileSize;
                int y = r*tileSize;

                if (tileMapChar == 'X') { // Parede substituindo o X
                    Block wall = new Block(wallImage, x, y, tileSize, tileSize);
                    walls.add(wall);
                }
                else if (tileMapChar == 'b') { // Fanstasma Azul
                    Block ghost = new Block(blueGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                    ghostOriginalImages.put(ghost, blueGhostImage);
                    }
                else if (tileMapChar == 'r') { // Fanstasma Vermelho
                    Block ghost = new Block(redGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                    ghostOriginalImages.put(ghost, redGhostImage);
                }
                else if (tileMapChar == 'p') { // Fanstasma Rosa
                    Block ghost = new Block(pinkGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                    ghostOriginalImages.put(ghost, pinkGhostImage);
                }
                else if (tileMapChar == 'o') { // Fanstasma Laranja
                    Block ghost = new Block(orangeGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                    ghostOriginalImages.put(ghost, orangeGhostImage);
                }
                else if (tileMapChar == 'P') { // Pacman
                    pacman = new Block(pacmanRightImage, x, y, tileSize, tileSize);
                }
                else if (tileMapChar == ' ') { // Comida
                    Block food = new Block(null, x + 14, y + 14, 4, 4);
                    foods.add(food);
                }
                else if (tileMapChar == 'c') { // Cereja
                    Block cherry = new Block(cherryImage, x, y, tileSize, tileSize);
                    cherrys.add(cherry);
                }
            }
        }
    }
    // "Pintando" as imagens na tela.
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }
    // Informando as imagens
    public void draw(Graphics g) {
        g.drawImage(pacman.image, pacman.x, pacman.y, pacman.width, pacman.height, null);

        for (Block ghost : ghosts){
            g.drawImage(ghost.image, ghost.x, ghost.y, ghost.width,ghost.height, null);
        }
        for (Block scaredGhost : scaredGhosts){
            g.drawImage(scaredGhost.image, scaredGhost.x, scaredGhost.y, scaredGhost.width,scaredGhost.height, null);
        }
        for (Block cherry : cherrys){
            g.drawImage(cherry.image, cherry.x, cherry.y, cherry.width,cherry.height, null);
        }
        for (Block wall : walls){
            g.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);
        }
        g.setColor(Color.WHITE);
        for (Block food : foods){
            g.fillRect(food.x, food.y, food.width, food.height);
        }
        // Pontuação e vidas
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        if (gameOver){
            g.drawString("GameOver: " + String.valueOf(score) + " / Para jogar novamente, pressione qualquer tecla", tileSize/2, tileSize/2);
        }
        else {
            g.drawString("Total de Vidas: " + String.valueOf(lives) + " / Pontuação: " + String.valueOf(score), tileSize/2, tileSize/2);
        }
    }

    // Movendo o Pacman
    public void move() {
        pacman.x += pacman.velocityX;
        pacman.y += pacman.velocityY;

        // Teleporte
        if (pacman.x < -tileSize) {
            pacman.x = boardWidth;
        } else if (pacman.x > boardWidth) {
            pacman.x = -tileSize;
        }

        // Colisão com a comida
        Block foodEaten = null;
        for (Block food : foods) {
            if (collision(pacman, food)) {
                foodEaten = food;
                score += 10;
            }
        }
        foods.remove(foodEaten);

        // Colisão com a cereja
        Block cherryEaten = null;
        for (Block cherry : cherrys) {
            if (collision(pacman, cherry)) {
                cherryEaten = cherry;
                score += 30;

                // Fantasmas ficam assustados
                ghostsAreScared = true;
                for (Block ghost : ghosts) {
                    ghost.image = scaredGhostImage;
                    scaredGhosts.add(ghost);
                }
                ghosts.clear();
                scaredTimer.restart();
            }
        }
        cherrys.remove(cherryEaten);

        if (foods.isEmpty() && cherrys.isEmpty()) {
            loadMap();
            resetPositions();
        }

        // Verificando se há colisões.
        for (Block wall : walls){
            if (collision(pacman, wall)) {
                pacman.x -= pacman.velocityX;
                pacman.y -= pacman.velocityY;
                break;
            }
        }
        // Colisões dos fantasmas e o pacman.
        for (Block ghost : ghosts) {
            if (collision(ghost, pacman)) {
                lives -= 1;
                if (lives == 0) {
                    gameOver = true;
                    return;
                }
                resetPositions();
            }
            ghost.x += ghost.velocityX;
            ghost.y += ghost.velocityY;

            //Colisão do fanstasma com a parede
            for (Block wall : walls) {
                if (collision(ghost, wall) || ghost.x <= 0 || ghost.x + ghost.width >= boardWidth) {
                    ghost.x -= ghost.velocityX;
                    ghost.y -= ghost.velocityY;
                    char newDirection = directions[random.nextInt(4)];
                    ghost.updateDirection(newDirection);
                    break;
                }
            }
        }

        // Movimento e colisão dos fantasmas assustados
        Block ghostEaten = null;
        for (Block scaredGhost : scaredGhosts) {
            scaredGhost.x += scaredGhost.velocityX;
            scaredGhost.y += scaredGhost.velocityY;

            for (Block wall : walls) {
                if (collision(scaredGhost, wall) || scaredGhost.x <= 0 || scaredGhost.x + scaredGhost.width >= boardWidth) {
                    scaredGhost.x -= scaredGhost.velocityX;
                    scaredGhost.y -= scaredGhost.velocityY;
                    char newDirection = directions[random.nextInt(4)];
                    scaredGhost.updateDirection(newDirection);
                    break;
                }
            }

            if (collision(pacman, scaredGhost)) {
                ghostEaten = scaredGhost;
                break;
            }
        }

        if (ghostEaten != null) {
            scaredGhosts.remove(ghostEaten);
            ghostEaten.reset();
            ghostEaten.image = ghostOriginalImages.get(ghostEaten);
            ghosts.add(ghostEaten);
            score += 50;
        }
    }

    // Colisões
    public boolean collision (Block a, Block b){
        return a.x < b.x + b.width &&
                a.x + a.width > b.x &&
                a.y < b.y + b.height &&
                a.y + a.height >b.y;
    }
    // Voltando ao inicio em caso de colisões
    public void resetPositions() {
        pacman.reset();
        pacman.velocityX = 0;
        pacman.velocityY = 0;
        for (Block ghost : ghosts) {
            ghost.reset();
            char newDirection = directions [random.nextInt(4)];
            ghost.updateDirection(newDirection);
        }
    }

    // Detecta atualizações e atualiza o mapa.
    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver){
            gameLoop.stop();
        }
    }

    // Definindo as teclas
    @Override
    public void keyTyped(KeyEvent e) {

    }
    @Override
    public void keyPressed(KeyEvent e) {

    }
    @Override
    public void keyReleased(KeyEvent e) {
        // Tecla para reiniciar
        if (gameOver) {
            loadMap();
            resetPositions();
            lives = 3;
            score = 0;
            gameOver = false;
            gameLoop.start();
        }
        // Teclas para se mover
        if (e.getKeyCode() == KeyEvent.VK_UP){
            pacman.updateDirection('U');
        }
        else if (e.getKeyCode() == KeyEvent.VK_DOWN){
            pacman.updateDirection('D');
        }
        else if (e.getKeyCode() == KeyEvent.VK_LEFT){
            pacman.updateDirection('L');
        }
        else if (e.getKeyCode() == KeyEvent.VK_RIGHT){
            pacman.updateDirection('R');
        }
        if (pacman.direction == 'U'){
            pacman.image = pacmanUpImage;
        }
        else if (pacman.direction == 'D'){
            pacman.image = pacmanDownImage;
        }
        else if (pacman.direction == 'R'){
            pacman.image = pacmanRightImage;
        }
        else if (pacman.direction == 'L'){
            pacman.image = pacmanLeftImage;
        }
    }
}

