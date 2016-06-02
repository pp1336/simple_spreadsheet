package spreadsheet;
import spreadsheet.api.*;
import spreadsheet.api.value.*;
import spreadsheet.api.observer.*;
import java.util.HashSet;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

public class Cell implements Observer<Cell>{

  private CellLocation location;
  private Spreadsheet sheet;
  private String expression = "";
  private Value value;
  private Set<Cell> meToOther = new HashSet<Cell>();
  private Set<Observer<Cell>> refToMe = new HashSet<Observer<Cell>>();
  private boolean loopDetected = false;
  private boolean needToRecompute = false;

  Cell(CellLocation location, Spreadsheet sheet, String expression) {
    this.location = location;
    this.sheet = sheet;
    this.expression = expression;
	this.value = new InvalidValue(expression);
    Set<CellLocation> locations = ExpressionUtils.getReferencedLocations(expression);
    subAll();
    sheet.addToRecompute(this);
  }

  private void subAll() {
    Set<CellLocation> locations = ExpressionUtils.getReferencedLocations(expression);
    if (!locations.isEmpty()) {
      for (CellLocation l : locations) {
	    if (l.equals(location)) {
		  meToOther.add(this);
		  continue;
		}
        Cell c = sheet.getCell(l);
        if (c == null) {
          sheet.setExpression(l, "");
        }
        c = sheet.getCell(l);
        meToOther.add(c);
        c.sub(this);
      }
    }
  }

  Value getValue() {
    return value;
  }

  void setValue(Value value) {
    this.value = value;
  }

  String getExp() {
    return expression;
  }

  void setExp(String expression) {
    for (Cell c : meToOther) {
      c.unsub(this);
    }
    this.expression = expression;
    value = new InvalidValue(expression);
    sheet.addToRecompute(this);
    subAll();
    updateAllAbove();
  }

  private void updateAllAbove() {
    for (Observer<Cell> c : refToMe) {
      c.update(this);
    }
  }

  public void update(Cell changed) {
    if (!needToRecompute) {
      value = new InvalidValue(expression);
      sheet.addToRecompute(this);
      updateAllAbove();
    }
  }

  void sub(Observer<Cell> c) {
    refToMe.add(c);
  }

  void unsub(Observer<Cell> c) {
    refToMe.remove(c);
  }
  
  boolean needToRecompute() {
    return needToRecompute;
  }

  void markAsRecompute(boolean b) {
    needToRecompute = b;
  }
  
  boolean isLoopDetected() {
    return loopDetected;
  }
  
  void setLoopDetected(boolean b) {
    loopDetected = b;
  }
  
  private ArrayList<Cell> cloneList(ArrayList<Cell> list) {
    ArrayList<Cell> clone = new ArrayList<Cell>(list.size());
    for(Cell item: list) clone.add(item);
    return clone;
  }
  
  void checkLoop(ArrayList<Cell> cells) {
	if (cells.contains(this)) {
	  int n = cells.indexOf(this);
	  for (int i = n; i < cells.size(); i++) {
	    Cell cell = cells.get(i);
		if (needToRecompute()) {
		  cell.setLoopDetected(true);
		  cell.markAsRecompute(false);
		  cell.setValue(LoopValue.INSTANCE);
		}
	  }
	} else {
	  if (meToOther == null || meToOther.size() == 0) return;
	  ArrayList<Cell> newCells = cloneList(cells);
	  newCells.add(this);
	  for (Cell c : meToOther) {
	    c.checkLoop(newCells);
	  }
	}
  }
  
  void calculateValue(Map<CellLocation, Double> m) {
    if (meToOther == null || meToOther.size() == 0) {
	  value = ExpressionUtils.computeValue(expression, m);
	  needToRecompute = false;
	  if (value instanceof StringValue) {
	    m.remove(location);
	  }
	  if (value instanceof DoubleValue) {
	    m.put(location, Double.valueOf((((DoubleValue) value).toString())));
	  }
	  return;
	}
    for (Cell c : meToOther) {
	  if (c.getValue() == LoopValue.INSTANCE || 
	     (c.getValue() instanceof InvalidValue 
		 && !c.needToRecompute())) {
	    value = new InvalidValue(expression);
		m.remove(location);
		needToRecompute = false;
		return;
	  } else if (c.needToRecompute()) {
	    c.calculateValue(m);
		c.markAsRecompute(false);
	  }
	}
	m.remove(location);
	value = ExpressionUtils.computeValue(expression, m);
	if (value instanceof DoubleValue) {
	    m.put(location, Double.valueOf((((DoubleValue) value).toString())));
	}
	needToRecompute = false;
  }
}