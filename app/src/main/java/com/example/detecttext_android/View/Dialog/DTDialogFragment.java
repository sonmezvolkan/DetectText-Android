package com.example.detecttext_android.View.Dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.detecttext_android.R;
import com.example.detecttext_android.View.Component.Label.DTLabel;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DTDialogFragment extends DialogFragment {

    private String title;
    private String message;
    private @DialogType int type;

    @BindView(R.id.image_view)
    ImageView imageView;

    @BindView(R.id.txt_title)
    DTLabel lblTitle;

    @BindView(R.id.txt_message)
    DTLabel lblMessage;

    @OnClick(R.id.button)
    void onBtnClick()
    {
        this.dismiss();
    }

    public @interface DialogType
    {
        int SUCCESS = 0;
        int ERROR = 1;
    }

    public DTDialogFragment(String title, String message, @DialogType int type)
    {
        this.title = title;
        this.message = message;
        this.type = type;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dialog, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.setControls();

    }

    private void setControls()
    {
        if (type == DialogType.SUCCESS)
            this.imageView.setImageDrawable(getResources().getDrawable(R.drawable.check_icon));
        //else
          //  this.imageView.setImageDrawable(getResources().getDrawable(R.drawable.unlink_icon_and_circle));
        this.lblTitle.setText(this.title);
        this.lblMessage.setText(this.message);

    }
}
