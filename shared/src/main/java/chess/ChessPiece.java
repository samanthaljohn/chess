package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private final ChessGame.TeamColor pieceColor;
    private ChessPiece.PieceType type;


    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /** __________________________________________________ */

    public boolean inBoundsMove(ChessPosition position){
        int row = position.getRow(), col = position.getColumn();

        if (row < 1 || row > 8 || col < 1 || col > 8){
            return false;
        }
        return true;
    }

    public int getDirection(ChessGame.TeamColor color){
        if (color == ChessGame.TeamColor.WHITE) {
            return 1;
        } else {return -1;}
    }

    public int getStartRow(ChessGame.TeamColor color){
        if (color == ChessGame.TeamColor.WHITE) {
            return 2;
        } else {return 7;}
    }

    public int getPromotionRow(ChessGame.TeamColor color){
        if (color == ChessGame.TeamColor.WHITE) {
            return 8;
        } else {return 1;}
    }

    public boolean isEmptySquare(ChessBoard board, ChessPosition position){
        return board.getPiece(position) == null;
    }

    public boolean isEnemyPiece(ChessBoard board, ChessPosition position, ChessGame.TeamColor myColor) {
        if(isEmptySquare(board, position)){
            return false;
        }

        ChessPiece piece = board.getPiece(position);
        return piece.getTeamColor() != myColor;
    }

    public boolean isFriendlyPiece(ChessBoard board, ChessPosition position, ChessGame.TeamColor myColor){
        if(isEmptySquare(board, position)){
            return false;
        }

        ChessPiece piece = board.getPiece(position);
        return piece.getTeamColor() == myColor;
    }

    public Collection<ChessMove> getKingMoves(ChessBoard board, ChessPosition position, ChessGame.TeamColor color){
        Collection<ChessMove> moves = new ArrayList<>();
        int row = position.getRow(), col = position.getColumn();

        for (int i = -1; i <= 1; i += 2){
            for (int j = -1; j <= 1; j += 2){
                ChessPosition newPosition = new ChessPosition(row + i, col + j);

                if (inBoundsMove(newPosition) && (isEmptySquare(board, newPosition) || isEnemyPiece(board, newPosition, color))){
                    moves.add(new ChessMove(position, newPosition, null));
                }
            }
        }

        return moves;
    }

    public Collection<ChessMove> getQueenMoves(ChessBoard board, ChessPosition position, ChessGame.TeamColor color){
        Collection<ChessMove> moves = new ArrayList<>();

        moves.addAll(getBishopMoves(board, position, color));
        moves.addAll(getRookMoves(board, position, color));

        return moves;
    }

    public Collection<ChessMove> getBishopMoves(ChessBoard board, ChessPosition position, ChessGame.TeamColor color){
        Collection<ChessMove> moves = new ArrayList<>();
        int row = position.getRow(), col = position.getColumn();
        int[][] directions = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};

        for (int i = 0; i < 4; i ++){
            ChessPosition newPosition = new ChessPosition(row + directions[i][0], col + directions[i][1]);

            while(inBoundsMove(newPosition) && !isFriendlyPiece(board, newPosition, color)){
                moves.add(new ChessMove(position, newPosition, null));

                if (isEnemyPiece(board, newPosition, color)){
                    break;
                }

                newPosition = new ChessPosition(newPosition.getRow() + directions[i][0], newPosition.getColumn() + directions[i][1]);
            }
        }

        return moves;
    }

    public Collection<ChessMove> getKnightMoves(ChessBoard board, ChessPosition position, ChessGame.TeamColor color){
        Collection<ChessMove> moves = new ArrayList<>();
        int row = position.getRow(), col = position.getColumn();

        for (int i = -1; i <= 1; i += 2){
            for (int j = -1; j <= 1; j += 2){
                ChessPosition newPosition1 = new ChessPosition(row + (2*i), col + j);

                if (inBoundsMove(newPosition1) && (isEmptySquare(board, newPosition1) || isEnemyPiece(board, newPosition1, color))){
                    moves.add(new ChessMove(position, newPosition1, null));
                }

                ChessPosition newPosition2 = new ChessPosition(row + i, col + (j*2));

                if (inBoundsMove(newPosition2) && (isEmptySquare(board, newPosition2) || isEnemyPiece(board, newPosition2, color))){
                    moves.add(new ChessMove(position, newPosition2, null));
                }
            }
        }

        return moves;
    }

    public Collection<ChessMove> getRookMoves(ChessBoard board, ChessPosition position, ChessGame.TeamColor color){
        Collection<ChessMove> moves = new ArrayList<>();
        int row = position.getRow(), col = position.getColumn();
        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

        for(int i = 0; i < 4; i++){
            ChessPosition newPosition = new ChessPosition(row + directions[i][0], col + directions[i][1]);

            while (inBoundsMove(newPosition) && !isFriendlyPiece(board, newPosition, color)){
                moves.add(new ChessMove(position, newPosition, null));

                if(isEnemyPiece(board, newPosition, color)){
                    break;
                }

                newPosition = new ChessPosition(newPosition.getRow() + directions[i][0], newPosition.getColumn() + directions[i][1]);
            }
        }

        return moves;
    }

    public Collection<ChessMove> getPawnMoves(ChessBoard board, ChessPosition startPosition, ChessGame.TeamColor color){
        Collection<ChessMove> moves = new ArrayList<>();
        int row = startPosition.getRow(), col = startPosition.getColumn();
        int dir = getDirection(color);
        int startingRow = getStartRow(color);
        int promotionRow = getPromotionRow(color);

        ChessPosition forwardOne = new ChessPosition(row + dir, col);
        if (inBoundsMove(forwardOne) && isEmptySquare(board, forwardOne)){
            if (row + dir == getPromotionRow(color)){
                //add promotions... maybe use a function here??
                for (PieceType piece : PieceType.values()) {
                    if (piece != PieceType.PAWN && piece != PieceType.KING) {
                        moves.add(new ChessMove(startPosition, forwardOne, piece));
                    }
                }
            }
            else {
                moves.add(new ChessMove(startPosition, forwardOne, null));
            }

            if (row == startingRow){
                ChessPosition forwardTwo = new ChessPosition(row + (dir * 2), col);
                if (inBoundsMove(forwardTwo) && isEmptySquare(board, forwardTwo)){
                    moves.add(new ChessMove(startPosition, forwardTwo, null));
                }
            }
        }

        // captures - check left, then right
        for (int i = -1; i <= 1; i += 2) {
            ChessPosition capture = new ChessPosition(row + dir, col + i);

            if (inBoundsMove(capture) && isEnemyPiece(board, capture, color)) {
                if (promotionRow == row + dir) {
                    // add all promotion options
                    for (PieceType piece : PieceType.values()) {
                        if (piece != PieceType.PAWN && piece != PieceType.KING) {
                            moves.add(new ChessMove(startPosition, capture, piece));
                        }
                    }
                } else {
                    moves.add(new ChessMove(startPosition, capture, null));
                }
            }
        }

        return moves;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece piece = board.getPiece(myPosition);
        PieceType pieceType = piece.getPieceType();
        ChessGame.TeamColor color = piece.getTeamColor();

        return switch(pieceType){
            case KING -> getKingMoves(board, myPosition, color);
            case QUEEN -> getQueenMoves(board, myPosition, color);
            case BISHOP -> getBishopMoves(board, myPosition, color);
            case KNIGHT -> getKnightMoves(board, myPosition, color);
            case ROOK -> getRookMoves(board, myPosition, color);
            case PAWN -> getPawnMoves(board, myPosition, color);
        };
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }

    @Override
    public String toString() {
        switch (type) {
            case PieceType.KING -> {
                return "k";
            }
            case PieceType.QUEEN -> {
                return "q";
            }
            case PieceType.BISHOP -> {
                return "b";
            }
            case PieceType.KNIGHT -> {
                return "n";
            }
            case PieceType.ROOK -> {
                return "r";
            }
            case PieceType.PAWN -> {
                return "p";
            }
            default -> {
                return null;
            }
        }
    }
}
