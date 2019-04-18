package com.example.admin.tcp_client_andoird;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Set;

@SuppressWarnings("ALL")
public class MainActivity extends AppCompatActivity {
    /*Variables*/
    private static final int ROVER_PORT = 4445;
    private static final String HOST_NAME = "gpsfutureuse.ddns.net";
    private Socket socket;
    private boolean serverConnected = false;
    private boolean serialConnected = false;
    private static DataOutputStream out;
    private static final int BUFFER_LENGTH = 4000;
    private static byte[] buffer = new byte[BUFFER_LENGTH];
    private static int bytesRead;
    public static byte[] tempbuffer = {1,1,1,1,1,1,1,1,1,1};
    private static String HostIP = "192.168.137.1";


    private TextView textViewLog;
    private Button btnConnectServer,btnConnectSerial, baudrateButton,sendButton ;

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case UsbService.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED
                    Toast.makeText(context, "USB Ready", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
                    Toast.makeText(context, "USB Permission not granted", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_NO_USB: // NO USB CONNECTED
                    Toast.makeText(context, "No USB connected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                    Toast.makeText(context, "USB disconnected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
                    Toast.makeText(context, "USB device not supported", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    private UsbService usbService;
    private MyHandler mHandler;
    private CheckBox box9600, box38400;
    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            usbService = ((UsbService.UsbBinder) arg1).getService();
            usbService.setHandler(mHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mHandler = new MyHandler(this);


        AnhXa();
        SetDefaultText();

        sendButton.setOnClickListener(new View.OnClickListener() {@Override

        public void onClick(View v) {


                }

        });

        btnConnectServer.setOnClickListener(new View.OnClickListener() {
          @Override
            public void onClick(View v) {
                new connectServerTask().execute();
            }
        });

        box9600.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(box9600.isChecked())
                    box38400.setChecked(false);
                else
                    box38400.setChecked(true);
            }
        });

        box38400.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(box38400.isChecked())
                    box9600.setChecked(false);
                else
                    box9600.setChecked(true);
            }
        });

        baudrateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(box9600.isChecked())
                {
                    try{
                        usbService.changeBaudRate(9600);
                        log("Changed baud rate to 9600");
                    } catch (Exception e){
                        e.printStackTrace();
                    }

                }
                else {
                    try{
                        usbService.changeBaudRate(38400);
                        log("Changed baud rate to 38400");
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });
    }
    @Override
    public void onResume() {
        super.onResume();
        setFilters();  // Start listening notifications from UsbService
        startService(UsbService.class, usbConnection, null); // Start UsbService(if it was not started before) and Bind it
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mUsbReceiver);
        unbindService(usbConnection);
    }
    private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
        if (!UsbService.SERVICE_CONNECTED) {
            Intent startService = new Intent(this, service);
            if (extras != null && !extras.isEmpty()) {
                Set<String> keys = extras.keySet();
                for (String key : keys) {
                    String extra = extras.getString(key);
                    startService.putExtra(key, extra);
                }
            }
            startService(startService);
        }
        Intent bindingIntent = new Intent(this, service);
        bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(UsbService.ACTION_NO_USB);
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED);
        registerReceiver(mUsbReceiver, filter);
    }

    /*
     * This handler will be passed to UsbService. Data received from serial port is displayed through this handler
     */
    private void SetDefaultText() {
        textViewLog.setText("Status Displayed Here");
    }

    private void AnhXa() {
        textViewLog     = findViewById(R.id.textLogger);
        btnConnectServer= findViewById(R.id.btnConnectServer);
        btnConnectSerial= findViewById(R.id.btnConnectSerial);
        baudrateButton  = findViewById(R.id.buttonBaudrate);
        sendButton      = findViewById(R.id.buttonSend);
        box9600         = findViewById(R.id.checkBox);
        box38400        = findViewById(R.id.checkBox2);
    }

    private void log(String msg) {
        System.out.println(textViewLog.getText());
        LogTask task = new LogTask(textViewLog,msg);
        task.execute();
    }

    public class connectServerTask extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... voids) {

            try {
                InetAddress serverAddr = InetAddress.getByName(HostIP);
                socket = new Socket(serverAddr,ROVER_PORT);
                out = new DataOutputStream(socket.getOutputStream());

                log("Connected to Server");
                serialConnected = true;

                /*Add communication thread*/
                (new Thread(new CommunicationThread(socket))).start();

            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e){
                serverConnected = false;
                try{
                    if(out!=null)
                        out.close();
                    if(socket!=null)
                        socket.close();
                } catch (IOException e1){
                    e1.printStackTrace();
                } finally {
                    log(e.getLocalizedMessage());
                    e.printStackTrace();
                }
            }
            return null;
        }
    }

    public class CommunicationThread implements Runnable{
        private Socket clientSocket;
        private DataInputStream in;

        private byte[] buffer = new byte[BUFFER_LENGTH];

        public CommunicationThread(Socket clientSocket) {
            this.clientSocket = clientSocket;
            try {
                this.in = new DataInputStream(this.clientSocket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void run() {
            while(!Thread.currentThread().isInterrupted()){
                try {

                    System.out.println("Thread is running");
                    bytesRead = in.read(buffer);
                    log("Receive "+bytesRead+" bytes");
//                    log(new String(buffer,0,bytesRead));
                    if (!buffer.equals("")) {
                        if (usbService != null) { // if UsbService was correctly binded, Send data
                       //for(int i=0;i<10;i++){
                          // tempbuffer[i]+=1;

                      // }
                    }};

                    usbService.write(buffer);
                    log("Sending data to Rover...");

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private static class MyHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        public MyHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:
                    String data = (String) msg.obj;
                    mActivity.get().textViewLog.append(data);
                    break;
                case UsbService.CTS_CHANGE:
                    Toast.makeText(mActivity.get(), "CTS_CHANGE",Toast.LENGTH_LONG).show();
                    break;
                case UsbService.DSR_CHANGE:
                    Toast.makeText(mActivity.get(), "DSR_CHANGE",Toast.LENGTH_LONG).show();
                    break;
                case UsbService.SYNC_READ:
                    String buffer = (String) msg.obj;
                    mActivity.get().textViewLog.append(buffer);
                    break;
            }
        }
    }
}

class LogTask extends AsyncTask<Void,Void,Void>{
    TextView view;
    String msg;

    public LogTask(TextView view, String msg) {
        this.view = view;
        this.msg = msg;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        System.out.println(msg);
        String currentLog = view.getText().toString();
        String newLog = msg + "\n" + currentLog;
        view.setText(newLog);
        super.onPostExecute(aVoid);
    }
}
