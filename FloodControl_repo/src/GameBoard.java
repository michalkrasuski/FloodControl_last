import java.util.Random;

import javafx.geometry.Rectangle2D;

public class GameBoard {

	static final int GAME_BOARD_WIDTH = 8;
	static final int GAME_BOARD_HEIGHT = 10;
	private static final GamePiece[][] boardSquares = new GamePiece[GAME_BOARD_WIDTH][GAME_BOARD_HEIGHT];
	private final Random random;
	public RotatingPiece rotatingPiece = null;
	public String rotPositionName = "";

	public GameBoard() {
		random = new Random();
		clearBoard();
	}

	public void clearBoard() {
		for (int x = 0; x < GAME_BOARD_WIDTH; x++)
			for (int y = 0; y < GAME_BOARD_HEIGHT; y++)
				boardSquares[x][y] = new GamePiece("Empty", "");
	}

	public Rectangle2D getSourceRect(int x, int y) {
		return boardSquares[x][y].getSourceRect();
	}

	public void randomPiece(int x, int y) {
		boardSquares[x][y].setPiece(GamePiece.pieceTypes[random.nextInt(GamePiece.maxPlayablePieceIndex + 1)], "");
	}

	public String getPieceType(int x, int y) {
		return boardSquares[x][y].getPieceType();
	}

	public void setPiece(int x, int y, String pieceType) {
		boardSquares[x][y].setPiece(pieceType, "");
	}

	public void generateNewPieces() {
		for (int y = 0; y < GAME_BOARD_HEIGHT; y++) {
			for (int x = 0; x < GAME_BOARD_WIDTH; x++) {
				if (getPieceType(x, y) == "Empty") {
					randomPiece(x, y);
				}
			}
		}
	}

	public void rotatePiece(int x, int y, boolean clockwise) {
		boardSquares[x][y].rotatePiece(clockwise);
	}
	
	public void addRotatingPiece (int x, int y, String pieceName, boolean clockwise){
		rotatingPiece = new RotatingPiece(pieceName, clockwise);
		rotPositionName = x + " " + y;
	}
}
