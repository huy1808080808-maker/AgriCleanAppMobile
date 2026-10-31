package com.example.agriclean;

import android.app.Application;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.PersistentCacheSettings;

/**
 * Lớp Application trung tâm của AgriClean:
 * Khởi chạy trước tất cả Activity, cấu hình bộ nhớ đệm Offline cho Firebase Firestore theo chuẩn thầy dạy.
 */
public class AgriCleanApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Cấu hình lưu trữ bộ nhớ đệm Offline bền vững (Persistent Cache) cho Firestore
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
                .build();
        FirebaseFirestore.getInstance().setFirestoreSettings(settings);
    }
}

