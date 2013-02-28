package smsgatexml;
/**
 *
 * @author Быков Вячеслав
 */
import java.awt.*;
import java.io.*;
import java.awt.print.*;

class FilePagePainter implements Printable{ 

 private BufferedReader br; 
 private String         file; 
 private int            page = -1; 
 private boolean        eof; 
 private String[]       line; 
 private int            numLines;
           
 
 public FilePagePainter(String file){

   this.file = file;
   try{
         br = new BufferedReader(new FileReader(file));
      }catch(IOException e){ eof = true; } 
 } 

  public int print(Graphics g, PageFormat pf, int ind)  throws PrinterException{
   g.setColor(Color.black);
   g.setFont(new Font("Serif", Font.PLAIN, 12));
 try{
   int h = (int)pf.getImageableHeight();
   int x = (int)pf.getImageableX() + 10;
   int у = (int)pf.getImageableY() + 12;


    if (ind != page){                           // Если система печати запросила эту страницу первый раз 
       if (eof) return Printable.NO_SUCH_PAGE; 
          page = ind;
          line = new String[h/12];             // Массив строк на странице
          numLines =0;                         // Число строк на странице
                                     
     while (у + 48 < pf.getImageableY() + h){   // Читаем строки из файла и формируем массив строк  
           line[numLines] = br.readLine(); 
           if (line[numLines] == null){ 
               eof = true; break;
            }
           numLines++; 
         у += 12;
      }

   }
    
   for (int i = 0; i < numLines; i++){              // Размещаем строки текста текущей страницы 
     g.drawString(line[i], x, у) ; 
     у += 12; 
   }
        return Printable.PAGE_EXISTS; 
  }catch (IOException e){ return Printable.NO_SUCH_PAGE;} 
 } 

} 