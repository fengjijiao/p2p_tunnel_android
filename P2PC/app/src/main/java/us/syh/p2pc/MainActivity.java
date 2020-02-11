package us.syh.p2pc;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {
    public static final int MESSAGE_READ = 1;
    public static final int MESSAGE_WRITE = 2;
    TextView textView_message;
    EditText editText_message;
    Button button_send, button_exit;
    private UDPSocket udpSocket;
    String serverAddress = "120.79.165.97:9000";
    boolean isServered = true;
    String peerAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        setupChat();
    }

    private void initView() {
        textView_message = findViewById(R.id.textView_message);
        editText_message = findViewById(R.id.editText_message);
        button_send = findViewById(R.id.button_send);
        button_exit = findViewById(R.id.button_exit);
    }

    private void updateMessage(String message) {
        textView_message.setText(textView_message.getText().toString() + "\n" + message);
    }

    private void setupChat(){
        udpSocket = new UDPSocket(mHandler,1999);
        udpSocket.startRecv();// 开始监听
        // 界面初始化
        button_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] addr;
                if(isServered) {
                    addr = serverAddress.split(":");
                }else{
                    addr = peerAddress.split(":");
                }
                udpSocket.Send(editText_message.getText().toString(), addr[0], Integer.parseInt(addr[1]));
                if(!isServered) {
                    sendPingPacket(addr);
                }
            }
        });

        button_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private  void sendPingPacket(final String[] addr){
        new Thread(new Runnable() {
            @Override
            public void run() {
                udpSocket.Send("ping", addr[0], Integer.parseInt(addr[1]));
            }
        }).start();
    }
    // handler用于Activity之间传递消息
    private final Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case MESSAGE_WRITE:
                    byte[]writeBuf =(byte[])msg.obj;
                    String writeMessage=new String(writeBuf);
                    if(writeMessage.compareTo("ping") != 0) {
                        updateMessage("我： " + writeMessage);
                    }
                    udpSocket.startRecv();
                    break;
                case MESSAGE_READ:
                    byte[]readBuf =(byte[])msg.obj;
                    String readMessage=new String(readBuf,0,msg.arg1);
                    peerAddress = readMessage;
                    isServered = false;
                    if(readMessage.compareTo("ping") != 0) {
                        updateMessage("他: " + readMessage);
                    }
                    break;
            }
        }
    };

}
