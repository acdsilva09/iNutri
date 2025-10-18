package com.example.inutri.data.remote;

public interface RecognitionDataSource {
    interface Result {
        String label();      // texto original do modelo (ex.: "bread", "baguette")
        float confidence();  // 0..1
        android.graphics.Rect bbox(); // opcional, se vier detector de objetos
    }
    void recognize(android.graphics.Bitmap bitmap,
                   java.util.function.Consumer<java.util.List<Result>> onSuccess,
                   java.util.function.Consumer<Throwable> onError);
}