package com.example.agriclean.ui.customer;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.agriclean.data.local.GioHangItem;
import com.example.agriclean.databinding.ActivityGioHangBinding;
import com.example.agriclean.utils.BanDoHelper;
import com.example.agriclean.utils.QuetQRHelper;
import com.example.agriclean.viewmodel.GioHangViewModel;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanIntentResult;
import com.journeyapps.barcodescanner.ScanOptions;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Lớp GioHangActivity: Màn hình giao diện Giỏ hàng nông sản.
 * - Áp dụng ViewBinding: ActivityGioHangBinding.
 * - Kiến trúc MVVM: Lắng nghe LiveData từ GioHangViewModel.
 * - Tích hợp Quét QR Camera (ZXing).
 * - Tích hợp mở bản đồ nông trại (Google Maps Intent).
 */
public class GioHangActivity extends AppCompatActivity {

    // ViewBinding liên kết trực tiếp với layout activity_gio_hang.xml
    private ActivityGioHangBinding binding;

    // ViewModel xử lý logic dữ liệu giỏ hàng
    private GioHangViewModel gioHangViewModel;

    // Trình đăng ký nhận kết quả sau khi quét mã QR
    private final ActivityResultLauncher<ScanOptions> trinhQuetQR =
            registerForActivityResult(new ScanContract(), (ScanIntentResult result) -> {
                if (result.getContents() != null) {
                    String maQuetDuoc = result.getContents();
                    Toast.makeText(this, "Quét thành công! Mã nông sản: " + maQuetDuoc, Toast.LENGTH_LONG).show();
                    // Tại đây có thể truy vấn Firestore để hiển thị nguồn gốc nông sản
                } else {
                    Toast.makeText(this, "Đã hủy quét mã QR", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Khởi tạo ViewBinding
        binding = ActivityGioHangBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 2. Khởi tạo ViewModel qua ViewModelProvider
        gioHangViewModel = new ViewModelProvider(this).get(GioHangViewModel.class);

        // 3. Cài đặt các sự kiện bấm nút
        caiDatGiaoDien();

        // 4. Lắng nghe thay đổi dữ liệu từ ViewModel
        langNgheDuLieu();
    }

    /**
     * Cài đặt các sự kiện tương tác của người dùng trên giao diện
     */
    private void caiDatGiaoDien() {
        // Nút Xóa tất cả sản phẩm khỏi giỏ
        binding.btnXoaHet.setOnClickListener(v -> {
            gioHangViewModel.xoaToanBoGioHang();
            Toast.makeText(this, "Đã dọn sạch giỏ hàng", Toast.LENGTH_SHORT).show();
        });

        // Nút Mở camera quét mã QR kiểm tra nguồn gốc
        binding.btnQuetQR.setOnClickListener(v -> {
            QuetQRHelper.batDauQuetQR(trinhQuetQR, "Hướng camera vào mã QR trên bao bì nông sản");
        });

        // Nút Mở Google Maps xem vị trí nông trại sản xuất
        binding.btnXemTrangTrai.setOnClickListener(v -> {
            // Tọa độ demo nông trại (Đà Lạt: 11.9404, 108.4583)
            BanDoHelper.moViTriTrangTrai(
                    this,
                    11.9404,
                    108.4583,
                    "Hợp tác xã Nông sản Sạch Đà Lạt"
            );
        });

        // Nút Đặt hàng
        binding.btnDatHang.setOnClickListener(v -> {
            Toast.makeText(this, "Chuyển sang màn hình xác nhận đơn hàng...", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Lắng nghe LiveData từ ViewModel để cập nhật giao diện tự động khi dữ liệu thay đổi
     */
    private void langNgheDuLieu() {
        gioHangViewModel.getDanhSachGioHang().observe(this, danhSach -> {
            double tongTien = 0.0;
            if (danhSach != null) {
                for (GioHangItem item : danhSach) {
                    tongTien += item.getGiaBan() * item.getSoLuong();
                }
            }

            // Định dạng tiền tệ theo chuẩn Việt Nam (VND)
            NumberFormat dinhDangTien = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            binding.tvTongTien.setText("Tổng: " + dinhDangTien.format(tongTien));

            // Kích hoạt hoặc vô hiệu hóa nút Đặt hàng nếu giỏ hàng trống
            binding.btnDatHang.setEnabled(danhSach != null && !danhSach.isEmpty());
        });
    }
}

