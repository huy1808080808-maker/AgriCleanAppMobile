package com.example.agriclean.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.agriclean.MainActivity;
import com.example.agriclean.databinding.ActivityDangKyBinding;
import com.example.agriclean.viewmodel.AuthViewModel;

/**
 * Màn hình Đăng ký tài khoản (DangKyActivity):
 * Chuẩn hóa 100% theo kiến trúc MVVM và ViewBinding.
 * - View thu thập dữ liệu và bắt sự kiện.
 * - Nghiệp vụ đăng ký tài khoản (UC01) và khởi tạo hồ sơ do AuthViewModel đảm nhận.
 * - Tự động gán vai trò mặc định "customer" và điều hướng vào hệ thống.
 */
public class DangKyActivity extends AppCompatActivity {

    private ActivityDangKyBinding binding;
    private AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDangKyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 1. Khởi tạo AuthViewModel
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // 2. Thiết lập sự kiện UI
        caiDatSuKien();

        // 3. Quan sát các luồng LiveData từ ViewModel
        quanSatDuLieu();
    }

    /**
     * Cài đặt các sự kiện click trên giao diện
     */
    private void caiDatSuKien() {
        // Quay lại màn hình Đăng nhập
        binding.tvChuyenDangNhap.setOnClickListener(v -> finish());

        // Bấm nút Đăng ký
        binding.btnDangKy.setOnClickListener(v -> xuLyDangKy());
    }

    /**
     * Thu thập và kiểm tra sơ bộ dữ liệu người dùng trước khi gửi cho ViewModel
     */
    private void xuLyDangKy() {
        String hoTen = binding.edtHoTen.getText() != null ?
                binding.edtHoTen.getText().toString().trim() : "";
        String email = binding.edtEmail.getText() != null ?
                binding.edtEmail.getText().toString().trim() : "";
        String sdt = binding.edtSoDienThoai.getText() != null ?
                binding.edtSoDienThoai.getText().toString().trim() : "";
        String matKhau = binding.edtMatKhau.getText() != null ?
                binding.edtMatKhau.getText().toString().trim() : "";

        // Kiểm tra định dạng số điện thoại nếu người dùng có nhập (bắt đầu bằng 0, gồm 10 chữ số)
        if (!sdt.isEmpty() && !sdt.matches("^0\\d{9}$")) {
            binding.edtSoDienThoai.setError("Số điện thoại không hợp lệ (phải gồm 10 số và bắt đầu bằng số 0)");
            binding.edtSoDienThoai.requestFocus();
            return;
        }

        // Gọi ViewModel thực hiện nghiệp vụ Đăng ký (UC01)
        viewModel.dangKy(email, matKhau, hoTen, sdt, "");
    }

    /**
     * Lắng nghe kết quả từ ViewModel
     */
    private void quanSatDuLieu() {
        // 1. Đăng ký thành công
        viewModel.getUserLiveData().observe(this, nguoiDung -> {
            if (nguoiDung != null) {
                Toast.makeText(DangKyActivity.this, "Đăng ký tài khoản thành công!", Toast.LENGTH_SHORT).show();

                // Điều hướng sang màn hình chính và xóa sạch Back Stack
                Intent intent = new Intent(DangKyActivity.this, MainActivity.class);
                intent.putExtra(DangNhapActivity.EXTRA_USER_ROLE, nguoiDung.getRole());
                intent.putExtra(DangNhapActivity.EXTRA_USER_UID, nguoiDung.getUid());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        // 2. Trạng thái Loading
        viewModel.getLoadingLiveData().observe(this, dangXuLy -> {
            boolean isLoading = Boolean.TRUE.equals(dangXuLy);
            binding.btnDangKy.setEnabled(!isLoading);
            binding.btnDangKy.setText(isLoading ? "ĐANG TẠO TÀI KHOẢN..." : "ĐĂNG KÝ");
        });

        // 3. Thông báo lỗi
        viewModel.getErrorMessageLiveData().observe(this, thongBaoLoi -> {
            if (thongBaoLoi != null && !thongBaoLoi.isEmpty()) {
                if (thongBaoLoi.contains("Email")) {
                    binding.edtEmail.setError(thongBaoLoi);
                    binding.edtEmail.requestFocus();
                } else if (thongBaoLoi.contains("Mật khẩu")) {
                    binding.edtMatKhau.setError(thongBaoLoi);
                    binding.edtMatKhau.requestFocus();
                } else {
                    Toast.makeText(DangKyActivity.this, thongBaoLoi, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
