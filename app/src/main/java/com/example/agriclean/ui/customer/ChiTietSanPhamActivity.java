package com.example.agriclean.ui.customer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.agriclean.R;
import com.example.agriclean.data.local.GioHangItem;
import com.example.agriclean.data.model.SanPham;
import com.example.agriclean.databinding.ActivityChiTietSanPhamBinding;
import com.example.agriclean.viewmodel.GioHangViewModel;
import com.example.agriclean.viewmodel.SanPhamViewModel;

/**
 * Màn hình Chi Tiết Nông Sản & Nguồn Gốc Nhà Vườn (UC07, UC08, UC09 - Bước 4):
 * - Hiển thị minh bạch thông tin nông sản, tiêu chuẩn chứng nhận và nhà vườn.
 * - Hỗ trợ cả 2 luồng: Nhận đối tượng SanPham (từ Danh sách) hoặc nhận productId (từ Quét QR UC08).
 * - Kích hoạt Google Maps chỉ đường đến nhà vườn bằng Implicit Intent (UC09).
 * - Tách nhật ký canh tác thành Timeline trực quan (UC08).
 * - Thêm sản phẩm vào Room Database giỏ hàng (UC10).
 */
public class ChiTietSanPhamActivity extends AppCompatActivity {

    public static final String EXTRA_SAN_PHAM = "extra_san_pham";
    public static final String EXTRA_PRODUCT_ID = "extra_product_id";

    private ActivityChiTietSanPhamBinding binding;
    private SanPhamViewModel sanPhamViewModel;
    private GioHangViewModel gioHangViewModel;

    private SanPham sanPhamHienTai;
    private int soLuongMua = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChiTietSanPhamBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 1. Khởi tạo các ViewModel
        sanPhamViewModel = new ViewModelProvider(this).get(SanPhamViewModel.class);
        gioHangViewModel = new ViewModelProvider(this).get(GioHangViewModel.class);

        // 2. Thiết lập các sự kiện nút bấm
        caiDatSuKien();

        // 3. Quan sát LiveData (khi truy vấn sản phẩm qua mã QR)
        quanSatDuLieu();

        // 4. Nạp dữ liệu sản phẩm
        napDuLieuSanPham();
    }

    private void caiDatSuKien() {
        // Nút quay lại
        binding.btnBack.setOnClickListener(v -> finish());

        // Nút xem giỏ hàng
        binding.btnGioHang.setOnClickListener(v -> {
            startActivity(new Intent(ChiTietSanPhamActivity.this, GioHangActivity.class));
        });

        // Giảm số lượng mua
        binding.btnGiam.setOnClickListener(v -> {
            if (soLuongMua > 1) {
                soLuongMua--;
                binding.tvSoLuong.setText(String.valueOf(soLuongMua));
            }
        });

        // Tăng số lượng mua
        binding.btnTang.setOnClickListener(v -> {
            if (sanPhamHienTai != null && soLuongMua < sanPhamHienTai.getTonKho()) {
                soLuongMua++;
                binding.tvSoLuong.setText(String.valueOf(soLuongMua));
            } else {
                Toast.makeText(this, "Đã đạt số lượng tồn kho tối đa", Toast.LENGTH_SHORT).show();
            }
        });

        // Chỉ đường đến trang trại bằng Implicit Intent Google Maps (UC09)
        binding.btnChiDuong.setOnClickListener(v -> moGoogleMapsChiDuong());

        // Thêm vào giỏ hàng Room Database (UC10)
        binding.btnThemVaoGio.setOnClickListener(v -> xuLyThemVaoGio());
    }

    private void quanSatDuLieu() {
        // Khi nạp sản phẩm từ mã QR Firestore
        sanPhamViewModel.getSanPhamChiTietLiveData().observe(this, sp -> {
            if (sp != null) {
                hienThiChiTiet(sp);
            }
        });

        sanPhamViewModel.getErrorMessageLiveData().observe(this, loi -> {
            if (loi != null && !loi.isEmpty()) {
                Toast.makeText(this, loi, Toast.LENGTH_SHORT).show();
            }
        });

        // Quan sát giỏ hàng để cập nhật Badge màu đỏ
        gioHangViewModel.getDanhSachGioHang().observe(this, danhSach -> {
            int tongSoLuong = 0;
            if (danhSach != null) {
                for (com.example.agriclean.data.local.GioHangItem item : danhSach) {
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

    /**
     * Nạp dữ liệu: Hỗ trợ nhận SanPham trực tiếp HOẶC nhận productId từ Quét QR
     */
    private void napDuLieuSanPham() {
        if (getIntent().hasExtra(EXTRA_SAN_PHAM)) {
            SanPham sp = (SanPham) getIntent().getSerializableExtra(EXTRA_SAN_PHAM);
            if (sp != null) {
                hienThiChiTiet(sp);
            }
        } else if (getIntent().hasExtra(EXTRA_PRODUCT_ID)) {
            String productId = getIntent().getStringExtra(EXTRA_PRODUCT_ID);
            if (productId != null && !productId.isEmpty()) {
                sanPhamViewModel.layChiTietSanPham(productId);
            }
        }
    }

    /**
     * Hiển thị toàn bộ thông tin nông sản lên giao diện
     */
    private void hienThiChiTiet(SanPham sp) {
        this.sanPhamHienTai = sp;

        // 1. Tải ảnh nông sản lớn với Glide (chuẩn NFR09)
        Glide.with(this)
                .load(sp.getHinhAnhUrl())
                .override(600, 400)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .into(binding.imgChiTiet);

        // 2. Thông tin cơ bản
        binding.tvTenSanPham.setText(sp.getTenSanPham());
        String donVi = (sp.getDonViTinh() != null && !sp.getDonViTinh().isEmpty()) ? sp.getDonViTinh() : "kg";
        binding.tvGia.setText(sp.getGiaDinhDang() + " / " + donVi);
        binding.tvTonKho.setText("• Còn " + sp.getTonKho() + " " + donVi);
        binding.tvTagDanhMuc.setText("#" + (sp.getDanhMuc() != null ? sp.getDanhMuc() : "RauCuSach"));

        String moTa = sp.getMoTa();
        binding.tvMoTa.setText((moTa != null && !moTa.isEmpty()) ? moTa : "Nông sản tươi sạch được chăm sóc theo quy chuẩn hữu cơ nghiêm ngặt.");

        String chungNhan = sp.getChungNhan();
        if (chungNhan != null && !chungNhan.isEmpty()) {
            binding.tvBadgeVietGAP.setText(chungNhan);
            binding.tvBadgeVietGAP.setVisibility(View.VISIBLE);
            binding.tvChungNhanChiTiet.setText("Chứng nhận: " + chungNhan);
        } else {
            binding.tvBadgeVietGAP.setVisibility(View.GONE);
            binding.tvChungNhanChiTiet.setText("Chứng nhận: Đang cập nhật");
        }

        // 3. Hồ sơ trang trại
        binding.tvTenTrangTrai.setText(sp.getTenTrangTrai() != null ? sp.getTenTrangTrai() : "Trang trại Đà Lạt Eco Farm");
        binding.tvChuTrangTrai.setText("Chủ trang trại: " + (sp.getChuTrangTrai() != null ? sp.getChuTrangTrai() : "Nhà vườn liên kết"));
        binding.tvDiaChiTrangTrai.setText("Địa chỉ: " + (sp.getDiaChiTrangTrai() != null ? sp.getDiaChiTrangTrai() : "Đà Lạt, Lâm Đồng"));

        // 4. Phân tách và hiển thị Timeline Nhật ký canh tác (UC08)
        taoTimelineCanhTac(sp.getQuyTrinhCanhTac());

        // 5. Kiểm tra trạng thái tồn kho
        if (sp.getTonKho() <= 0) {
            binding.btnThemVaoGio.setEnabled(false);
            binding.btnThemVaoGio.setText("HẾT HÀNG");
            binding.btnTang.setEnabled(false);
            binding.btnGiam.setEnabled(false);
        } else {
            binding.btnThemVaoGio.setEnabled(true);
            binding.btnThemVaoGio.setText("THÊM VÀO GIỎ HÀNG");
            binding.btnTang.setEnabled(true);
            binding.btnGiam.setEnabled(true);
        }
    }

    /**
     * Tự động phân tách chuỗi quy trình canh tác thành Timeline trực quan (UC08)
     */
    private void taoTimelineCanhTac(String quyTrinh) {
        binding.layoutTimeline.removeAllViews();

        if (quyTrinh == null || quyTrinh.trim().isEmpty()) {
            TextView tvRong = new TextView(this);
            tvRong.setText("Nhật ký canh tác đang được nhà vườn cập nhật.");
            tvRong.setTextColor(getColor(R.color.text_secondary));
            tvRong.setTextSize(13);
            binding.layoutTimeline.addView(tvRong);
            return;
        }

        String[] dongList = quyTrinh.split("\n");
        int count = 0;

        for (String dong : dongList) {
            String dongTrim = dong.trim();
            if (dongTrim.isEmpty()) continue;

            // Khởi tạo một hàng Timeline
            LinearLayout hangTimeline = new LinearLayout(this);
            hangTimeline.setOrientation(LinearLayout.HORIZONTAL);
            hangTimeline.setPadding(0, 8, 0, 8);
            hangTimeline.setGravity(Gravity.TOP);

            // Chấm tròn mốc sự kiện (xen kẽ xanh và cam)
            ImageView dot = new ImageView(this);
            LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(26, 26);
            dotParams.setMargins(4, 12, 16, 0);
            dot.setLayoutParams(dotParams);
            dot.setImageResource(count % 2 == 0 ? R.drawable.dot_timeline : R.drawable.dot_timeline_orange);
            hangTimeline.addView(dot);

            // Nội dung sự kiện
            LinearLayout layoutNoiDung = new LinearLayout(this);
            layoutNoiDung.setOrientation(LinearLayout.VERTICAL);

            if (dongTrim.contains(" - ")) {
                String[] phan = dongTrim.split(" - ", 2);
                TextView tvNgay = new TextView(this);
                tvNgay.setText(phan[0]);
                tvNgay.setTextColor(getColor(count % 2 == 0 ? R.color.primary_green : R.color.accent_orange));
                tvNgay.setTextSize(13);
                tvNgay.setTypeface(null, android.graphics.Typeface.BOLD);
                layoutNoiDung.addView(tvNgay);

                TextView tvSuKien = new TextView(this);
                tvSuKien.setText(phan[1]);
                tvSuKien.setTextColor(getColor(R.color.text_primary));
                tvSuKien.setTextSize(13);
                layoutNoiDung.addView(tvSuKien);
            } else {
                TextView tvSuKien = new TextView(this);
                tvSuKien.setText(dongTrim);
                tvSuKien.setTextColor(getColor(R.color.text_primary));
                tvSuKien.setTextSize(13);
                layoutNoiDung.addView(tvSuKien);
            }

            hangTimeline.addView(layoutNoiDung);
            binding.layoutTimeline.addView(hangTimeline);
            count++;
        }
    }

    /**
     * Kích hoạt Implicit Intent mở Google Maps chỉ đường đến nhà vườn (UC09)
     */
    private void moGoogleMapsChiDuong() {
        if (sanPhamHienTai == null || sanPhamHienTai.getDiaChiTrangTrai() == null) {
            Toast.makeText(this, "Chưa có địa chỉ nhà vườn", Toast.LENGTH_SHORT).show();
            return;
        }

        String diaChi = sanPhamHienTai.getDiaChiTrangTrai();
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(diaChi));
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        try {
            startActivity(mapIntent);
        } catch (Exception e) {
            // Nếu thiết bị không cài ứng dụng Google Maps, mở qua trình duyệt web
            Uri webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(diaChi));
            startActivity(new Intent(Intent.ACTION_VIEW, webUri));
        }
    }

    /**
     * Thêm sản phẩm vào giỏ hàng Room Database (UC10)
     */
    private void xuLyThemVaoGio() {
        if (sanPhamHienTai == null) return;

        GioHangItem item = new GioHangItem(
                sanPhamHienTai.getProductId(),
                sanPhamHienTai.getTenSanPham(),
                sanPhamHienTai.getGia(),
                soLuongMua,
                sanPhamHienTai.getHinhAnhUrl(),
                sanPhamHienTai.getTenTrangTrai()
        );

        gioHangViewModel.themVaoGio(item);
        Toast.makeText(this, "Đã thêm " + soLuongMua + " sản phẩm vào giỏ hàng!", Toast.LENGTH_SHORT).show();
    }
}

