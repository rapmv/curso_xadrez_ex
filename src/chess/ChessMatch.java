package chess;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import boardgame.Board;
import boardgame.Piece;
import boardgame.Position;
import chess.pieces.Bishop;
import chess.pieces.King;
import chess.pieces.Knight;
import chess.pieces.Pawn;
import chess.pieces.Queen;
import chess.pieces.Rook;

public class ChessMatch {
	
	private int turn;
	
	private Color currentPlayer;

	private Board board;
	
	private boolean check;
	
	private boolean checkMate;
	
	private ChessPiece enPassantVulnerable;
	
	private ChessPiece promoted;
	
	private List<Piece> piecesOnTheBoard = new ArrayList<>();
	private List<Piece> capturedPieces = new ArrayList<>();
	
	public ChessMatch() {
		
		//dimens�o do tabuleiro
		board = new Board(8, 8);
		turn = 1;
		currentPlayer = Color.WHITE;
		
		//enfatizar, pois ja come�a com false
		check = false;
		
		initialSetup();
	}
	
	public int getTurn() {
		return turn;
	}
	
	public Color getCurrentPlayer() {
		return currentPlayer;
	}
	
	public boolean getCheck() {
		return check;
	}
	
	public boolean getCheckMate() {
		return checkMate;
	}
	
	public ChessPiece getEnPassantVulnerable() {
		return enPassantVulnerable;
	}
	
	public ChessPiece getPromoted() {
		return promoted;
	}
	
	//fun��o que retorna as pe�as de xadrez
	public ChessPiece[][] getPieces(){
		
		ChessPiece [][] mat = new ChessPiece[board.getRows()][board.getColumns()];
		for (int i=0; i<board.getRows(); i++) {
			for (int j=0; j<board.getColumns(); j++) {
				mat[i][j]= (ChessPiece) board.piece(i,j);
			}
		}
		return mat;
	}
	
	//mostrar as posi��es possiveis
	public boolean[][] possibleMoves(ChessPosition sourcePosition){
		Position position = sourcePosition.toPosition();
		validateSourcePosition(position);
		return board.piece(position).possibleMoves();
	}
	
	//fun��o que mostras os possiveis movimentos e a movimenta��o da pe�a
	public ChessPiece performChessMove(ChessPosition sourcePosition, ChessPosition targetPosition) {
		
		Position source = sourcePosition.toPosition();
		Position target = targetPosition.toPosition();
		
		validateSourcePosition(source);
		validateTargetPosition(source, target);
		
		Piece capturedPiece = makeMove(source, target);
		
		//fun��o que verifica se o proprio jogador se colocou em check e desfaz o movimento
		if(testCheck(currentPlayer)) {
			undoMove(source, target, capturedPiece);
			throw new ChessException("You can't put yourself in check.");
		}
		
		//movimento especial en passant (pe�o)
		ChessPiece movedPiece = (ChessPiece)board.piece(target);
		
		//Promo��o do pe�o
		promoted = null;
		if(movedPiece instanceof Pawn) {
			if((movedPiece.getColor()==Color.WHITE && target.getRow()==0) || (movedPiece.getColor()==Color.BLACK && target.getRow()==7)) {
				promoted = (ChessPiece)board.piece(target);
				promoted = replacePromotedPiece("Q");
			}
		}
		
		//verifica se o oponente ficou em check depois da jogada
		check = (testCheck(opponent(currentPlayer))) ? true: false;
		
		//verifica se o jogo acabou com o checkmate
		if (testCheckMate(opponent(currentPlayer))) {
			checkMate = true;
		}
		else {
			nextTurn();
		}
		
		//movimento especial en passant (pe�o)
		if(movedPiece instanceof Pawn && (target.getRow() == source.getRow()-2 || target.getRow() == source.getRow() + 2)) {
			enPassantVulnerable = movedPiece;
		}
		else {
			enPassantVulnerable = null;
		}
		
		return (ChessPiece)capturedPiece;
		
	}
	
	public ChessPiece replacePromotedPiece(String type) {
		if(promoted == null) {
			throw new IllegalStateException("There is no piece to be promoted.");
		}
		if(!type.contentEquals("B") && !type.contentEquals("C") && !type.contentEquals("R") && !type.contentEquals("Q") ) {
			return promoted;
		}
		
		Position pos = promoted.getChessPosition().toPosition();
		Piece p = board.removePiece(pos);
		piecesOnTheBoard.remove(p);
		
		ChessPiece newPiece = newPiece(type, promoted.getColor());
		board.placePiece(newPiece, pos);
		piecesOnTheBoard.add(newPiece);
		
		return newPiece;
		
	}
	
	//fun��o que retorna a pe�a escolhida para promo��o
	private ChessPiece newPiece(String type, Color color) {
		if(type.equals("B")) return new Bishop(board, color);
		if(type.equals("C")) return new Knight(board, color);
		if(type.equals("Q")) return new Queen(board, color);
		
		return new Rook(board, color);
	}
	
	//fun��o que valida a posi��o pra mover
	private void validateSourcePosition(Position position) {
		if(!board.thereIsAPiece(position)) {
			throw new ChessException("There is no piece on source position.");
		}
		if(currentPlayer != ((ChessPiece)board.piece(position)).getColor()) {
			throw new ChessException("The chosen piece is not yours.");
		}
		if (!board.piece(position).isThereAnyPossibleMove()) {
			throw new ChessException("There is no possible moves for the chosen piece.");
		}
	}
	
	//fun��o que valida a posi��o onde esta se movendo
	private void validateTargetPosition(Position source, Position target) {
		
		if(!board.piece(source).possibleMove(target)) {
			throw new ChessException("The chosen piece can't move to target position. ");
		}
	}
	
	//fun��o que faz a movimenta��o da pe�a
	private Piece makeMove(Position source, Position target) {
		
		//remove a pe�a na posi��o que esta(a pe�a que esta movimentando)
		ChessPiece p = (ChessPiece)board.removePiece(source);
		
		//contador de movimento da pe�a
		p.increaseMoveCount();
		
		//remove a pe�a na posi��o que esta(pe�a que talvez esteja sendo capturada)
		Piece capturedPiece = board.removePiece(target);
		
		//coloca a pe�a no lugar(coloca a pe�a no lugar em que esta movimentando)
		board.placePiece(p, target);
		
		//verifica se a pe�a foi capturada, se foi remove e add na lista de capturadas.
		if(capturedPiece != null) {
			piecesOnTheBoard.remove(capturedPiece);
			capturedPieces.add(capturedPiece);
		}
		
		//Movimento Castling pequeno
		if(p instanceof King && target.getColumn()== source.getColumn()+2) {
			Position sourceT = new Position(source.getRow(), source.getColumn()+3);
			Position targetT = new Position(source.getRow(), source.getColumn()+1);
			ChessPiece rook = (ChessPiece)board.removePiece(sourceT);
			board.placePiece(rook, targetT);
			rook.increaseMoveCount();
		}
		
		//Movimento Castling grande
		if (p instanceof King && target.getColumn() == source.getColumn() - 2) {
			Position sourceT = new Position(source.getRow(), source.getColumn() - 4);
			Position targetT = new Position(source.getRow(), source.getColumn() - 1);
			ChessPiece rook = (ChessPiece) board.removePiece(sourceT);
			board.placePiece(rook, targetT);
			rook.increaseMoveCount();
		}
		
		//Movimento en passant
		if(p instanceof Pawn) {
			if (source.getColumn() != target.getColumn() && capturedPiece == null) {
				Position pawnPosition;
				if(p.getColor()== Color.WHITE) {
					pawnPosition = new Position(target.getRow( )+ 1, target.getColumn());
				}
				else {
					pawnPosition = new Position(target.getRow() - 1, target.getColumn());
				}
				capturedPiece = board.removePiece(pawnPosition);
				capturedPieces.add(capturedPiece);
				piecesOnTheBoard.remove(capturedPiece);
			}
		}
		
		return capturedPiece;
	}
	
	//fun��o para desfazer um movimento
	private void undoMove(Position source, Position target, Piece capturedPiece) {
		
		
		ChessPiece p = (ChessPiece)board.removePiece(target);
		
		//decrementa os movimentos da pe�a
		board.placePiece(p,source);
		
		p.decreaseMoveCount();
		
		//fun�ao que verifica se uma pe�a foi capturada e devolve para o lugar quando voltando o movimento.
		if(capturedPiece !=null) {
			board.placePiece(capturedPiece, target);
			capturedPieces.remove(capturedPiece);
			piecesOnTheBoard.add(capturedPiece);
		}
		
		//Desfazer o movimento Castling pequeno
		if (p instanceof King && target.getColumn() == source.getColumn() + 2) {
			Position sourceT = new Position(source.getRow(), source.getColumn() + 3);
			Position targetT = new Position(source.getRow(), source.getColumn() + 1);
			ChessPiece rook = (ChessPiece) board.removePiece(targetT);
			board.placePiece(rook, sourceT);
			rook.decreaseMoveCount();
		}

		//Desfazer o movimento Castling grande
		if (p instanceof King && target.getColumn() == source.getColumn() - 2) {
			Position sourceT = new Position(source.getRow(), source.getColumn() - 4);
			Position targetT = new Position(source.getRow(), source.getColumn() - 1);
			ChessPiece rook = (ChessPiece) board.removePiece(targetT);
			board.placePiece(rook, sourceT);
			rook.decreaseMoveCount();
		}
		
		//Movimento en passant
		if (p instanceof Pawn) {
			if (source.getColumn() != target.getColumn() && capturedPiece == enPassantVulnerable) {
				ChessPiece pawn = (ChessPiece)board.removePiece(target);
				Position pawnPosition;
				if (p.getColor() == Color.WHITE) {
					pawnPosition = new Position(3, target.getColumn());
				} else {
					pawnPosition = new Position(4, target.getColumn());
				}
				
				board.placePiece(pawn, pawnPosition);
				
				capturedPiece = board.removePiece(pawnPosition);
			}
		}
		
	}
	
	//fun��o para instanciar as coordenas do xadrez
	private void placeNewPiece(char column, int row, ChessPiece piece) {
		
		board.placePiece(piece, new ChessPosition(column,row).toPosition());
		piecesOnTheBoard.add(piece);
	}
	
	//fun��o para mostrar a cor do oponente
	private Color opponent(Color color) {
		return (color ==Color.WHITE) ? Color.BLACK : Color.WHITE;
	}
	
	//fun��o para localizar o King de uma determina cor
	private ChessPiece king(Color color) {
		
		List<Piece> list = piecesOnTheBoard.stream().filter(x->((ChessPiece)x).getColor()==color).collect(Collectors.toList());
		for(Piece p: list) {
			if(p instanceof King) {
				return(ChessPiece)p;
			}
		}
		throw new IllegalStateException("There is no" + color + "King on the board.");
	}
	
	//fun��o para testar se algum king esta em check
	private boolean testCheck(Color color) {
		
		//pega a posi��o do king
		Position kingPosition = king(color).getChessPosition().toPosition();
		
		List<Piece> opponentPieces = piecesOnTheBoard.stream().filter(x->((ChessPiece)x).getColor()==opponent(color)).collect(Collectors.toList());
		
		//verifica se alguma pe�a tem a possibilidade de capturar o King do oponente
		for(Piece p: opponentPieces) {
			
			boolean [][] mat = p.possibleMoves();
			
			if(mat[kingPosition.getRow()][kingPosition.getColumn()]) {
				return true;
			}
			
		}
		return false;
	}
	
	//fun��o de verifica��o do checkmate
	private boolean testCheckMate(Color color) {
		
		//verifica se esta em check
		if (!testCheck(color)) {
			return false;
		}
		
		List<Piece> list = piecesOnTheBoard.stream().filter(x -> ((ChessPiece)x).getColor() == color).collect(Collectors.toList());
		
		//verifica se as pe�as tem algum movimento para tirar o check
		for (Piece p : list) {
			
			//pega os possiveis movimentos da pe�a(p)
			boolean[][] mat = p.possibleMoves();
			for (int i=0; i<board.getRows(); i++) {
				for (int j=0; j<board.getColumns(); j++) {
					
					//verifica se o movimento tira o check
					if (mat[i][j]) {
						Position source = ((ChessPiece)p).getChessPosition().toPosition();
						Position target = new Position(i, j);
						
						//faz o movimento para testar o check
						Piece capturedPiece = makeMove(source, target);
						//testa se o king ainda esta em check
						boolean testCheck = testCheck(color);
						//volta o movimento pois estava so testando
						undoMove(source, target, capturedPiece);
						//verifica se o king ainda fica em check com o movimento.
						if (!testCheck) {
							return false;
						}
					}
				}
			}
		}
		//se o for terminar significa que deu checkmate
		return true;
	}
	
	//fun��o para troca de jogador
	private void nextTurn() {
		turn++;
		currentPlayer = (currentPlayer == Color.WHITE) ? Color.BLACK : Color.WHITE;
	}
	
	//coloca as pe�as iniciais do jogo
	private void initialSetup() {
	   placeNewPiece('a', 1, new Rook(board, Color.WHITE));
	   placeNewPiece('b', 1, new Knight(board, Color.WHITE));
	   placeNewPiece('c', 1, new Bishop(board, Color.WHITE));
	   placeNewPiece('d', 1, new Queen(board, Color.WHITE));
	   placeNewPiece('e', 1, new King(board, Color.WHITE, this));
	   placeNewPiece('f', 1, new Bishop(board, Color.WHITE));
	   placeNewPiece('g', 1, new Knight(board, Color.WHITE));
	   placeNewPiece('h', 1, new Rook(board, Color.WHITE));
	   placeNewPiece('a', 2, new Pawn(board, Color.WHITE, this));
	   placeNewPiece('b', 2, new Pawn(board, Color.WHITE, this));
	   placeNewPiece('c', 2, new Pawn(board, Color.WHITE, this));
	   placeNewPiece('d', 2, new Pawn(board, Color.WHITE, this));
	   placeNewPiece('e', 2, new Pawn(board, Color.WHITE, this));
	   placeNewPiece('f', 2, new Pawn(board, Color.WHITE, this));
	   placeNewPiece('g', 2, new Pawn(board, Color.WHITE, this));
	   placeNewPiece('h', 2, new Pawn(board, Color.WHITE, this));

	   placeNewPiece('a', 8, new Rook(board, Color.BLACK));
	   placeNewPiece('b', 8, new Knight(board, Color.BLACK));
	   placeNewPiece('c', 8, new Bishop(board, Color.BLACK));
	   placeNewPiece('d', 8, new Queen(board, Color.BLACK));
       placeNewPiece('e', 8, new King(board, Color.BLACK, this));
       placeNewPiece('g', 8, new Knight(board, Color.BLACK));
       placeNewPiece('f', 8, new Bishop(board, Color.BLACK));
       placeNewPiece('h', 8, new Rook(board, Color.BLACK));
       placeNewPiece('a', 7, new Pawn(board, Color.BLACK, this));
       placeNewPiece('b', 7, new Pawn(board, Color.BLACK, this));
       placeNewPiece('c', 7, new Pawn(board, Color.BLACK, this));
       placeNewPiece('d', 7, new Pawn(board, Color.BLACK, this));
       placeNewPiece('e', 7, new Pawn(board, Color.BLACK, this));
       placeNewPiece('f', 7, new Pawn(board, Color.BLACK, this));
       placeNewPiece('g', 7, new Pawn(board, Color.BLACK, this));
       placeNewPiece('h', 7, new Pawn(board, Color.BLACK, this));
		
	}
	
	
}
