package com.example.detecttext_android.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;

import com.example.detecttext_android.Constant.Constant;
import com.example.detecttext_android.R;
import com.example.detecttext_android.Utility.Vision.Abstract.IDetectText;
import com.example.detecttext_android.Utility.Vision.Abstract.ResultListener;
import com.example.detecttext_android.Utility.Vision.Concrete.VisionFirebase.VisionFirebase;
import com.example.detecttext_android.View.Component.Activity.BaseActivity;
import com.example.detecttext_android.View.Component.ImageView.DTImageView;
import com.example.detecttext_android.View.Dialog.DTDialogFragment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import butterknife.BindView;
import butterknife.OnClick;

public class MainActivity extends BaseActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static int IMAGE_REQUEST_CODE = 11;
    private static int WRITE_STORAGE_REQUEST_CODE = 12;

    private Uri outputFileUri;

    private IDetectText detectText;

    @BindView(R.id.image_view)
    DTImageView imageView;

    @OnClick(R.id.btn_detect_text)
    void onBtnDetectTextClick()
    {
        this.openImageIntent();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setup();
    }

    private void setup()
    {
        this.detectText = new VisionFirebase();
    }

    @Override
    public int provideLayoutResId() {
        return R.layout.activity_main;
    }

    private boolean isStoragePermissionGranted()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                return true;
            }
            else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_STORAGE_REQUEST_CODE);
                return false;
            }
        }
        else {
            return true;
        }
    }

    private void openImageIntent()
    {
        if (!this.isStoragePermissionGranted())
        {
            return;
        }

        Uri photoUri = this.createImageFile();
        if (photoUri == null)
            return;

        outputFileUri = photoUri;

        List<Intent> cameraIntents = this.getCameraIntents();
        Intent galleryIntent = this.getGalleryIntent();

        final Intent chooserIntent = Intent.createChooser(galleryIntent, getString(R.string.select_source));
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[cameraIntents.size()]));

        startActivityForResult(chooserIntent, IMAGE_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == WRITE_STORAGE_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            this.openImageIntent();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_REQUEST_CODE) {
                final boolean isCamera;
                if (data == null) {
                    isCamera = true;
                } else {
                    final String action = data.getAction();
                    if (action == null) {
                        isCamera = false;
                    } else {
                        isCamera = action.equals(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    }
                }

                Uri selectedImageUri;
                if (isCamera) {
                    selectedImageUri = outputFileUri;
                } else {
                    selectedImageUri = data == null ? null : data.getData();
                }
                if (selectedImageUri != null)
                {
                    this.imageView.setImageURI(selectedImageUri);
                    this.detectText(this.getBitMapFromUri(selectedImageUri));
                }
            }
        }
    }

    private Bitmap getBitMapFromUri(Uri uri)
    {
        try {
            return MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return null;
    }

    private void detectText(Bitmap bitmap)
    {
        if (bitmap == null)
            return;
        this.detectText.getText(bitmap, new ResultListener() {
            @Override
            public void onSuccess(String result) {
                checkResult(result);
            }

            @Override
            public void onError(String error) {
                showErrorMessage(error);
            }
        });
    }

    private void checkResult(String result)
    {
        for (String word : Constant.SEARCHED_WORD)
        {
            if (result.contains(word))
            {
                this.showSuccessMessage();
                return;
            }
        }
        this.showErrorMessage(getString(R.string.error_text));
    }

    private void showErrorMessage(String errorText)
    {
        DTDialogFragment fragment = new DTDialogFragment("Bir sorun var!", errorText, DTDialogFragment.DialogType.ERROR);
        fragment.show(this.getFragmentTransaction(), "dialog");
    }

    private void showSuccessMessage()
    {
        DTDialogFragment fragment = new DTDialogFragment("Tebrikler", "200 puan kazandınız", DTDialogFragment.DialogType.SUCCESS);
        fragment.show(this.getFragmentTransaction(), "dialog");
    }

    private FragmentTransaction getFragmentTransaction()
    {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null)
            fragmentTransaction.remove(prev);
        fragmentTransaction.addToBackStack(null);
        return fragmentTransaction;
    }

    private Uri createImageFile()
    {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File image = File.createTempFile(
                    imageFileName,
                    ".jpg",
                    storageDir
            );
            return Uri.fromFile(image);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return null;
    }

    private List<Intent> getCameraIntents()
    {
        final List<Intent> cameraIntents = new ArrayList<Intent>();
        final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for(ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            cameraIntents.add(intent);
        }
        return cameraIntents;
    }

    private Intent getGalleryIntent()
    {
        final Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        String[] mineTypes =  { "image/jpeg", "image/png", "image/jpg" };
        galleryIntent.putExtra(Intent.EXTRA_MIME_TYPES, mineTypes);
        return galleryIntent;
    }

}
