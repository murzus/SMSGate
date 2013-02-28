package smsgatexml;
/**
 *
 * @author Быков Вячеслав
 */
import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import java.awt.print.PrinterException;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.MaskFormatter;
import java.sql.*;
import java.text.*;
import java.util.Date;
import java.util.Vector;
import java.io.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class SMSGateXML extends JFrame {
    final static String ownerName  = "Шебекинский районный суд";
    final static String uName = "SYSDBA" ;
    final static String pass  = "mas";
    final static String   connectString = "//192.168.1.113/C:/DATA/justice/Uni_Work2003.gdb";
    final static String   connectString2 = "//192.168.1.113/C:/DATA/justice/sms.gdb";
    final static String[] countryCod = { "Россия +7" ,
                                         "Украина +38" , 
                                         "Белоруссия +37"
    };

    final static String[] statusString= { "",
                                         "Передано оператору" ,
                                         "Доставлено" , 
                                         "Не доставлено",
                                         "Время СМС истекло"
    };
    MaskFormatter dateMask = null;
    MaskFormatter timeMask = null;
    JFormattedTextField dateField=null ;
    JFormattedTextField timeField=null ;
		
    private static boolean admin_mode = false;
    
    JTextField  passwordField ,serverField, phoneNumberField;    
    JTextArea   textSmsArea;
    JComboBox   codsCombo,statusCombo;
    JPanel      connectionPanel, tableRezultPanel, buttonsPanel, buttonsPanel2, statusPanel;
    JPanel      newMessagePan, messagePhoneNumberPan;
    JLabel      passwordLabel  , serverLabel , statusLabel , cod;
    JButton     updateButton, printButton, saveButton, toFileButton, exitButton,  sendButton, clearButton; 
  
    LimitTextField messagePhoneNumber;
    LimitTextArea  messageText;

    JScrollPane tableAggregate, messageTextPan;
    JTable      table1,printTable;
    SmsTableModel stm;
    Image   murzus;
    PreparedStatement  stmt1;
    Statement stmt;
    static ResultSet   rs;
    static Connection con = null;
    static int count= 0; 
    String  currentUser,useridString;
    int countOfRec;
//===========================================================================================================
 public SMSGateXML(String s) {
        super(s);
        initForm();
 } 
//===============================================================================================================

 private void activateConnectionDialog() {
   final int limit = 3;//numbers of try 
   final JPasswordField jPasswordField = new JPasswordField();
   jPasswordField.setEchoChar('*');
   JOptionPane pane = new JOptionPane(jPasswordField,JOptionPane.PLAIN_MESSAGE,JOptionPane.DEFAULT_OPTION);
   JDialog dlg = pane.createDialog(this,"Введите пароль");
   dlg.addWindowListener(new WindowAdapter() {
	@Override
	public void windowOpened(WindowEvent e) {
		jPasswordField.requestFocus();
	}
   });
  dlg.setVisible(true);
  dlg.dispose();
  Object val = pane.getValue();
  int check = val != null ? ((Integer)val).intValue() : JOptionPane.CLOSED_OPTION;
                   if (check==0){
                     String s=new String(jPasswordField.getPassword());
	            if(connectToBase(s)==0){
                      if(++count>=limit){
                       JOptionPane.showMessageDialog(pane,"Вы исчерпали все попытки","Ошибка",JOptionPane.ERROR_MESSAGE);
                       System.exit(0);
                      }
                      JOptionPane.showMessageDialog(pane,"Неправильный пароль","Ошибка",JOptionPane.ERROR_MESSAGE);
                      dlg.dispose();
                      activateConnectionDialog();
                     }else{
                          if(admin_mode){statusLabel = new JLabel("Режим Оператора      Пользователь -  " + currentUser);
                                   printButton.setVisible(false);
                                   saveButton.setVisible(true);
                                   toFileButton.setVisible(true);
                          } else { statusLabel = new JLabel(""+ownerName+"     Пользователь  " + currentUser );   
                            }
                           statusPanel.add(statusLabel);
                           this.add(statusPanel,BorderLayout.SOUTH);  
                           setVisible(true);
                           dlg.dispose();
                           }
                  }else {System.exit(0);}
    }
//---------------------------------------------------------------------------------------------------------------
  
  private int connectToBase(String psw) {
   try {
    Class.forName ("org.firebirdsql.jdbc.FBDriver");
    }
    catch (java.lang.ClassNotFoundException e) {
     JOptionPane.showMessageDialog(connectionPanel,"Firebird JCA-JDBC driver not found in class path",
                   "Ошибка загрузки драйвера",JOptionPane.ERROR_MESSAGE);
              System.exit(0);
    } 
           
     try {
     con = DriverManager.getConnection("jdbc:firebirdsql:"+connectString,uName,pass);
     }
     catch (java.sql.SQLException sqle) {
      JOptionPane.showMessageDialog(connectionPanel,sqle.getMessage(),
                   "Ошибка",JOptionPane.ERROR_MESSAGE);
       System.exit(0);
     } 
      stmt = null;
     try {
      stmt  = con.createStatement();
     }
     catch (java.sql.SQLException EsqlConn) { 
      JOptionPane.showMessageDialog(connectionPanel,EsqlConn.getMessage(),
                   "Ошибка",JOptionPane.ERROR_MESSAGE);
       System.exit(0);
     } 
      rs  = null;
     try {
      rs  = stmt.executeQuery("SELECT groupcontentid,username,userpsw from groupcontent");
     }
     catch (java.sql.SQLException EsqlConn) {
      JOptionPane.showMessageDialog(connectionPanel,EsqlConn.getMessage(),
                   "Ошибка",JOptionPane.ERROR_MESSAGE);
        System.exit(0);
     } 
      try {
       while(rs.next()) {
        int userId = rs.getInt ("groupcontentid");
        String userName = rs.getString ("username");
        String userPsw  = rs.getString("userpsw"); 
         if (!(psw.equals(""))&&(userPsw.equals(psw))){
           currentUser = userName;
           if (userName.toUpperCase().equals("SMS")) admin_mode = true;
           useridString = Integer.toString(userId);
           rs.close();
           stmt.close();
           con.close(); //also closing stmt & rs
     try {
     con = DriverManager.getConnection("jdbc:firebirdsql:"+connectString2,uName,pass);
     }
     catch (java.sql.SQLException sqle) {
      JOptionPane.showMessageDialog(connectionPanel,sqle.getMessage(),
                   "Ошибка",JOptionPane.ERROR_MESSAGE);
       System.exit(0);
     } 
      stmt = null;
     try {
      stmt  = con.createStatement();
     }
     catch (java.sql.SQLException EsqlConn) { 
      JOptionPane.showMessageDialog(connectionPanel,EsqlConn.getMessage(),
                   "Ошибка",JOptionPane.ERROR_MESSAGE);
       System.exit(0);
     } 
           table1 = new JTable();
           initDataModel(admin_mode);
	   tableAggregate = new JScrollPane(table1);
           tableRezultPanel.add(tableAggregate);
           add(tableRezultPanel);
           pack();
           return userId;
         } //if
       } //While   
      }catch (java.sql.SQLException EsqlFetch) {
        JOptionPane.showMessageDialog(connectionPanel,EsqlFetch.getMessage(),
              "Ошибка",JOptionPane.ERROR_MESSAGE);
        System.exit(0);
      } 
    return 0;
 }
//------------------------------------------------------------------------------------------------------------
private void addNewMessageIntoBase(){
       try {
        Date date  = new Date();
        SimpleDateFormat sdf  = new SimpleDateFormat("HH:mm EE,dd MMM ,yy");
        String prefix="";
        String prefixName = (String)codsCombo.getSelectedItem();
         if(prefixName.equals("Россия +7")){prefix="7";}
         if(prefixName.equals("Украина +38")){prefix="38";}
         if(prefixName.equals("Белоруссия +37")){prefix="37";}           
              
        stmt.executeUpdate("INSERT INTO SMS VALUES(null,"+Integer.toString(++countOfRec)+",'"+useridString + "','" 
                                                   + currentUser +"','" + (String)sdf.format(date) +"','"
                                                   + prefix + messagePhoneNumber.getText()+"'," 
                                                   + antiSqlIjection(messageText.getText()) + ",'','')");
        initDataModel(admin_mode);//rewrite TableModel

       }catch (java.sql.SQLException EsqlConn) {
                   JOptionPane.showMessageDialog(connectionPanel,EsqlConn.getMessage(),
                   "Ошибка",JOptionPane.ERROR_MESSAGE);
        System.exit(0);
       }       
  }
//-------------------------------------------------------------------------------------------------------
 private String antiSqlIjection(String str){
   
   str=str.replaceAll("'","*");
   str=str.replaceAll("\"","*");
   //str=str.replaceAll("\\","*");
   str="'"+str+"'";
 //  System.out.println(str);
return str;
 }

//-----------------------------------------------------------------------------------------------------------------
  private void saveUpdateIntoBase(){
        int j=0; 
        int z; 
        rs=null;
        Vector<Integer> vector = new Vector<Integer>();
        try {
        rs = stmt.executeQuery("SELECT ID FROM SMS WHERE STATUS not in('Доставлено','Не доставлено','Время СМС истекло')");
         if (rs!=null){
           while(rs.next()) { 
             vector.addElement(rs.getInt("ID"));
           }
         }
        }catch (java.sql.SQLException EsqlConn) {
                   JOptionPane.showMessageDialog(connectionPanel,EsqlConn.getMessage(),
                   "Ошибка",JOptionPane.ERROR_MESSAGE);
         System.exit(0);
         }  
       if (vector.size()>0){
        for(int i=0; i<vector.size();i++) { 
          j=0;
          //System.out.println(vector.elementAt(i));

          while((j<table1.getRowCount())
               &&(!((table1.getValueAt(j,0)).toString()).equals(Integer.toString(vector.elementAt(i)))))
           {j++;}
              System.out.println(j);

              if((j<table1.getRowCount())&&(!((String)table1.getValueAt(j,5)).equals(""))&&(!((String)table1.getValueAt(j,6)).equals(""))){
         try{
         z=stmt.executeUpdate("UPDATE SMS SET STATUS='" +
                            (String)table1.getValueAt(j,5) +
                           "', RECEIVEDATE='" + 
                             (String)table1.getValueAt(j,6) + 
                           "'  WHERE ID = '"+ Integer.toString(vector.elementAt(i)) + "'"
                           );
                              
        }catch (java.sql.SQLException EsqlConn) {
                   JOptionPane.showMessageDialog(connectionPanel,EsqlConn.getMessage(),
                   "Ошибка",JOptionPane.ERROR_MESSAGE);
           System.exit(0);
         } 
              }//if
        }//for
       }//if(vector.size()>0){
  }


//----------------------------------------------------------------------------------------------------
 private void printReport(){

   int countRows=table1.getSelectedRowCount();
    if (countRows<1){
                    JOptionPane.showMessageDialog(rootPane,"Для отчета выделите одну или несколько строк.",
                                                  "! Пустой отчет !",JOptionPane.ERROR_MESSAGE);
     } else{
                       int mas[]=table1.getSelectedRows();
               Date date  = new Date();
               SimpleDateFormat sdf  = new SimpleDateFormat("HH:mm dd MMMM yyyy г.");
               try{
               PrintStream pr = new  PrintStream(new BufferedOutputStream(new FileOutputStream("SMSreport.txt",false)));
                pr.println("********************  ОТЧЕТ ПО СМС ИЗВЕЩЕНИЯМ *********************");
                pr.println(" ");
                pr.println("   "+ownerName+"                          "+(String)sdf.format(date));
                    pr.println(" ");
                    pr.println("-----------------------------------------------------");
                  for (int i=0;i<mas.length;i++){
                    if ((!(table1.getValueAt(mas[i],5).equals("")))&&
                        (!(table1.getValueAt(mas[i],5).equals("Передано оператору")))){
                    pr.println(" ID "+table1.getValueAt(mas[i],0).toString());
                    pr.println(" "); 
                    pr.println(" № Телефонa:      "+table1.getValueAt(mas[i],3));
                    pr.println(" ");
                    pr.println(" Текст сообщения: ");

                    String str=(String)table1.getValueAt(mas[i],4);
                    int pos=0;
                    int rowSize=70;
                 while((str.length()/rowSize)>0){
                     pos=rowSize;
                   while((pos>0)&&(!(str.substring(pos-1,pos).equals(" ")))){pos--;}
                    if(pos<1){pos=rowSize;}
                     pr.println( str.substring(0,pos));
                     str=str.substring(pos);
                 }
                    pr.println(str);
                    pr.println( );
                    pr.println(" ");
                    pr.println(" Состояние:       "+table1.getValueAt(mas[i],5));
                    pr.println(" Время и Дата:    "+table1.getValueAt(mas[i],6));
                    pr.println(" ");
                    pr.println("-----------------------------------------------------");
                    }//if 
                  }//for
                 pr.close();
               } catch (FileNotFoundException f){
                                     JOptionPane.showMessageDialog(rootPane,"Файл не найден",
                                                  "Ошибка",JOptionPane.ERROR_MESSAGE);
                 }
                PrinterJob pj = PrinterJob.getPrinterJob();
                PageFormat pf = pj.pageDialog(pj.defaultPage());
                pj.setPrintable(new FilePagePainter("SMSreport.txt"),pf);
                if (pj.printDialog()){
                 try{
                    pj.print();
                    }catch(PrinterException e){
                     JOptionPane.showMessageDialog(rootPane,"Не могу печатать",
                                                  "Ошибка принтера",JOptionPane.ERROR_MESSAGE);
                     }
                }
               
        }//else 
 }
//-----------------------------------------------------------------------------------------------------
  private void selectToFile(){              
     int countRows=table1.getSelectedRowCount();
     if (countRows<1){JOptionPane.showMessageDialog(rootPane,"Выберите  строки!!",
                                                     "Ошибка",JOptionPane.ERROR_MESSAGE);
       } else{
         int mas[]=table1.getSelectedRows();
          try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            // root elements
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("request");
            doc.appendChild(rootElement);
            // message elements
            int cnt=1;
          for (int i=0;i<mas.length;i++){
           if (table1.getValueAt(mas[i],5).equals("")){  
            Element message = doc.createElement("message");
             rootElement.appendChild(message);
             Attr typeAttr = doc.createAttribute("type");
             typeAttr.setValue("sms");
             message.setAttributeNode(typeAttr);
            Element senderElement = doc.createElement("sender");
             senderElement.setTextContent("ShebRaySud");
             message.appendChild(senderElement);
            Element textElement = doc.createElement("text");
             textElement.setTextContent((String)table1.getValueAt(mas[i],4));
             message.appendChild(textElement);
            Element abonentElement = doc.createElement("abonent");
             
             Attr phoneAttr = doc.createAttribute("phone");
             phoneAttr.setValue((String)table1.getValueAt(mas[i],3));
             abonentElement.setAttributeNode(phoneAttr);
             
             Attr numberSmsAttr = doc.createAttribute("number_sms");
             numberSmsAttr.setValue(Integer.toString(cnt++));
             abonentElement.setAttributeNode(numberSmsAttr);
             
             message.appendChild(abonentElement);
           }//if           
          }//for  
           // security elements
            Element security = doc.createElement("security");
            rootElement.appendChild(security);
            // login element
            Element login = doc.createElement("login");
            security.appendChild(login);
            // set attribute to login element
            Attr loginAttr = doc.createAttribute("value");
            loginAttr.setValue("shers");
            login.setAttributeNode(loginAttr);
            // password element
            Element password = doc.createElement("password");
            security.appendChild(password);
            // set attribute to password element
            Attr passwordAttr = doc.createAttribute("value");
            passwordAttr.setValue("nxzRe7Qf");
            password.setAttributeNode(passwordAttr);
            // Document to String
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StringWriter stringWriter = new StringWriter();
            StreamResult result = new StreamResult(stringWriter);
            transformer.transform(source, result);
            String request = stringWriter.getBuffer().toString();
            // Sring to File
             PrintStream pr = new  PrintStream(new BufferedOutputStream(new FileOutputStream("smsForSend.xml",false)),false,"utf-8");
              pr.println(request);
                 pr.close();            
           } catch (Exception ex) {} 
        }//else  
 }
//----------------------------------------------------------------------------------------------------
 private void initDataModel(boolean flag){
           rs=null;
   try{
       if(flag){
           rs =stmt.executeQuery(
           "SELECT ID,USERNAME,SENDDATE,PHONE,TEXTMESSAGE,STATUS,RECEIVEDATE FROM SMS");
        }else{
           rs =stmt.executeQuery(
           "SELECT ID,NUM,SENDDATE,PHONE,TEXTMESSAGE,STATUS,RECEIVEDATE FROM SMS WHERE USERID = "
           + useridString);
         }  
           Vector  colNames = new Vector();
           colNames.add(" ID");
           if(flag){
             colNames.add("Пользователь ");
           } else{ colNames.add(" № ");
             }
           colNames.add(" Создано ");
           colNames.add(" № Телефонa");
           colNames.add(" Текст сообщения ");
           colNames.add(" Состояние ");
           colNames.add(" Отчет ");
            countOfRec=0;
           Vector dataVector = new Vector();
           while(rs.next()) {
              Vector newRow = new Vector();
               newRow.addElement(rs.getInt("ID"));
               if(flag){ newRow.addElement(rs.getString("USERNAME"));
               } else{ newRow.addElement(rs.getInt("NUM"));
                 }
               newRow.addElement(rs.getString("SENDDATE"));
               newRow.addElement(rs.getString ("PHONE"));
               newRow.addElement(rs.getString ("TEXTMESSAGE"));
               newRow.addElement(rs.getString ("STATUS"));
               newRow.addElement(rs.getString ("RECEIVEDATE"));
              dataVector.addElement(newRow);
              countOfRec++; 
           } 
               Vector dataVector1 = new Vector();
             for(int i=1;i<=countOfRec;i++){
                       dataVector1.addElement((Vector)dataVector.elementAt(countOfRec-i));
             }
              /*Table Init*/
              table1.setModel(new SmsTableModel(dataVector1,colNames,admin_mode));
              table1.getTableHeader().setReorderingAllowed(false);
              
               table1.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer(){
               @Override
                     public Component getTableCellRendererComponent(JTable table1,Object value,
                     boolean isSelected,boolean hasFocus,int row,int column){
                         super.getTableCellRendererComponent(table1,value,isSelected,hasFocus,row,column);
                    setForeground(Color.BLUE);
                    return this;
                     }
              });
              table1.getColumnModel().getColumn(5).setCellEditor(new DefaultCellEditor(statusCombo));
              table1.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer(){
               @Override
                     public Component getTableCellRendererComponent(JTable table1,Object value,
                     boolean isSelected,boolean hasFocus,int row,int column){
                         super.getTableCellRendererComponent(table1,value,isSelected,hasFocus,row,column);
                     String str=getText();
                   if(str.equals("Передано оператору")){setForeground(Color.BLUE);}
                   else{setForeground(Color.BLACK);}
                         return this;
                     }
              });
              /*Tool Tips*/
              //table1.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
                table1.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer(){
               @Override
              public Component getTableCellRendererComponent(JTable table1,Object value,boolean isSelected,
                       boolean hasFocus,int row,int column){
                  super.getTableCellRendererComponent(table1,value,isSelected,hasFocus,row,column);
                  Vector <String> stext= new Vector <String>();
                  String str=getText();
                 if(!(str.equals(""))){
                    int pos=0;
                    int rowSize=70;
                 while((str.length()/rowSize)>0){
                     pos=rowSize;
                   while((pos>0)&&(!(str.substring(pos-1,pos).equals(" ")))){pos--;}
                    if(pos<1){pos=rowSize;}
                     stext.add( str.substring(0,pos));
                     str=str.substring(pos);
                 }
                   stext.add(str);
                    str="<html><body><line1>"+stext.elementAt(0);
                   if (stext.size()>1){ 
                  for(int y=1;y<stext.size();y++){
                  str=str+"<br><line>"+stext.elementAt(y);
                  }
                   }
                  str=str+"</body></html>";
                 }
                  setToolTipText(str);
                  return this;
              }
              });
              try{
		dateMask = new MaskFormatter("##:## ##-##-####");
		dateMask.setValidCharacters("0123456789");
		 }catch( ParseException pe ) {}
	      dateField = new JFormattedTextField(dateMask);
	      table1.getColumnModel().getColumn(6).setCellEditor(new DefaultCellEditor(dateField));
              table1.getColumnModel().getColumn(0).setMaxWidth(40);
              table1.getColumnModel().getColumn(0).setMinWidth(10);
             if (flag) { table1.getColumnModel().getColumn(1).setMaxWidth(120);
                         table1.getColumnModel().getColumn(1).setMinWidth(90);
             } else{ table1.getColumnModel().getColumn(1).setMaxWidth(40);
                     table1.getColumnModel().getColumn(1).setMinWidth(40);
               }
              table1.getColumnModel().getColumn(2).setMaxWidth(120);
              table1.getColumnModel().getColumn(2).setMinWidth(105);
              table1.getColumnModel().getColumn(3).setMaxWidth(100);
              table1.getColumnModel().getColumn(5).setMaxWidth(130);
                     for(int i=3;i<6;i++){
              table1.getColumnModel().getColumn(i).setMinWidth(90);
              }
       }catch (java.sql.SQLException EsqlFetch) {
       JOptionPane.showMessageDialog(connectionPanel,EsqlFetch.getMessage(),
              "Ошибка",JOptionPane.ERROR_MESSAGE);
         System.exit(0);
      } 
 }
//------------------------------------------------------------------------------------------------------------
 private void initForm(){
         this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
         this.addWindowListener(new WindowAdapter(){
            @Override
           public void windowClosing(WindowEvent e){
             int rez1=JOptionPane.showConfirmDialog(rootPane,"Выйти из программы?",
                                                    "SMSGate",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);                                                     
              if(rez1==0){System.exit(0);}    
           }
         });
       
      //  murzus = Toolkit.getDefaultToolkit().getImage("murzus.gif");
      //  setIconImage(murzus);
           statusCombo = new JComboBox();
               for(int i=0;i<5;i++){ statusCombo.addItem(statusString[i]);}
        tableRezultPanel  = new JPanel(new BorderLayout());
        statusPanel   = new JPanel();
        newMessagePan = new JPanel(new BorderLayout());
        printButton = new JButton("Печатать отчет");
        printButton.addActionListener(new ActionListener() {
            @Override
	        public void actionPerformed(ActionEvent e) {
	         printReport();
                 }
	    }
	);
        updateButton = new JButton("Обновить");
        updateButton.addActionListener(new ActionListener() {
            @Override
	        public void actionPerformed(ActionEvent e) {
	        initDataModel(admin_mode);//rewrite TableModel
	        }
	    }
	);
        saveButton = new JButton("Сохранить");
        saveButton.addActionListener(new ActionListener() {
            @Override
	        public void actionPerformed(ActionEvent e) {
                  saveUpdateIntoBase();
	        //initDataModel(admin_mode);//rewrite TableModel
	        }
	    }
	);
        toFileButton = new JButton("Выборку в файл");
        toFileButton.addActionListener(new ActionListener() {
            @Override
	        public void actionPerformed(ActionEvent e) {
	          selectToFile();  
	        }
	    }
	);
       saveButton.setVisible(false); 
       toFileButton.setVisible(false);
       buttonsPanel2  = new JPanel();
       buttonsPanel2.add(toFileButton);
       buttonsPanel2.add(updateButton);
       buttonsPanel2.add(printButton);
       buttonsPanel2.add(saveButton);
       
      tableRezultPanel.add(buttonsPanel2,BorderLayout.SOUTH);
             
     setBackground(Color.lightGray);
     messageText = new LimitTextArea();
     messageText.setTextLengthLimit(670);
     messageText.setLineWrap(true);
     messageText.setWrapStyleWord(true);
     messageText.setBorder(new BevelBorder(BevelBorder.LOWERED));
     JScrollPane STextMess = new JScrollPane(messageText);
     JLabel lab2= new JLabel("     ВВОДИМ ТЕКСТ ВАШЕГО СМС");
     STextMess.setColumnHeaderView(lab2);
     codsCombo = new JComboBox(countryCod);     
     messagePhoneNumber = new LimitTextField(5);
     messagePhoneNumber.setTextLengthLimit(10);
     messagePhoneNumberPan =new JPanel(new BorderLayout());
     messagePhoneNumberPan.add(codsCombo,BorderLayout.WEST);
     messagePhoneNumberPan.add(messagePhoneNumber);
     JLabel lab= new JLabel("        ВВОДИМ  № ТЕЛЕФОНА ");
     messagePhoneNumberPan.add(lab,BorderLayout.NORTH);
     buttonsPanel  = new JPanel();
    	sendButton = new JButton("Отправить");
        sendButton.addActionListener(new ActionListener() {
            @Override
	        public void actionPerformed(ActionEvent e) {
	  if(messagePhoneNumber.getText().length()<10) {
                     JOptionPane.showMessageDialog(newMessagePan,"Введите корректный номер телефона",
                                                                  "Ошибка",JOptionPane.ERROR_MESSAGE);
          } else {if(messageText.getText().length()==0){
                     JOptionPane.showMessageDialog(newMessagePan,"Введите текст сообщения",
                                                                  "Ошибка",JOptionPane.ERROR_MESSAGE);                                                     
                  }else{ 
                      int rez=JOptionPane.showConfirmDialog(rootPane,"Вы уверены, что хотите отправить \n это сообщение оператору?",
                                                    "SMSGate",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);                                                     
                      if(rez==0){
                       addNewMessageIntoBase(); 
                       messagePhoneNumber.setText("");
	               messageText.setText("");
                      } 
                   }  
            }
               }
	});
        clearButton = new JButton("Очистить");
        clearButton.addActionListener(new ActionListener() {
            @Override
	        public void actionPerformed(ActionEvent e) {
	        messagePhoneNumber.setText("");
	        messageText.setText(""); 
                }
	});
        exitButton = new JButton("Выход");
        exitButton.addActionListener(new ActionListener() {
            @Override
	        public void actionPerformed(ActionEvent e) {
	        int rez1=JOptionPane.showConfirmDialog(rootPane,"Выйти из программы?",
                                                    "SMSGate",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);                                                     
                      if(rez1==0){
                       System.exit(0);
                      }
                }
	});
	      
     buttonsPanel.add(sendButton);
     buttonsPanel.add(clearButton);
     buttonsPanel.add(exitButton);
     newMessagePan.add(messagePhoneNumberPan,BorderLayout.NORTH);
     newMessagePan.add(STextMess);
     newMessagePan.add(buttonsPanel,BorderLayout.SOUTH);
     newMessagePan.setBackground(Color.lightGray);
     add(newMessagePan,BorderLayout.EAST);
     setVisible(false);
     setMinimumSize(new Dimension(800,600));
     activateConnectionDialog();
    }

//================================MAIN============================================================================
  public static void main(String s[]) {
     JFrame f =  new SMSGateXML("SMS Gate");
     
 } 
}
















