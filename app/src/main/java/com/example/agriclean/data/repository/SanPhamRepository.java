package com.example.agriclean.data.repository;

import android.net.Uri;

import com.example.agriclean.data.model.SanPham;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Lớp SanPhamRepository: Quản lý truy xuất và lưu trữ dữ liệu nông sản sạch từ
 * Cloud Firestore (Collection "Products") và Firebase Storage (Thư mục "products/").
 */
public class SanPhamRepository {

    public static final String COLLECTION_PRODUCTS = "Products";

    private static volatile SanPhamRepository instance;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private ListenerRegistration listener;

    public interface OnSanPhamCallback {
        void onResult(List<SanPham> danhSach);
    }

    public interface OnSingleSanPhamCallback {
        void onSuccess(SanPham sanPham);
        void onFailure(String errorMessage);
    }

    public interface OnActionCallback {
        void onSuccess();
        void onFailure(String loi);
    }

    public interface OnThemSanPhamCallback {
        void onSuccess(String productId);
        void onFailure(String loi);
    }

    private SanPhamRepository() {
    }

    public static SanPhamRepository getInstance() {
        if (instance == null) {
            synchronized (SanPhamRepository.class) {
                if (instance == null) {
                    instance = new SanPhamRepository();
                }
            }
        }
        return instance;
    }

    /**
     * Lấy toàn bộ danh sách nông sản sạch theo thời gian thực (Realtime - UC04)
     */
    public void layTatCaSanPham(OnSanPhamCallback callback) {
        if (listener != null) {
            listener.remove();
        }

        listener = db.collection(COLLECTION_PRODUCTS)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) {
                        thuDocCollectionPhu(callback);
                        return;
                    }

                    List<SanPham> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        SanPham sp = doc.toObject(SanPham.class);
                        if (sp != null) {
                            if (sp.getProductId() == null || sp.getProductId().isEmpty()) {
                                sp.setProductId(doc.getId());
                            }
                            list.add(sp);
                        }
                    }

                    if (list.isEmpty()) {
                        thuDocCollectionPhu(callback);
                    } else {
                        callback.onResult(list);
                    }
                });
    }

    private void thuDocCollectionPhu(OnSanPhamCallback callback) {
        db.collection("san_pham")
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<SanPham> list = new ArrayList<>();
                    if (snapshots != null) {
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            SanPham sp = doc.toObject(SanPham.class);
                            if (sp != null) {
                                sp.setProductId(doc.getId());
                                list.add(sp);
                            }
                        }
                    }
                    callback.onResult(list);
                })
                .addOnFailureListener(e -> callback.onResult(new ArrayList<>()));
    }

    /**
     * Lấy chi tiết một sản phẩm theo mã productId (UC08 Quét mã QR & UC07 Chi tiết)
     */
    public void laySanPhamTheoId(String productId, OnSingleSanPhamCallback callback) {
        if (productId == null || productId.trim().isEmpty()) {
            callback.onFailure("Mã sản phẩm không hợp lệ");
            return;
        }

        db.collection(COLLECTION_PRODUCTS).document(productId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        SanPham sp = doc.toObject(SanPham.class);
                        if (sp != null) {
                            sp.setProductId(doc.getId());
                            callback.onSuccess(sp);
                            return;
                        }
                    }
                    db.collection("san_pham").document(productId)
                            .get()
                            .addOnSuccessListener(subDoc -> {
                                if (subDoc.exists()) {
                                    SanPham sp = subDoc.toObject(SanPham.class);
                                    if (sp != null) {
                                        sp.setProductId(subDoc.getId());
                                        callback.onSuccess(sp);
                                        return;
                                    }
                                }
                                callback.onFailure("Không tìm thấy thông tin nông sản cho mã: " + productId);
                            })
                            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Lọc danh sách nông sản theo danh mục (UC06)
     */
    public void laySanPhamTheoDanhMuc(String danhMuc, OnSanPhamCallback callback) {
        if (danhMuc == null || danhMuc.equals(SanPham.DANH_MUC_TAT_CA)) {
            layTatCaSanPham(callback);
            return;
        }

        db.collection(COLLECTION_PRODUCTS)
                .whereEqualTo("danhMuc", danhMuc)
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<SanPham> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        SanPham sp = doc.toObject(SanPham.class);
                        if (sp != null) {
                            sp.setProductId(doc.getId());
                            list.add(sp);
                        }
                    }
                    callback.onResult(list);
                })
                .addOnFailureListener(e -> callback.onResult(new ArrayList<>()));
    }

    /**
     * Lưu nông sản (Thêm mới hoặc Cập nhật) kèm tải ảnh lên Firebase Storage (NFR10)
     */
    public void luuSanPham(SanPham sp, Uri imageUri, boolean isUpdate, OnActionCallback callback) {
        // Nếu thêm mới và chưa có productId, tự động sinh mã ngẫu nhiên 8 ký tự
        if (sp.getProductId() == null || sp.getProductId().trim().isEmpty()) {
            sp.setProductId("SP_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }

        // Trường hợp có chọn ảnh mới từ thiết bị -> Tải lên Firebase Storage
        if (imageUri != null) {
            StorageReference storageRef = storage.getReference()
                    .child("products/" + sp.getProductId() + ".jpg");

            storageRef.putFile(imageUri)
                    .continueWithTask(task -> {
                        if (!task.isSuccessful() && task.getException() != null) {
                            throw task.getException();
                        }
                        return storageRef.getDownloadUrl();
                    })
                    .addOnSuccessListener(downloadUrl -> {
                        sp.setHinhAnhUrl(downloadUrl.toString());
                        luuSanPhamVaoFirestore(sp, callback);
                    })
                    .addOnFailureListener(e -> callback.onFailure("Lỗi tải ảnh lên Storage: " + e.getMessage()));
        } else {
            // Không chọn ảnh mới (khi sửa giữ nguyên ảnh cũ) -> Lưu trực tiếp Firestore
            luuSanPhamVaoFirestore(sp, callback);
        }
    }

    /**
     * Ghi đối tượng SanPham vào Firestore
     */
    private void luuSanPhamVaoFirestore(SanPham sp, OnActionCallback callback) {
        db.collection(COLLECTION_PRODUCTS)
                .document(sp.getProductId())
                .set(sp)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure("Lỗi lưu dữ liệu: " + e.getMessage()));
    }

    /**
     * Thêm mới sản phẩm (Tương thích)
     */
    public void themSanPham(SanPham sp, OnThemSanPhamCallback callback) {
        if (sp.getProductId() == null || sp.getProductId().trim().isEmpty()) {
            sp.setProductId("SP_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }

        db.collection(COLLECTION_PRODUCTS)
                .document(sp.getProductId())
                .set(sp)
                .addOnSuccessListener(aVoid -> callback.onSuccess(sp.getProductId()))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Cập nhật thông tin nông sản (Tương thích)
     */
    public void capNhatSanPham(SanPham sp, OnActionCallback callback) {
        if (sp.getProductId() == null || sp.getProductId().isEmpty()) {
            callback.onFailure("Mã sản phẩm không hợp lệ");
            return;
        }

        db.collection(COLLECTION_PRODUCTS)
                .document(sp.getProductId())
                .set(sp)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Xóa sản phẩm theo mã (Dành cho Admin - UC14)
     */
    public void xoaSanPham(String productId, OnActionCallback callback) {
        if (productId == null || productId.isEmpty()) {
            callback.onFailure("Mã sản phẩm không hợp lệ");
            return;
        }

        db.collection(COLLECTION_PRODUCTS)
                .document(productId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Thử xóa ảnh tương ứng trên Storage nếu có (không bắt buộc lỗi)
                    storage.getReference().child("products/" + productId + ".jpg").delete();
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Hủy đăng ký listener khi không sử dụng
     */
    public void huyListener() {
        if (listener != null) {
            listener.remove();
            listener = null;
        }
    }
}
