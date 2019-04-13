import javax.swing.*;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer {
    private static final int BASE_PORT = 4444;
    private static final int ROVER_PORT = 4445;
    private static final int BUFFER_LENGTH = 4000;
    private static byte[] buffer = new byte[BUFFER_LENGTH];
    private static int bytesRead;
    private JTextArea textArea;

    public static void main(String[] args) {
        ServerBoard frame = new ServerBoard();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public TCPServer(JTextArea textArea) {
        this.textArea = textArea;
    }

    public void start() {
        log("Start server");
        new Base(BASE_PORT).start();
        new Rover(ROVER_PORT).start();
    }
    private void log(String text){
        this.textArea.append(text+"\n");
    }

    private class Rover extends Thread{
        private int port;

        public Rover(int port) {
            this.port = port;
        }


        @Override
        public void run() {
            System.out.println("Rover Started");
            while(true){
                try(
                        ServerSocket serverSocket = new ServerSocket(port);
                        Socket roverSocket = serverSocket.accept();
                        DataOutputStream out = new DataOutputStream(new BufferedOutputStream(roverSocket.getOutputStream())))
                {
                    log("Just connect to rover at "+roverSocket.getRemoteSocketAddress());
                    while(true){
                        synchronized (buffer){
                            buffer.wait();
                            out.write(buffer,0,bytesRead);
                            System.out.println("Send data to Rover");
                            buffer.notify();
                        }
                    }
                } catch (IOException e){
                    log("Rover connection reset");
                    e.printStackTrace();
                }  catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
    }
    private class Base extends Thread{
        private int port;

        public Base(int port) {
            this.port = port;
        }

        @Override
        public void run() {
            System.out.println("Base Started");
            while(true){
                try(
                        ServerSocket serverSocket = new ServerSocket(port);
                        Socket baseSocket = serverSocket.accept();
                        DataInputStream in = new DataInputStream(baseSocket.getInputStream());
                ){
                    log("Just connected to base at "+baseSocket.getRemoteSocketAddress());
                    while(true){
                        synchronized (buffer){
                            bytesRead = in.read(buffer);
                            if(bytesRead>0){
//                                log("Read "+bytesRead+" bytes: "+ new String(buffer,StandardCharsets.UTF_8));
                                log("Read "+bytesRead+" bytes: ");
                                log(new String(buffer,0,bytesRead));
                                log("------------------------");
                                buffer.notify();
                                buffer.wait(250);
                            }
                        }
                    }
                } catch (IOException e){
                    log("Base connection Reset.");
                    e.printStackTrace();                            
                } catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
    }
}
