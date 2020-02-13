package us.syh.april;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Base64;

import com.google.crypto.tink.Aead;
import com.google.crypto.tink.CleartextKeysetHandle;
import com.google.crypto.tink.JsonKeysetReader;
import com.google.crypto.tink.JsonKeysetWriter;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.aead.AeadConfig;
import com.google.crypto.tink.aead.AeadKeyTemplates;
import com.google.crypto.tink.proto.KeyTemplate;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    public static final int MESSAGE_READ = 1;
    public static final int MESSAGE_WRITE = 2;
    KeysetHandle serverKeysetHandle, peerKeysetHandle;
    Aead serverAead, peerAead;
    EditText editText_message, editText_localKey, editText_peerKey;
    TextView textView_showMessage;
    Button button_send, button_connectServer, button_exit;
    private SocketOperater udpSocket;
    String remoteAddress = "120.79.165.97:9000";
    String remoteCryptKey = "defaultremotekey";
    String peerCryptKey = "defaultpeerkey";
    boolean isServered = true;
    boolean isPinged = false;
    String peerAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FileOperater.setFilesDir(getExternalFilesDir("").getAbsolutePath());
        //aead crypto init
        try {
            AeadConfig.register();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        serverKeysetHandle = getOrgenerateKeyset("server_keyset.json");
        peerKeysetHandle = getOrgenerateKeyset("peer_keyset.json");
        try {
            peerAead = peerKeysetHandle.getPrimitive(Aead.class);
            serverAead = serverKeysetHandle.getPrimitive(Aead.class);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        initView();
        initOnClick();
        setupChat();
    }

    private void initView() {
        editText_message = findViewById(R.id.editText_message);
        editText_localKey = findViewById(R.id.editText_localKey);
        editText_peerKey = findViewById(R.id.editText_peerKey);
        textView_showMessage = findViewById(R.id.textView_showMessage);
        button_send = findViewById(R.id.button_send);
        button_connectServer = findViewById(R.id.button_connectServer);
        button_exit = findViewById(R.id.button_exit);
    }

    private void initOnClick() {
        button_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] addr = (isServered ? remoteAddress : peerAddress).split(":");
                udpSocket.Send(base64Encode(enCrypto(isServered ? serverAead : peerAead,editText_message.getText().toString().getBytes(), isServered ? remoteCryptKey.getBytes() : peerCryptKey.getBytes())), addr[0], Integer.parseInt(addr[1]));
                if(!isServered && !isPinged) {
                    isPinged = true;
                    sendPingPacket();
                }
            }
        });
        button_connectServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] addr = remoteAddress.split(":");
                udpSocket.Send(base64Encode(enCrypto(serverAead,(editText_localKey.getText().toString() + "|" + editText_peerKey.getText().toString()).getBytes(), remoteCryptKey.getBytes())), addr[0], Integer.parseInt(addr[1]));
            }
        });
        button_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setupChat() {
        udpSocket = new SocketOperater(mHandler, 1999);
        udpSocket.startRecv();// 开始监听
    }

    private void sendPingPacket(){
        final String[] addr = (isServered ? remoteAddress : peerAddress).split(":");
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("开始运行ping心跳包!");
                new Timer("schedulePingPacket").schedule(new TimerTask() {
                    @Override
                    public void run() {
                        udpSocket.Send(base64Encode(enCrypto(isServered ? serverAead : peerAead,"ping".getBytes(), isServered ? remoteCryptKey.getBytes() : peerCryptKey.getBytes())), addr[0], Integer.parseInt(addr[1]));
                        sendPingPacket();
                    }
                }, 3000);
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
                    writeMessage = new String(deCrypto(isServered ? serverAead : peerAead, base64Decode(writeMessage), isServered ? remoteCryptKey.getBytes() : peerCryptKey.getBytes()));
                    if(!writeMessage.equals("ping")) {
                        updateMessage(new Date().toString() + " 我： " + writeMessage);
                    }
                    udpSocket.startRecv();
                    System.out.println("发送消息到对端：" + writeMessage);
                    break;
                case MESSAGE_READ:
                    byte[]readBuf =(byte[])msg.obj;
                    String readMessage=new String(readBuf,0,msg.arg1);
                    readMessage = new String(deCrypto(isServered ? serverAead : peerAead, base64Decode(readMessage), isServered ? remoteCryptKey.getBytes() : peerCryptKey.getBytes()));
                    //if(readMessage == null) break;
                    if(isServered && readMessage.indexOf(":") != -1) {
                        peerAddress = readMessage;
                        isServered = false;
                        updateMessage(new Date().toString() + " 建立点对点连接成功！");
                    }else {
                        if(!readMessage.equals("ping")) {
                            updateMessage(new Date().toString() + " 他: " + readMessage);
                        }
                    }
                    System.out.println("收到来自对端消息：" + readMessage);
                    break;
            }
        }
    };

    private void updateMessage(String message) {
        textView_showMessage.setText(textView_showMessage.getText().toString() + "\n" + message);
    }

    private byte[] deCrypto(Aead aead, byte[] chiphertext, byte[] key) {
        try {
            // 3. Use the primitive to encrypt a plaintext,
            return aead.decrypt(chiphertext, key);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return null;
    }

    private byte[] enCrypto(Aead aead, byte[] plaintext, byte[] key) {
        try {
            // 3. Use the primitive to encrypt a plaintext,
            return aead.encrypt(plaintext, key);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return null;
    }

    private KeysetHandle getOrgenerateKeyset(String fileName) {
        if(FileOperater.isFileExists(FileOperater.FilesDir, fileName)) {
            return loadKeyset(fileName);
        }else {
            return saveKeyset(genKeyset(), fileName);
        }
    }
    private KeysetHandle genKeyset() {
        KeyTemplate keyTemplate = AeadKeyTemplates.AES128_GCM;
        try {
            return KeysetHandle.generateNew(keyTemplate);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        return null;
    }
    private KeysetHandle saveKeyset(KeysetHandle keysetHandle, String keysetFilename) {
        // and write it to a file.
        try {
            CleartextKeysetHandle.write(keysetHandle, JsonKeysetWriter.withFile(
                    new File(FileOperater.FilesDir + keysetFilename)));
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return keysetHandle;
    }

    private KeysetHandle loadKeyset(String keysetFilename) {
        // and write it to a file.
        try {
            return CleartextKeysetHandle.read(JsonKeysetReader.withFile(
                    new File(FileOperater.FilesDir + keysetFilename)));
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String base64Encode(byte[] input) {
        return Base64.encodeToString(input, Base64.DEFAULT);
    }

    private static byte[] base64Decode(String input) {
        return Base64.decode(input, Base64.DEFAULT);
    }
}
