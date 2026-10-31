package com.example.agriclean.data.repository;

import androidx.annotation.NonNull;

import com.example.agriclean.data.model.NguoiDung;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Lớp AuthRepository: Đóng gói toàn bộ logic tương tác giữa Firebase Authentication
 * và Cloud Firestore (Collection "Users") theo kiến trúc MVVM.
 */
public class AuthRepository {

    public static final String COLLECTION_USERS = "Users";

    private static volatile AuthRepository instance;
    private final FirebaseAuth mAuth;
    private final FirebaseFirestore db;

    /**
     * Interface Callback kết quả xác thực và nạp dữ liệu người dùng
     */
    public interface OnAuthCallback {
        void onSuccess(NguoiDung nguoiDung);
        void onFailure(String errorMessage);
    }

    /**
     * Interface Callback cho các thao tác đơn giản không cần trả về model
     */
    public interface OnActionCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    private AuthRepository() {
        this.mAuth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Khởi tạo Singleton cho AuthRepository
     */
    public static AuthRepository getInstance() {
        if (instance == null) {
            synchronized (AuthRepository.class) {
                if (instance == null) {
                    instance = new AuthRepository();
                }
            }
        }
        return instance;
    }

    /**
     * Lấy thông tin phiên đăng nhập hiện tại từ Firebase Auth
     */
    public FirebaseUser getCurrentFirebaseUser() {
        return mAuth.getCurrentUser();
    }

    /**
     * Kiểm tra người dùng đã đăng nhập trước đó hay chưa
     */
    public boolean daDangNhap() {
        return mAuth.getCurrentUser() != null;
    }

    /**
     * Xử lý Đăng nhập với Email và Mật khẩu (UC02)
     * Sau khi xác thực Firebase Auth thành công, tiếp tục truy vấn hồ sơ từ Firestore
     * để lấy vai trò (role: "admin" hoặc "customer").
     */
    public void dangNhap(@NonNull String email, @NonNull String matKhau, @NonNull OnAuthCallback callback) {
        mAuth.signInWithEmailAndPassword(email, matKhau)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && mAuth.getCurrentUser() != null) {
                        String uid = mAuth.getCurrentUser().getUid();
                        // Nạp hồ sơ người dùng từ Firestore
                        layThongTinNguoiDung(uid, callback);
                    } else {
                        String errorMsg = task.getException() != null ?
                                task.getException().getMessage() : "Đăng nhập thất bại. Vui lòng thử lại.";
                        callback.onFailure(errorMsg);
                    }
                });
    }

    /**
     * Xử lý Đăng ký tài khoản mới (UC01)
     * - Tạo tài khoản bằng FirebaseAuth với Email & Mật khẩu.
     * - Tự động khởi tạo hồ sơ trong Firestore "Users" với role mặc định là "customer".
     */
    public void dangKy(@NonNull String email,
                       @NonNull String matKhau,
                       String hoTen,
                       String sdt,
                       String diaChi,
                       @NonNull OnAuthCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, matKhau)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && mAuth.getCurrentUser() != null) {
                        String uid = mAuth.getCurrentUser().getUid();

                        // Khởi tạo model NguoiDung với role mặc định là "customer"
                        NguoiDung nguoiDungMoi = new NguoiDung(
                                uid,
                                email,
                                NguoiDung.ROLE_CUSTOMER,
                                hoTen,
                                sdt,
                                diaChi
                        );

                        // Lưu hồ sơ người dùng lên collection "Users"
                        db.collection(COLLECTION_USERS)
                                .document(uid)
                                .set(nguoiDungMoi)
                                .addOnSuccessListener(aVoid -> callback.onSuccess(nguoiDungMoi))
                                .addOnFailureListener(e -> callback.onFailure("Đăng ký thành công nhưng không thể tạo hồ sơ: " + e.getMessage()));
                    } else {
                        String errorMsg = task.getException() != null ?
                                task.getException().getMessage() : "Đăng ký thất bại. Vui lòng thử lại.";
                        callback.onFailure(errorMsg);
                    }
                });
    }

    /**
     * Lấy hồ sơ thông tin người dùng từ Firestore theo UID
     */
    public void layThongTinNguoiDung(@NonNull String uid, @NonNull OnAuthCallback callback) {
        db.collection(COLLECTION_USERS).document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        NguoiDung nguoiDung = documentSnapshot.toObject(NguoiDung.class);
                        if (nguoiDung != null) {
                            // Đảm bảo UID luôn được gắn đúng
                            nguoiDung.setUid(uid);
                            callback.onSuccess(nguoiDung);
                        } else {
                            callback.onFailure("Lỗi định dạng dữ liệu người dùng.");
                        }
                    } else {
                        // Thử tra cứu fallback với tên collection chữ thường nếu tài khoản cũ lưu ở "users"
                        thuTimVoiCollectionPhu(uid, callback);
                    }
                })
                .addOnFailureListener(e -> callback.onFailure("Không thể nạp thông tin người dùng: " + e.getMessage()));
    }

    /**
     * Hỗ trợ tìm kiếm dự phòng collection "users" viết thường để đảm bảo tương thích dữ liệu cũ
     */
    private void thuTimVoiCollectionPhu(String uid, OnAuthCallback callback) {
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        NguoiDung nguoiDung = doc.toObject(NguoiDung.class);
                        if (nguoiDung != null) {
                            nguoiDung.setUid(uid);
                            callback.onSuccess(nguoiDung);
                            return;
                        }
                    }
                    callback.onFailure("Không tìm thấy thông tin tài khoản trong hệ thống.");
                })
                .addOnFailureListener(e -> callback.onFailure("Không tìm thấy thông tin tài khoản: " + e.getMessage()));
    }

    /**
     * Đăng xuất khỏi hệ thống
     */
    public void dangXuat() {
        mAuth.signOut();
    }
}

