import jssc.SerialPort;
import jssc.SerialPortException;

import javax.swing.*;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class TCP_Client_Base {
    private static final int BASE_PORT = 4444;
    private static final String HOST_NAME = "gpsfutureuse.ddns.net";
    private JTextArea textArea;
    private static SerialPort inPort;
    private static Socket socket;
    private static DataOutputStream out;
    private static boolean serverConnected = false;
    private static boolean serialConnected = false;
    private Timer timer;

    public static void main(String[] args) {
        BaseBoard frame = new BaseBoard();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
    public TCP_Client_Base(JTextArea textArea) {
        this.textArea = textArea;
    }
    private void log(String text){
        textArea.append(text+"\n");
    }
    public boolean connectServer(){
        try {
            if(serverConnected){
                out.close();
                socket.close();
                log("Disconnect from server");
                serverConnected = false;
                return true;
            } else{
//                socket = new Socket(HOST_NAME,BASE_PORT);
                socket = new Socket(InetAddress.getByName(Inet4Address.getLocalHost().getHostAddress()),BASE_PORT);
                out = new DataOutputStream(socket.getOutputStream());
                log("Connect to server");
                serverConnected = true;
                return true;
            }
        } catch (IOException e) {
               
                serverConnected = false;

                if(out!=null) {
                    try {
                        out.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }

                if(socket!=null) {
                    try {
                        socket.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }

                e.printStackTrace();
        }
            return false;
    }

    public boolean connectSerial(){
        try{
            if(serialConnected){
                timer.cancel();
                if(inPort!=null)
                    inPort.closePort();
                log("Close Serial port");
                serialConnected = false;
                return true;
            }
            else{
                inPort = new SerialPort("COM3");
                inPort.openPort();
                inPort.setParams(9600,8,1,0);
                log("Open serial port");
                serialConnected=true;
                timer = new Timer();
                timer.schedule(new ReadDataTask(),0,200);
                return true;
            }
        } catch (SerialPortException e){
            log("Serial port Errors");
            serialConnected = false;
            e.printStackTrace();
        }
        return  false;
    }

    private static class ReadDataTask extends TimerTask {
        @Override
        public void run() {
            try {
                byte[] data = inPort.readBytes();

                if((data!=null) && serverConnected){
                    out.write(data);
                    System.out.println("Write "+data.length+" Bytes");
                }
            } catch (SerialPortException e) {
                e.printStackTrace();
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}
