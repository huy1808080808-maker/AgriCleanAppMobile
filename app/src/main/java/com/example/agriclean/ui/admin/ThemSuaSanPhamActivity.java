package com.example.agriclean.ui.admin;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.agriclean.data.model.SanPham;
import com.example.agriclean.databinding.ActivityThemSuaSanPhamBinding;
import com.example.agriclean.viewmodel.SanPhamViewModel;

/**
 * Màn hình Biểu Mẫu Thêm & Chỉnh Sửa Nông Sản (UC14 - Bước 2):
 * - Chuẩn hóa 100% kiến trúc MVVM: Không gọi Firebase Storage trong Activity,
 *   toàn bộ xử lý tải ảnh và Firestore do SanPhamViewModel / SanPhamRepository điều phối.
 * - Danh mục và Chứng nhận dạng Dropdown chuẩn hóa tuyệt đối.
 * - Hồ sơ trang trại và nhật ký canh tác phục vụ trực tiếp cho UC08 (Quét QR) và UC09 (Bản đồ).
 */
public class ThemSuaSanPhamActivity extends AppCompatActivity {

    public static final String EXTRA_SAN_PHAM = "extra_san_pham";

    private ActivityThemSuaSanPhamBinding binding;
    private SanPhamViewModel viewModel;

    private boolean isUpdate = false;
    private SanPham sanPhamHienTai;
    private Uri uriAnhChon;

    // Trình chọn ảnh từ thư viện thiết bị
    private final ActivityResultLauncher<String> chonAnhLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    uriAnhChon = uri;
                    Glide.with(this)
                            .load(uri)
                            .centerCrop()
                            .into(binding.imgPreview);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityThemSuaSanPhamBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 1. Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(SanPhamViewModel.class);

        // 2. Thiết lập Dropdown Danh mục và Chứng nhận
        caiDatDropdown();

        // 3. Kiểm tra chế độ Thêm mới hay Chỉnh sửa
        kiemTraCheDo();

        // 4. Cài đặt các sự kiện UI
        caiDatSuKien();

        // 5. Quan sát LiveData từ ViewModel
        quanSatDuLieu();
    }

    /**
     * Cài đặt các lựa chọn cố định cho Dropdown (Khớp 100% với báo cáo)
     */
    private void caiDatDropdown() {
        // Danh mục nông sản (UC06)
        String[] danhMucList = {
                SanPham.DANH_MUC_RAU_XANH,
                SanPham.DANH_MUC_CU_QUA,
                SanPham.DANH_MUC_TRAI_CAY,
                SanPham.DANH_MUC_NAM
        };
        ArrayAdapter<String> adapterDanhMuc = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, danhMucList
        );
        binding.actvDanhMuc.setAdapter(adapterDanhMuc);
        binding.actvDanhMuc.setText(SanPham.DANH_MUC_RAU_XANH, false);

        // Chứng nhận nông nghiệp sạch
        String[] chungNhanList = {
                SanPham.CHUNG_NHAN_VIETGAP,
                SanPham.CHUNG_NHAN_HUU_CO
        };
        ArrayAdapter<String> adapterChungNhan = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, chungNhanList
        );
        binding.actvChungNhan.setAdapter(adapterChungNhan);
        binding.actvChungNhan.setText(SanPham.CHUNG_NHAN_VIETGAP, false);
    }

    /**
     * Kiểm tra Intent: Nếu có SanPham truyền sang -> Chế độ Chỉnh sửa (B4)
     */
    private void kiemTraCheDo() {
        if (getIntent().hasExtra(EXTRA_SAN_PHAM)) {
            isUpdate = true;
            sanPhamHienTai = (SanPham) getIntent().getSerializableExtra(EXTRA_SAN_PHAM);

            if (sanPhamHienTai != null) {
                binding.tvTitleForm.setText("Chỉnh Sửa Nông Sản (UC14)");
                binding.btnLuuSanPham.setText("CẬP NHẬT NÔNG SẢN");

                // Điền dữ liệu cũ vào các ô nhập
                binding.edtTenSanPham.setText(sanPhamHienTai.getTenSanPham());
                binding.actvDanhMuc.setText(sanPhamHienTai.getDanhMuc(), false);
                binding.edtGia.setText(String.valueOf(sanPhamHienTai.getGia()));
                binding.edtDonViTinh.setText(sanPhamHienTai.getDonViTinh());
                binding.edtTonKho.setText(String.valueOf(sanPhamHienTai.getTonKho()));
                binding.edtMoTa.setText(sanPhamHienTai.getMoTa());

                binding.edtTenTrangTrai.setText(sanPhamHienTai.getTenTrangTrai());
                binding.edtChuTrangTrai.setText(sanPhamHienTai.getChuTrangTrai());
                binding.edtDiaChiTrangTrai.setText(sanPhamHienTai.getDiaChiTrangTrai());
                binding.actvChungNhan.setText(sanPhamHienTai.getChungNhan(), false);
                binding.edtQuyTrinhCanhTac.setText(sanPhamHienTai.getQuyTrinhCanhTac());

                // Hiển thị ảnh hiện tại
                if (sanPhamHienTai.getHinhAnhUrl() != null && !sanPhamHienTai.getHinhAnhUrl().isEmpty()) {
                    Glide.with(this)
                            .load(sanPhamHienTai.getHinhAnhUrl())
                            .centerCrop()
                            .into(binding.imgPreview);
                }
            }
        } else {
            // Mặc định gợi ý mẫu nhật ký canh tác cho Admin
            binding.edtQuyTrinhCanhTac.setText(
                    "10/01/2026 - Gieo hạt giống chuẩn F1 tại nhà kính Đà Lạt\n" +
                    "25/01/2026 - Chăm sóc hữu cơ và bón phân sinh học trùn quế\n" +
                    "20/02/2026 - Kiểm định chất lượng đạt tiêu chuẩn VietGAP\n" +
                    "28/02/2026 - Thu hoạch sáng sớm và đóng gói bảo quản mát"
            );
        }
    }

    private void caiDatSuKien() {
        // Nút quay lại
        binding.btnBack.setOnClickListener(v -> finish());

        // Nút chọn ảnh từ thư viện
        binding.btnChonAnh.setOnClickListener(v -> chonAnhLauncher.launch("image/*"));

        // Nút Lưu nông sản
        binding.btnLuuSanPham.setOnClickListener(v -> xuLyLuuSanPham());
    }

    /**
     * Thu thập dữ liệu từ giao diện và chuyển cho ViewModel xử lý (Chuẩn MVVM)
     */
    private void xuLyLuuSanPham() {
        String ten = binding.edtTenSanPham.getText() != null ?
                binding.edtTenSanPham.getText().toString().trim() : "";
        String danhMuc = binding.actvDanhMuc.getText().toString().trim();
        String giaStr = binding.edtGia.getText() != null ?
                binding.edtGia.getText().toString().trim() : "0";
        String donVi = binding.edtDonViTinh.getText() != null ?
                binding.edtDonViTinh.getText().toString().trim() : "kg";
        String tonKhoStr = binding.edtTonKho.getText() != null ?
                binding.edtTonKho.getText().toString().trim() : "0";
        String moTa = binding.edtMoTa.getText() != null ?
                binding.edtMoTa.getText().toString().trim() : "";

        String tenTrangTrai = binding.edtTenTrangTrai.getText() != null ?
                binding.edtTenTrangTrai.getText().toString().trim() : "";
        String chuTrangTrai = binding.edtChuTrangTrai.getText() != null ?
                binding.edtChuTrangTrai.getText().toString().trim() : "";
        String diaChiTrangTrai = binding.edtDiaChiTrangTrai.getText() != null ?
                binding.edtDiaChiTrangTrai.getText().toString().trim() : "";
        String chungNhan = binding.actvChungNhan.getText().toString().trim();
        String quyTrinh = binding.edtQuyTrinhCanhTac.getText() != null ?
                binding.edtQuyTrinhCanhTac.getText().toString().trim() : "";

        double gia = 0;
        try {
            gia = Double.parseDouble(giaStr);
        } catch (Exception ignored) {}

        int tonKho = 0;
        try {
            tonKho = Integer.parseInt(tonKhoStr);
        } catch (Exception ignored) {}

        // Tạo hoặc cập nhật đối tượng SanPham
        SanPham sp = isUpdate && sanPhamHienTai != null ? sanPhamHienTai : new SanPham();
        sp.setTenSanPham(ten);
        sp.setDanhMuc(danhMuc);
        sp.setGia(gia);
        sp.setDonViTinh(donVi);
        sp.setTonKho(tonKho);
        sp.setMoTa(moTa);
        sp.setTenTrangTrai(tenTrangTrai);
        sp.setChuTrangTrai(chuTrangTrai);
        sp.setDiaChiTrangTrai(diaChiTrangTrai);
        sp.setChungNhan(chungNhan);
        sp.setQuyTrinhCanhTac(quyTrinh);

        // Gọi ViewModel xử lý (Toàn bộ logic Upload Storage + Firestore nằm ở tầng Model)
        viewModel.luuSanPham(sp, uriAnhChon, isUpdate);
    }

    private void quanSatDuLieu() {
        // Quan sát kết quả thành công
        viewModel.getActionSuccessLiveData().observe(this, thanhCong -> {
            if (Boolean.TRUE.equals(thanhCong)) {
                String thongBao = isUpdate ? "Cập nhật nông sản thành công!" : "Thêm mới nông sản thành công!";
                Toast.makeText(this, thongBao, Toast.LENGTH_SHORT).show();
                finish(); // Đóng form để quay về danh sách Realtime
            }
        });

        // Quan sát trạng thái xử lý
        viewModel.getLoadingLiveData().observe(this, dangXuLy -> {
            boolean isLoading = Boolean.TRUE.equals(dangXuLy);
            binding.btnLuuSanPham.setEnabled(!isLoading);
            binding.btnLuuSanPham.setText(isLoading ? "ĐANG TẢI LÊN HỆ THỐNG..." : (isUpdate ? "CẬP NHẬT NÔNG SẢN" : "LƯU NÔNG SẢN"));
            binding.pbDangLuu.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        // Quan sát lỗi
        viewModel.getErrorMessageLiveData().observe(this, loi -> {
            if (loi != null && !loi.isEmpty()) {
                Toast.makeText(this, loi, Toast.LENGTH_SHORT).show();
            }
        });
    }
}

