package ui;

import chess.*;
import static ui.EscapeSequences.*;

public class BoardDrawer {

    private static final int BOARD_SIZE_IN_SQUARES = 8;
    private static final String[] HEADERS = { " a ", " b ", " c ", " d ", " e ", " f ", " g ", " h " };

    public static void drawBoard(ChessBoard board, boolean isWhiteView) {
        // Phase 5 requires printing the board from the perspective of the player
        printHeaders(isWhiteView);

        // If White view: rows 8 down to 1. If Black view: rows 1 up to 8.
        int rowStart = isWhiteView ? 8 : 1;
        int rowEnd = isWhiteView ? 1 : 8;
        int rowStep = isWhiteView ? -1 : 1;

        for (int r = rowStart; isWhiteView ? r >= rowEnd : r <= rowEnd; r += rowStep) {
            printRow(board, r, isWhiteView);
        }

        printHeaders(isWhiteView);
        System.out.print(RESET_BG_COLOR + RESET_TEXT_COLOR); // Clean up terminal
    }

    private static void printHeaders(boolean isWhiteView) {
        System.out.print(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_WHITE + "    ");
        if (isWhiteView) {
            for (int i = 0; i < 8; i++) System.out.print(HEADERS[i]);
        } else {
            for (int i = 7; i >= 0; i--) System.out.print(HEADERS[i]);
        }
        System.out.println("    " + RESET_BG_COLOR);
    }

    private static void printRow(ChessBoard board, int row, boolean isWhiteView) {
        // Print side header (the numbers)
        System.out.print(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_WHITE + " " + row + " ");

        // Columns: if White view: a-h (1-8). If Black view: h-a (8-1).
        int colStart = isWhiteView ? 1 : 8;
        int colEnd = isWhiteView ? 8 : 1;
        int colStep = isWhiteView ? 1 : -1;

        for (int c = colStart; isWhiteView ? c <= colEnd : c >= colEnd; c += colStep) {
            // Determine square color
            if ((row + c) % 2 == 0) {
                System.out.print(SET_BG_COLOR_LIGHT_GREY);
            } else {
                System.out.print(SET_BG_COLOR_BLACK);
            }

            // Print Piece
            ChessPiece piece = board.getPiece(new ChessPosition(row, c));
            if (piece != null) {
                printPiece(piece);
            } else {
                System.out.print(EMPTY);
            }
        }

        // Print side footer
        System.out.println(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_WHITE + " " + row + " " + RESET_BG_COLOR);
    }

    private static void printPiece(ChessPiece piece) {
        boolean isWhite = piece.getTeamColor() == ChessGame.TeamColor.WHITE;
        System.out.print(isWhite ? SET_TEXT_COLOR_WHITE : SET_TEXT_COLOR_RED); // Using Red for Black pieces for visibility

        switch (piece.getPieceType()) {
            case KING -> System.out.print(isWhite ? WHITE_KING : BLACK_KING);
            case QUEEN -> System.out.print(isWhite ? WHITE_QUEEN : BLACK_QUEEN);
            case BISHOP -> System.out.print(isWhite ? WHITE_BISHOP : BLACK_BISHOP);
            case KNIGHT -> System.out.print(isWhite ? WHITE_KNIGHT : BLACK_KNIGHT);
            case ROOK -> System.out.print(isWhite ? WHITE_ROOK : BLACK_ROOK);
            case PAWN -> System.out.print(isWhite ? WHITE_PAWN : BLACK_PAWN);
        }
    }
}
