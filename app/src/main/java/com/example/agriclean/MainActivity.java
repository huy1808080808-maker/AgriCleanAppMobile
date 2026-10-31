package com.example.agriclean;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.agriclean.data.local.GioHangItem;
import com.example.agriclean.data.model.NguoiDung;
import com.example.agriclean.data.model.SanPham;
import com.example.agriclean.databinding.ActivityMainBinding;
import com.example.agriclean.ui.admin.AdminMainActivity;
import com.example.agriclean.ui.auth.DangNhapActivity;
import com.example.agriclean.ui.customer.GioHangActivity;
import com.example.agriclean.ui.customer.LichSuDonHangActivity;
import com.example.agriclean.ui.customer.SanPhamCustomerAdapter;
import com.example.agriclean.utils.QuetQRHelper;
import com.example.agriclean.viewmodel.AuthViewModel;
import com.example.agriclean.viewmodel.GioHangViewModel;
import com.example.agriclean.viewmodel.SanPhamViewModel;
import com.google.android.material.button.MaterialButton;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

/**
 * Màn hình Trang Chủ Khách Hàng (MainActivity):
 * - Không gian mua sắm nông sản sạch dành riêng cho Khách hàng (Customer - Bước 3).
 * - Hiển thị dạng lưới 2 cột (GridLayoutManager) danh sách nông sản Realtime (UC04).
 * - Tìm kiếm nông sản thời gian thực (UC05).
 * - Lọc danh mục ngang (UC06).
 * - Thêm nhanh vào giỏ hàng Room Database (UC10 - Bước 5).
 * - Quét mã QR nguồn gốc nông sản (UC08 - Bước 4).
 */
public class MainActivity extends AppCompatActivity implements SanPhamCustomerAdapter.OnSanPhamCustomerListener {

    private ActivityMainBinding binding;
    private AuthViewModel authViewModel;
    private SanPhamViewModel sanPhamViewModel;
    private GioHangViewModel gioHangViewModel;
    private SanPhamCustomerAdapter adapter;

    // Trình kích hoạt Camera quét mã QR truy xuất nguồn gốc (UC08)
    private final ActivityResultLauncher<ScanOptions> qrLauncher = registerForActivityResult(
            new ScanContract(),
            result -> {
                if (result.getContents() != null) {
                    String qrCode = result.getContents().trim();
                    Intent intent = new Intent(MainActivity.this, com.example.agriclean.ui.customer.ChiTietSanPhamActivity.class);
                    intent.putExtra(com.example.agriclean.ui.customer.ChiTietSanPhamActivity.EXTRA_PRODUCT_ID, qrCode);
                    startActivity(intent);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 1. Khởi tạo các ViewModel
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        sanPhamViewModel = new ViewModelProvider(this).get(SanPhamViewModel.class);
        gioHangViewModel = new ViewModelProvider(this).get(GioHangViewModel.class);

        // 2. Kiểm tra phân quyền bảo mật (NFR06)
        if (!kiemTraQuyenKhachHang()) {
            return;
        }

        // 3. Cài đặt RecyclerView hiển thị dạng lưới 2 cột (UC04)
        caiDatRecyclerView();

        // 4. Cài đặt các sự kiện giao diện (Tìm kiếm, Lọc danh mục, Giỏ hàng, Đăng xuất)
        caiDatSuKien();

        // 5. Quan sát LiveData từ các ViewModel
        quanSatDuLieu();

        // 6. Tải dữ liệu nông sản sạch thời gian thực từ Firestore
        sanPhamViewModel.taiDanhSachSanPhamRealtime();
    }

    /**
     * Kiểm tra bảo mật vai trò (NFR06):
     * Nếu là Admin -> Chuyển sang AdminMainActivity, không cho Admin đặt hàng.
     */
    private boolean kiemTraQuyenKhachHang() {
        String role = getIntent().getStringExtra(DangNhapActivity.EXTRA_USER_ROLE);

        if (NguoiDung.ROLE_ADMIN.equalsIgnoreCase(role)) {
            Intent intent = new Intent(MainActivity.this, AdminMainActivity.class);
            intent.putExtra(DangNhapActivity.EXTRA_USER_ROLE, role);
            startActivity(intent);
            finish();
            return false;
        }
        return true;
    }

    /**
     * Cài đặt RecyclerView dạng lưới 2 cột
     */
    private void caiDatRecyclerView() {
        adapter = new SanPhamCustomerAdapter(this, this);
        binding.rvSanPham.setLayoutManager(new GridLayoutManager(this, 2));
        binding.rvSanPham.setAdapter(adapter);
    }

    /**
     * Cài đặt các sự kiện tương tác trên giao diện
     */
    private void caiDatSuKien() {
        // Nút Giỏ hàng -> Mở GioHangActivity
        binding.btnGioHang.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, GioHangActivity.class));
        });

        // Nút Đăng xuất (UC03)
        binding.btnDangXuat.setOnClickListener(v -> hienThiDialogDangXuat());

        // Thanh điều hướng đáy (Bottom Navigation - Bước 7)
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                binding.rvSanPham.smoothScrollToPosition(0);
                return true;
            } else if (itemId == R.id.nav_qr) {
                QuetQRHelper.batDauQuetQR(qrLauncher, "Hướng camera vào mã QR trên bao bì nông sản");
                return true;
            } else if (itemId == R.id.nav_orders) {
                startActivity(new Intent(MainActivity.this, LichSuDonHangActivity.class));
                return true;
            }
            return false;
        });

        // Tìm kiếm nông sản thời gian thực (UC05)
        binding.edtTimKiem.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                sanPhamViewModel.timKiem(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Bộ lọc danh mục ngang (UC06)
        binding.btnCatTatCa.setOnClickListener(v -> chonDanhMuc(binding.btnCatTatCa, SanPham.DANH_MUC_TAT_CA));
        binding.btnCatRauXanh.setOnClickListener(v -> chonDanhMuc(binding.btnCatRauXanh, SanPham.DANH_MUC_RAU_XANH));
        binding.btnCatCuQua.setOnClickListener(v -> chonDanhMuc(binding.btnCatCuQua, SanPham.DANH_MUC_CU_QUA));
        binding.btnCatTraiCay.setOnClickListener(v -> chonDanhMuc(binding.btnCatTraiCay, SanPham.DANH_MUC_TRAI_CAY));
        binding.btnCatNam.setOnClickListener(v -> chonDanhMuc(binding.btnCatNam, SanPham.DANH_MUC_NAM));
    }

    /**
     * Cập nhật màu sắc nút danh mục và gọi lọc dữ liệu trong ViewModel (UC06)
     */
    private void chonDanhMuc(MaterialButton selectedBtn, String tenDanhMuc) {
        MaterialButton[] buttons = {
                binding.btnCatTatCa,
                binding.btnCatRauXanh,
                binding.btnCatCuQua,
                binding.btnCatTraiCay,
                binding.btnCatNam
        };

        for (MaterialButton btn : buttons) {
            if (btn == selectedBtn) {
                btn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2E7D32"))); // primary_green
                btn.setTextColor(Color.WHITE);
                btn.setStrokeWidth(0);
            } else {
                btn.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                btn.setTextColor(Color.parseColor("#1B1B1B"));
                btn.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#E0E6E0")));
                btn.setStrokeWidth(2);
            }
        }

        sanPhamViewModel.locTheoDanhMuc(tenDanhMuc);
    }

    /**
     * Quan sát dữ liệu LiveData từ các ViewModel
     */
    private void quanSatDuLieu() {
        // Quan sát danh sách nông sản hiển thị (UC04)
        sanPhamViewModel.getDanhSachHienThiLiveData().observe(this, danhSach -> {
            adapter.capNhatDanhSach(danhSach);
            boolean coDuLieu = danhSach != null && !danhSach.isEmpty();
            binding.tvTrong.setVisibility(coDuLieu ? View.GONE : View.VISIBLE);
        });

        // Quan sát trạng thái Loading
        sanPhamViewModel.getLoadingLiveData().observe(this, dangTai -> {
            binding.pbLoading.setVisibility(Boolean.TRUE.equals(dangTai) ? View.VISIBLE : View.GONE);
        });

        // Quan sát thông báo lỗi
        sanPhamViewModel.getErrorMessageLiveData().observe(this, loi -> {
            if (loi != null && !loi.isEmpty()) {
                Toast.makeText(this, loi, Toast.LENGTH_SHORT).show();
            }
        });

        // Quan sát trạng thái Đăng xuất
        authViewModel.getDangXuatSuccessLiveData().observe(this, daDangXuat -> {
            if (Boolean.TRUE.equals(daDangXuat)) {
                Toast.makeText(this, "Đã đăng xuất thành công!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, DangNhapActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        // Quan sát số lượng sản phẩm trong Giỏ hàng để cập nhật Badge màu đỏ
        gioHangViewModel.getDanhSachGioHang().observe(this, danhSach -> {
            int tongSoLuong = 0;
            if (danhSach != null) {
                for (GioHangItem item : danhSach) {
                    tongSoLuong += item.getSoLuong();
                }
            }
            if (tongSoLuong > 0) {
                binding.tvBadgeGioHang.setVisibility(View.VISIBLE);
                binding.tvBadgeGioHang.setText(tongSoLuong > 99 ? "99+" : String.valueOf(tongSoLuong));
            } else {
                binding.tvBadgeGioHang.setVisibility(View.GONE);
            }
        });
    }

    // --- Xử lý sự kiện từ SanPhamCustomerAdapter ---

    /**
     * Mở màn hình Chi tiết Nông sản & Nông trại (UC07 - Bước 4)
     */
    @Override
    public void onXemChiTiet(SanPham sanPham) {
        Intent intent = new Intent(MainActivity.this, com.example.agriclean.ui.customer.ChiTietSanPhamActivity.class);
        intent.putExtra(com.example.agriclean.ui.customer.ChiTietSanPhamActivity.EXTRA_SAN_PHAM, sanPham);
        startActivity(intent);
    }

    /**
     * Thêm nhanh vào Giỏ hàng Room Database (UC10 - Bước 5)
     */
    @Override
    public void onThemVaoGio(SanPham sanPham) {
        GioHangItem item = new GioHangItem(
                sanPham.getProductId(),
                sanPham.getTenSanPham(),
                sanPham.getGia(),
                1,
                sanPham.getHinhAnhUrl(),
                sanPham.getTenTrangTrai()
        );
        gioHangViewModel.themVaoGio(item);
        Toast.makeText(this, "Đã thêm " + sanPham.getTenSanPham() + " vào giỏ hàng!", Toast.LENGTH_SHORT).show();
    }

    /**
     * Hộp thoại xác nhận đăng xuất (UC03)
     */
    private void hienThiDialogDangXuat() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất tài khoản?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    authViewModel.dangXuat();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
