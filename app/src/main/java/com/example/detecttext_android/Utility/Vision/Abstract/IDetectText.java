package com.example.detecttext_android.Utility.Vision.Abstract;


import android.graphics.Bitmap;

import butterknife.BindView;

public interface IDetectText {

    void setUp();
    void getText(Bitmap bitmap, ResultListener onResult);
}
