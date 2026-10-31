package com.example.agriclean.viewmodel;

import android.util.Patterns;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.agriclean.data.model.NguoiDung;
import com.example.agriclean.data.repository.AuthRepository;
import com.google.firebase.auth.FirebaseUser;

/**
 * Lớp AuthViewModel: Quản lý trạng thái và luồng nghiệp vụ xác thực tài khoản (UC01, UC02).
 * Cầu nối giữa UI (DangNhapActivity, DangKyActivity) và AuthRepository theo chuẩn MVVM.
 */
public class AuthViewModel extends ViewModel {

    private final AuthRepository authRepository;

    // LiveData quan sát kết quả người dùng đăng nhập/đăng ký thành công
    private final MutableLiveData<NguoiDung> userLiveData = new MutableLiveData<>();

    // LiveData quan sát trạng thái xử lý (loading) để bật/tắt ProgressBar trên UI
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>();

    // LiveData quan sát các thông báo lỗi để hiển thị Toast / Error message
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();

    // LiveData quan sát trạng thái đăng xuất
    private final MutableLiveData<Boolean> dangXuatSuccessLiveData = new MutableLiveData<>();

    public AuthViewModel() {
        this.authRepository = AuthRepository.getInstance();
    }

    public AuthViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    // --- Cung cấp LiveData cho Activity quan sát ---

    public LiveData<NguoiDung> getUserLiveData() {
        return userLiveData;
    }

    public LiveData<Boolean> getLoadingLiveData() {
        return loadingLiveData;
    }

    public LiveData<String> getErrorMessageLiveData() {
        return errorMessageLiveData;
    }

    public LiveData<Boolean> getDangXuatSuccessLiveData() {
        return dangXuatSuccessLiveData;
    }

    /**
     * Kiểm tra tính hợp lệ của Email và Mật khẩu (Validation theo UC01 & UC02)
     * Trả về true nếu hợp lệ, ngược lại bắn thông báo lỗi qua errorMessageLiveData
     */
    public boolean kiemTraHopLeDauVao(String email, String matKhau) {
        if (email == null || email.trim().isEmpty()) {
            errorMessageLiveData.setValue("Vui lòng nhập Email");
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            errorMessageLiveData.setValue("Email không đúng định dạng");
            return false;
        }

        if (matKhau == null || matKhau.trim().isEmpty()) {
            errorMessageLiveData.setValue("Vui lòng nhập Mật khẩu");
            return false;
        }

        if (matKhau.trim().length() < 6) {
            errorMessageLiveData.setValue("Mật khẩu phải từ 6 ký tự trở lên");
            return false;
        }

        return true;
    }

    /**
     * Nghiệp vụ Đăng nhập (UC02)
     */
    public void dangNhap(String email, String matKhau) {
        if (!kiemTraHopLeDauVao(email, matKhau)) {
            return;
        }

        loadingLiveData.setValue(true);
        authRepository.dangNhap(email.trim(), matKhau.trim(), new AuthRepository.OnAuthCallback() {
            @Override
            public void onSuccess(NguoiDung nguoiDung) {
                loadingLiveData.setValue(false);
                userLiveData.setValue(nguoiDung);
            }

            @Override
            public void onFailure(String errorMessage) {
                loadingLiveData.setValue(false);
                errorMessageLiveData.setValue(errorMessage);
            }
        });
    }

    /**
     * Nghiệp vụ Đăng ký rút gọn theo UC01 (Chỉ cần Email & Mật khẩu, các trường khác để rỗng)
     */
    public void dangKy(String email, String matKhau) {
        dangKy(email, matKhau, "", "", "");
    }

    /**
     * Nghiệp vụ Đăng ký đầy đủ thông tin cá nhân
     */
    public void dangKy(String email, String matKhau, String hoTen, String sdt, String diaChi) {
        if (!kiemTraHopLeDauVao(email, matKhau)) {
            return;
        }

        loadingLiveData.setValue(true);
        authRepository.dangKy(email.trim(), matKhau.trim(), hoTen, sdt, diaChi, new AuthRepository.OnAuthCallback() {
            @Override
            public void onSuccess(NguoiDung nguoiDung) {
                loadingLiveData.setValue(false);
                userLiveData.setValue(nguoiDung);
            }

            @Override
            public void onFailure(String errorMessage) {
                loadingLiveData.setValue(false);
                errorMessageLiveData.setValue(errorMessage);
            }
        });
    }

    /**
     * Kiểm tra phiên đăng nhập còn hiệu lực khi mở ứng dụng
     */
    public void kiemTraPhienDangNhap() {
        if (authRepository.daDangNhap()) {
            FirebaseUser currentUser = authRepository.getCurrentFirebaseUser();
            if (currentUser != null) {
                loadingLiveData.setValue(true);
                authRepository.layThongTinNguoiDung(currentUser.getUid(), new AuthRepository.OnAuthCallback() {
                    @Override
                    public void onSuccess(NguoiDung nguoiDung) {
                        loadingLiveData.setValue(false);
                        userLiveData.setValue(nguoiDung);
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        loadingLiveData.setValue(false);
                        // Nếu phiên lỗi hoặc profile bị xóa, thông báo nhẹ
                        errorMessageLiveData.setValue(errorMessage);
                    }
                });
            }
        }
    }

    /**
     * Nghiệp vụ Đăng xuất
     */
    public void dangXuat() {
        authRepository.dangXuat();
        userLiveData.setValue(null);
        dangXuatSuccessLiveData.setValue(true);
    }
}

