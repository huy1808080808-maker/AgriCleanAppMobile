package com.example.agriclean.ui.customer;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.agriclean.MainActivity;
import com.example.agriclean.data.local.GioHangItem;
import com.example.agriclean.data.model.ChiTietDonHang;
import com.example.agriclean.data.model.DonHang;
import com.example.agriclean.data.model.NguoiDung;
import com.example.agriclean.data.repository.AuthRepository;
import com.example.agriclean.databinding.ActivityDatHangBinding;
import com.example.agriclean.viewmodel.DonHangViewModel;
import com.example.agriclean.viewmodel.GioHangViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity xử lý Màn hình Đặt hàng & Thanh toán (Checkout - UC12).
 * Tự động nạp thông tin người nhận từ Firestore, xác thực số điện thoại 10 chữ số,
 * tóm tắt các món hàng trong giỏ, tạo đơn với trạng thái "Chờ duyệt",
 * và dọn sạch giỏ hàng Room Database sau khi đặt hàng thành công.
 */
public class DatHangActivity extends AppCompatActivity {

    private ActivityDatHangBinding binding;
    private DonHangViewModel donHangViewModel;
    private GioHangViewModel gioHangViewModel;
    private TomTatDonHangAdapter tomTatAdapter;

    private final DecimalFormat formatter = new DecimalFormat("#,###");
    private List<GioHangItem> danhSachHienTai = new ArrayList<>();
    private double tongTienDonHang = 0.0;
    private boolean daXacNhanDatHang = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDatHangBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        khoiTaoViewModels();
        khoiTaoRecyclerView();
        napThongTinKhachHang();
        caiDatSuKien();
        quanSatDuLieu();
    }

    private void khoiTaoViewModels() {
        donHangViewModel = new ViewModelProvider(this).get(DonHangViewModel.class);
        gioHangViewModel = new ViewModelProvider(this).get(GioHangViewModel.class);
    }

    private void khoiTaoRecyclerView() {
        tomTatAdapter = new TomTatDonHangAdapter();
        binding.rvTomTatSanPham.setLayoutManager(new LinearLayoutManager(this));
        binding.rvTomTatSanPham.setAdapter(tomTatAdapter);
    }

    /**
     * Tự động nạp thông tin Họ tên, SĐT, Địa chỉ của tài khoản hiện tại từ Firestore
     */
    private void napThongTinKhachHang() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            FirebaseFirestore.getInstance()
                    .collection(AuthRepository.COLLECTION_USERS)
                    .document(currentUser.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            NguoiDung nguoiDung = documentSnapshot.toObject(NguoiDung.class);
                            if (nguoiDung != null) {
                                if (nguoiDung.getHoTen() != null && !nguoiDung.getHoTen().isEmpty()) {
                                    binding.edtHoTen.setText(nguoiDung.getHoTen());
                                }
                                if (nguoiDung.getSdt() != null && !nguoiDung.getSdt().isEmpty()) {
                                    binding.edtSoDienThoai.setText(nguoiDung.getSdt());
                                }
                                if (nguoiDung.getDiaChi() != null && !nguoiDung.getDiaChi().isEmpty()) {
                                    binding.edtDiaChi.setText(nguoiDung.getDiaChi());
                                }
                            }
                        }
                    });
        }
    }

    private void caiDatSuKien() {
        // Nút quay lại
        binding.btnBack.setOnClickListener(v -> finish());

        // Thay đổi phương thức thanh toán
        binding.rgPhuongThucThanhToan.setOnCheckedChangeListener((group, checkedId) -> {
            boolean isChuyenKhoan = (checkedId == binding.rbChuyenKhoan.getId());
            binding.layoutThongTinChuyenKhoan.setVisibility(isChuyenKhoan ? View.VISIBLE : View.GONE);
        });

        // Nút xác nhận đặt hàng
        binding.btnXacNhanDatHang.setOnClickListener(v -> xuLyXacNhanDatHang());
    }

    private void quanSatDuLieu() {
        // Lắng nghe dữ liệu sản phẩm trong giỏ hàng
        gioHangViewModel.getDanhSachGioHang().observe(this, danhSach -> {
            if (daXacNhanDatHang) return; // Nếu vừa đặt hàng thành công và đang xóa giỏ thì bỏ qua

            if (danhSach != null && !danhSach.isEmpty()) {
                this.danhSachHienTai = danhSach;
                tomTatAdapter.capNhatDanhSach(danhSach);

                double tong = 0.0;
                for (GioHangItem item : danhSach) {
                    tong += (item.getGiaBan() * item.getSoLuong());
                }
                this.tongTienDonHang = tong;

                String tongTienStr = formatter.format(tong) + " đ";
                binding.tvTamTinh.setText(tongTienStr);
                binding.tvTongTien.setText(tongTienStr);
            }
        });

        // Lắng nghe trạng thái loading
        donHangViewModel.getLoading().observe(this, isLoading -> {
            binding.btnXacNhanDatHang.setEnabled(!isLoading);
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.btnXacNhanDatHang.setText(isLoading ? "" : "Xác nhận đặt hàng");
        });

        // Lắng nghe lỗi đặt hàng
        donHangViewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });

        // Lắng nghe khi đặt hàng thành công
        donHangViewModel.getTaoDonThanhCong().observe(this, maDonHang -> {
            if (maDonHang != null) {
                daXacNhanDatHang = true;

                // 1. Dọn sạch giỏ hàng Room Database (NFR04 & UC12)
                gioHangViewModel.xoaToanBoGioHang();

                // 2. Hiển thị Dialog thông báo thành công
                hienThiDialogThanhCong(maDonHang);
            }
        });
    }

    /**
     * Kiểm tra hợp lệ dữ liệu và tiến hành đặt hàng
     */
    private void xuLyXacNhanDatHang() {
        String hoTen = binding.edtHoTen.getText() != null ? binding.edtHoTen.getText().toString().trim() : "";
        String sdt = binding.edtSoDienThoai.getText() != null ? binding.edtSoDienThoai.getText().toString().trim() : "";
        String diaChi = binding.edtDiaChi.getText() != null ? binding.edtDiaChi.getText().toString().trim() : "";
        String ghiChu = binding.edtGhiChu.getText() != null ? binding.edtGhiChu.getText().toString().trim() : "";

        // Reset các lỗi hiển thị trước đó
        binding.tilHoTen.setError(null);
        binding.tilSoDienThoai.setError(null);
        binding.tilDiaChi.setError(null);

        // 1. Kiểm tra Họ và tên
        if (TextUtils.isEmpty(hoTen)) {
            binding.tilHoTen.setError("Vui lòng nhập họ và tên người nhận");
            binding.edtHoTen.requestFocus();
            return;
        }

        // 2. Kiểm tra Số điện thoại: Bắt buộc đúng 10 số, bắt đầu bằng số 0 (UC12)
        if (TextUtils.isEmpty(sdt)) {
            binding.tilSoDienThoai.setError("Vui lòng nhập số điện thoại nhận hàng");
            binding.edtSoDienThoai.requestFocus();
            return;
        }
        if (!sdt.matches("^0[0-9]{9}$")) {
            binding.tilSoDienThoai.setError("Số điện thoại không hợp lệ (Bắt buộc gồm 10 chữ số và bắt đầu bằng số 0)");
            binding.edtSoDienThoai.requestFocus();
            return;
        }

        // 3. Kiểm tra Địa chỉ giao hàng
        if (TextUtils.isEmpty(diaChi)) {
            binding.tilDiaChi.setError("Vui lòng nhập địa chỉ nhận hàng chi tiết");
            binding.edtDiaChi.requestFocus();
            return;
        }

        // 4. Kiểm tra danh sách giỏ hàng
        if (danhSachHienTai == null || danhSachHienTai.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng đang trống, không thể đặt hàng!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 5. Chuẩn bị đối tượng Đơn hàng
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String maKhachHang = currentUser != null ? currentUser.getUid() : "guest";

        // Tự sinh mã đơn hàng (Định dạng: DH + timestamp)
        String maDonHang = "DH" + System.currentTimeMillis();

        // Chuyển danh sách giỏ hàng sang danh sách ChiTietDonHang
        List<ChiTietDonHang> danhSachChiTiet = new ArrayList<>();
        for (GioHangItem item : danhSachHienTai) {
            danhSachChiTiet.add(new ChiTietDonHang(
                    maDonHang,
                    item.getMaSanPham(),
                    item.getTenSanPham(),
                    item.getGiaBan(),
                    item.getSoLuong()
            ));
        }

        String phuongThucThanhToan = binding.rbChuyenKhoan.isChecked()
                ? "Chuyển khoản ngân hàng / Quét QR"
                : "Thanh toán khi nhận hàng (COD)";

        DonHang donHangMoi = new DonHang(
                maDonHang,
                maKhachHang,
                hoTen,
                sdt,
                diaChi,
                tongTienDonHang,
                "Chờ duyệt", // Khởi tạo bắt buộc trạng thái "Chờ duyệt"
                Timestamp.now(),
                danhSachChiTiet,
                phuongThucThanhToan,
                ghiChu
        );

        // 6. Gửi đơn hàng lên Firestore thông qua ViewModel
        donHangViewModel.datHang(donHangMoi);
    }

    /**
     * Hiển thị thông báo đặt hàng thành công và điều hướng về màn hình chính
     */
    private void hienThiDialogThanhCong(String maDonHang) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("🎉 Đặt hàng thành công!")
                .setMessage("Mã đơn hàng: #" + maDonHang + "\n\nCảm ơn bạn đã lựa chọn nông sản sạch AgriClean. Đơn hàng đang được nhà vườn tiếp nhận và chuẩn bị.")
                .setCancelable(false)
                .setPositiveButton("Xem đơn hàng", (dialog, which) -> {
                    Intent intent = new Intent(DatHangActivity.this, LichSuDonHangActivity.class);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Về trang chủ", (dialog, which) -> {
                    Intent intent = new Intent(DatHangActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                })
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}

