package com.example.agriclean.data.repository;

import com.example.agriclean.data.model.SanPham;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

/**
 * Lớp SanPhamRepository: Quản lý truy xuất dữ liệu nông sản sạch từ Cloud Firestore theo thời gian thực (Realtime).
 * Xây dựng theo mẫu Repository + Callback từ bài giảng của thầy.
 */
public class SanPhamRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListenerRegistration listener;

    /**
     * Interface Callback trả kết quả danh sách sản phẩm về cho UI / ViewModel
     */
    public interface OnSanPhamCallback {
        void onResult(List<SanPham> danhSach);
    }

    public interface OnActionCallback {
        void onSuccess();
        void onFailure(String loi);
    }

    /**
     * Lấy toàn bộ danh sách nông sản (lắng nghe thay đổi Realtime)
     */
    public void layTatCaSanPham(OnSanPhamCallback callback) {
        if (listener != null) {
            listener.remove(); // Tránh trùng lặp listener
        }

        listener = db.collection("san_pham")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) {
                        callback.onResult(new ArrayList<>());
                        return;
                    }

                    List<SanPham> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        SanPham sp = doc.toObject(SanPham.class);
                        if (sp != null) {
                            sp.setMaSanPham(doc.getId()); // Gán DocumentId vào mã sản phẩm
                            list.add(sp);
                        }
                    }
                    callback.onResult(list);
                });
    }

    /**
     * Lọc danh sách nông sản theo danh mục
     */
    public void laySanPhamTheoDanhMuc(String maDanhMuc, OnSanPhamCallback callback) {
        db.collection("san_pham")
                .whereEqualTo("maDanhMuc", maDanhMuc)
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<SanPham> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        SanPham sp = doc.toObject(SanPham.class);
                        if (sp != null) {
                            sp.setMaSanPham(doc.getId());
                            list.add(sp);
                        }
                    }
                    callback.onResult(list);
                })
                .addOnFailureListener(e -> callback.onResult(new ArrayList<>()));
    }

    /**
     * Thêm mới sản phẩm (Dành cho Admin)
     */
    public void themSanPham(SanPham sp, OnActionCallback callback) {
        db.collection("san_pham")
                .add(sp)
                .addOnSuccessListener(documentReference -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Cập nhật thông tin sản phẩm (Dành cho Admin)
     */
    public void capNhatSanPham(SanPham sp, OnActionCallback callback) {
        if (sp.getMaSanPham() == null || sp.getMaSanPham().isEmpty()) {
            callback.onFailure("Mã sản phẩm không hợp lệ");
            return;
        }

        db.collection("san_pham")
                .document(sp.getMaSanPham())
                .set(sp)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Xóa sản phẩm theo mã (Dành cho Admin)
     */
    public void xoaSanPham(String maSanPham, OnActionCallback callback) {
        db.collection("san_pham")
                .document(maSanPham)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Xóa bỏ listener khi Activity bị hủy để tránh rò rỉ bộ nhớ (Memory Leak)
     */
    public void huyListener() {
        if (listener != null) {
            listener.remove();
        }
    }
}

