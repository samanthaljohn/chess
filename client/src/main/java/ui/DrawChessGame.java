package ui;

import chess.ChessPiece;
import chess.ChessPosition;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static ui.EscapeSequences.*;

public class DrawChessGame {
    private static final int BOARD_SQUARE_SIZE = 2;

    private static final String EMPTY = " ";

    private enum BoardColumns{
        a,
        b,
        c,
        d,
        e,
        f,
        g,
        h
    };

    public static void main(String[] args){
        var out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        drawChessBoard(out, "WHITE");
    }

    // color setters
    private static void setLightPink(PrintStream out){
        out.print(SET_BG_COLOR_LIGHT_PINK);
        out.print(SET_TEXT_COLOR_LIGHT_PINK);
    }

    private static void setBorder(PrintStream out){
        out.print(SET_BG_COLOR_PURPLE);
        out.print(SET_TEXT_COLOR_WHITE);

    }

    private static void setWhite(PrintStream out){
        out.print(SET_BG_COLOR_WHITE);
        out.print(SET_TEXT_COLOR_WHITE);
    }

    private static void setBlack(PrintStream out){
        out.print(SET_BG_COLOR_BLACK);
        out.print(SET_TEXT_COLOR_BLACK);
    }

    private static void setDefault(PrintStream out){
        out.print(RESET_BG_COLOR);
        out.print(RESET_TEXT_COLOR);
    }


    public static void drawChessBoard(PrintStream out, String playerColor){

        for (int row = 0; row < 10; row++){
            drawRow(out, colLabels, rowLabels, row);
        }
    }

    private static List<BoardColumns> getColLabels(String playerColor){
        List<BoardColumns> colLabels = new ArrayList<>(Arrays.asList(BoardColumns.values()));
        if (playerColor.equals("BLACK")){
            colLabels = colLabels.reversed();
        }
        return colLabels;
    }

    private static List<Integer> getRowLabels(String playerColor){
        Integer[] rowLabelsArray = {8, 7,6, 5, 4, 3, 2, 1};
        List<Integer> rowLabels = Arrays.asList(rowLabelsArray);
        if (playerColor.equals("WHITE")){
            rowLabels = rowLabels.reversed();
        }
        return rowLabels;
    }

    private static boolean labelCols(PrintStream out, List<BoardColumns> colLabels, int col, int row, int charRow){
        if ((col > 0 && col < 9) && ((row == 0 && charRow == 1) || (row == 9 && charRow == 0))){
            out.print(EMPTY.repeat(2));
            out.print(colLabels.get(col - 1));
            out.print(EMPTY.repeat(2));
            return true;
        }
        return false;
    }

    private static boolean labelRows(PrintStream out, List<Integer> rowLabels, int col, int row, int charRow) {
        if (charRow == 0 && (row > 0 && row < 9)) {
            int rowNum = rowLabels.get(row - 1);
            if (col == 0) {
                out.print(EMPTY.repeat(3) + rowNum + EMPTY);
                return true;
            } else if (col == 9) {
                out.print(EMPTY + rowNum + EMPTY.repeat(3));
                return true;
            }
        }
        return false;
    }

    private static ChessPiece getPiece(int row, int col){
        ChessPosition position = new ChessPosition(row - 1, col - 1);

    }

    private static void drawRow(PrintStream out, List<BoardColumns> colLabels, List<Integer> rowLabels, int row){
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
                } else if (getPiece(row, col) != null){
                    ChessPiece piece = getPiece(row, col);
                } else if ((row + col) % 2 == 0){
                    setWhite(out);
                } else {
                    setLightPink(out);
                }
                out.print(EMPTY.repeat(5));
            }
            setDefault(out);
            out.println();
        }
    }

}
