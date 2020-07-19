package ph.lig.chatapp.ui.common;

import android.view.View;

import androidx.databinding.BindingAdapter;

public class BindingAdapters {
    @BindingAdapter("android:visibility")
    public static void setVisibility(View view, String value) {
        view.setVisibility(value == null || value.equals("") ? View.GONE : View.VISIBLE);
    }

    @BindingAdapter("android:visibility")
    public static void setVisibility(View view, boolean value) {
        view.setVisibility(value ? View.VISIBLE : View.GONE);
    }
}
