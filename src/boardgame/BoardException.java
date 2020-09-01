package boardgame;

//RuntimeException foi escolhido para que seja uma opção de ser tratada
public class BoardException extends RuntimeException{

	//versão default
	private static final long serialVersionUID = 1L;
	
	public BoardException (String msg) {
		super(msg);
	}
	
}
