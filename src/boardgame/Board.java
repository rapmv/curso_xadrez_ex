package boardgame;

public class Board {

	private int rows;
	private int columns;
	
	//matriz de pe�as
	private Piece[][] pieces;

	public Board(int rows, int columns) {
		
		if(rows<1 || columns <1) {
			throw new BoardException("Error creating board: there must be at least 1 row and 1 column");
		}
		
		this.rows = rows;
		this.columns = columns;
		pieces = new Piece[rows][columns];
		
	}

	public int getRows() {
		return rows;
	}


	public int getColumns() {
		return columns;
	}

	//retorna a pe�a
	public Piece piece (int row, int column) {
		
		if(!positionExists(row, column)) {
			throw new BoardException("Position not on the board.");
		}
		return pieces [row][column];
	}
	
	//posi��o em que a pe�a esta
	public Piece piece (Position position) {
		
		if(!positionExists(position)) {
			throw new BoardException("Position not on the board.");
		}
		return pieces [position.getRow()] [position.getColumn()];
	}
	
	//fun��o para por a pe�a na posi��o do tabuleiro.
	public void placePiece(Piece piece, Position position) {
		
		if(thereIsAPiece(position)) {
			throw new BoardException("There is already a piece on position: " + position);
		}
		pieces[position.getRow()][position.getColumn()] = piece;
		piece.position = position;
		
	}
	
	//remove a pe�a quando for capturada.
	public Piece removePiece(Position position) {
		if(!positionExists(position)) {
			throw new BoardException("Position not on the board.");
		}
		if (piece(position)==null) {
			return null;
		}
		
		Piece aux = piece(position);
		
		aux.position = null;
		pieces[position.getRow()][position.getColumn()]= null;
		
		return aux;
	}
	
	//fun��o auxiliar que verifica se a posi��o existe
	private boolean positionExists(int row, int column) {
		
		return row>=0 && row < rows && column >=0 && column <columns;
	}
	
	//fun��o que verifica se a posi��o existe
	public boolean positionExists(Position position) {
		return positionExists(position.getRow(), position.getColumn());
	}
	
	//fun�o que verifica se tem uma pe�a na posi��o
	public boolean thereIsAPiece(Position position) {
		
		if(!positionExists(position)) {
			throw new BoardException("Position not on the board");
		}
		return piece(position)!=null;
	}
	
}
