/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dassboard.transaction.reader;
        
import com.dassboard.object.Operation;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;     
import javax.swing.filechooser.FileSystemView;
import com.dassboard.object.Operation;
import java.awt.Frame;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import javax.swing.SwingUtilities;
        
        
        

/**
 *
 * @author usuario
 */
public class TransactionReader extends JFrame {
    
 
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    JTextField fileName,delay,ip,port;
    JButton clear,open,run,connect,disconect;
    JTextArea displayData;
    JProgressBar progressBar;
    JPanel topPanel,buttonsPanel,filePanel,filePanel2,mediumPannel;
    File file;
    DataInputStream dataInputStream;
    private static String OS = System.getProperty("os.name").toLowerCase();
    private static String fileName_c = "config.properties";
    private static Properties prop = new Properties();
    

    
    
    TransactionReader(){
        super("Lector de Transacciones");

        displayData = new JTextArea();
        fileName = new JTextField(25);
        delay = new JTextField(3);
        delay.setText("0");
        
        ip = new JTextField(10);
        ip.setText("localhost");
        
        
        port =  new JTextField(10);
        port.setText("33958");
        
        
        open = new JButton("Open");
        clear = new JButton("Clear");
        run = new JButton("Run");
        
        connect =  new JButton("Connect");
        disconect = new JButton("Disconnect");

        progressBar = new JProgressBar();

        topPanel = new JPanel();
        mediumPannel = new JPanel();
        buttonsPanel = new JPanel();
        filePanel = new JPanel();
        filePanel2 = new JPanel();

        topPanel.setLayout(new GridLayout(3, 2));
        filePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        
        buttonsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        filePanel.add(new JLabel("File Name"));
        filePanel.add(fileName);
        
        filePanel.add(new JLabel("Delay"));
        filePanel.add(delay);
        
        
        filePanel2.add(new JLabel("IP"));
        filePanel2.add(ip);
        
        buttonsPanel.add(clear);
        buttonsPanel.add(open);
        buttonsPanel.add(run);

        topPanel.add(filePanel);
        mediumPannel.add(filePanel2);
        mediumPannel.add(port);
        mediumPannel.add(connect);
        mediumPannel.add(disconect);
        
        topPanel.add(buttonsPanel);

        getContentPane().add(topPanel, BorderLayout.NORTH);
        getContentPane().add(mediumPannel, BorderLayout.AFTER_LINE_ENDS);
        getContentPane().add(progressBar, BorderLayout.SOUTH);
        getContentPane().add(new JScrollPane(displayData), BorderLayout.CENTER);

        this.setExtendedState(Frame.MAXIMIZED_BOTH);
        this.setVisible(true);
        
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                
                System.out.println("Cerrando el socket........");
                try {
                    in.close();
                    out.close();
                    clientSocket.close();
                } catch (IOException ex) {
                    Logger.getLogger(TransactionReader.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        

        // Adding Listeners
        open.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
                jfc.setDialogTitle("Custom button");
                //FileNameExtensionFilter filter = new FileNameExtensionFilter("TEXT FILES", "txt", "text");
                //jfc.setFileFilter(filter);

                int returnValue = jfc.showDialog(null, "A button!");
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    fileName.setText(jfc.getSelectedFile().getPath());
                }
            }
        });

        run.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {

                if (!fileName.getText().isEmpty()) {
                    displayData.append("Leyendo el archivo...\n");
                    List<String> list = new ArrayList<>();
                    try {
                        
                        list = Files.readAllLines(new File(fileName.getText()).toPath(), Charset.defaultCharset());
                        displayData.append("Total de Transacciones: "+list.size()+ " \n");
                        int i = 0;
                        displayData.append("Enviando Transacciones...\n");
                        
                        
                        while (i < list.size()) {
                            System.out.println("Linea" + i);
                            System.out.println(list.get(i));
                            
                            Thread.sleep(Long.valueOf(delay.getText()+"000"));
                            
                            if(!list.get(i).trim().isEmpty()){ 
                               System.out.println("list.get(i)= "+ list.get(i));
                                sendMessage(list.get(i));
                            }
                            i++;
                        }

                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(null, "Error Leyendo el archivo");
                        
                    } catch (InterruptedException ex) {
                        Logger.getLogger(TransactionReader.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    //displayData.setText("Termino de leer archivo total de linea leidas = "+ list.size());
                    displayData.append("Finalizo la lectura y envio de transacciones");   
                }else{
                    JOptionPane.showMessageDialog(null, "El origen del archivo no puede estar vacio");
                    
                }
            }
        });
        
        
        
        connect.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent arg0) {
                try {

                    if (ip.getText().isEmpty()) {
                        JOptionPane.showMessageDialog(null, "No dejar campos IP", "Error!", JOptionPane.ERROR_MESSAGE);   
                        return;
                    }
                    if (port.getText().isEmpty()) {
                        JOptionPane.showMessageDialog(null, "No dejar campos IP", "Error!", JOptionPane.ERROR_MESSAGE);   
                        return;
                    }
                    startConnection(ip.getText(), Integer.valueOf(port.getText()));
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "No se puede conectar al socket", "Error!", JOptionPane.ERROR_MESSAGE);   
                    displayData.setText("no se pudo conectar al socket....");
                   ex.printStackTrace();
                }
            }
        });
        
        
        
        disconect.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent arg0) {
                try {

                  
                    stopConnection();
                    
                    
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "No se puede conectar al socket", "Error!", JOptionPane.ERROR_MESSAGE);   
                    displayData.setText("no se pudo conectar al socket....");
                   ex.printStackTrace();
                }
            }
        });
        
        
        clear.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent arg0) {
                displayData.setText("");
                progressBar.setMinimum(0);
                progressBar.setMaximum(0);
            }
        });
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        setVisible(true);
        setSize(400, 300);
        
    }
    
    
    
   

    public void startConnection(String ip, int port) throws IOException {
        clientSocket = new Socket(ip, port);
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
         JOptionPane.showMessageDialog(null, "Conectado", "Socket Conectado!", JOptionPane.INFORMATION_MESSAGE);   
         displayData.setText("Conectado");
        
    }
    
    

    public void sendMessage(String msg) throws IOException {
        out.println(msg);
    }
    
    

    public void stopConnection() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
        JOptionPane.showMessageDialog(null, "Socket Desconectado", "Desconectado!", JOptionPane.ERROR_MESSAGE);   
        displayData.setText("Desconectado");
        
    }
    
    
    
    public void loadFile(){
        LoadingThread thread = new LoadingThread();
        thread.start();
    }
    
       private static Operation contructObject(String response) {
    String[] plot = response.split(";");
    Operation operation = new Operation();
    try {
      operation.setMessageTypeIdentifier(plot[0].trim());
      operation.setPrimaryAccountNumber2(plot[1].trim());
      operation.setProcessingCode3(plot[2].trim());
      operation.setAmounTransaction4(convertAmount(plot[3].trim()));
      operation.setTransmissionDateTime7(plot[4].trim());
      operation.setSystemTraceAuditNumber11(plot[5].trim());
      operation.setTimeLocalTransaction12(plot[6].trim());
      operation.setLocalTransactiondate13(plot[7].trim());
      operation.setSettlementDate15(plot[8].trim());
      operation.setDateCapture17(plot[9].trim());
      operation.setMerchantCategoryCode18(plot[10].trim());
      operation.setPosEntryMode22(plot[11].trim());
      operation.setCodeAcquiringInstitution32(plot[12].trim());
      operation.setForwardingInstitutionCode33(plot[13].trim());
      operation.setTrack2Data35(plot[14].trim());
      operation.setRetrievalReferenceNumber37(plot[15].trim());
      operation.setAuthorizationCode38(plot[16].trim());
      operation.setResponseCode39(plot[17].trim());
      operation.setIdentificationReceivingTerminalCard41(plot[18].trim());
      operation.setNameAndLocationReceiverCard43(plot[19].trim());
      operation.setTransactionCurrencyCode49(plot[20].trim());
      operation.setReserved58(plot[21].trim());
      operation.setAccountIdentification102(plot[22].trim());
      operation.setAccountIdentification103(plot[23].trim());
      operation.setTransactionDescription104(plot[24]);
      operation.setReserved123(plot[25].trim());
      operation.setDestinationRoute(plot[26].trim());
      
    } catch (Exception ex) {
        ex.printStackTrace();
      operation.setResponseCode39("Error");
    } 
    return operation;
  }
    

    
    public static void main(String[] args) {
         loadProperties();
        new TransactionReader();
    }
    
    class LoadingThread extends Thread {
        String text ="";
        int ch;
        int fileSize;
        int completedStatus;
        
        public LoadingThread() {
            try {
                displayData.setText("");
                file = new File(fileName.getText());
                dataInputStream = new DataInputStream(new FileInputStream(file));
                fileSize = (int)file.length();
                progressBar.setMinimum(0);
                progressBar.setMaximum(fileSize);
                completedStatus = 0;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        public void run() {
            try {
                while ((ch = dataInputStream.read()) != -1) {
                    String text = ((char) ch +"");
                    displayData.append(text);
                    progressBar.setValue(++completedStatus);
                    progressBar.setStringPainted(true);
                    progressBar.setString("Opening...");
                    Thread.sleep(100);
                }
                progressBar.setValue(0);
                progressBar.setString("");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    
     
    public static void saveOperation(Operation operation) throws Exception {
        try {

            Class.forName(prop.getProperty("dbdriver"));
            Connection conn = DriverManager.getConnection(prop.getProperty("jdbc"), prop.getProperty("dbuser"), prop.getProperty("dbpassword"));
            // the mysql insert statement
            String query = " INSERT INTO `dasshboard`.`operations`  (`messageTypeIdentifier`, `primaryAccountNumber`, `processingCode`, `amounTransaction`, `transmissionDateTime`, `systemTraceAuditNumber`, `timeLocalTransaction`,`localTransactionDate`,`settlementDate`, `dateCapture`, `merchantCategoryCode`, `posEntryMode`,"
                    + " `codeAcquiringInstitution`, `forwardingInstitutionCode`, `track2Data`, `retrievalReferenceNumber`, `authorizationCode`, `responseCode`, `identificationReceivingTerminalCard`, `nameAndLocationReceiverCard`, `currencyCode`, `reserved58`, "
                    + "`accountIdentificationSource`, `accountIdentificationDestination`,"+ " `description`, `reversed`,`routeName`) VALUES "
                    + "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";

            java.sql.Date sqlDate = new java.sql.Date(new Date().getTime());
            // create the mysql insert preparedstatement
            PreparedStatement preparedStmt = conn.prepareStatement(query);
            preparedStmt.setString(1,operation.getMessageTypeIdentifier());
            preparedStmt.setString(2,operation.getPrimaryAccountNumber2());
            preparedStmt.setString(3,operation.getProcessingCode3());
            preparedStmt.setFloat(4,operation.getAmounTransaction4());
            preparedStmt.setTimestamp(5,getDateField7(operation.getTransmissionDateTime7()));
            preparedStmt.setLong(6,Long.valueOf(operation.getSystemTraceAuditNumber11()));
            preparedStmt.setTimestamp(7, getDateField12(operation.getTimeLocalTransaction12()));
            preparedStmt.setString(8,operation.getLocalTransactiondate13());
            preparedStmt.setString(9,operation.getSettlementDate15());
            preparedStmt.setString(10,operation.getDateCapture17());
            preparedStmt.setString(11,operation.getMerchantCategoryCode18());
            preparedStmt.setString(12,operation.getPosEntryMode22());
            preparedStmt.setString(13,operation.getCodeAcquiringInstitution32());
            preparedStmt.setString(14,operation.getForwardingInstitutionCode33());
            preparedStmt.setString(15,operation.getTrack2Data35());
            preparedStmt.setString(16,operation.getRetrievalReferenceNumber37());
            preparedStmt.setString(17,operation.getAuthorizationCode38());
            preparedStmt.setString(18,operation.getResponseCode39());
            preparedStmt.setString(19,operation.getIdentificationReceivingTerminalCard41());
            preparedStmt.setString(20,operation.getNameAndLocationReceiverCard43());
            preparedStmt.setString(21,operation.getTransactionCurrencyCode49());
            preparedStmt.setString(22,operation.getReserved58());
            preparedStmt.setString(23,operation.getAccountIdentification102());
            preparedStmt.setString(24,operation.getAccountIdentification103());
            preparedStmt.setString(25,operation.getTransactionDescription104());
            preparedStmt.setString(26,operation.getReserved123());
            preparedStmt.setString(27,operation.getDestinationRoute());
            // execute the preparedstatement
            preparedStmt.execute();

            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception();
        }
    }
    
    
        
    public static Timestamp getDateField7(String field7Value){
          Calendar cal = Calendar.getInstance();
          cal.set(Calendar.MONTH, Integer.valueOf(field7Value.substring(0,2))-1);
          cal.set(Calendar.DAY_OF_MONTH, Integer.valueOf(field7Value.substring(2,4)));
          cal.set(Calendar.HOUR_OF_DAY, Integer.valueOf(field7Value.substring(4,6)));
          cal.set(Calendar.MINUTE, Integer.valueOf(field7Value.substring(6,8)));
          cal.set(Calendar.SECOND, Integer.valueOf(field7Value.substring(8,10)));
        return new Timestamp(cal.getTimeInMillis());
    }
    
    public static Timestamp getDateField12(String field12Value ){
          Calendar cal = Calendar.getInstance();
               cal.set(Calendar.HOUR_OF_DAY, Integer.valueOf(field12Value.substring(0,2)));
          cal.set(Calendar.MINUTE, Integer.valueOf(field12Value.substring(2,4)));
          cal.set(Calendar.SECOND, Integer.valueOf(field12Value.substring(4,6)));
        return new Timestamp(cal.getTimeInMillis());
    }
    
    
        public static Float convertAmount(String val) {
        char char0 = '0';
        String decimalValue = val.substring(val.length() - 2, val.length());
        String enterValue = val.substring(0, val.length() - 2);
        String finalenterValue = "";
        for (int i = 0; i <= enterValue.length(); i++) {
            if (enterValue.charAt(i) == char0) {
                continue;
            } else {
                finalenterValue = enterValue.substring(i, enterValue.length());
                 break;
                
            }
           
        }
        return Float.valueOf(finalenterValue + "." + decimalValue);
    }
        
    public static void loadProperties() {
        String propertiesSource = "/home/" + fileName_c;
        if (isWindows()) {
            propertiesSource = "c://" + fileName_c;
        }
        System.out.println(propertiesSource);
        try (InputStream input = new FileInputStream(propertiesSource)) {
            // load a properties file
            prop.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    
    public static boolean isWindows() {
        return (OS.indexOf("win") >= 0);
    }
    
    
    
}