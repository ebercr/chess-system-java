package chess;

import boardgame.Board;
import boardgame.Piece;
import boardgame.Position;
import chess.pieces.*;

import java.util.ArrayList;
import java.util.List;

public class ChessMatch {

    private int turn;
    private Color currentPlayer;
    private Board board;
    private boolean check;
    private boolean checkMate;
    private ChessPiece enPassantVulnerable;
    private ChessPiece promoted;

    private List<Piece> piecesOnTheBoard = new ArrayList<>();
    private List<Piece> capturedPieces = new ArrayList<>();

    public ChessMatch() {
        board = new Board(8, 8);  // Initializes an 8x8 board
        turn = 1;  // Game starts at turn 1
        currentPlayer = Color.WHITE;  // White starts the game
        initialSetup();  // Sets up the initial board configuration
    }

    public int getTurn() {
        return turn;
    }

    public Color getCurrentPlayer() {
        return currentPlayer;
    }

    public boolean getCheck() {
        return check;
    }

    public boolean getCheckMate() {
        return checkMate;
    }

    public ChessPiece getEnPassantVulnerable() {
        return enPassantVulnerable;
    }

    public ChessPiece getPromoted() {
        return promoted;
    }

    public ChessPiece[][] getPieces() {
        ChessPiece[][] mat = new ChessPiece[board.getRows()][board.getColumns()];
        for (int i = 0; i < board.getRows(); i++) {
            for (int j = 0; j < board.getColumns(); j++) {
                mat[i][j] = (ChessPiece) board.piece(i, j);  // Casts each piece on the board to ChessPiece
            }
        }
        return mat;
    }

    public boolean[][] possibleMoves(ChessPosition sourcePosition) {
        Position position = sourcePosition.toPosition();  // Converts ChessPosition to Position
        validateSourcePosition(position);  // Validates the source position
        return board.piece(position).possibleMoves();  // Returns the possible moves for the piece at the source position
    }

    public ChessPiece performChessMove(ChessPosition sourcePosition, ChessPosition targetPosition) {
        Position source = sourcePosition.toPosition();  // Converts source ChessPosition to Position
        Position target = targetPosition.toPosition();  // Converts target ChessPosition to Position
        validateSourcePosition(source);  // Validates the source position
        validateTargetPosition(source, target);  // Validates the target position
        Piece capturedPiece = makeMove(source, target);  // Makes the move and captures any piece on the target position

        if (testCheck(currentPlayer)) {  // Checks if the current player's move puts their king in check
            undoMove(source, target, capturedPiece);  // Undo the move if it puts the player in check
            throw new ChessException("You can't put yourself in check");
        }

        ChessPiece movedPiece = (ChessPiece) board.piece(target);  // Gets the moved piece

        // Special move: Promotion
        promoted = null;
        if (movedPiece instanceof Pawn) {
            if (movedPiece.getColor() == Color.WHITE && target.getRow() == 0 || movedPiece.getColor() == Color.BLACK && target.getRow() == 7) {
                promoted = (ChessPiece) board.piece(target);
                promoted = replacePromotedPiece("Q");  // Automatically promotes to Queen
            }
        }

        check = testCheck(opponent(currentPlayer));  // Checks if the opponent is in check

        if (testCheckMate(opponent(currentPlayer))) {
            checkMate = true;  // Checks if the opponent is in checkmate
        } else {
            nextTurn();  // Proceeds to the next turn if not in checkmate
        }

        // Special move: En Passant
        if (movedPiece instanceof Pawn && (target.getRow() == source.getRow() - 2 || target.getRow() == source.getRow() + 2)) {
            enPassantVulnerable = movedPiece;  // Sets the pawn that is vulnerable to en passant
        } else {
            enPassantVulnerable = null;
        }

        return (ChessPiece) capturedPiece;  // Returns the captured piece, if any
    }

    public ChessPiece replacePromotedPiece(String type) {
        if (promoted == null) throw new IllegalStateException("There is no piece to be promoted");
        if (!type.equals("B") && !type.equals("N") && !type.equals("R") && !type.equals("Q")) {
            return promoted;  // Only allows promotion to Bishop, Knight, Rook, or Queen
        }

        Position pos = promoted.getChessPosition().toPosition();  // Gets the position of the promoted piece
        Piece p = board.removePiece(pos);  // Removes the promoted piece from the board
        piecesOnTheBoard.remove(p);

        ChessPiece newPiece = newPiece(type, promoted.getColor());  // Creates the new piece of the specified type
        board.placePiece(newPiece, pos);  // Places the new piece on the board
        piecesOnTheBoard.add(newPiece);

        return newPiece;
    }

    private ChessPiece newPiece(String type, Color color) {
        return switch (type) {
            case "B" -> new Bishop(board, color);
            case "N" -> new Knight(board, color);
            case "R" -> new Rook(board, color);
            default -> new Queen(board, color);  // Defaults to Queen if type is not recognized
        };
    }

    private Piece makeMove(Position source, Position target) {
        ChessPiece p = (ChessPiece) board.removePiece(source);  // Removes the piece from the source position
        p.increaseMoveCount();  // Increases the move count of the piece
        Piece capturedPiece = board.removePiece(target);  // Captures any piece on the target position
        board.placePiece(p, target);  // Places the piece on the target position
        if (capturedPiece != null) {
            piecesOnTheBoard.remove(capturedPiece);  // Removes the captured piece from the list
            capturedPieces.add(capturedPiece);  // Adds the captured piece to the captured pieces list
        }

        // Special move: Castling King side Rook
        if (p instanceof King && target.getColumn() == source.getColumn() + 2) {
            Position sourceT = new Position(source.getRow(), source.getColumn() + 3);
            Position targetT = new Position(source.getRow(), source.getColumn() + 1);
            ChessPiece rook = (ChessPiece) board.removePiece(sourceT);
            board.placePiece(rook, targetT);
            rook.increaseMoveCount();
        }

        // Special move: Castling Queen side Rook
        if (p instanceof King && target.getColumn() == source.getColumn() - 2) {
            Position sourceT = new Position(source.getRow(), source.getColumn() - 4);
            Position targetT = new Position(source.getRow(), source.getColumn() - 1);
            ChessPiece rook = (ChessPiece) board.removePiece(sourceT);
            board.placePiece(rook, targetT);
            rook.increaseMoveCount();
        }

        // Special move: En Passant
        if (p instanceof Pawn) {
            if (source.getColumn() != target.getColumn() && capturedPiece == null) {
                Position pawnPosition;
                if (p.getColor() == Color.WHITE) {
                    pawnPosition = new Position(target.getRow() + 1, target.getColumn());
                } else {
                    pawnPosition = new Position(target.getRow() - 1, target.getColumn());
                }
                capturedPiece = board.removePiece(pawnPosition);
                capturedPieces.add(capturedPiece);
                piecesOnTheBoard.remove(capturedPiece);
            }
        }

        return capturedPiece;  // Returns the captured piece
    }

    private void undoMove(Position source, Position target, Piece capturedPiece) {
        ChessPiece p = (ChessPiece) board.removePiece(target);  // Removes the piece from the target position
        p.decreaseMoveCount();  // Decreases the move count of the piece
        board.placePiece(p, source);  // Places the piece back to the source position

        if (capturedPiece != null) {
            board.placePiece(capturedPiece, target);  // Restores the captured piece to the target position
            capturedPieces.remove(capturedPiece);  // Removes the piece from the captured list
            piecesOnTheBoard.add(capturedPiece);  // Adds the piece back to the board
        }

        // Undo Castling King side Rook
        if (p instanceof King && target.getColumn() == source.getColumn() + 2) {
            Position sourceT = new Position(source.getRow(), source.getColumn() + 3);
            Position targetT = new Position(source.getRow(), source.getColumn() + 1);
            ChessPiece rook = (ChessPiece) board.removePiece(targetT);
            board.placePiece(rook, sourceT);
            rook.decreaseMoveCount();
        }

        // Undo Castling Queen side Rook
        if (p instanceof King && target.getColumn() == source.getColumn() - 2) {
            Position sourceT = new Position(source.getRow(), source.getColumn() - 4);
            Position targetT = new Position(source.getRow(), source.getColumn() - 1);
            ChessPiece rook = (ChessPiece) board.removePiece(targetT);
            board.placePiece(rook, sourceT);
            rook.decreaseMoveCount();
        }

        // Undo En Passant
        if (p instanceof Pawn) {
            if (source.getColumn() != target.getColumn() && capturedPiece == enPassantVulnerable) {
                ChessPiece pawn = (ChessPiece) board.removePiece(target);
                Position pawnPosition;
                if (p.getColor() == Color.WHITE) {
                    pawnPosition = new Position(3, target.getColumn());
                } else {
                    pawnPosition = new Position(4, target.getColumn());
                }
                board.placePiece(pawn, pawnPosition);
            }
        }
    }

    private void validateSourcePosition(Position position) {
        if (!board.thereIsPiece(position)) throw new ChessException("There is no piece on source position");  // Ensures there is a piece at the source position
        if (currentPlayer != ((ChessPiece) board.piece(position)).getColor()) throw new ChessException("The chosen piece is not yours");  // Ensures the piece belongs to the current player
        if (!board.piece(position).isThereAnyPossibleMove()) throw new ChessException("There is no possible moves for the chosen piece");  // Ensures the piece has any valid move
    }

    private void validateTargetPosition(Position source, Position target) {
        if (!board.piece(source).possibleMove(target)) throw new ChessException("The chosen piece can't move to target position");  // Ensures the move to the target position is valid
    }

    private void nextTurn() {
        turn++;
        currentPlayer = (currentPlayer == Color.WHITE) ? Color.BLACK : Color.WHITE;  // Switches the current player
    }

    private Color opponent(Color color) {
        return (color == Color.WHITE) ? Color.BLACK : Color.WHITE;  // Returns the opponent's color
    }

    private ChessPiece king(Color color) {
        List<Piece> list = piecesOnTheBoard.stream().filter(x -> ((ChessPiece) x).getColor() == color).toList();
        for (Piece p : list) {
            if (p instanceof King) return (ChessPiece) p;  // Finds and returns the king of the specified color
        }
        throw new IllegalStateException("There is no " + color + " king on the board");  // Throws an exception if no king is found
    }

    private boolean testCheck(Color color) {
        Position kingPosition = king(color).getChessPosition().toPosition();  // Gets the position of the king
        List<Piece> opponentPieces = piecesOnTheBoard.stream().filter(x -> ((ChessPiece) x).getColor() == opponent(color)).toList();
        for (Piece p : opponentPieces) {
            boolean[][] mat = p.possibleMoves();  // Gets possible moves for each opponent piece
            if (mat[kingPosition.getRow()][kingPosition.getColumn()]) return true;  // Checks if any opponent piece can attack the king
        }
        return false;  // Returns false if the king is not in check
    }

    private boolean testCheckMate(Color color) {
        if (!testCheck(color)) return false;  // Returns false if the king is not in check
        List<Piece> list = piecesOnTheBoard.stream().filter(x -> ((ChessPiece) x).getColor() == color).toList();
        for (Piece p : list) {
            boolean[][] mat = p.possibleMoves();  // Gets possible moves for each piece of the specified color
            for (int i = 0; i < board.getRows(); i++) {
                for (int j = 0; j < board.getColumns(); j++) {
                    if (mat[i][j]) {
                        Position source = ((ChessPiece) p).getChessPosition().toPosition();
                        Position target = new Position(i, j);
                        Piece capturedPiece = makeMove(source, target);  // Makes the move
                        boolean testCheck = testCheck(color);  // Checks if the king is still in check
                        undoMove(source, target, capturedPiece);  // Undoes the move
                        if (!testCheck) return false;  // Returns false if there is a valid move that gets the king out of check
                    }
                }
            }
        }
        return true;  // Returns true if no valid move gets the king out of check (checkmate)
    }

    private void placeNewPiece(char column, int row, ChessPiece piece) {
        board.placePiece(piece, new ChessPosition(column, row).toPosition());  // Places a new piece on the board
        piecesOnTheBoard.add(piece);  // Adds the piece to the list of pieces on the board
    }

    private void initialSetup() {
        // Places all pieces in their initial positions
        placeNewPiece('a', 1, new Rook(board, Color.WHITE));
        placeNewPiece('b', 1, new Knight(board, Color.WHITE));
        placeNewPiece('c', 1, new Bishop(board, Color.WHITE));
        placeNewPiece('d', 1, new Queen(board, Color.WHITE));
        placeNewPiece('e', 1, new King(board, Color.WHITE, this));
        placeNewPiece('f', 1, new Bishop(board, Color.WHITE));
        placeNewPiece('g', 1, new Knight(board, Color.WHITE));
        placeNewPiece('h', 1, new Rook(board, Color.WHITE));
        for (char c = 'a'; c <= 'h'; c++) {
            placeNewPiece(c, 2, new Pawn(board, Color.WHITE, this));
        }

        placeNewPiece('a', 8, new Rook(board, Color.BLACK));
        placeNewPiece('b', 8, new Knight(board, Color.BLACK));
        placeNewPiece('c', 8, new Bishop(board, Color.BLACK));
        placeNewPiece('d', 8, new Queen(board, Color.BLACK));
        placeNewPiece('e', 8, new King(board, Color.BLACK, this));
        placeNewPiece('f', 8, new Bishop(board, Color.BLACK));
        placeNewPiece('g', 8, new Knight(board, Color.BLACK));
        placeNewPiece('h', 8, new Rook(board, Color.BLACK));
        for (char c = 'a'; c <= 'h'; c++) {
            placeNewPiece(c, 7, new Pawn(board, Color.BLACK, this));
        }
    }
}
