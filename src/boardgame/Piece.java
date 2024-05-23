package boardgame;

public abstract class Piece {
    protected Position position;
    private Board board;

    public Piece(Board board) {
        this.board = board;
        position = null;  // A new piece is not placed on the board initially
    }

    protected Board getBoard() {
        return board;  // Returns the board the piece belongs to
    }

    public abstract boolean[][] possibleMoves();  // Abstract method to be implemented by each specific piece

    public boolean possibleMove(Position position) {
        return possibleMoves()[position.getRow()][position.getColumn()];  // Checks if the piece can move to the specified position
    }

    public boolean isThereAnyPossibleMove() {
        boolean[][] mat = possibleMoves();
        for (int i = 0; i < mat.length; i++) {
            for (int j = 0; j < mat[i].length; j++) {
                if (mat[i][j]) {
                    return true;  // Returns true if there is at least one possible move
                }
            }
        }
        return false;  // Returns false if no moves are possible
    }
}
