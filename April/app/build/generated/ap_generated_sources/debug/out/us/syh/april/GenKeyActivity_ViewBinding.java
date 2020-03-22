// Generated code from Butter Knife. Do not modify!
package us.syh.april;

import android.view.View;
import androidx.annotation.CallSuper;
import androidx.annotation.UiThread;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import java.lang.IllegalStateException;
import java.lang.Override;

public class GenKeyActivity_ViewBinding implements Unbinder {
  private GenKeyActivity target;

  @UiThread
  public GenKeyActivity_ViewBinding(GenKeyActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public GenKeyActivity_ViewBinding(GenKeyActivity target, View source) {
    this.target = target;

    target.topbar = Utils.findRequiredViewAsType(source, R.id.topbar, "field 'topbar'", QMUITopBarLayout.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    GenKeyActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.topbar = null;
  }
}
