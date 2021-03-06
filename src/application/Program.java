package application;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

import chess.ChessException;
import chess.ChessMatch;
import chess.ChessPiece;
import chess.ChessPosition;

public class Program {

	public static void main(String[] args) {
		
		Scanner sc = new Scanner(System.in);
		
		ChessMatch chessMatch = new ChessMatch();
		
		List<ChessPiece> captured = new ArrayList<>();
		
		//repete enquanto n�o tiver um CheckMate
		while (!chessMatch.getCheckMate()) {
			
			try {
				//limpa tela
				UI.clearScreen();
				
				UI.printMatch(chessMatch, captured);
				
				System.out.println();
				System.out.print("Source: ");
				ChessPosition source = UI.readChessPosition(sc);
				
				boolean[][] possibleMoves = chessMatch.possibleMoves(source);
				
				UI.clearScreen();
				
				//imprimi o tabuleiro junto com as pe�as e mais as possiveis movimenta��es da pe�a
				UI.printBoard(chessMatch.getPieces(), possibleMoves);
				
				System.out.println();
				System.out.print("Target: ");
				ChessPosition target = UI.readChessPosition(sc);
				
				ChessPiece capturedPiece = chessMatch.performChessMove(source, target);
				
				if(capturedPiece != null) {
					captured.add(capturedPiece);
				}
				
				//verifica��o pra quando a pe�a e promovida
				if(chessMatch.getPromoted() != null) {
					System.out.print("Enter piece for promotion (B/C/R/Q): ");					
					String type = sc.nextLine().toUpperCase();
					while(!type.contentEquals("B") && !type.contentEquals("C") && !type.contentEquals("R") && !type.contentEquals("Q") ) {
						System.out.print("Invalid value! Enter piece for promotion (B/C/R/Q): ");
						type = sc.nextLine().toUpperCase();
					}
					chessMatch.replacePromotedPiece(type);
					
				}
			}
			catch(ChessException e) {
				System.out.println(e.getMessage());
				sc.nextLine();
			}
			catch(InputMismatchException e) {
				System.out.println(e.getMessage());
				sc.nextLine();
			}
		}
		UI.clearScreen();
		UI.printMatch(chessMatch, captured);
		
	}

}
