package com.example.agriclean.data.repository;

import com.example.agriclean.data.model.DonHang;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Lớp DonHangRepository: Xử lý quy trình tạo đơn hàng và theo dõi lịch sử đơn hàng
 * trên Cloud Firestore collection "Orders".
 * Sắp xếp dữ liệu cục bộ trên Client để tránh yêu cầu tạo Composite Index (FAILED_PRECONDITION).
 */
public class DonHangRepository {

    public static final String COLLECTION_ORDERS = "Orders";

    private static volatile DonHangRepository instance;
    private final FirebaseFirestore db;

    private DonHangRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    public static synchronized DonHangRepository getInstance() {
        if (instance == null) {
            instance = new DonHangRepository();
        }
        return instance;
    }

    public interface OnDonHangCallback {
        void onResult(List<DonHang> danhSachDonHang);
        void onFailure(String errorMessage);
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
     * Tạo đơn hàng mới đẩy lên Firestore collection "Orders"
     */
    public void taoDonHang(DonHang donHang, OnTaoDonCallback callback) {
        if (donHang.getMaDonHang() != null && !donHang.getMaDonHang().isEmpty()) {
            db.collection(COLLECTION_ORDERS)
                    .document(donHang.getMaDonHang())
                    .set(donHang)
                    .addOnSuccessListener(aVoid -> callback.onSuccess(donHang.getMaDonHang()))
                    .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
        } else {
            db.collection(COLLECTION_ORDERS)
                    .add(donHang)
                    .addOnSuccessListener(doc -> {
                        donHang.setMaDonHang(doc.getId());
                        doc.update("maDonHang", doc.getId());
                        callback.onSuccess(doc.getId());
                    })
                    .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
        }
    }

    /**
     * Lắng nghe Realtime lịch sử mua hàng của một khách hàng cụ thể (UC13).
     * Bỏ orderBy trên query Firestore để không cần Composite Index; sắp xếp theo thời gian mới nhất trong bộ nhớ.
     */
    public ListenerRegistration langNgheDonHangKhachHang(String maKhachHang, OnDonHangCallback callback) {
        return db.collection(COLLECTION_ORDERS)
                .whereEqualTo("maKhachHang", maKhachHang)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        callback.onFailure(error.getMessage());
                        return;
                    }

                    List<DonHang> list = new ArrayList<>();
                    if (snapshots != null) {
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            DonHang don = doc.toObject(DonHang.class);
                            if (don != null) {
                                if (don.getMaDonHang() == null || don.getMaDonHang().isEmpty()) {
                                    don.setMaDonHang(doc.getId());
                                }
                                list.add(don);
                            }
                        }
                    }

                    // Sắp xếp đơn hàng mới nhất lên đầu trong bộ nhớ
                    sapXepDonHangMoiNhat(list);
                    callback.onResult(list);
                });
    }

    /**
     * Lấy lịch sử mua hàng một lần của một khách hàng cụ thể
     */
    public void layDonHangTheoKhachHang(String maKhachHang, OnDonHangCallback callback) {
        db.collection(COLLECTION_ORDERS)
                .whereEqualTo("maKhachHang", maKhachHang)
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<DonHang> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        DonHang don = doc.toObject(DonHang.class);
                        if (don != null) {
                            if (don.getMaDonHang() == null || don.getMaDonHang().isEmpty()) {
                                don.setMaDonHang(doc.getId());
                            }
                            list.add(don);
                        }
                    }

                    // Sắp xếp đơn hàng mới nhất lên đầu trong bộ nhớ
                    sapXepDonHangMoiNhat(list);
                    callback.onResult(list);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Lắng nghe Realtime toàn bộ đơn hàng cho Admin quản lý (UC15, UC16)
     */
    public ListenerRegistration langNgheTatCaDonHang(OnDonHangCallback callback) {
        return db.collection(COLLECTION_ORDERS)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        callback.onFailure(error.getMessage());
                        return;
                    }

                    List<DonHang> list = new ArrayList<>();
                    if (snapshots != null) {
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            DonHang don = doc.toObject(DonHang.class);
                            if (don != null) {
                                if (don.getMaDonHang() == null || don.getMaDonHang().isEmpty()) {
                                    don.setMaDonHang(doc.getId());
                                }
                                list.add(don);
                            }
                        }
                    }

                    // Sắp xếp đơn hàng mới nhất lên đầu trong bộ nhớ
                    sapXepDonHangMoiNhat(list);
                    callback.onResult(list);
                });
    }

    /**
     * Lấy tất cả đơn hàng cho Admin quản lý và duyệt trạng thái
     */
    public void layTatCaDonHang(OnDonHangCallback callback) {
        db.collection(COLLECTION_ORDERS)
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<DonHang> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        DonHang don = doc.toObject(DonHang.class);
                        if (don != null) {
                            if (don.getMaDonHang() == null || don.getMaDonHang().isEmpty()) {
                                don.setMaDonHang(doc.getId());
                            }
                            list.add(don);
                        }
                    }

                    // Sắp xếp đơn hàng mới nhất lên đầu trong bộ nhớ
                    sapXepDonHangMoiNhat(list);
                    callback.onResult(list);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Cập nhật trạng thái đơn hàng (Dành cho Admin: "Đã xác nhận", "Đang giao", "Hoàn thành", "Đã huỷ")
     */
    public void capNhatTrangThaiDonHang(String maDonHang, String trangThaiMoi, OnCapNhatTrangThaiCallback callback) {
        db.collection(COLLECTION_ORDERS)
                .document(maDonHang)
                .update("trangThaiDon", trangThaiMoi)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /**
     * Hàm phụ trợ sắp xếp đơn hàng giảm dần theo thời gian (đơn mới nhất lên đầu)
     */
    private void sapXepDonHangMoiNhat(List<DonHang> list) {
        if (list == null || list.size() <= 1) return;
        Collections.sort(list, (d1, d2) -> {
            if (d1.getNgayDatHang() == null && d2.getNgayDatHang() == null) return 0;
            if (d1.getNgayDatHang() == null) return 1;
            if (d2.getNgayDatHang() == null) return -1;
            return d2.getNgayDatHang().compareTo(d1.getNgayDatHang());
        });
    }
}
