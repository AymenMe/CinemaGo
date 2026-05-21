package com.cinemago.firebase;

import android.net.Uri;
import com.cinemago.models.FavoriteMovie;
import com.cinemago.utils.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.List;

public class FirebaseManager {

    // ── Callbacks ──────────────────────────────────────────
    public interface AuthCallback {
        void onSuccess(FirebaseUser user);
        void onError(String error);
    }

    public interface FavoritesCallback {
        void onSuccess(List<FavoriteMovie> favorites);
        void onError(String error);
    }

    public interface SimpleCallback {
        void onSuccess();
        void onError(String error);
    }

    public interface UploadCallback {
        void onSuccess(String downloadUrl);
        void onError(String error);
    }

    // ── Singleton ──────────────────────────────────────────
    private static FirebaseManager instance;
    private final FirebaseAuth auth;
    private final FirebaseFirestore db;
    private final FirebaseStorage storage;

    private FirebaseManager() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    public static synchronized FirebaseManager getInstance() {
        if (instance == null) instance = new FirebaseManager();
        return instance;
    }

    // ── Auth ───────────────────────────────────────────────
    public void login(String email, String password, AuthCallback callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(r -> callback.onSuccess(r.getUser()))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void register(String email, String password, AuthCallback callback) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(r -> callback.onSuccess(r.getUser()))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void logout() { auth.signOut(); }

    public FirebaseUser getCurrentUser() { return auth.getCurrentUser(); }

    public boolean isLoggedIn() { return auth.getCurrentUser() != null; }

    // ── Favorites ──────────────────────────────────────────
    public void addFavorite(FavoriteMovie movie, SimpleCallback callback) {
        String uid = getCurrentUser().getUid();
        db.collection(Constants.COLLECTION_FAVORITES)
                .document(uid + "_" + movie.getMovieId())
                .set(movie)
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void removeFavorite(String movieId, SimpleCallback callback) {
        String uid = getCurrentUser().getUid();
        db.collection(Constants.COLLECTION_FAVORITES)
                .document(uid + "_" + movieId)
                .delete()
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void getFavorites(FavoritesCallback callback) {
        String uid = getCurrentUser().getUid();
        db.collection(Constants.COLLECTION_FAVORITES)
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener(snap -> {
                    List<FavoriteMovie> list = snap.toObjects(FavoriteMovie.class);
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void isFavorite(String movieId, com.google.android.gms.tasks.OnSuccessListener<Boolean> listener) {
        String uid = getCurrentUser().getUid();
        db.collection(Constants.COLLECTION_FAVORITES)
                .document(uid + "_" + movieId)
                .get()
                .addOnSuccessListener(snap -> listener.onSuccess(snap.exists()));
    }

    // ── Storage ────────────────────────────────────────────
    public void uploadPhoto(Uri imageUri, UploadCallback callback) {
        String uid = getCurrentUser().getUid();
        StorageReference ref = storage.getReference()
                .child(Constants.COLLECTION_PHOTOS + "/" + uid + "/" + System.currentTimeMillis() + ".jpg");

        ref.putFile(imageUri)
                .addOnSuccessListener(snap -> ref.getDownloadUrl()
                        .addOnSuccessListener(uri -> callback.onSuccess(uri.toString()))
                        .addOnFailureListener(e -> callback.onError(e.getMessage())))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void savePhotoUrl(String url, SimpleCallback callback) {
        String uid = getCurrentUser().getUid();
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("url", url);
        data.put("userId", uid);
        data.put("timestamp", System.currentTimeMillis());

        db.collection(Constants.COLLECTION_PHOTOS)
                .add(data)
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
}