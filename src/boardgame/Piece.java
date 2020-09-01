package boardgame;

public abstract class Piece {

	protected Position position;
	
	private Board board;

	public Piece(Board board) {
		
		this.board = board;
		
		//enfatizar e ficar de datico pois o java ja inicia com null
		position = null;
	}

	//somente classes no mesmo pacote e subclasses podem acessar.
	protected Board getBoard() {
		return board;
	}

	public abstract boolean[][] possibleMoves();

	//função que testa se tem possibilidade de mover
	public boolean possibleMove(Position position) {
		
		return possibleMoves()[position.getRow()][position.getColumn()];
	}

	//função se tem pelo menos uma possibilidade de a peça mover
	public boolean isThereAnyPossibleMove() {
		
		boolean[][] mat = possibleMoves();
		for (int i=0; i<mat.length; i++) {
			for (int j=0; j<mat.length; j++) {
				if (mat[i][j]) {
					return true;
				}
			}
		}
		return false;
	}
	
}
