package smsgatexml;
/**
 *
 * @author Быков Вячеслав
 */

import javax.swing.table.*; 
import java.util.*;

 public class SmsTableModel extends DefaultTableModel{ 

 protected Vector  rows  = new Vector();
 int rowsize;  
 private boolean flag;
 protected Vector colons = new Vector(); 
 
   SmsTableModel(Vector aRows,Vector aColons,boolean admin_mode){
     super(aRows.size(),aColons.size());
     rows = aRows;
     rowsize = rows.size();
     colons = aColons;
     flag = admin_mode;
    
    }

    @Override
    public Object getValueAt(int row, int col) {
    Vector atrow = (Vector)rows.elementAt(row);
    return atrow.elementAt(col);    
    }

    @Override
    public int getRowCount() {
       return rowsize; 
    }

    public int getColumnCount() {
        return colons.size(); 
    }
	
    public String getColumnName(int col) {
        return (String)colons.elementAt(col); 
    }
    
    public boolean isCellEditable(int row, int col) { 
       if(col>4 && flag) return true;
       else return false;
    }
    public void setValueAt(Object val,int row, int col) {
     if(col>4 && flag){ 
         Vector atrow= (Vector)rows.elementAt(row);
         atrow.set(col,val);
         rows.set(row,atrow);
         //fireTableCellUpdated(row,col);  
     }
    }
     public Class getColumnClass(int col) {
         Vector atrow= (Vector)rows.elementAt(0);
         return atrow.elementAt(col).getClass(); 
     
    }

}
