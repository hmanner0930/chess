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

    private static void printRow(ChessBoard board, int row, boolean whitePerspective, java.util.Collection<ChessMove> highlightedMoves) {
        System.out.print(SET_BG_COLOR_DARK_GREY + SET_TEXT_COLOR_WHITE + " " + row + " ");

        int colStart = whitePerspective ? 1 : 8;
        int colEnd = whitePerspective ? 8 : 1;
        int colStep = whitePerspective ? 1 : -1;

        for (int col = colStart; whitePerspective ? col <= colEnd : col >= colEnd; col += colStep) {
            ChessPosition currentPos = new ChessPosition(row, col);

            // 1. Determine base square color
            boolean isLightSquare = (row + col) % 2 == 1;
            String backgroundColor = isLightSquare ? SET_BG_COLOR_LIGHT_GREY : SET_BG_COLOR_BLACK;

            // 2. Check for highlights
            if (highlightedMoves != null) {
                for (ChessMove move : highlightedMoves) {
                    if (move.getStartPosition().equals(currentPos)) {
                        // Highlight the selected piece's starting square
                        backgroundColor = SET_BG_COLOR_YELLOW;
                    } else if (move.getEndPosition().equals(currentPos)) {
                        // Highlight legal destination squares
                        backgroundColor = isLightSquare ? SET_BG_COLOR_GREEN : SET_BG_COLOR_DARK_GREEN;
                    }
                }
            }

            System.out.print(backgroundColor);

            ChessPiece piece = board.getPiece(currentPos);
            if (piece != null) {
                printPiece(piece);
            } else {
                System.out.print(EMPTY);
            }
        }
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

    // Keep this for standard draws
    public static void drawBoard(ChessBoard board, boolean whitePerspective) {
        drawBoard(board, whitePerspective, null);
    }

    // Add this for highlighted draws
    public static void drawBoard(ChessBoard board, boolean whitePerspective, java.util.Collection<ChessMove> highlightedMoves) {
        printHeaders(whitePerspective);

        int rowStart = whitePerspective ? 8 : 1;
        int rowEnd = whitePerspective ? 1 : 8;
        int rowStep = whitePerspective ? -1 : 1;

        for (int row = rowStart; whitePerspective ? row >= rowEnd : row <= rowEnd; row += rowStep) {
            printRow(board, row, whitePerspective, highlightedMoves); // Pass moves here
        }

        printHeaders(whitePerspective);
        System.out.print(RESET_BG_COLOR + RESET_TEXT_COLOR);
    }
}
