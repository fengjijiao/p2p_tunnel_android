package us.syh.april;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;

import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.util.QMUIViewHelper;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;

import java.util.Stack;

import butterknife.BindView;
import butterknife.ButterKnife;
import us.syh.april.fragment.AboutFragment;
import us.syh.april.fragment.MainFragment;

public class GenKeyActivity extends AppCompatActivity {
    @BindView(R.id.topbar) QMUITopBarLayout topbar;
    FragmentManager fragmentManager;
    private Stack<Fragment> fragmentStack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QMUIStatusBarHelper.translucent(this);
        //QMUIStatusBarHelper.setStatusBarLightMode(this);
        View root = LayoutInflater.from(this).inflate(R.layout.activity_gen_key, null);
        ButterKnife.bind(this, root);
        setContentView(root);
        initTopBar();
        fragmentManager = getSupportFragmentManager();
        fragmentStack = new Stack<>();
        addFragment(new MainFragment(), "fragment_main");
    }

    private void initTopBar() {
        //topbar.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
        topbar.addLeftBackImageButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //finish();
                backStack();
                overridePendingTransition(R.anim.slide_still, R.anim.slide_out_right);
            }
        });
        topbar.setTitle(getResources().getString(R.string.key_management)).setTextColor(ContextCompat.getColor(this,R.color.qmui_config_color_white));
        topbar.addRightImageButton(R.mipmap.icon_topbar_about, QMUIViewHelper.generateViewId()).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFragment(new AboutFragment(), "fragment_about");
            }
        });
    }

    public void addFragment(Fragment fragment, String tag) {
        // 开启事务
        FragmentTransaction beginTransaction = fragmentManager
                .beginTransaction();
        // 执行事务,添加Fragment
        beginTransaction.add(R.id.container_main, fragment, tag);
        if(!fragmentStack.empty() && fragmentStack.peek().getClass().getName() == fragment.getClass().getName()) return;
        if(!fragmentStack.empty()) beginTransaction.hide(fragmentStack.peek());
        //overridePendingTransition(R.anim.slide_still, R.anim.slide_out_right);
        beginTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        // 添加到回退栈,并定义标记
        beginTransaction.addToBackStack(tag);
        // 提交事务
        beginTransaction.commit();
        fragmentStack.push(fragment);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 判断当前按键是返回键
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            backStack();
        }
        return true;
    }

    public void backStack() {
        // 获取当前回退栈中的Fragment个数
        int backStackEntryCount = fragmentManager.getBackStackEntryCount();
        // 判断当前回退栈中的fragment个数,
        if (backStackEntryCount > 1) {
            // 立即回退一步
            fragmentManager.popBackStackImmediate();
            fragmentStack.pop();
            // 获取当前退到了哪一个Fragment上,重新获取当前的Fragment回退栈中的个数
            FragmentManager.BackStackEntry backStack = fragmentManager
                    .getBackStackEntryAt(fragmentManager
                            .getBackStackEntryCount() - 1);
            // 获取当前栈顶的Fragment的标记值
            String tag = backStack.getName();
            // 判断当前是哪一个标记
                /*if ("fragment1".equals(tag)) {
                    // 设置首页选中
                    rb_home.setChecked(true);
                } else if ("fragment2".equals(tag)) {
                    // 设置购物车的tag
                    rb_cart.setChecked(true);
                } else if ("fragment3".equals(tag)) {
                    rb_category.setChecked(true);
                } else if ("fragment4".equals(tag)) {
                    rb_personal.setChecked(true);
                }*/
        } else {
            //回退栈中只剩一个时,退出应用
            finish();
        }
    }
}
