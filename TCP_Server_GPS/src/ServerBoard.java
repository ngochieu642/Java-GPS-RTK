import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ServerBoard extends JFrame {
    private static final long serialVersionUID = 1L;
    private JTextArea messagesArea;
    private JButton startServer;
    private TCPServer mServer;

    public ServerBoard(){
        /*Panel settings*/
        JPanel panelFields = new JPanel();
        panelFields.setLayout(new BoxLayout(panelFields,BoxLayout.Y_AXIS));

        messagesArea = new JTextArea();
        messagesArea.setColumns(30);
        messagesArea.setRows(10);
        messagesArea.setEditable(false);

        JScrollPane scroll = new JScrollPane(messagesArea,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        startServer = new JButton("Start");
        startServer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startServer.setEnabled(false);
                mServer = new TCPServer(messagesArea);
                mServer.start();
                System.out.println("Server Started!");
            }
        });

        panelFields.add(startServer);
        panelFields.setPreferredSize(new Dimension(450,400));
        panelFields.add(scroll);
        add(panelFields);

        setVisible(true);

    }
}
