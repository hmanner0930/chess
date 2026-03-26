package ui;

import chess.*;
import static ui.EscapeSequences.*;

public class BoardDrawer {

    private static final String[] HEADERS = { " a ", " b ", " c ", " d ", " e ", " f ", " g ", " h " };

    private static void printHeaders(boolean isWhiteView) {
        System.out.print(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_WHITE + "    ");
        if (isWhiteView) {
            for (int i = 0; i < 8; i++){ System.out.print(HEADERS[i]);
                }
        } else {
            for (int i = 7; i >= 0; i--){
                System.out.print(HEADERS[i]);
            }
        }
        System.out.println("    " + RESET_BG_COLOR);
    }

    private static void printRow(ChessBoard board, int row, boolean whitePerspective) {

        System.out.print(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_WHITE + " " + row + " ");
        int colStart;
        int colEnd;
        int colStep;

        if (whitePerspective) {
            colStart = 1;
            colEnd = 8;
            colStep = 1;
        } else {
            colStart = 8;
            colEnd = 1;
            colStep = -1;
        }

        for (int col = colStart; whitePerspective ? col <= colEnd : col >= colEnd; col += colStep) {
            //For code quality you can use ternary operators inside?
            if ((row + col) % 2 == 0) {
                System.out.print(SET_BG_COLOR_LIGHT_GREY);
            } else {
                System.out.print(SET_BG_COLOR_BLACK);
            }
            ChessPiece piece = board.getPiece(new ChessPosition(row, col));
            if (piece != null) {
                printPiece(piece);
            } else {
                System.out.print(EMPTY);
            }
        }

        //This is the side
        System.out.println(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_WHITE + " " + row + " " + RESET_BG_COLOR);
    }

    private static void printPiece(ChessPiece piece) {
        boolean isWhite = piece.getTeamColor() == ChessGame.TeamColor.WHITE;
        System.out.print(isWhite ? SET_TEXT_COLOR_WHITE : SET_TEXT_COLOR_RED);

        switch (piece.getPieceType()) {
            case KING -> {
                if (isWhite) {
                    System.out.print(WHITE_KING);
                } else {
                    System.out.print(BLACK_KING);
                }
            }
            case QUEEN -> {
                if (isWhite) {
                    System.out.print(WHITE_QUEEN);
                } else {
                    System.out.print(BLACK_QUEEN);
                }
            }
            case BISHOP -> {
                if (isWhite) {
                    System.out.print(WHITE_BISHOP);
                } else {
                    System.out.print(BLACK_BISHOP);
                }
            }
            case KNIGHT -> {
                if (isWhite) {
                    System.out.print(WHITE_KNIGHT);
                } else {
                    System.out.print(BLACK_KNIGHT);
                }
            }
            case ROOK -> {
                if (isWhite) {
                    System.out.print(WHITE_ROOK);
                } else {
                    System.out.print(BLACK_ROOK);
                }
            }
            case PAWN -> {
                if (isWhite) {
                    System.out.print(WHITE_PAWN);
                } else {
                    System.out.print(BLACK_PAWN);
                }
            }
        }
    }

    public static void drawBoard(ChessBoard board, boolean whitePerspective) {
        printHeaders(whitePerspective);

        int rowStart;
        int rowEnd;
        int rowStep;
        if (whitePerspective) {
            rowStart = 8;
            rowEnd = 1;
            rowStep = -1;
        } else {
            rowStart = 1;
            rowEnd = 8;
            rowStep = 1;
        }

        for (int row = rowStart; whitePerspective ? row >= rowEnd : row <= rowEnd; row += rowStep) {
            printRow(board, row, whitePerspective);
        }

        printHeaders(whitePerspective);
        System.out.print(RESET_BG_COLOR + RESET_TEXT_COLOR);
    }
}
