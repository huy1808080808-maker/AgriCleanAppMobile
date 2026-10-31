package com.example.agriclean.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.agriclean.MainActivity;
import com.example.agriclean.data.model.NguoiDung;
import com.example.agriclean.databinding.ActivityDangNhapBinding;
import com.example.agriclean.viewmodel.AuthViewModel;

/**
 * Màn hình Đăng nhập (DangNhapActivity):
 * Chuẩn hóa 100% theo kiến trúc MVVM và ViewBinding.
 * - View chỉ đảm nhận hiển thị và bắt sự kiện người dùng.
 * - Toàn bộ nghiệp vụ xác thực do AuthViewModel điều phối.
 * - Phân quyền và điều hướng chặt chẽ (UC02 & NFR06) giữa Admin và Customer.
 */
public class DangNhapActivity extends AppCompatActivity {

    public static final String EXTRA_USER_ROLE = "extra_user_role";
    public static final String EXTRA_USER_UID = "extra_user_uid";

    private ActivityDangNhapBinding binding;
    private AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDangNhapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 1. Khởi tạo ViewModel theo chuẩn Android Architecture Components
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // 2. Thiết lập lắng nghe các sự kiện UI
        caiDatSuKien();

        // 3. Quan sát các luồng dữ liệu (LiveData) từ ViewModel
        quanSatDuLieu();

        // 4. Tự động kiểm tra phiên đăng nhập đã lưu trước đó (Auto Login)
        viewModel.kiemTraPhienDangNhap();
    }

    /**
     * Bắt sự kiện click từ các thành phần View
     */
    private void caiDatSuKien() {
        // Chuyển sang màn hình Đăng ký
        binding.tvChuyenDangKy.setOnClickListener(v -> {
            startActivity(new Intent(DangNhapActivity.this, DangKyActivity.class));
        });

        // Bấm nút Đăng nhập: Chuyển dữ liệu cho ViewModel xử lý
        binding.btnDangNhap.setOnClickListener(v -> {
            String email = binding.edtEmail.getText() != null ?
                    binding.edtEmail.getText().toString().trim() : "";
            String matKhau = binding.edtMatKhau.getText() != null ?
                    binding.edtMatKhau.getText().toString().trim() : "";

            viewModel.dangNhap(email, matKhau);
        });
    }

    /**
     * Lắng nghe LiveData từ AuthViewModel để cập nhật giao diện và điều hướng
     */
    private void quanSatDuLieu() {
        // Quan sát kết quả xác thực người dùng thành công
        viewModel.getUserLiveData().observe(this, nguoiDung -> {
            if (nguoiDung != null) {
                dieuHuongTheoVaiTro(nguoiDung);
            }
        });

        // Quan sát trạng thái xử lý (Loading) để tối ưu trải nghiệm (UX)
        viewModel.getLoadingLiveData().observe(this, dangXuLy -> {
            boolean isLoading = Boolean.TRUE.equals(dangXuLy);
            binding.btnDangNhap.setEnabled(!isLoading);
            binding.btnDangNhap.setText(isLoading ? "ĐANG ĐĂNG NHẬP..." : "ĐĂNG NHẬP");
        });

        // Quan sát thông báo lỗi để hiển thị phản hồi cho người dùng
        viewModel.getErrorMessageLiveData().observe(this, thongBaoLoi -> {
            if (thongBaoLoi != null && !thongBaoLoi.isEmpty()) {
                if (thongBaoLoi.contains("Email")) {
                    binding.edtEmail.setError(thongBaoLoi);
                    binding.edtEmail.requestFocus();
                } else if (thongBaoLoi.contains("Mật khẩu")) {
                    binding.edtMatKhau.setError(thongBaoLoi);
                    binding.edtMatKhau.requestFocus();
                } else {
                    Toast.makeText(DangNhapActivity.this, thongBaoLoi, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Phân quyền và điều hướng chặt chẽ (UC02 & NFR06)
     * - Admin -> AdminMainActivity
     * - Customer -> MainActivity
     * Xóa sạch Back Stack bằng FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK
     * để người dùng không thể bấm Back quay lại màn hình đăng nhập.
     */
    private void dieuHuongTheoVaiTro(NguoiDung nguoiDung) {
        Intent intent;
        if (nguoiDung.isAdmin()) {
            Toast.makeText(this, "Đăng nhập thành công với vai trò Quản Trị Viên (Admin)", Toast.LENGTH_SHORT).show();
            intent = new Intent(DangNhapActivity.this, com.example.agriclean.ui.admin.AdminMainActivity.class);
        } else {
            Toast.makeText(this, "Chào mừng Khách hàng trở lại!", Toast.LENGTH_SHORT).show();
            intent = new Intent(DangNhapActivity.this, MainActivity.class);
        }

        intent.putExtra(EXTRA_USER_ROLE, nguoiDung.getRole());
        intent.putExtra(EXTRA_USER_UID, nguoiDung.getUid());

        // Xóa sạch Back Stack bảo mật luồng màn hình
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
