package com.example.inutri.data.repository;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.example.inutri.model.MealRecord;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Repository responsável por salvar/ler refeições na nuvem.
 * Estruturas:
 *  - Storage:  /meals/{uid}/{timestamp}.jpg
 *  - Firestore: collection "meals" com docId = "{uid}_{timestamp}"
 *    (o documento contém: uid, photoUrl, timestamp, itens[...])
 *
 * As regras de segurança devem garantir que o usuário só acesse os próprios dados.
 */
public class MealRepository {

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private static final String COLLECTION_MEALS = "meals";

    /**
     * Sobe a foto e salva o registro no Firestore.
     * @param localImage Uri local da imagem (FileProvider/Cache)
     * @param record     MealRecord com itens/timestamp (se timestamp==0, é preenchido aqui)
     * @param callback   true se tudo ok, false se falhar
     */
    public void saveMeal(@NonNull Uri localImage,
                         @NonNull MealRecord record,
                         @NonNull Consumer<Boolean> callback) {

        if (auth.getCurrentUser() == null) {
            callback.accept(false);
            return;
        }

        final String uid = auth.getCurrentUser().getUid();
        final long ts = record.timestamp > 0 ? record.timestamp : System.currentTimeMillis();
        final String fileName = ts + ".jpg";
        final String storagePath = "meals/" + uid + "/" + fileName;

        StorageReference fileRef = storage.getReference().child(storagePath);

        // 1) Upload da foto
        UploadTask uploadTask = fileRef.putFile(localImage);

        // 2) Pegar URL pública de download
        Task<Uri> urlTask = uploadTask.continueWithTask((Continuation<UploadTask.TaskSnapshot, Task<Uri>>) task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            return fileRef.getDownloadUrl();
        });

        // 3) Gravar documento no Firestore
        urlTask.addOnSuccessListener(uri -> {
            record.uid = uid;
            record.photoUrl = uri.toString();
            record.timestamp = ts;

            String docId = uid + "_" + ts;
            db.collection(COLLECTION_MEALS)
                    .document(docId)
                    .set(record)
                    .addOnSuccessListener(unused -> callback.accept(true))
                    .addOnFailureListener(e -> callback.accept(false));

        }).addOnFailureListener(e -> callback.accept(false));
    }

    /**
     * Busca as refeições do usuário logado (mais recentes primeiro).
     * @param callback retorna uma lista (pode ser vazia).
     */
    public void fetchMyMeals(@NonNull Consumer<List<MealRecord>> callback) {
        if (auth.getCurrentUser() == null) {
            callback.accept(new ArrayList<>());
            return;
        }
        final String uid = auth.getCurrentUser().getUid();

        CollectionReference col = db.collection(COLLECTION_MEALS);
        Query q = col.whereEqualTo("uid", uid)
                .orderBy("timestamp", Query.Direction.DESCENDING);

        q.get().addOnCompleteListener(task -> {
            List<MealRecord> out = new ArrayList<>();
            if (task.isSuccessful() && task.getResult() != null) {
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    MealRecord mr = doc.toObject(MealRecord.class);
                    out.add(mr);
                }
            }
            callback.accept(out);
        });
    }

    /**
     * (Opcional) Remove uma refeição do usuário (Firestore + Storage).
     * @param timestamp timestamp usado como id do arquivo
     * @param callback true se removido, false caso contrário
     */
    public void deleteMyMeal(long timestamp, @NonNull Consumer<Boolean> callback) {
        if (auth.getCurrentUser() == null) {
            callback.accept(false);
            return;
        }
        final String uid = auth.getCurrentUser().getUid();
        final String docId = uid + "_" + timestamp;
        final String storagePath = "meals/" + uid + "/" + timestamp + ".jpg";

        // Apaga Firestore primeiro, depois a foto
        db.collection(COLLECTION_MEALS).document(docId)
                .delete()
                .addOnSuccessListener(unused ->
                        storage.getReference().child(storagePath)
                                .delete()
                                .addOnSuccessListener(unused2 -> callback.accept(true))
                                .addOnFailureListener(e -> callback.accept(false))
                )
                .addOnFailureListener(e -> callback.accept(false));
    }
}
