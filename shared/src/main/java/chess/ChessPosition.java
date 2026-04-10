package chess;

import java.util.Objects;

public class ChessPosition {
    private final int row;
    private final int col;

    public ChessPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return col;
    }

    @Override
    public String toString() {
        return String.format("[%d, %d]", row, col);
    }
    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }
    @Override
    public boolean equals(Object object) {
        if (this == object) {return true;}
        if (object == null || getClass() != object.getClass()) {return false;}
        ChessPosition other = (ChessPosition) object;
        return row == other.row && col == other.col;
    }

}
