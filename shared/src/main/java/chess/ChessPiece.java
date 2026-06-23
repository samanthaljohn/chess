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

    public boolean isEnemyPiece(ChessBoard board, ChessPosition enemyPosition, ChessGame.TeamColor color) {
        ChessPiece piece = board.getPiece(enemyPosition);
        return piece.getTeamColor() != color;
    }

    public Collection<ChessMove> getKingMoves(ChessBoard board, ChessPosition position, ChessGame.TeamColor color){
        Collection<ChessMove> moves = new ArrayList<>();
        int row = position.getRow(), col = position.getColumn();

        for(int i = -1; i <= 1; i++){
            for(int j = -1; j <= 1; j++){
                if (i == 0 && j == 0){
                    continue;
                }

                ChessPosition newPosition = new ChessPosition(row + i, col + j);
                if (inBoundsMove(newPosition) && (board.getPiece(newPosition) == null || isEnemyPiece(board, newPosition, color))){
                    moves.add(new ChessMove(position, newPosition, null));
                }
            }
        }

        return moves;
    }

    public Collection<ChessMove> getQueenMoves(ChessBoard board, ChessPosition position, ChessGame.TeamColor color){
        throw new RuntimeException("Not implemented");
    }

    public Collection<ChessMove> getBishopMoves(ChessBoard board, ChessPosition position, ChessGame.TeamColor color){
        throw new RuntimeException("Not implemented");
    }

    public Collection<ChessMove> getKnightMoves(ChessBoard board, ChessPosition position, ChessGame.TeamColor color){
        throw new RuntimeException("Not implemented");
    }

    public Collection<ChessMove> getRookMoves(ChessBoard board, ChessPosition position, ChessGame.TeamColor color){
        throw new RuntimeException("Not implemented");
    }

    public Collection<ChessMove> getPawnMoves(ChessBoard board, ChessPosition startPosition, ChessGame.TeamColor color){
        Collection<ChessMove> moves = new ArrayList<>();
        int row = startPosition.getRow(), col = startPosition.getColumn();
        int dir = getDirection(color);
        int startingRow = getStartRow(color);
        int promotionRow = getPromotionRow(color);

        ChessPosition forwardOne = new ChessPosition(row + dir, col);
        if (inBoundsMove(forwardOne) && board.getPiece(forwardOne) == null){
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
                if (inBoundsMove(forwardTwo) && board.getPiece(forwardTwo) == null){
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
