package spreadsheet;

import spreadsheet.api.*;
import spreadsheet.api.value.*;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;

public class Spreadsheet implements SpreadsheetInterface {

  private Map<CellLocation, Cell> map = new HashMap<CellLocation, Cell>();
  private Set<Cell> toRecompute = new HashSet<Cell>();
  private Map<CellLocation, Double> valueMap = new HashMap<CellLocation, Double>();

  public void setExpression(CellLocation location, String expression) {
    Cell c = getCell(location);
    if (c == null) {
      Cell cell = new Cell(location, this, expression);
      map.put(location, cell);
    } else {
      c.setExp(expression);
    }
  }

  public String getExpression(CellLocation location){
    Cell c = getCell(location);
    if (c == null) {
      return "";
    }  else {
      return c.getExp();
    }
  }

  public Value getValue(CellLocation location) {
    Cell c = getCell(location);
    if (c == null) {
      return null;
    }  else {
      return c.getValue();
    }
  }

  public Cell getCell(CellLocation location) {
    return map.get(location);
  }

  public void recompute() {
    checkLoop();
	calculateValue();
    for (Cell c : toRecompute) {
     c.setLoopDetected(false);
	 c.markAsRecompute(false);
    }
    toRecompute.clear();
  }
  
  private void checkLoop() {
    for (Cell c : toRecompute) {
      if (!c.isLoopDetected()) {
	    c.checkLoop(new ArrayList<Cell>());
	  }
    }
  }
  
  private void calculateValue() {
    for (Cell c : toRecompute) {
	  if (!c.isLoopDetected()) {
	    c.calculateValue(valueMap);
	  }
	}
  }

  public void addToRecompute(Cell c) {
    toRecompute.add(c);
	c.markAsRecompute(true);
  }
}
