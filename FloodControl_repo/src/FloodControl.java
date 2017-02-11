import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class FloodControl {

	private Stage stage;
	private Canvas canvas;
	private GraphicsContext graphicsContext;
	private long startNanoTime;

	private Image playingPieces;
	private Image backgroundScreen;
	private Image titleScreen;
	private UserInputQueue userInputQueue;
	private GameAnimationTimer animationTimer;
	private GameBoard gameBoard;
	Point2D boardOrigin;

	private enum State {
		TitleScreen, Playing
	};

	private State state;

	private class GameAnimationTimer extends AnimationTimer {
		@Override
		public void handle(long currentNanoTime) {
			update(currentNanoTime);
			draw(currentNanoTime);
		}
	}

	public FloodControl(Stage primaryStage) {
		stage = primaryStage;
		stage.setTitle("Flood Control");
		stage.getIcons().add(new Image("content/icons/Game.png"));
		startNanoTime = System.nanoTime(); // czas rozpocz�cia gry
	}

	public void run() {
		initialize();
		loadContent();
		stage.show();
		animationTimer = new GameAnimationTimer();
		animationTimer.start();
	}

	private void initialize() {
		Group root = new Group();
		canvas = new Canvas(800, 600);
		root.getChildren().add(canvas);

		graphicsContext = canvas.getGraphicsContext2D();

		Scene scene = new Scene(root);
		stage.setScene(scene);
		stage.setResizable(false);
		stage.sizeToScene();
		stage.setOnCloseRequest(e -> stage_CloseRequest(e));
		state = State.TitleScreen;

		userInputQueue = new UserInputQueue();
		scene.setOnKeyPressed(keyEvent -> userInputQueue.addKey(keyEvent));
		scene.setOnMouseClicked(mouseEvent -> userInputQueue.addMouse(mouseEvent));
		gameBoard = new GameBoard();
		boardOrigin = new Point2D(70, 89); // lewy gorny rog obszaru klienta
											// okna, od ktorego zaczyna sie
											// rysowanie planszy gry

	}

	private void stage_CloseRequest(WindowEvent windowEvent) {
		windowEvent.consume();

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				if (AlertBox.showAndWait(AlertType.CONFIRMATION, "Flood Control", "Do you want to stop the game?")
						.orElse(ButtonType.CANCEL) == ButtonType.OK) {
					animationTimer.stop();
					unloadContent();
					stage.close();
				}
			}
		});
	}

	private void loadContent() {
		playingPieces = new Image("content/textures/Tile_Sheet.png");
		backgroundScreen = new Image("content/textures/Background.png");
		titleScreen = new Image("content/textures/TitleScreen.png");
	}

	private void unloadContent() {
	}

	private void update(long currentNanoTime) {
		KeyCode keyCode = userInputQueue.getKeyCode();

		switch (state) {
		case TitleScreen:
			if (keyCode == KeyCode.SPACE) {
				state = State.Playing;
				gameBoard.generateNewPieces();
			} else if (keyCode == KeyCode.ESCAPE) {
				stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
			}
			break;
		case Playing:
			break;
		}
	}

	private void draw(long currentNanoTime) {
		graphicsContext.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
		switch (state) {
		case TitleScreen:
			graphicsContext.drawImage(titleScreen, 0, 0);
			break;
		case Playing:
			graphicsContext.drawImage(backgroundScreen, 0, 0);

			PixelReader pixReader = playingPieces.getPixelReader();
			WritableImage emptyPiece = new WritableImage(pixReader, 1, 247, GamePiece.pieceWidth,
					GamePiece.pieceHeight);

			for (int x = 0; x < GameBoard.GAME_BOARD_WIDTH; x++) {
				for (int y = 0; y < GameBoard.GAME_BOARD_HEIGHT; y++) {
					int pixelX = (int) boardOrigin.getX() + (x * GamePiece.pieceWidth);
					int pixelY = (int) boardOrigin.getY() + (y * GamePiece.pieceHeight);

					graphicsContext.drawImage(emptyPiece, pixelX, pixelY);

					Rectangle2D rect = gameBoard.getSourceRect(x, y);
					WritableImage thePiece = new WritableImage(pixReader, (int) rect.getMinX(), (int) rect.getMinY(),
							GamePiece.pieceWidth, GamePiece.pieceHeight);
					graphicsContext.drawImage(thePiece, pixelX, pixelY);
				}
			}

			break;
		}
	}

	private void handleMouseInput(MouseEvent me) {
		MouseEvent mouseState = userInputQueue.getMouse();
		if (mouseState == null)
			return;
		int x = (int) (mouseState.getSceneX() - (int) boardOrigin.getX()) / GamePiece.pieceWidth;
		int y = (int) (mouseState.getSceneY() - (int) boardOrigin.getY()) / GamePiece.pieceHeight;
		if ((x >= 0) && (x < GameBoard.GAME_BOARD_WIDTH) && (y >= 0 && y < GameBoard.GAME_BOARD_HEIGHT)) {
			if (mouseState.getButton() == MouseButton.PRIMARY) {
				gameBoard.addRotatingPiece(x, y, gameBoard.getPieceType(x, y), false);
				gameBoard.rotatePiece(x, y, false);
			} else if (mouseState.getButton() == MouseButton.SECONDARY) {
				gameBoard.addRotatingPiece(x, y, gameBoard.getPieceType(x, y), true);
				gameBoard.rotatePiece(x, y, true);
			}
		}
	}

}
