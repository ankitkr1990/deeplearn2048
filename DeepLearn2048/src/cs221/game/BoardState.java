package cs221.game;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import cs221.game.Tile;
import cs221.game.Game2048.Move;
import cs221.util.Pair;
import cs221.util.Triplet;

public class BoardState {
  private Tile[] myTiles;

  public BoardState() {
  	reset();
  }
  
  public BoardState(BoardState o) {
  	this.myTiles = new Tile[o.myTiles.length];
  	for (int i = 0; i < o.myTiles.length; i++) {
      this.myTiles[i] = new Tile(o.myTiles[i]);
    }
  }

  public Tile[] getTiles() {
  	return myTiles;
  }
  
  public Tile tileAt(int x, int y) {
    return myTiles[x + y * 4];
  }
  
  public void reset() {
    myTiles = new Tile[4 * 4];
    for (int i = 0; i < myTiles.length; i++) {
      myTiles[i] = new Tile();
    }
    addTile();
    addTile();
  }

  public ArrayList<Move> getValidMoves() {
		ArrayList<Move> validMoves = new ArrayList<Move>();
		for (Move move : Move.values()) {
			if (evaluateMove(move).getThird())
				validMoves.add(move);
		}
		return validMoves;
	}
  
  // returns (afterState, reward, isValidMove)
	public Triplet<BoardState, Integer, Boolean> evaluateMove(Move move) {
		// Preserve a copy
		Tile[] oldState = new Tile[16];
		System.arraycopy(myTiles, 0, oldState, 0, 16);
		Triplet<BoardState, Integer, Boolean> output = null;
		if (move.equals(Move.UP)) {
			output = up();
		} else if (move.equals(Move.DOWN)) {
			output = down();
		} else if (move.equals(Move.LEFT)) {
			output = left();
		} else if (move.equals(Move.RIGHT)) {
			output = right();
		}
		// Revert state;
		myTiles = oldState;
		return output;
	}

  // Returns (afterstate, rewards, done)
  public Triplet<BoardState, Integer, Boolean> left() {
  	BoardState afterState = new BoardState();
  	int reward = 0;
    boolean needAddTile = false;
    for (int i = 0; i < 4; i++) {
      Tile[] line = getLine(i);
      Tile[] movedLine = moveLine(line);
      afterState.setLine(i, movedLine);
      Pair<Integer, Tile[]> rewardAndmergedTiles = mergeLine(movedLine);
      reward += rewardAndmergedTiles.getFirst().intValue();
      setLine(i, rewardAndmergedTiles.getSecond());
      if (!needAddTile && !compare(line, rewardAndmergedTiles.getSecond())) {
        needAddTile = true;
      }
    }
    if (needAddTile) {
      addTile();
    }
    return new Triplet<BoardState, Integer, Boolean>(afterState, reward, needAddTile);
  }

  public Triplet<BoardState, Integer, Boolean> right() {
    rotate(180);
    Triplet<BoardState, Integer, Boolean> output = left();
    rotate(180);
    output.getFirst().rotate(180);
    return new Triplet<BoardState, Integer, Boolean>(output.getFirst(), output.getSecond(), output.getThird());
  }

  public Triplet<BoardState, Integer, Boolean> up() {
    rotate(270);
    Triplet<BoardState, Integer, Boolean> output = left();
    rotate(90);
    output.getFirst().rotate(90);
    return new Triplet<BoardState, Integer, Boolean>(output.getFirst(), output.getSecond(), output.getThird());
  }

  public Triplet<BoardState, Integer, Boolean> down() {
    rotate(90);
    Triplet<BoardState, Integer, Boolean> output = left();
    rotate(270);
    output.getFirst().rotate(270);
    return new Triplet<BoardState, Integer, Boolean>(output.getFirst(), output.getSecond(), output.getThird());
  }


  private void addTile() {
    List<Tile> list = availableSpace();
    if (!availableSpace().isEmpty()) {
      int index = (int) (Math.random() * list.size()) % list.size();
      Tile emptyTime = list.get(index);
      emptyTime.value = Math.random() < 0.9 ? 2 : 4;
    }
  }

  private List<Tile> availableSpace() {
    final List<Tile> list = new ArrayList<Tile>(16);
    for (Tile t : myTiles) {
      if (t.isEmpty()) {
        list.add(t);
      }
    }
    return list;
  }

  private boolean isFull() {
    return availableSpace().size() == 0;
  }

  public boolean canMove() {
    if (!isFull()) {
      return true;
    }
    for (int x = 0; x < 4; x++) {
      for (int y = 0; y < 4; y++) {
        Tile t = tileAt(x, y);
        if ((x < 3 && t.value == tileAt(x + 1, y).value)
          || ((y < 3) && t.value == tileAt(x, y + 1).value)) {
          return true;
        }
      }
    }
    return false;
  }
  
  public boolean isWin() {
  	for (int x = 0; x < 4; x++)
      for (int y = 0; y < 4; y++)
        if (tileAt(x, y).value == 2048)
        	return true;
  	return false;
  }

  private boolean compare(Tile[] line1, Tile[] line2) {
    if (line1 == line2) {
      return true;
    } else if (line1.length != line2.length) {
      return false;
    }
    for (int i = 0; i < line1.length; i++) {
      if (line1[i].value != line2[i].value) {
        return false;
      }
    }
    return true;
  }

  private void rotate(int angle) {
    Tile[] newTiles = new Tile[4 * 4];
    int offsetX = 3, offsetY = 3;
    if (angle == 90) {
      offsetY = 0;
    } else if (angle == 270) {
      offsetX = 0;
    }
    double rad = Math.toRadians(angle);
    int cos = (int) Math.cos(rad);
    int sin = (int) Math.sin(rad);
    for (int x = 0; x < 4; x++) {
      for (int y = 0; y < 4; y++) {
        int newX = (x * cos) - (y * sin) + offsetX;
        int newY = (x * sin) + (y * cos) + offsetY;
        newTiles[(newX) + (newY) * 4] = tileAt(x, y);
      }
    }
    System.arraycopy(newTiles, 0, myTiles, 0, 16);
  }

  private Tile[] moveLine(Tile[] oldLine) {
    LinkedList<Tile> l = new LinkedList<Tile>();
    for (int i = 0; i < 4; i++) {
      if (!oldLine[i].isEmpty())
        l.addLast(oldLine[i]);
    }
    if (l.size() == 0) {
      return oldLine;
    } else {
      Tile[] newLine = new Tile[4];
      ensureSize(l, 4);
      for (int i = 0; i < 4; i++) {
        newLine[i] = l.removeFirst();
      }
      return newLine;
    }
  }

  // returns Pair(reward, newLine)
  private Pair<Integer, Tile[]> mergeLine(Tile[] oldLine) {
    LinkedList<Tile> list = new LinkedList<Tile>();
    int reward = 0;
    for (int i = 0; i < 4 && !oldLine[i].isEmpty(); i++) {
      int num = oldLine[i].value;
      if (i < 3 && oldLine[i].value == oldLine[i + 1].value) {
        num *= 2;
        reward += num;
        i++;
      }
      list.add(new Tile(num));
    }
    if (list.size() == 0) {
      return new Pair<Integer, Tile[]>(0, oldLine);
    } else {
      ensureSize(list, 4);
      return new Pair<Integer, Tile[]>(reward, list.toArray(new Tile[4]));
    }
  }

  // Fill up the list with empty tiles to make the length
  private static void ensureSize(List<Tile> l, int s) {
    while (l.size() != s) {
      l.add(new Tile());
    }
  }

  private Tile[] getLine(int index) {
    Tile[] result = new Tile[4];
    for (int i = 0; i < 4; i++) {
      result[i] = tileAt(i, index);
    }
    return result;
  }

  private void setLine(int index, Tile[] src) {
    System.arraycopy(src, 0, myTiles, index * 4, 4);
  }
  
  public boolean equals(Object o) {
  	if (!(o instanceof BoardState)) {
  		return false;
  	}
  	BoardState b = (BoardState) o;
  	for (int x = 0; x < 4; x++)
      for (int y = 0; y < 4; y++)
        if (!this.tileAt(x, y).equals(b.tileAt(x, y)))
        	return false;
  	return true;
  }
  
  public String toString() {
  	String str = new String();
  	for (int y = 0; y < 4; y++) {
  		str += "|";
      for (int x= 0; x < 4; x++) {
        str += tileAt(x, y).value + "|";
      }
      str += "\n";
  	}
  	return str;
  }
}