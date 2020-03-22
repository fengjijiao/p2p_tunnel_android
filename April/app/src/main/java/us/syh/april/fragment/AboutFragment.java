package us.syh.april.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.qmuiteam.qmui.util.QMUIPackageHelper;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmui.widget.grouplist.QMUIGroupListView;

import java.text.SimpleDateFormat;
import java.util.Locale;

import us.syh.april.R;

public class AboutFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_about,container,false);

        AppCompatActivity activity = (AppCompatActivity) getActivity();

        TextView mVersionTextView = view.findViewById(R.id.version);
        QMUIGroupListView mAboutGroupListView = view.findViewById(R.id.about_list);
        TextView mCopyrightTextView = view.findViewById(R.id.copyright);

        mVersionTextView.setText(QMUIPackageHelper.getAppVersion(getContext()));
        QMUIGroupListView.newSection(getContext())
                .addItemView(mAboutGroupListView.createItemView(getResources().getString(R.string.about_item_opensource)), new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getContext(),"developing...",Toast.LENGTH_LONG).show();
                    }
                })
                .addItemView(mAboutGroupListView.createItemView(getResources().getString(R.string.about_item_giant)), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getContext(),"developing...",Toast.LENGTH_LONG).show();
                    }
                })
                .addTo(mAboutGroupListView);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy", Locale.CHINA);
        String currentYear = dateFormat.format(new java.util.Date());
        mCopyrightTextView.setText(String.format(getResources().getString(R.string.about_copyright), currentYear));
        return view;
    }
}
