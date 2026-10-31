package com.example.agriclean.ui.admin;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.agriclean.data.model.SanPham;
import com.example.agriclean.databinding.ActivityQuanLySanPhamBinding;
import com.example.agriclean.databinding.DialogXemQrBinding;
import com.example.agriclean.viewmodel.SanPhamViewModel;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

/**
 * Màn hình Quản Lý Nông Sản Dành Cho Admin (UC14 - Bước 2 trong Báo cáo):
 * - Xem danh sách toàn bộ nông sản sạch thời gian thực.
 * - Tìm kiếm nhanh theo tên sản phẩm.
 * - Xem và hiển thị mã QR truy xuất nguồn gốc nông sản bằng thư viện ZXing (B3).
 * - Xác nhận và xóa sản phẩm khỏi Firestore (B5).
 * - Nút thêm mới nông sản kết nối tới Task 1.3.
 */
public class QuanLySanPhamActivity extends AppCompatActivity implements SanPhamAdminAdapter.OnSanPhamAdminListener {

    private ActivityQuanLySanPhamBinding binding;
    private SanPhamViewModel viewModel;
    private SanPhamAdminAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQuanLySanPhamBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 1. Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(SanPhamViewModel.class);

        // 2. Cài đặt RecyclerView và Adapter
        caiDatRecyclerView();

        // 3. Cài đặt các sự kiện giao diện
        caiDatSuKien();

        // 4. Quan sát LiveData
        quanSatDuLieu();

        // 5. Tải danh sách nông sản Realtime
        viewModel.taiDanhSachSanPhamRealtime();
    }

    private void caiDatRecyclerView() {
        adapter = new SanPhamAdminAdapter(this, this);
        binding.rvSanPhamAdmin.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSanPhamAdmin.setAdapter(adapter);
    }

    private void caiDatSuKien() {
        // Nút quay lại
        binding.btnBack.setOnClickListener(v -> finish());

        // Tìm kiếm nhanh nông sản theo từ khóa (UC05)
        binding.edtTimKiem.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.timKiem(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Nút thêm sản phẩm mới (Mở Form Thêm Task 1.3)
        binding.fabThemSanPham.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(QuanLySanPhamActivity.this, ThemSuaSanPhamActivity.class);
            startActivity(intent);
        });
    }

    private void quanSatDuLieu() {
        // Quan sát danh sách sản phẩm hiển thị
        viewModel.getDanhSachHienThiLiveData().observe(this, danhSach -> {
            adapter.capNhatDanhSach(danhSach);
            boolean coDuLieu = danhSach != null && !danhSach.isEmpty();
            binding.tvTrong.setVisibility(coDuLieu ? View.GONE : View.VISIBLE);
        });

        // Quan sát trạng thái Loading
        viewModel.getLoadingLiveData().observe(this, dangTai -> {
            binding.pbLoading.setVisibility(Boolean.TRUE.equals(dangTai) ? View.VISIBLE : View.GONE);
        });

        // Quan sát thông báo lỗi
        viewModel.getErrorMessageLiveData().observe(this, loi -> {
            if (loi != null && !loi.isEmpty()) {
                Toast.makeText(this, loi, Toast.LENGTH_SHORT).show();
            }
        });

        // Quan sát thao tác thành công (Xóa/Thêm/Sửa)
        viewModel.getActionSuccessLiveData().observe(this, thanhCong -> {
            if (Boolean.TRUE.equals(thanhCong)) {
                Toast.makeText(this, "Thao tác thành công!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- Xử lý sự kiện từ Adapter ---

    /**
     * B3 (Bước 2): Hiển thị Dialog mã QR Code của sản phẩm để in hoặc quét thử
     */
    @Override
    public void onXemQR(SanPham sanPham) {
        if (sanPham.getProductId() == null || sanPham.getProductId().isEmpty()) {
            Toast.makeText(this, "Sản phẩm chưa có mã định danh QR", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Sinh mã QR Bitmap bằng ZXing BarcodeEncoder
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap qrBitmap = barcodeEncoder.encodeBitmap(
                    sanPham.getProductId(),
                    BarcodeFormat.QR_CODE,
                    600,
                    600
            );

            // Tạo Dialog hiển thị ảnh QR
            DialogXemQrBinding dialogBinding = DialogXemQrBinding.inflate(LayoutInflater.from(this));
            dialogBinding.tvDialogTenSanPham.setText(sanPham.getTenSanPham());
            dialogBinding.tvDialogMaSanPham.setText("Mã ProductID: " + sanPham.getProductId());
            dialogBinding.imgQrCode.setImageBitmap(qrBitmap);

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setView(dialogBinding.getRoot())
                    .create();

            dialogBinding.btnDongDialog.setOnClickListener(v -> dialog.dismiss());
            dialog.show();

        } catch (Exception e) {
            Toast.makeText(this, "Lỗi tạo mã QR: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * B5 (Bước 2): Hiển thị AlertDialog xác nhận xóa nông sản
     */
    @Override
    public void onXoa(SanPham sanPham) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa nông sản")
                .setMessage("Bạn có chắc chắn muốn xóa sản phẩm \"" + sanPham.getTenSanPham() + "\" khỏi hệ thống?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    viewModel.xoaSanPham(sanPham.getProductId());
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    /**
     * Chỉnh sửa thông tin nông sản (kết nối Form ở Task 1.3)
     */
    @Override
    public void onSua(SanPham sanPham) {
        android.content.Intent intent = new android.content.Intent(QuanLySanPhamActivity.this, ThemSuaSanPhamActivity.class);
        intent.putExtra(ThemSuaSanPhamActivity.EXTRA_SAN_PHAM, sanPham);
        startActivity(intent);
    }
}

