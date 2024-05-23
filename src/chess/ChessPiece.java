package chess;

import boardgame.Board;
import boardgame.Piece;
import boardgame.Position;

public abstract class ChessPiece extends Piece {
    private Color color;
    private int moveCount;

    public ChessPiece(Board board, Color color) {
        super(board);
        this.color = color;  // Sets the color of the chess piece
    }

    public Color getColor() {
        return color;
    }

    public int getMoveCount() {
        return moveCount;  // Returns the number of moves the piece has made
    }

    public void increaseMoveCount() {
        moveCount++;  // Increases the move count
    }

    public void decreaseMoveCount() {
        moveCount--;  // Decreases the move count
    }

    public ChessPosition getChessPosition() {
        return ChessPosition.fromPosition(position);  // Converts the position to a ChessPosition
    }

    protected boolean isThereOpponentPiece(Position position) {
        ChessPiece p = (ChessPiece) getBoard().piece(position);  // Gets the piece at the specified position
        return p != null && p.getColor() != color;  // Checks if there is an opponent piece at the position
    }
}
