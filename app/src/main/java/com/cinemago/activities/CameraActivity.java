package com.cinemago.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import com.cinemago.R;
import com.cinemago.firebase.FirebaseManager;
import com.google.common.util.concurrent.ListenableFuture;
import java.io.File;
import java.util.concurrent.ExecutionException;

public class CameraActivity extends AppCompatActivity {

    private ImageCapture imageCapture;
    private PreviewView previewView;
    private ProgressBar progressBar;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startCamera();
                } else {
                    Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        previewView = findViewById(R.id.preview_view);
        progressBar = findViewById(R.id.progress_bar);
        Button btnCapture = findViewById(R.id.btn_capture);
        Button btnBack    = findViewById(R.id.btn_back);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }

        btnCapture.setOnClickListener(v -> takePhoto());
        btnBack.setOnClickListener(v -> finish());
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(this);

        future.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = future.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                // Try back camera, fall back to front if needed
                CameraSelector selector = CameraSelector.DEFAULT_BACK_CAMERA;
                if (!cameraProvider.hasCamera(selector)) {
                    selector = CameraSelector.DEFAULT_FRONT_CAMERA;
                }

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, selector, preview, imageCapture);

            } catch (Exception e) {
                Toast.makeText(this, "Camera setup failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhoto() {
        if (imageCapture == null) return;

        File photoFile = new File(getExternalFilesDir(null), "photo_" + System.currentTimeMillis() + ".jpg");
        ImageCapture.OutputFileOptions options = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        progressBar.setVisibility(View.VISIBLE);

        imageCapture.takePicture(options, ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults output) {
                        uploadPhoto(Uri.fromFile(photoFile));
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(CameraActivity.this, "Capture failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadPhoto(Uri uri) {
        FirebaseManager.getInstance().uploadPhoto(uri, new FirebaseManager.UploadCallback() {
            @Override
            public void onSuccess(String downloadUrl) {
                FirebaseManager.getInstance().savePhotoUrl(downloadUrl, new FirebaseManager.SimpleCallback() {
                    @Override
                    public void onSuccess() {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(CameraActivity.this, "Photo saved!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    @Override
                    public void onError(String error) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(CameraActivity.this, "Save error: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(CameraActivity.this, "Upload error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}