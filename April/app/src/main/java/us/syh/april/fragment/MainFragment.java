package us.syh.april.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.io.File;

import us.syh.april.R;
import us.syh.april.utility.FileOperater;

public class MainFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        TextView textView = view.findViewById(R.id.textView);
        File serverkeyset_file = new File(FileOperater.FilesDir + "keyset/server_keyset.json");
        String serverkeyset_md5 = FileOperater.getFileMD5(serverkeyset_file);
        textView.setText("服务端密匙校验码：" + serverkeyset_md5 + "\n");
        File peerkeyset_file = new File(FileOperater.FilesDir + "keyset/peer_keyset.json");
        String peerkeyset_md5 = FileOperater.getFileMD5(peerkeyset_file);
        textView.setText(textView.getText() + "另一设备密匙校验码：" + peerkeyset_md5 +"\n");
        return view;
    }
    @Override
    public void onStart() {
        super.onStart();
    }
}
