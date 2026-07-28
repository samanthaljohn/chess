package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static ui.EscapeSequences.*;

public class DrawChessGame {
    private static final int BOARD_SQUARE_SIZE = 3;

    public static void main(String[] args){
        var out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        ChessBoard board = new ChessBoard();
        board.resetBoard();
        drawChessBoard(out, board,"BLACK");
    }

    // color setters
    private static void setLightPink(PrintStream out){
        out.print(SET_BG_COLOR_LIGHT_PINK);
    }

    private static void setBorder(PrintStream out){
        out.print(SET_BG_COLOR_PURPLE);
    }

    private static void setWhite(PrintStream out){
        out.print(SET_BG_COLOR_WHITE);
    }

    private static void setWhiteText(PrintStream out){
        out.print(SET_TEXT_COLOR_WHITE);
    }

    private static void setBlackText(PrintStream out){
        out.print(SET_TEXT_COLOR_BLACK);
    }

    private static void setPinkText(PrintStream out){
        out.print(SET_TEXT_COLOR_PINK);
    }

    private static void setDefault(PrintStream out){
        out.print(RESET_BG_COLOR);
        out.print(RESET_TEXT_COLOR);
    }

    private static String toFullWidth(char c){
        return String.valueOf((char)(c + 0xFEE0));
    }

    // getters
    private static ChessPiece getPiece(ChessBoard board, String playerColor, int row, int col){
        int piece_row, piece_col;
        if (playerColor.equals("BLACK")){
            piece_row = row;
            piece_col = 9 - col;
        } else {
            piece_row = 9 - row;
            piece_col = col;
        }

        ChessPosition position = new ChessPosition(piece_row, piece_col);
        ChessPiece piece = board.getPiece(position);
        return piece;
    }

    private static String getPieceChar(PrintStream out, ChessPiece piece){
        boolean isWhite = piece.getTeamColor() == ChessGame.TeamColor.WHITE;

        if (isWhite){
            setPinkText(out);
        } else {
            setBlackText(out);
        }

        switch (piece.getPieceType()){
            case KING:
                if (isWhite){
                    return WHITE_KING;
                } else { return BLACK_KING; }
            case QUEEN:
                if (isWhite){
                    return WHITE_QUEEN;
                } else { return BLACK_QUEEN; }
            case BISHOP:
                if (isWhite){
                    return WHITE_BISHOP;
                } else { return BLACK_BISHOP; }
            case KNIGHT:
                if (isWhite){
                    return WHITE_KNIGHT;
                } else { return BLACK_KNIGHT; }
            case ROOK:
                if (isWhite){
                    return WHITE_ROOK;
                } else { return BLACK_ROOK; }
            case PAWN:
                if (isWhite){
                    return WHITE_PAWN;
                } else { return BLACK_PAWN; }
            default:     return EMPTY;
        }
    }

    private static List<String> getColLabels(String playerColor){
      String[] colLabelsArray = {"a", "b", "c", "d", "e", "f", "g", "h"};
      List<String> colLabels = Arrays.asList(colLabelsArray);
        if (playerColor.equals("BLACK")){
            colLabels = colLabels.reversed();
        }
        return colLabels;
    }

    private static List<Integer> getRowLabels(String playerColor){
        Integer[] rowLabelsArray = {8, 7, 6, 5, 4, 3, 2, 1};
        List<Integer> rowLabels = Arrays.asList(rowLabelsArray);
        if (playerColor.equals("BLACK")){
            rowLabels = rowLabels.reversed();
        }
        return rowLabels;
    }
    private static boolean labelCols(PrintStream out, List<String> colLabels, int col, int row, int charRow){
        if ((col > 0 && col < 9) && ((row == 0 && charRow == 1) || (row == 9 && charRow == 0))){
            char colChar = String.valueOf(colLabels.get(col - 1)).charAt(0);
            setWhiteText(out);
            out.print(EMPTY + " ");
            out.print(toFullWidth(colChar));
            out.print(EMPTY + " ");
            return true;
        }
        return false;
    }

    private static boolean labelRows(PrintStream out, List<Integer> rowLabels, int col, int row, int charRow) {
        if (charRow == 0 && (row > 0 && row < 9)) {
            int rowNum = rowLabels.get(row - 1);
            char rowChar = String.valueOf(rowNum).charAt(0);
            setWhiteText(out);
            if (col == 0) {
                out.print(EMPTY.repeat(2) + toFullWidth(rowChar) + "  ");
                return true;
            } else if (col == 9) {
                out.print("  " + toFullWidth(rowChar) + EMPTY.repeat(2));
                return true;
            }
        }
        return false;
    }

    private static void drawRow(PrintStream out, ChessBoard board, String playerColor, List<String> colLabels, List<Integer> rowLabels, int row){
        for (int charRow = 0; charRow < BOARD_SQUARE_SIZE; charRow++){
            for (int col = 0; col < 10; col++){
                if((row == 0 || row == 9) || (col == 0 || col == 9)){
                    setBorder(out);
                    if (labelCols(out, colLabels, col, row, charRow)){
                        continue;
                    }
                    if(labelRows(out, rowLabels, col, row, charRow)){
                        continue;
                    }
                } else if ((row + col) % 2 == 0){
                    setWhite(out);
                } else {
                    setLightPink(out);
                }

                if (charRow == 1 && (row > 0 && row < 9) && (col > 0 && col < 9)){
                    ChessPiece piece = getPiece(board, playerColor, row, col);

                    if (piece != null){
                        out.print(EMPTY.repeat(1));
                        out.print(getPieceChar(out, piece));
                        out.print(EMPTY.repeat(1));
                    } else {
                        out.print(EMPTY.repeat(3));
                    }
                } else {
                    out.print(EMPTY.repeat(3));
                }
            }
            setDefault(out);
            out.println();
        }
    }

    public static void drawChessBoard(PrintStream out, ChessBoard board, String playerColor){
        for (int row = 0; row < 10; row++){
            drawRow(out, board, playerColor, getColLabels(playerColor), getRowLabels(playerColor), row);
        }
    }
}
