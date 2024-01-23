package com.example.inutri.ui.calculotmb;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CalculotmbViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public CalculotmbViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is gallery fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
