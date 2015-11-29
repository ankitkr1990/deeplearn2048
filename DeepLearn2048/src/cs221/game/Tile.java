package cs221.game;

import java.awt.Color;

public class Tile {
  int value;

  public Tile() {
    this(0);
  }

  public Tile(int num) {
    value = num;
  }

  public Tile(Tile tile) {
		this.value = tile.value;
	}

	public boolean isEmpty() {
    return value == 0;
  }

  public Color getForeground() {
    return value < 16 ? new Color(0x776e65) :  new Color(0xf9f6f2);
  }

  public Color getBackground() {
    switch (value) {
      case 2:    return new Color(0xeee4da);
      case 4:    return new Color(0xede0c8);
      case 8:    return new Color(0xf2b179);
      case 16:   return new Color(0xf59563);
      case 32:   return new Color(0xf67c5f);
      case 64:   return new Color(0xf65e3b);
      case 128:  return new Color(0xedcf72);
      case 256:  return new Color(0xedcc61);
      case 512:  return new Color(0xedc850);
      case 1024: return new Color(0xedc53f);
      case 2048: return new Color(0xedc22e);
    }
    return new Color(0xcdc1b4);
  }
  
  public boolean equals(Object o) {
  	if (!(o instanceof Tile)) return false;
  	return this.value == ((Tile) o).value;
  }
}