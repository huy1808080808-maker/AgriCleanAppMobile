package com.example.agriclean.data.repository;

import com.example.agriclean.data.model.DonHang;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class DonHangRepository {


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
     */
    public void taoDonHang(DonHang donHang, OnTaoDonCallback callback) {
                    .add(donHang)
                    .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
        }

    /**
     */
    public void layDonHangTheoKhachHang(String maKhachHang, OnDonHangCallback callback) {
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
    }

    /**
     * Lấy tất cả đơn hàng cho Admin quản lý và duyệt trạng thái
     */
    public void layTatCaDonHang(OnDonHangCallback callback) {
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
    }

    /**
     * Cập nhật trạng thái đơn hàng (Dành cho Admin: "Đã xác nhận", "Đang giao", "Hoàn thành", "Đã huỷ")
     */
    public void capNhatTrangThaiDonHang(String maDonHang, String trangThaiMoi, OnCapNhatTrangThaiCallback callback) {
                .document(maDonHang)
                .update("trangThaiDon", trangThaiMoi)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
    }
