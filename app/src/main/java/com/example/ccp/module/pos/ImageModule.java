package com.example.ccp.module.pos;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Outline;

import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Size;
import android.view.Surface;
import android.view.View;
import android.view.ViewOutlineProvider;

import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;

import com.example.ccp.common.Common;
import com.example.ccp.common.Convert;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ImageModule {
    private final PreviewView previewView;
    private final Executor executor;
    private ImageCapture imageCapture;
    public final static MutableLiveData<File> captureFile = new MutableLiveData<>();

    public ImageModule(PreviewView previewView) {
        this.previewView = previewView;
        executor = Executors.newSingleThreadExecutor();
    }

    public void startCamera(Context context) { initCamera(context); }

    private void initCamera(Context context) {
        previewView.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
            outline.setRoundRect(
            0, 0, view.getWidth(),
            view.getHeight() + 30, 30F);
            }
        });
        previewView.setClipToOutline(true);
        ListenableFuture<ProcessCameraProvider> listenableFuture =
            ProcessCameraProvider.getInstance(context);
        listenableFuture.addListener(() -> {
            try {
                ProcessCameraProvider processCameraProvider = listenableFuture.get();
                Preview preview = new Preview.Builder().build();
                CameraSelector cameraSelector = new CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build();
                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                    .build();
                imageCapture = new ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .setTargetRotation(Surface.ROTATION_0)
                    .setTargetResolution(new Size(3024, 3024))
                    .build();
                Preview.SurfaceProvider surfaceProvider = previewView.getSurfaceProvider();
                preview.setSurfaceProvider(surfaceProvider);
                processCameraProvider.unbindAll();
                processCameraProvider.bindToLifecycle((LifecycleOwner) context,
                    cameraSelector, preview, imageAnalysis, imageCapture);
            } catch(Exception e) { e.printStackTrace(); }
        }, ContextCompat.getMainExecutor(context));
    }

    public void captureImage(Context context) {
        executor.execute(() -> {
            File file = new File(Common.PUBLIC_DOWNLOAD, System.currentTimeMillis() + ".jpg");
            ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(file).build();
            imageCapture.takePicture(outputFileOptions, executor, new ImageCapture.OnImageSavedCallback() {
                @Override
                public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                    String msg = "[image] File Create Success";
                    File convertFile = null;
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.fromFile(file));
                        if(bitmap != null) {
                            ExifInterface ei = new ExifInterface(file);
                            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                                ExifInterface.ORIENTATION_UNDEFINED);
                            Bitmap rotatedBitmap;
                            switch(orientation) {
                                case ExifInterface.ORIENTATION_ROTATE_90:
                                    rotatedBitmap = Convert.rotateImage(bitmap, 90); break;
                                case ExifInterface.ORIENTATION_ROTATE_180:
                                    rotatedBitmap = Convert.rotateImage(bitmap, 180); break;
                                case ExifInterface.ORIENTATION_ROTATE_270:
                                    rotatedBitmap = Convert.rotateImage(bitmap, 270); break;
                                case ExifInterface.ORIENTATION_NORMAL:
                                default: rotatedBitmap = bitmap;
                            }
                            if(!file.delete()) Common.log("[image] 캡쳐한 기존 파일 삭제에 실패하셨습니다");
                            convertFile = BitmapConvertFile(rotatedBitmap);
                            if(convertFile == null) { msg = "[image] 파일 변환에 실패하였습니다"; }
                        } else msg = "[image] 비트맵 생성에 실패하였습니다";
                    } catch(Exception e) { msg = "[image] 파일 생성에 실패하였습니다\n" + e; }
                    Common.logW(msg);
                    captureFile.postValue(convertFile);
                }
                @Override
                public void onError(@NonNull ImageCaptureException error) { error.printStackTrace(); }
            });
        });
    }

    private File BitmapConvertFile(Bitmap bitmap) {
        OutputStream out = null;
        try {
            File file = new File(Common.PUBLIC_DOWNLOAD, System.currentTimeMillis() + ".jpg");
            if(file.createNewFile()) {
                out = Files.newOutputStream(file.toPath());
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                return file;
            } else { return null; }
        } catch (Exception e) { e.printStackTrace(); return null; }
        finally {
            try { if(out != null) out.close(); }
            catch (IOException e) { e.printStackTrace(); }
        }
    }
}
