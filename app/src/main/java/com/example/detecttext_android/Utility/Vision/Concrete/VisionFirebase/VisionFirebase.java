package com.example.detecttext_android.Utility.Vision.Concrete.VisionFirebase;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.detecttext_android.Utility.Vision.Abstract.IDetectText;
import com.example.detecttext_android.Utility.Vision.Abstract.ResultListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

public class VisionFirebase implements IDetectText
{
    FirebaseVisionTextRecognizer recognizer;

    public VisionFirebase()
    {
        this.setUp();
    }

    @Override
    public void setUp()
    {
        this.recognizer = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
    }

    @Override
    public void getText(Bitmap bitmap, ResultListener onResult) {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        recognizer.processImage(image).addOnSuccessListener(firebaseVisionText -> {
            Log.d("success", firebaseVisionText.getText());
            onResult.onSuccess(firebaseVisionText.getText());
        }).addOnFailureListener(e -> {
           e.printStackTrace();
           onResult.onError(e.getLocalizedMessage());
        });
    }
}
