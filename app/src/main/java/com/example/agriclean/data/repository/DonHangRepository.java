package com.example.agriclean.data.repository;

import com.example.agriclean.data.model.DonHang;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Lớp DonHangRepository: Xử lý quy trình tạo đơn hàng và theo dõi lịch sử đơn hàng.
 * Xây dựng theo mẫu Repository + Callback từ bài giảng của thầy.
 */
public class DonHangRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface OnDonHangCallback {
        void onResult(List<DonHang> danhSachDonHang);
    }

    public interface OnTaoDonCallback {
        void onSuccess(String maDonHang);
        void onFailure(String loi);
    }

    public interface OnCapNhatTrangThaiCallback {
        void onSuccess();
        void onFailure(String loi);
    }

    /**
     * Tạo đơn hàng mới đẩy lên Firestore
     */
    public void taoDonHang(DonHang donHang, OnTaoDonCallback callback) {
        db.collection("don_hang")
                .add(donHang)
                .addOnSuccessListener(doc -> callback.onSuccess(doc.getId()))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Lấy lịch sử mua hàng của một khách hàng cụ thể
     */
    public void layDonHangTheoKhachHang(String maKhachHang, OnDonHangCallback callback) {
        db.collection("don_hang")
                .whereEqualTo("maKhachHang", maKhachHang)
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<DonHang> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        DonHang don = doc.toObject(DonHang.class);
                        if (don != null) {
                            don.setMaDonHang(doc.getId());
                            list.add(don);
                        }
                    }
                    callback.onResult(list);
                })
                .addOnFailureListener(e -> callback.onResult(new ArrayList<>()));
    }

    /**
     * Lấy tất cả đơn hàng cho Admin quản lý và duyệt trạng thái
     */
    public void layTatCaDonHang(OnDonHangCallback callback) {
        db.collection("don_hang")
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<DonHang> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        DonHang don = doc.toObject(DonHang.class);
                        if (don != null) {
                            don.setMaDonHang(doc.getId());
                            list.add(don);
                        }
                    }
                    callback.onResult(list);
                })
                .addOnFailureListener(e -> callback.onResult(new ArrayList<>()));
    }

    /**
     * Cập nhật trạng thái đơn hàng (Dành cho Admin: "Đã xác nhận", "Đang giao", "Hoàn thành", "Đã huỷ")
     */
    public void capNhatTrangThaiDonHang(String maDonHang, String trangThaiMoi, OnCapNhatTrangThaiCallback callback) {
        db.collection("don_hang")
                .document(maDonHang)
                .update("trangThaiDon", trangThaiMoi)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
}

