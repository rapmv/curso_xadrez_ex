package boardgame;

//RuntimeException foi escolhido para que seja uma op��o de ser tratada
public class BoardException extends RuntimeException{

	//vers�o default
	private static final long serialVersionUID = 1L;
	
	public BoardException (String msg) {
		super(msg);
	}
	
}
