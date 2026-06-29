package chess;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Objects;

/**
 * A class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private TeamColor turn;
    private ChessBoard board;

    private boolean wKingMoved = false;
    private boolean bKingMoved = false;
    private boolean wLeftRookMoved = false;
    private boolean bLeftRookMoved = false;
    private boolean wRightRookMoved = false;
    private boolean bRightRookMoved = false;

    public ChessGame() {
        this.turn = TeamColor.WHITE;
        this.board = new ChessBoard();
        this.board.resetBoard();
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

    public int rowBasedColor(TeamColor color){
        if (color == TeamColor.WHITE){
            return 1;
        }
        return 8;
    }

    public boolean hasMoved(ChessPosition startPosition){
        ChessPiece piece = board.getPiece(startPosition);
        ChessPiece.PieceType type = piece.getPieceType();
        TeamColor color = piece.getTeamColor();

        if(type == ChessPiece.PieceType.KING){
            if (color == TeamColor.WHITE){
                return wKingMoved;
            }
            else {
                return bKingMoved;
            }
        }
        else if (type == ChessPiece.PieceType.ROOK){
            if (startPosition.equals(new ChessPosition(1, 1))){
                return wLeftRookMoved;
            }
            if (startPosition.equals(new ChessPosition(1, 8))){
                return wRightRookMoved;
            }
            if (startPosition.equals(new ChessPosition(8, 1))){
                return bLeftRookMoved;
            }
            if (startPosition.equals(new ChessPosition(8, 8))){
                return bRightRookMoved;
            }
        }
        return false;
    }

    private boolean isSquareUnderAttack(ChessPosition startPosition, TeamColor color) {
        ChessBoard copy = new ChessBoard(board);

        ChessPosition kingPos = findKingPosition(color);
        copy.addPiece(kingPos, null);

        copy.addPiece(startPosition, new ChessPiece(color, ChessPiece.PieceType.KING));

        ChessBoard saved = board;
        board = copy;
        boolean inCheck = isInCheck(color);
        board = saved;
        return inCheck;
    }

    public boolean leftCastle(ChessPosition leftRook, TeamColor color){
        int row = rowBasedColor(color);
        if (board.getPiece(leftRook).getPieceType() == ChessPiece.PieceType.ROOK && !hasMoved(leftRook)){
            for (int i = 2; i <= 4; i ++){
                ChessPosition tempPosition = new ChessPosition(row, i);
                if (board.getPiece(tempPosition) != null){
                    return false;
                }
                else if (i !=2 && isSquareUnderAttack(tempPosition, color)){
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public boolean rightCastle(ChessPosition rightRook, TeamColor color){
        int row = rowBasedColor(color);
        if (board.getPiece(rightRook).getPieceType() == ChessPiece.PieceType.ROOK && !hasMoved(rightRook)){
            for (int i = 6; i <= 7; i++){
                ChessPosition tempPosition = new ChessPosition(row, i);
                if (board.getPiece(tempPosition) != null || isSquareUnderAttack(tempPosition, color)){
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public Collection<ChessMove> findCastlingMoves(ChessPosition startPosition){
        Collection<ChessMove> castlingMoves = new ArrayList<>();

        ChessPiece piece = board.getPiece(startPosition);
        TeamColor color = piece.getTeamColor();
        int row = rowBasedColor(color);
        System.out.println("=== findCastlingMoves ===");
        System.out.println("isKing: " + (piece.getPieceType() == ChessPiece.PieceType.KING));
        System.out.println("hasMoved: " + hasMoved(startPosition));
        System.out.println("isInCheck: " + isInCheck(color));
        System.out.println("wKingMoved: " + wKingMoved);
        System.out.println("wLeftRookMoved: " + wLeftRookMoved);
        System.out.println("wRightRookMoved: " + wRightRookMoved);
        if(piece.getPieceType() == ChessPiece.PieceType.KING && !hasMoved(startPosition) && !isInCheck(color)){
            //check each of the rook sides:
            ChessPosition leftRook = new ChessPosition(row, 1), rightRook = new ChessPosition(row, 8);
            System.out.println("leftRookPiece: " + board.getPiece(leftRook));
            System.out.println("rightRookPiece: " + board.getPiece(rightRook));
            System.out.println("leftCastle: " + (board.getPiece(leftRook) != null && leftCastle(leftRook, color)));
            System.out.println("rightCastle: " + (board.getPiece(rightRook) != null && rightCastle(rightRook, color)));

            if(board.getPiece(leftRook) != null && leftCastle(leftRook, color)){
                ChessPosition newKingPosition = new ChessPosition(row, 3);
                ChessMove rightCastle = new ChessMove(startPosition, newKingPosition, null);
                castlingMoves.add(rightCastle);
            }

            if (board.getPiece(rightRook) != null && rightCastle(rightRook, color)){
                ChessPosition newKingPosition = new ChessPosition(row, 7);
                ChessMove rightCastle = new ChessMove(startPosition, newKingPosition, null);
                castlingMoves.add(rightCastle);
            }
        }

        return castlingMoves;
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

        Collection<ChessMove> castlingMoves = findCastlingMoves(startPosition);
        if (!castlingMoves.isEmpty()){
            validMoves.addAll(castlingMoves);
        }

        return validMoves;
    }

    public boolean noPieceWithValidMoves(TeamColor teamColor){
        for (int i = 1; i <= 8; i++){
            for (int j = 1; j <= 8; j++){
                ChessPosition position = new ChessPosition(i, j);
                ChessPiece piece = board.getPiece(position);

                if (piece != null && piece.getTeamColor() == teamColor){
                    Collection<ChessMove> moves = validMoves(position);

                    if (moves != null && moves.size() != 0){
                        return false;
                    }
                }
            }
        }
        return true;
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
       ChessPiece.PieceType type = piece.getPieceType();

       Collection<ChessMove> validMoves = validMoves(startPosition);
       TeamColor color = piece.getTeamColor();

       if (validMoves.contains(move) && turn == color){
            board.addPiece(startPosition, null);
            // check if a promotion is part of the move
            if (piece.getPieceType() == ChessPiece.PieceType.PAWN){
                if (piece.getPromotionRow(color) == endPosition.getRow()){
                    piece = new ChessPiece(color, move.getPromotionPiece());
                }
            }
            // check if the move is a castle, move rook to right spot
           if (piece.getPieceType() == ChessPiece.PieceType.KING){
               // left castle
               int row = startPosition.getRow();
               if(startPosition.getColumn() - endPosition.getColumn() == 2){
                   ChessPosition leftRookPosition = new ChessPosition(row, 1);
                   ChessPiece leftRook = board.getPiece(leftRookPosition);
                   ChessPosition newLeftRookPosition = new ChessPosition(row, 4);
                   board.addPiece(leftRookPosition, null);
                   board.addPiece(newLeftRookPosition, leftRook);
               }
               // right castle
               else if (endPosition.getColumn() - startPosition.getColumn() == 2){
                   ChessPosition rightRookPosition = new ChessPosition(row, 8);
                   ChessPiece rightRook = board.getPiece(rightRookPosition);
                   ChessPosition newLeftRookPosition = new ChessPosition(row, 6);
                   board.addPiece(rightRookPosition, null);
                   board.addPiece(newLeftRookPosition, rightRook);
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
           // marked things as moved
           if (type == ChessPiece.PieceType.KING) {
               if (color == TeamColor.WHITE) wKingMoved = true;
               else bKingMoved = true;
           }
           //check if we are moving from the starting position
           if (type == ChessPiece.PieceType.ROOK) {
               if (startPosition.equals(new ChessPosition(1, 1))){
                   wLeftRookMoved = true;
               }
               else if (startPosition.equals(new ChessPosition(1, 8))) {
                   wRightRookMoved = true;
               }
               else if (startPosition.equals(new ChessPosition(8, 1))){
                   bLeftRookMoved = true;
               }
               else if (startPosition.equals(new ChessPosition(8, 8))){
                   bRightRookMoved = true;
               }
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
        if (isInCheck(teamColor)){
            return noPieceWithValidMoves(teamColor);
        }
        return false;
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
        return noPieceWithValidMoves(teamColor);
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return turn == chessGame.turn && Objects.equals(board, chessGame.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(turn, board);
    }

    @Override
    public String toString() {
        return "ChessGame{" +
                "turn=" + turn +
                ", board=" + board +
                '}';
    }
}
