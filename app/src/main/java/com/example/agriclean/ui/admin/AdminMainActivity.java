package com.example.agriclean.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.agriclean.data.model.NguoiDung;
import com.example.agriclean.databinding.ActivityAdminMainBinding;
import com.example.agriclean.ui.auth.DangNhapActivity;
import com.example.agriclean.viewmodel.AuthViewModel;

/**
 * Màn hình Bảng Điều Khiển Quản Trị Viên (AdminMainActivity):
 * - Dành riêng cho Quản trị viên/Bên cung cấp nông sản (UC14, UC15, UC16).
 * - Kiểm tra bảo mật vai trò nghiêm ngặt (NFR06): chặn toàn bộ người dùng không phải Admin.
 * - Xử lý Đăng xuất an toàn theo chuẩn MVVM (UC03).
 */
public class AdminMainActivity extends AppCompatActivity {

    private ActivityAdminMainBinding binding;
    private AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 1. Khởi tạo AuthViewModel
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // 2. Kiểm tra phân quyền bảo mật (NFR06)
        if (!kiemTraQuyenAdmin()) {
            return;
        }

        // 3. Cài đặt các sự kiện giao diện
        caiDatSuKien();

        // 4. Quan sát LiveData (trạng thái đăng xuất UC03)
        quanSatDuLieu();
    }

    /**
     * Kiểm tra bảo mật vai trò (NFR06):
     * Nếu không có quyền Admin hoặc chưa đăng nhập, hủy phiên và đưa về màn hình Đăng nhập
     */
    private boolean kiemTraQuyenAdmin() {
        String role = getIntent().getStringExtra(DangNhapActivity.EXTRA_USER_ROLE);

        if (role == null || !NguoiDung.ROLE_ADMIN.equalsIgnoreCase(role)) {
            Toast.makeText(this, "Bạn không có quyền truy cập khu vực Quản trị!", Toast.LENGTH_LONG).show();
            viewModel.dangXuat();
            dieuHuongVeDangNhap();
            return false;
        }
        return true;
    }

    /**
     * Cài đặt các sự kiện tương tác
     */
    private void caiDatSuKien() {
        // Nút Đăng xuất (UC03)
        binding.btnDangXuat.setOnClickListener(v -> hienThiDialogXacNhanDangXuat());

        // Quản lý Nông sản (UC14)
        binding.cardQuanLySanPham.setOnClickListener(v -> moQuanLySanPham());
        binding.btnQuanLySanPham.setOnClickListener(v -> moQuanLySanPham());

        // Quản lý Đơn hàng (UC15, UC16)
        binding.cardQuanLyDonHang.setOnClickListener(v -> moQuanLyDonHang());
        binding.btnQuanLyDonHang.setOnClickListener(v -> moQuanLyDonHang());
    }

    /**
     * Mở tính năng Quản lý Sản phẩm (UC14 - Bước 2)
     */
    private void moQuanLySanPham() {
        Intent intent = new Intent(AdminMainActivity.this, QuanLySanPhamActivity.class);
        startActivity(intent);
    }

    /**
     * Mở tính năng Quản lý Đơn hàng (UC15, UC16)
     */
    private void moQuanLyDonHang() {
        Intent intent = new Intent(AdminMainActivity.this, QuanLyDonHangActivity.class);
        startActivity(intent);
    }

    /**
     * Hiển thị hộp thoại xác nhận đăng xuất (UC03)
     */
    private void hienThiDialogXacNhanDangXuat() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất khỏi phiên làm việc Quản trị viên?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    viewModel.dangXuat();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    /**
     * Lắng nghe LiveData từ ViewModel
     */
    private void quanSatDuLieu() {
        viewModel.getDangXuatSuccessLiveData().observe(this, daDangXuat -> {
            if (Boolean.TRUE.equals(daDangXuat)) {
                Toast.makeText(this, "Đã đăng xuất thành công!", Toast.LENGTH_SHORT).show();
                dieuHuongVeDangNhap();
            }
        });
    }

    /**
     * Xóa sạch Back Stack và điều hướng về DangNhapActivity
     */
    private void dieuHuongVeDangNhap() {
        Intent intent = new Intent(AdminMainActivity.this, DangNhapActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
