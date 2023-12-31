package com.example.inutri.ui.camera;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageCapture;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.camera.core.imagecapture.*;

import com.example.inutri.R;
import com.example.inutri.databinding.FragmentCameraBinding;
import com.example.inutri.databinding.FragmentCameraBinding;
import com.example.inutri.ui.camera.CameraViewModel;


public class CameraFragment extends Fragment {

    private FragmentCameraBinding binding;
    private RecyclerView resultRv;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        CameraViewModel galleryViewModel =
                new ViewModelProvider(this).get(CameraViewModel.class);

        binding = FragmentCameraBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        ImageCapture imageCapture =
                new ImageCapture.Builder()
                        .setTargetRotation(root.getDisplay().getRotation())
                        .build();

        cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, imageCapture, imageAnalysis, preview);


        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}