package us.syh.april;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.crypto.tink.Aead;
import com.google.crypto.tink.CleartextKeysetHandle;
import com.google.crypto.tink.JsonKeysetReader;
import com.google.crypto.tink.JsonKeysetWriter;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.aead.AeadConfig;
import com.google.crypto.tink.aead.AeadKeyTemplates;
import com.google.crypto.tink.proto.KeyTemplate;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.util.QMUIViewHelper;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import us.syh.april.utility.FileOperater;
import us.syh.april.utility.SocketOperater;

public class MainActivity extends AppCompatActivity {

    public static final int MESSAGE_READ = 1;
    public static final int MESSAGE_WRITE = 2;
    KeysetHandle serverKeysetHandle, peerKeysetHandle;
    Aead serverAead, peerAead;
    EditText editText_message, editText_localKey, editText_peerKey;
    TextView textView_showMessage;
    Button button_send, button_connectServer, button_exit, button_initkeyset, button_keymanager;
    QMUITopBarLayout topbar;

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
        QMUIStatusBarHelper.translucent(this);
        setContentView(R.layout.activity_main);
        initUpdate();
        initView();
        initTopBar();
        FileOperater.setFilesDir(Objects.requireNonNull(getExternalFilesDir("")).getAbsolutePath());
        //aead crypto init
        try {
            AeadConfig.register();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        serverKeysetHandle = getOrgenerateKeyset("keyset/server_keyset.json");
        peerKeysetHandle = getOrgenerateKeyset("keyset/peer_keyset.json");
        try {
            peerAead = peerKeysetHandle.getPrimitive(Aead.class);
            serverAead = serverKeysetHandle.getPrimitive(Aead.class);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        initOnClick();
        setupChat();
    }

    private void initTopBar() {
        //topbar.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
        topbar.setTitle(getResources().getString(R.string.app_name)).setTextColor(ContextCompat.getColor(this,R.color.qmui_config_color_white));
        topbar.addRightImageButton(R.mipmap.icon_topbar_overflow, QMUIViewHelper.generateViewId())
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showMoreSheetList();
                    }
                });
    }

    private void showMoreSheetList() {
        new QMUIBottomSheet.BottomListSheetBuilder(this)
                .addItem(getResources().getString(R.string.key_management))
                .addItem(getResources().getString(R.string.import_keyset))
                .addItem(getResources().getString(R.string.setting))
                .addItem(getResources().getString(R.string.logout))
                .setOnSheetItemClickListener(new QMUIBottomSheet.BottomListSheetBuilder.OnSheetItemClickListener() {
                    @Override
                    public void onClick(QMUIBottomSheet dialog, View itemView, int position, String tag) {
                        if (position == 0) {
                            Intent intent = new Intent();
                            intent.setClass(MainActivity.this,GenKeyActivity.class);
                            startActivity(intent);
                        }else if(position == 1) {
                            initKeyset();
                        }else if(position == 2) {
                            //
                        }else if(position ==3) {
                            finish();
                        }
                        dialog.dismiss();
                    }
                })
                .build()
                .show();
    }

    private void initUpdate() {
    }

    private void initView() {
        topbar = findViewById(R.id.topbar);
        editText_message = findViewById(R.id.editText_message);
        editText_localKey = findViewById(R.id.editText_localKey);
        editText_peerKey = findViewById(R.id.editText_peerKey);
        textView_showMessage = findViewById(R.id.textView_showMessage);
        button_send = findViewById(R.id.button_send);
        button_connectServer = findViewById(R.id.button_connectServer);
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
    }

    private void setupChat() {
        udpSocket = new SocketOperater(mHandler, 1999);
        udpSocket.startRecv();// 开始监听
    }

    private void initKeyset() {
        FileOperater.putAssetsToSDCard(getApplicationContext(),"keyset", FileOperater.FilesDir);
        Toast.makeText(MainActivity.this, "导入完成！", Toast.LENGTH_LONG).show();
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
    @SuppressLint("HandlerLeak")
    private final Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case MESSAGE_WRITE:
                    byte[]writeBuf =(byte[])msg.obj;
                    String writeMessage=new String(writeBuf);
                    writeMessage = new String(Objects.requireNonNull(deCrypto(isServered ? serverAead : peerAead, base64Decode(writeMessage), isServered ? remoteCryptKey.getBytes() : peerCryptKey.getBytes())));
                    if(!writeMessage.equals("ping")) {
                        updateMessage(new Date().toString() + " 我： " + writeMessage);
                    }
                    udpSocket.startRecv();
                    System.out.println("发送消息到对端：" + writeMessage);
                    break;
                case MESSAGE_READ:
                    byte[]readBuf =(byte[])msg.obj;
                    String readMessage=new String(readBuf,0,msg.arg1);
                    readMessage = new String(Objects.requireNonNull(deCrypto(isServered ? serverAead : peerAead, base64Decode(readMessage), isServered ? remoteCryptKey.getBytes() : peerCryptKey.getBytes())));
                    //if(readMessage == null) break;
                    if(isServered && readMessage.contains(":")) {
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

    @SuppressLint("SetTextI18n")
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

    public static String getAppVersionName(Context context) {
        String versionName = "";
        try {
            // ---get the package info---
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
            if (versionName == null || versionName.length() <= 0) {
                return "";
            }
        } catch (Exception e) {
            Log.e("VersionInfo", "Exception", e);
        }
        return versionName;
    }

    public static int getAppVersionCode(Context context) {
        int versionCode = 0;
        try {
            // ---get the package info---
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionCode = pi.versionCode;
        } catch (Exception e) {
            Log.e("VersionInfo", "Exception", e);
        }
        return versionCode;
    }

    private static String base64Encode(byte[] input) {
        return Base64.encodeToString(input, Base64.DEFAULT);
    }

    private static byte[] base64Decode(String input) {
        return Base64.decode(input, Base64.DEFAULT);
    }
}
