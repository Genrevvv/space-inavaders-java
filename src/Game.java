public class Game {
    int tileSize;
    int rows, cols;
    int width, height;
     
    Game(int tileSize, int x, int y) {
        this.tileSize = tileSize;
        this.rows = y;
        this.cols = x;
        this.width = tileSize * this.cols;
        this.height = tileSize * this.rows;
    }
}