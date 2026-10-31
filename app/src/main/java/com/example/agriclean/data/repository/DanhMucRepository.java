package com.example.agriclean.data.repository;

import com.example.agriclean.data.model.DanhMuc;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Lớp DanhMucRepository: Quản lý danh sách các danh mục nông sản sạch từ Firestore.
 */
public class DanhMucRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface OnDanhMucCallback {
        void onResult(List<DanhMuc> danhSachDanhMuc);
    }

    /**
     * Lấy toàn bộ danh mục sản phẩm (Rau củ, Trái cây, Nấm, Thảo dược...)
     */
    public void layTatCaDanhMuc(OnDanhMucCallback callback) {
        db.collection("danh_muc")
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<DanhMuc> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        DanhMuc dm = doc.toObject(DanhMuc.class);
                        if (dm != null) {
                            dm.setMaDanhMuc(doc.getId());
                            list.add(dm);
                        }
                    }
                    callback.onResult(list);
                })
                .addOnFailureListener(e -> callback.onResult(new ArrayList<>()));
    }
}

