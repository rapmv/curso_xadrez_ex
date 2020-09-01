package chess;

import boardgame.Position;

public class ChessPosition {

	private char column;
	private int row;
	
	public ChessPosition(char column, int row) {
		
		if(column<'a' || column>'h' || row<1 || row>8) {
			throw new ChessException("Error instantiating ChessPosition. Valid value are a1 to h8.");
		}
		this.column = column;
		this.row = row;
	}
	
	public char getColumn() {
		return column;
	}

	public int getRow() {
		return row;
	}

	//fun��o que retorna a posi��o na matriz
	protected Position toPosition() {
		return new Position(8-row, column-'a');
	}
	
	//fun��o que retorna a posi��o no xadrez
	protected static ChessPosition fromPosition(Position position) {
		return new ChessPosition((char)('a' + position.getColumn()), 8 - position.getRow());
	}
	
	@Override
	public String toString() {
		//string vazio e um macete para concatenar
		return "" + column + row;
	}
	
}
