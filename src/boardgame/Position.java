package boardgame;

public class Position {
    private int row;
    private int column;

    public Position(int row, int column) {
        this.row = row;
        this.column = column;  // Sets the row and column of the position
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;  // Sets the row of the position
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;  // Sets the column of the position
    }

    public void setValues(int row, int column) {
        this.row = row;
        this.column = column;
    }

    @Override
    public String toString() {
        return row + ", " + column;
    }
}
