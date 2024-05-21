package chess.pieces;

import boardgame.Board;
import boardgame.Position;
import chess.ChessMatch;
import chess.ChessPiece;
import chess.Color;

public class King extends ChessPiece {

    private ChessMatch chessMatch;

    public King(Board board, Color color, ChessMatch chessMatch) {
        super(board, color);
        this.chessMatch = chessMatch;
    }

    private boolean testRookCastling(Position position) {
        ChessPiece p = (ChessPiece) getBoard().piece(position);
        return p instanceof Rook && p.getColor() == getColor() && p.getMoveCount() == 0;
    }

    @Override
    public String toString() {
        return "K";
    }

    private boolean canMove(Position position) {
        ChessPiece p = (ChessPiece)getBoard().piece(position);
        return p == null || p.getColor() != getColor();
    }

    @Override
    public boolean[][] possibleMoves() {
        boolean[][] mat = new boolean[getBoard().getRows()][getBoard().getColumns()];

        Position p = new Position(0, 0);

        // Above
        p.setValues(position.getRow() - 1, position.getColumn());
        if (getBoard().positionExists(p) && canMove(p))  mat[p.getRow()][p.getColumn()] = true;

        // Below
        p.setValues(position.getRow() + 1, position.getColumn());
        if (getBoard().positionExists(p) && canMove(p)) mat[p.getRow()][p.getColumn()] = true;

        // Left
        p.setValues(position.getRow(), position.getColumn() - 1);
        if (getBoard().positionExists(p) && canMove(p)) mat[p.getRow()][p.getColumn()] = true;

        // Right
        p.setValues(position.getRow(), position.getColumn() + 1);
        if (getBoard().positionExists(p) && canMove(p)) mat[p.getRow()][p.getColumn()] = true;

        // Northwest
        p.setValues(position.getRow() - 1, position.getColumn() - 1);
        if (getBoard().positionExists(p) && canMove(p)) mat[p.getRow()][p.getColumn()] = true;

        // Northeast
        p.setValues(position.getRow() - 1, position.getColumn() + 1);
        if (getBoard().positionExists(p) && canMove(p)) mat[p.getRow()][p.getColumn()] = true;

        // Southwest
        p.setValues(position.getRow() + 1, position.getColumn() - 1);
        if (getBoard().positionExists(p) && canMove(p)) mat[p.getRow()][p.getColumn()] = true;

        // Southeast
        p.setValues(position.getRow() + 1, position.getColumn() + 1);
        if (getBoard().positionExists(p) && canMove(p)) mat[p.getRow()][p.getColumn()] = true;

        // #Specialmove Castling
        if (getMoveCount() == 0 && !chessMatch.getCheck()) {
            // #Specialmove Castling King side Rook
            Position posT1 = new Position(position.getRow(), position.getColumn() + 3);
            if (testRookCastling(posT1)) {
                Position p1 = new Position(position.getRow(), position.getColumn() + 1);
                Position p2 = new Position(position.getRow(), position.getColumn() + 2);
                if (getBoard().piece(p1) == null && getBoard().piece(p2) == null) {
                    mat[position.getRow()][position.getColumn() + 2] = true;
                }
            }
            // #Specialmove Castling Queen side Rook
            Position posT2 = new Position(position.getRow(), position.getColumn() - 4);
            if (testRookCastling(posT2)) {
                Position p1 = new Position(position.getRow(), position.getColumn() - 1);
                Position p2 = new Position(position.getRow(), position.getColumn() - 2);
                Position p3 = new Position(position.getRow(), position.getColumn() - 3);
                if (getBoard().piece(p1) == null && getBoard().piece(p2) == null && getBoard().piece(p3) == null) {
                    mat[position.getRow()][position.getColumn() - 2] = true;
                }
            }
        }

        return mat;
    }
}
