package com.example.agriclean.ui.customer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.agriclean.data.local.GioHangItem;
import com.example.agriclean.databinding.ActivityGioHangBinding;
import com.example.agriclean.viewmodel.GioHangViewModel;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Màn hình Giỏ Hàng Nông Sản Sạch (UC10, UC11 - Bước 5):
 * - Lưu trữ và quản lý hoàn toàn Offline bằng Room Database (NFR04).
 * - Tự động cập nhật số lượng và tính toán tổng tiền thanh toán theo thời gian thực (Realtime).
 * - Hộp thoại xác nhận an toàn khi xóa sản phẩm hoặc làm trống giỏ hàng.
 * - Sẵn sàng chuyển tiếp sang luồng Đặt hàng & Thanh toán (UC12 - Bước 6).
 */
public class GioHangActivity extends AppCompatActivity implements GioHangAdapter.OnGioHangItemListener {

    private ActivityGioHangBinding binding;
    private GioHangViewModel gioHangViewModel;
    private GioHangAdapter adapter;
    private final DecimalFormat formatter = new DecimalFormat("#,###");

    private List<GioHangItem> danhSachHienTai;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGioHangBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 1. Khởi tạo ViewModel
        gioHangViewModel = new ViewModelProvider(this).get(GioHangViewModel.class);

        // 2. Cài đặt RecyclerView và Adapter
        caiDatRecyclerView();

        // 3. Cài đặt các sự kiện nút bấm giao diện
        caiDatSuKien();

        // 4. Lắng nghe LiveData từ Room Database
        quanSatDuLieu();
    }

    private void caiDatRecyclerView() {
        adapter = new GioHangAdapter(this, this);
        binding.rvDanhSachGioHang.setLayoutManager(new LinearLayoutManager(this));
        binding.rvDanhSachGioHang.setAdapter(adapter);
    }

    private void caiDatSuKien() {
        // Nút quay lại
        binding.btnBack.setOnClickListener(v -> finish());

        // Nút tiếp tục mua sắm khi giỏ hàng trống
        binding.btnTiepTucMuaSam.setOnClickListener(v -> finish());

        // Nút xóa tất cả sản phẩm khỏi giỏ hàng
        binding.btnXoaHet.setOnClickListener(v -> hienThiDialogXoaTatCa());

        // Nút Tiến hành đặt hàng (UC12)
        binding.btnDatHang.setOnClickListener(v -> {
            if (danhSachHienTai != null && !danhSachHienTai.isEmpty()) {
                Intent intent = new Intent(GioHangActivity.this, DatHangActivity.class);
                startActivity(intent);
            }
        });
    }

    private void quanSatDuLieu() {
        gioHangViewModel.getDanhSachGioHang().observe(this, danhSach -> {
            this.danhSachHienTai = danhSach;
            adapter.capNhatDanhSach(danhSach);

            boolean coHang = danhSach != null && !danhSach.isEmpty();

            // Hiển thị/Ẩn giao diện giỏ hàng trống
            binding.layoutGioHangTrong.setVisibility(coHang ? View.GONE : View.VISIBLE);
            binding.rvDanhSachGioHang.setVisibility(coHang ? View.VISIBLE : View.GONE);
            binding.layoutBottomBar.setVisibility(coHang ? View.VISIBLE : View.GONE);
            binding.btnXoaHet.setVisibility(coHang ? View.VISIBLE : View.GONE);

            // Tính toán tổng số tiền thanh toán
            if (coHang) {
                double tongTien = 0.0;
                for (GioHangItem item : danhSach) {
                    tongTien += (item.getGiaBan() * item.getSoLuong());
                }
                binding.tvTongTien.setText(formatter.format(tongTien) + " đ");
            }
        });
    }

    // --- Xử lý sự kiện từ GioHangAdapter ---

    @Override
    public void onTangSoLuong(GioHangItem item) {
        gioHangViewModel.capNhatSoLuong(item.getMaSanPham(), item.getSoLuong() + 1);
    }

    @Override
    public void onGiamSoLuong(GioHangItem item) {
        if (item.getSoLuong() > 1) {
            gioHangViewModel.capNhatSoLuong(item.getMaSanPham(), item.getSoLuong() - 1);
        } else {
            // Khi số lượng về 1 và bấm giảm -> Hỏi xác nhận xóa
            hienThiDialogXoaItem(item);
        }
    }

    @Override
    public void onXoaItem(GioHangItem item) {
        hienThiDialogXoaItem(item);
    }

    private void hienThiDialogXoaItem(GioHangItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa khỏi giỏ hàng")
                .setMessage("Bạn có chắc chắn muốn bỏ \"" + item.getTenSanPham() + "\" khỏi giỏ hàng?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    gioHangViewModel.xoaKhoiGio(item);
                    Toast.makeText(this, "Đã xóa sản phẩm", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void hienThiDialogXoaTatCa() {
        new AlertDialog.Builder(this)
                .setTitle("Làm trống giỏ hàng")
                .setMessage("Bạn có chắc chắn muốn xóa tất cả sản phẩm trong giỏ hàng?")
                .setPositiveButton("Xóa tất cả", (dialog, which) -> {
                    gioHangViewModel.xoaToanBoGioHang();
                    Toast.makeText(this, "Đã dọn sạch giỏ hàng", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
