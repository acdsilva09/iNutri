package com.example.inutri.ui.camera;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.inutri.databinding.FragmentCameraBinding;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;




public class CameraFragment extends Fragment {

    private FragmentCameraBinding binding;
    private String currentPhotoPath;
    private static final String STATE_PHOTO_PATH = "currentPhotoPath";
    private static final String STATE_ROOT_ID = "rootId";
    private static final String STATE_IS_VIEW_CREATED = "isViewCreated";
    private Bitmap imageBitmap = null;
    private View root;
    private PreviewView previewView;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private RecyclerView resultRv;
    private ImageCapture imageCapture;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        boolean isViewCreated = false;

        if (savedInstanceState != null) {
            isViewCreated = savedInstanceState.getBoolean(STATE_IS_VIEW_CREATED);
            int rootId = savedInstanceState.getInt(STATE_ROOT_ID);
            root = container.findViewById(rootId);
            binding = FragmentCameraBinding.bind(root);
            currentPhotoPath = savedInstanceState.getString(STATE_PHOTO_PATH);
        }

        if (!isViewCreated) {
            binding = FragmentCameraBinding.inflate(inflater, container, false);
            root = binding.getRoot();

            binding.scannerButton.setOnClickListener(v -> scannerButton());
            binding.galeriaButton.setOnClickListener(v -> buscarGaleriaButton());
        }

        return root;
    }
    private void buscarGaleriaButton() {
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            File f = new File(currentPhotoPath);
            Uri contentUri = Uri.fromFile(f);
            mediaScanIntent.setData(contentUri);
            getActivity().sendBroadcast(mediaScanIntent);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        if (storageDir != null && !storageDir.exists()) {
            if (!storageDir.mkdirs()) {
                return null; // Falha ao criar o diretório de armazenamento
            }
        }

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void scannerButton() {
// inside this method we are calling an implicit intent to capture an image.
        imageBitmap =null;
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(getActivity(), "Erro ao capturar imagem: " + ex.getMessage(),
                        Toast.LENGTH_SHORT).show();

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getActivity(),
                        "com.example.inutri.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, 1);
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            if (imageBitmap != null) {
                binding.imageCaptura.setImageBitmap(imageBitmap);
            } else {
                // Carregar a imagem do arquivo especificado pelo caminho
                if (currentPhotoPath != null) {
                    File imgFile = new File(currentPhotoPath);
                    if (imgFile.exists()) {
                        imageBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                        // Verificar a orientação da imagem
                        ExifInterface exif;
                        try {
                            exif = new ExifInterface(currentPhotoPath);
                            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

                            int rotationDegrees = 0;
                            switch (orientation) {
                                case ExifInterface.ORIENTATION_ROTATE_90:
                                    rotationDegrees = 90;
                                    break;
                                case ExifInterface.ORIENTATION_ROTATE_180:
                                    rotationDegrees = 180;
                                    break;
                                case ExifInterface.ORIENTATION_ROTATE_270:
                                    rotationDegrees = 270;
                                    break;
                            }

                            // Rotacionar a imagem se necessário
                            if (rotationDegrees != 0) {
                                Matrix matrix = new Matrix();
                                matrix.postRotate(rotationDegrees);
                                imageBitmap = Bitmap.createBitmap(imageBitmap, 0, 0, imageBitmap.getWidth(), imageBitmap.getHeight(), matrix, true);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Drawable imageDrawable = new BitmapDrawable(getResources(),imageBitmap);
                        binding.imageCaptura.setBackground(imageDrawable);
                    }
                }
            }
        }
    }

    private Executor getExecutor() {
        return ContextCompat.getMainExecutor(this.getActivity());
    }





    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }



    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_PHOTO_PATH, currentPhotoPath);
        if (root != null) {
            outState.putInt(STATE_ROOT_ID, root.getId());
            outState.putBoolean(STATE_IS_VIEW_CREATED, true);
        }
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            int rootId = savedInstanceState.getInt(STATE_ROOT_ID);
            View view = requireView();
            root = view.findViewById(rootId);
            binding = FragmentCameraBinding.bind(view);
            currentPhotoPath = savedInstanceState.getString(STATE_PHOTO_PATH);
        }
    }

}