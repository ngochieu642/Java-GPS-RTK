import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class BaseBoard extends JFrame {
    private static final long serialVersionUID=1L;
    private JTextArea messagesArea;
    private JButton connectServer, connectSerial;
    private TCP_Client_Base client;

    public BaseBoard(){
        /*Panel Elements*/
        super("GPS Base Client");
        JPanel panelFields = new JPanel();
        JPanel panelFields2 = new JPanel();
        
        getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
        panelFields.setLayout(new BoxLayout(panelFields,BoxLayout.X_AXIS));
        
        messagesArea = new JTextArea();
        messagesArea.setColumns(30);
        messagesArea.setRows(10);
        messagesArea.setEditable(false);
        
        client = new TCP_Client_Base(messagesArea);

        JScrollPane scroll = new JScrollPane(messagesArea,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        /*Connect Server Button*/
        connectServer =new JButton("Connect Server");
        connectServer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connectServer.setEnabled(false);
                client.connectServer();
            }
        });

        /*Connect Serial Button*/
        connectSerial = new JButton("Connect serial Port");
        connectSerial.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(client.connectSerial()){
                    if(connectSerial.getText() =="Connect serial port")
                        connectSerial.setText("Disconnect serial port");
                    else
                        connectSerial.setText("Connect serial port");
                }
            }
        });

        /*Tying things up*/
        panelFields.add(connectServer);
        panelFields.add(connectSerial);
        panelFields2.add(scroll);

        getContentPane().add(panelFields);
        getContentPane().add(panelFields2);

        setVisible(true);

    }
}
