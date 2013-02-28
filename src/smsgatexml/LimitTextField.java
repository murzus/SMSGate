package smsgatexml;
/**
 *
 * @author Быков Вячеслав
 * разработано  для Шебекинского районного суда Белгородской обл.
 */
import javax.swing.JTextField;
import javax.swing.text.*;
class LimitTextField extends JTextField
{
  protected int textLengthLimit = -1;
  public LimitTextField() {super();}

  public LimitTextField(String text) {super(text);}

  public LimitTextField( int col) {super(col);}

  public int getTextLengthLimit() {return textLengthLimit;}

  public void setTextLengthLimit(int textLengthLimit){this.textLengthLimit=textLengthLimit;}

  protected Document createDefaultModel() { return new TextLengthLimitDocument();}

  protected class TextLengthLimitDocument extends PlainDocument
  {
    public void insertString(int offs, String str, AttributeSet a) throws BadLocationException
    {
      if (str == null) return;
       String insertStr = str;

      try { Long.parseLong(str);}
      catch (Exception e) { return;}

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