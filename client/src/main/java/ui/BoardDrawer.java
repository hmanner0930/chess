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

        int columnStart;
        int columnEnd;
        int columnStep;

        if (whitePerspective) {
            columnStart = 1;
            columnEnd = 8;
            columnStep = 1;
        } else {
            columnStart = 8;
            columnEnd = 1;
            columnStep = -1;
        }

        for (int col = columnStart; ; col += columnStep) {

            if (whitePerspective) {
                if (col > columnEnd) {
                    break;
                }
            } else {
                if (col < columnEnd) {
                    break;
                }
            }

            ChessPosition currentPosition = new ChessPosition(row, col);
            boolean isLightSquare = (row + col) % 2 == 1;
            String backgroundColor = isLightSquare ? SET_BG_COLOR_LIGHT_GREY : SET_BG_COLOR_BLACK;

            if (highlightedMoves != null) {
                for (ChessMove move : highlightedMoves) {
                    if (move.getStartPosition().equals(currentPosition)) {
                        backgroundColor = SET_BG_COLOR_BLUE;
                    } else if (move.getEndPosition().equals(currentPosition)) {
                        backgroundColor = isLightSquare ? SET_BG_COLOR_GREEN : SET_BG_COLOR_DARK_GREEN;
                    }
                }
            }

            System.out.print(backgroundColor);

            ChessPiece piece = board.getPiece(currentPosition);
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

    public static void drawBoard(ChessBoard board, boolean whitePerspective) {
        drawBoard(board, whitePerspective, null);
    }

    public static void drawBoard(ChessBoard board, boolean whiteView, java.util.Collection<ChessMove> highlightedMoves) {
        printHeaders(whiteView);

        int rowStart;
        int rowEnd;
        int rowStep;

        if (whiteView) {
            rowStart = 8;
            rowEnd = 1;
            rowStep = -1;
        } else {
            rowStart = 1;
            rowEnd = 8;
            rowStep = 1;
        }

        for (int row = rowStart; ; row += rowStep) {

            if (whiteView) {
                if (row < rowEnd) {
                    break;
                }
            } else {
                if (row > rowEnd) {
                    break;
                }
            }

            printRow(board, row, whiteView, highlightedMoves);
        }

        printHeaders(whiteView);
        System.out.print(RESET_BG_COLOR + RESET_TEXT_COLOR);
    }
}
