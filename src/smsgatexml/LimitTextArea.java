package smsgatexml;
/**
 *
 * @author Быков Вячеслав
 */


import java.awt.Component;
import javax.swing.JTextArea;
import javax.swing.table.*;
import javax.swing.JTable;
import javax.swing.text.*;

class LimitTextArea extends JTextArea {
  protected int textLengthLimit = -1;
  public LimitTextArea() {super();}
  public LimitTextArea(String text) {super(text);}
  public LimitTextArea(String text, int aRow ,int aCol) {super(text,aRow,aCol);}
  public int getTextLengthLimit() {return textLengthLimit;}
  public void setTextLengthLimit(int textLengthLimit){this.textLengthLimit=textLengthLimit;}
  public int getLength() {if (this!=null){return this.getLength();}else {return 0;}}

  protected Document createDefaultModel() { return new TextLengthLimitDocument();}

  protected class TextLengthLimitDocument extends PlainDocument
  {
    public void insertString(int offs, String str, AttributeSet a) throws BadLocationException
    {
      if (str == null) return;
       String insertStr = str;

      if (textLengthLimit > 0)
      {
        int curLength = this.getLength();
        if (curLength >= textLengthLimit) return;
        int insertLength = insertStr.length();
        if ((curLength + insertLength) > textLengthLimit)
          insertStr = insertStr.substring(0, (textLengthLimit - curLength - 1));
      }
      super.insertString(offs, insertStr, a);
    }
  }
}