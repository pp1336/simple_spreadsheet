package spreadsheet;
import spreadsheet.gui.*;

public class Main {

    private static final int DEFAULT_NUM_ROWS = 5000;
    private static final int DEFAULT_NUM_COLUMNS = 5000;

    public static void main(String[] args) {
      int row;
      int col;
      if (args.length == 0) {
        row = DEFAULT_NUM_ROWS;
        col = DEFAULT_NUM_COLUMNS;
      } else {
        assert args.length >= 2 : "insufficient argument";
        assert (isInteger(args[0]) && isInteger(args[1])) : "invalid argument";
        row = Integer.parseInt(args[0]);
        col = Integer.parseInt(args[1]);
        assert (row > 0 && col > 0) : "cannot create zero spreadsheet";
      }
      Spreadsheet ssht = new Spreadsheet();
      SpreadsheetGUI gui = new SpreadsheetGUI(ssht, row, col); 
      gui.start();
    }

    private static boolean isInteger(String s) {
      try { 
        Integer.parseInt(s); 
      } catch(NumberFormatException e) { 
        return false; 
      }
      return true;
    }

}
