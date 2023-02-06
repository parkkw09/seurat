package app.peterkwp.customlayout2.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import app.peterkwp.customlayout2.R;

public class CustomDialog extends Dialog implements View.OnClickListener {

    private CustomDialogInterface customDialogInterface = null;
    private String confirmText;
    private View containerView;

    public CustomDialog(@NonNull Context context) {
        super(context);
    }

    public static class Builder {

        private CustomDialog dialog;


        public Builder(Context context) {
            dialog = new CustomDialog(context);
        }

        public Builder setListener(CustomDialogInterface listener) {
            dialog.customDialogInterface = listener;
            return this;
        }

        public Builder setView(View view) {
            dialog.containerView = view;
            return this;
        }

        public Builder setButtonTitle(String text) {
            dialog.confirmText = text;
            return this;
        }

        public CustomDialog show() {
            dialog.show();
            return dialog;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_custom);

        setCanceledOnTouchOutside(false);
        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            params.height = WindowManager.LayoutParams.MATCH_PARENT;
            window.setAttributes(params);
        }

        Button btnConfirm = findViewById(R.id.btn_confirm);
        FrameLayout layout = findViewById(R.id.container);

        if (containerView != null) {
            layout.addView(containerView);
        }

        if (confirmText != null) btnConfirm.setText(confirmText);
        btnConfirm.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        customDialogInterface.onConfirm();
        cancel();
    }
}
