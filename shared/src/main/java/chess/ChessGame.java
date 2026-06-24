package chess;

import java.util.Collection;
import java.util.ArrayList;

/**
 * A class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private TeamColor turn;
    private ChessBoard board;


    public ChessGame() {
        this.turn = TeamColor.WHITE;
        this.board = new ChessBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return turn;
    }

    /**
     * Sets which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        turn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets all valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessBoard currentBoard = board;
        ChessPiece piece = board.getPiece(startPosition);

        if (piece == null) {return null;}

        Collection<ChessMove> allMoves = piece.pieceMoves(board, startPosition);
        TeamColor color = piece.getTeamColor();
        Collection<ChessMove> validMoves = new ArrayList<>();

        for (ChessMove move : allMoves){
            ChessBoard tempBoard = new ChessBoard(board);
            tempBoard.addPiece(move.getStartPosition(), null);
            tempBoard.addPiece(move.getEndPosition(), piece);
            setBoard(tempBoard);

            if (!isInCheck(color)){
                validMoves.add(move);
            }
            board = currentBoard;
        }

        return validMoves;
    }

    /**
     * Makes a move in the chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        // do i need to promote a piece when it is a pawn?
       ChessPosition startPosition = move.getStartPosition(), endPosition = move.getEndPosition();
       ChessPiece piece = board.getPiece(startPosition);

       if (piece == null){
           throw new InvalidMoveException("No piece is in provided start position");
       }

       Collection<ChessMove> allMoves = piece.pieceMoves(board, startPosition);
       TeamColor color = piece.getTeamColor();

       if (allMoves.contains(move) && turn == color){
            board.addPiece(startPosition, null);
            // check if a promotion is part of the move
            if (piece.getPieceType() == ChessPiece.PieceType.PAWN){
                if (piece.getPromotionRow(color) == endPosition.getRow()){
                    piece = new ChessPiece(color, move.getPromotionPiece());
                }
            }
            // in case we got rid of a piece by overwriting
            ChessPiece captured = board.getPiece(endPosition);
            board.addPiece(endPosition, piece);

            //check if we are in check in the new position
           if (isInCheck(color)){
               board.addPiece(startPosition, piece);
               board.addPiece(endPosition, captured);
               throw new InvalidMoveException();
           }
            // change team colors
           if (turn == TeamColor.WHITE){
               turn = TeamColor.BLACK;
           } else {
               turn = TeamColor.WHITE;
           }

       } else {
           throw new InvalidMoveException();
       }
    }

    public ChessPosition findKingPosition(TeamColor teamColor){
        for(int i = 1; i <= 8; i++){
            for(int j = 1; j <= 8;j++){
                ChessPosition position = new ChessPosition(i, j);

                ChessPiece piece = board.getPiece(position);
                if (piece == null) {continue;}

                TeamColor color = piece.getTeamColor();
                ChessPiece.PieceType type = piece.getPieceType();

                if (type == ChessPiece.PieceType.KING && color == teamColor){
                    return position;
                }
            }
        }
        return null;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        // pretend the king is a different piece, find all possible moves for that piece, if it encounters
        // a piece of that same type on enemy team, then it is in check
        ChessPosition kingPosition = findKingPosition(teamColor);
        ChessPiece king = board.getPiece(kingPosition);

        for (ChessPiece.PieceType type : ChessPiece.PieceType.values()){
            ChessPiece tempPiece = new ChessPiece(teamColor, type);
            board.addPiece(kingPosition, tempPiece);
            Collection<ChessMove> moves = tempPiece.pieceMoves(board, kingPosition);

            for (ChessMove move : moves){
                ChessPosition endPosition = move.getEndPosition();
                ChessPiece target = board.getPiece(endPosition);
                if (target == null) {continue;}

                if (teamColor != target.getTeamColor() && target.getPieceType() == type){
                    board.addPiece(kingPosition, king);
                    return true;
                }
            }
        }
        board.addPiece(kingPosition, king);
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)){
            return false;
        }
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)){
            return false;
        }
        throw new RuntimeException("Not implemented");
    }

    /**
     * Sets this game's chessboard to a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }


}
