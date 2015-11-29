package cs221.game;

import javax.swing.*;
import cs221.util.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

/**
 * @author Konstantin Bulenkov
 */
public class Game2048 {
	private BoardState state;
	public Display display;
	boolean win;
  boolean lose;
  int score;

  public enum Move {
  	LEFT, RIGHT, UP, DOWN
  }
  
  public enum GameStatus {
  	WIN, LOSE, NOT_OVER
  }

	public Game2048(boolean automated, boolean displayActive) {
		state = new BoardState();
		win = false;
		lose = false;
		score = 0;
		if (displayActive)
			display = new Display(this, automated);
		else
			display = null;
	}

	public void resetGame() {
		state.reset();
		win = false;
		lose = false;
		score = 0;
		if (display != null)
			display.repaint();
	}
	
	public int getScore() {
		return score;
	}
	
	public BoardState getState() {
		return state;
	}

	// Returns (afterstate, increment)
	public Pair<BoardState, Integer> performMove(Move move) {
		Triplet<BoardState, Integer, Boolean> output = null;
		if (move.equals(Move.UP)) {
			output = state.up();
		} else if (move.equals(Move.DOWN)) {
			output = state.down();
		} else if (move.equals(Move.RIGHT)) {
			output = state.right();
		} else if (move.equals(Move.LEFT)) {
			output = state.left();
		}
		score += output.getSecond(); // Increment score
		// Refrest game status
		if (state.isWin()) win = true;
		else if (!state.canMove()) lose = true;
		assert output.getThird();
		if (display != null) {
			display.repaint(); // Repaint the display
			// Eat some CPU time to make display bearable.
			try {
			  Thread.sleep(1000);
			} catch (InterruptedException ie) {
				System.out.println("Thread woken up by interrupt : " + ie.toString());
			}
		}
		return new Pair<BoardState, Integer>(output.getFirst(), output.getSecond());
	}
	
	public ArrayList<Move> getValidMoves() {
		return state.getValidMoves();
	}
	
	public boolean isValidMove(Move move) {
		return getValidMoves().contains(move);
	}
	
	// returns (afterState, reward, isValidMove)
	public Triplet<BoardState, Integer, Boolean> evaluateMove(Move move) {
		return state.evaluateMove(move);
	}

	public GameStatus getGameStatus() {
		if (lose) return GameStatus.LOSE;
		if (win) return GameStatus.WIN;
		return GameStatus.NOT_OVER;
	}

	static class Display extends JPanel {
		private static final Color BG_COLOR = new Color(0xbbada0);
		private static final String FONT_NAME = "Arial";
		private static final int TILE_SIZE = 64;
		private static final int TILES_MARGIN = 16;
		private Boolean automated;
		private Game2048 game;

		public Display(Game2048 _game, Boolean _automated) {
			automated = _automated;
			game = _game;
			setFocusable(true);
			if (!automated) {
				addKeyListener(new KeyAdapter() {
					@Override
					public void keyPressed(KeyEvent e) {
						if (!game.win && !game.lose) {
							switch (e.getKeyCode()) {
							case KeyEvent.VK_LEFT:
								if (game.isValidMove(Move.LEFT)) { 
									game.performMove(Move.LEFT);
								}
								break;
							case KeyEvent.VK_RIGHT:
								if (game.isValidMove(Move.RIGHT)) { 
									game.performMove(Move.RIGHT);
								}
								break;
							case KeyEvent.VK_DOWN:
								if (game.isValidMove(Move.DOWN)) { 
									game.performMove(Move.DOWN);
								}
								break;
							case KeyEvent.VK_UP:
								if (game.isValidMove(Move.UP)) { 
									game.performMove(Move.UP);
								}
								break;
							}
						}
						repaint();
					}
				});
			}
	  }
	  
	  @Override
	  public void paint(Graphics g) {
	    super.paint(g);
	    g.setColor(BG_COLOR);
	    g.fillRect(0, 0, this.getSize().width, this.getSize().height);
	    for (int y = 0; y < 4; y++) {
	      for (int x = 0; x < 4; x++) {
	        drawTile(g, game.state.getTiles()[x + y * 4], x, y);
	      }
	    }
	  }

	  private void drawTile(Graphics g2, Tile tile, int x, int y) {
	    Graphics2D g = ((Graphics2D) g2);
	    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	    g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
	    int value = tile.value;
	    int xOffset = offsetCoors(x);
	    int yOffset = offsetCoors(y);
	    g.setColor(tile.getBackground());
	    g.fillRoundRect(xOffset, yOffset, TILE_SIZE, TILE_SIZE, 14, 14);
	    g.setColor(tile.getForeground());
	    final int size = value < 100 ? 36 : value < 1000 ? 32 : 24;
	    final Font font = new Font(FONT_NAME, Font.BOLD, size);
	    g.setFont(font);

	    String s = String.valueOf(value);
	    final FontMetrics fm = getFontMetrics(font);

	    final int w = fm.stringWidth(s);
	    final int h = -(int) fm.getLineMetrics(s, g).getBaselineOffsets()[2];

	    if (value != 0)
	      g.drawString(s, xOffset + (TILE_SIZE - w) / 2, yOffset + TILE_SIZE - (TILE_SIZE - h) / 2 - 2);

	    if (game.win || game.lose) {
	      g.setColor(new Color(255, 255, 255, 30));
	      g.fillRect(0, 0, getWidth(), getHeight());
	      g.setColor(new Color(78, 139, 202));
	      g.setFont(new Font(FONT_NAME, Font.BOLD, 48));
	      if (game.win) {
	        g.drawString("You won!", 68, 150);
	      }
	      if (game.lose) {
	        g.drawString("Game over!", 50, 130);
	        g.drawString("You lose!", 64, 200);
	      }
	      if (game.win || game.lose) {
	        g.setFont(new Font(FONT_NAME, Font.PLAIN, 16));
	        g.setColor(new Color(128, 128, 128, 128));
	      }
	    }
	    g.setFont(new Font(FONT_NAME, Font.PLAIN, 18));
	    g.drawString("Score: " + game.score, 200, 365);
	  }

	  private static int offsetCoors(int arg) {
	    return arg * (TILES_MARGIN + TILE_SIZE) + TILES_MARGIN;
	  }
	}
}
