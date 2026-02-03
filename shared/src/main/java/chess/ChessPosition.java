package chess;

import java.util.Objects;
/**
 * Represents a single square position on a chess board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPosition {
    //stores the coordinates of the position
    private final int row;
    private final int col;

    //Constructor; Assigns the parameters
    public ChessPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }
    /**
     * @return which row this position is in
     * 1 codes for the bottom row
     */
    //Getter; returns the row value
    public int getRow() {
        return row;
    }

    /**
     * @return which column this position is in
     * 1 codes for the left row
     */
    //Getter; returns the column value
    public int getColumn() {
        return col;
    }

    //combines these into a value so that certain positions get the same hash value
    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true; //Chekcs if they are the same object returns true if true
        if (obj == null || getClass() != obj.getClass()) return false; //if null or not a position returns false
        ChessPosition other = (ChessPosition) obj; // casts obj to chess position
        return row == other.row && col == other.col; // Returns true when row and col numbers are the same meaning same square
    }

}
