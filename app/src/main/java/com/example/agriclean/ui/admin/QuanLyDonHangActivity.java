package com.example.agriclean.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.agriclean.R;
import com.example.agriclean.data.model.DonHang;
import com.example.agriclean.data.model.NguoiDung;
import com.example.agriclean.data.repository.AuthRepository;
import com.example.agriclean.databinding.ActivityQuanLyDonHangBinding;
import com.example.agriclean.ui.auth.DangNhapActivity;
import com.example.agriclean.viewmodel.DonHangViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity Màn hình Quản lý & Duyệt Đơn hàng dành cho Admin (UC15, UC16).
 * - Kiểm soát bảo mật phân quyền nghiêm ngặt (NFR06).
 * - Lắng nghe Realtime toàn bộ đơn hàng trong hệ thống (NFR03).
 * - Lọc danh sách theo trạng thái bằng ChipGroup.
 * - Hộp thoại cập nhật trạng thái đơn hàng (Duyệt, Đang giao, Hoàn thành, Huỷ).
 */
public class QuanLyDonHangActivity extends AppCompatActivity implements DonHangAdminAdapter.OnDonHangAdminListener {

    private ActivityQuanLyDonHangBinding binding;
    private DonHangViewModel donHangViewModel;
    private DonHangAdminAdapter adapter;

    private final List<DonHang> danhSachGoc = new ArrayList<>();
    private String trangThaiLoc = "Tất cả";
    private int selectedStatusIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQuanLyDonHangBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 1. Kiểm tra phân quyền bảo mật Admin (NFR06)
        if (!kiemTraQuyenAdmin()) {
            return;
        }

        donHangViewModel = new ViewModelProvider(this).get(DonHangViewModel.class);

        caiDatRecyclerView();
        caiDatSuKien();
        quanSatDuLieu();

        // 2. Bắt đầu lắng nghe Realtime danh sách đơn hàng (NFR03)
        donHangViewModel.batDauLangNgheAdmin();
    }

    /**
     * Xác thực quyền Admin bảo mật theo NFR06
     */
    private boolean kiemTraQuyenAdmin() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            chanTruyCap();
            return false;
        }

        FirebaseFirestore.getInstance()
                .collection(AuthRepository.COLLECTION_USERS)
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        NguoiDung user = doc.toObject(NguoiDung.class);
                        if (user == null || !user.isAdmin()) {
                            chanTruyCap();
                        }
                    } else {
                        chanTruyCap();
                    }
                })
                .addOnFailureListener(e -> chanTruyCap());

        return true;
    }

    private void chanTruyCap() {
        Toast.makeText(this, "Bạn không có quyền truy cập khu vực Quản trị!", Toast.LENGTH_LONG).show();
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, DangNhapActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void caiDatRecyclerView() {
        adapter = new DonHangAdminAdapter(this);
        binding.rvDonHangAdmin.setLayoutManager(new LinearLayoutManager(this));
        binding.rvDonHangAdmin.setAdapter(adapter);
    }

    private void caiDatSuKien() {
        // Nút quay lại
        binding.btnBack.setOnClickListener(v -> finish());

        // Nút đồng bộ thủ công
        binding.btnDongBo.setOnClickListener(v -> {
            Toast.makeText(this, "Đang đồng bộ đơn hàng mới nhất...", Toast.LENGTH_SHORT).show();
            donHangViewModel.batDauLangNgheAdmin();
        });

        // Lắng nghe sự kiện chọn Chip lọc trạng thái
        binding.chipGroupTrangThai.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == binding.chipChoDuyet.getId()) {
                trangThaiLoc = "Chờ duyệt";
            } else if (checkedId == binding.chipDaXacNhan.getId()) {
                trangThaiLoc = "Đã xác nhận";
            } else if (checkedId == binding.chipDangGiao.getId()) {
                trangThaiLoc = "Đang giao";
            } else if (checkedId == binding.chipHoanThanh.getId()) {
                trangThaiLoc = "Hoàn thành";
            } else if (checkedId == binding.chipDaHuy.getId()) {
                trangThaiLoc = "Đã huỷ";
            } else {
                trangThaiLoc = "Tất cả";
            }
            locDanhSachTheoTrangThai();
        });
    }

    private void quanSatDuLieu() {
        // Lắng nghe danh sách đơn hàng Realtime
        donHangViewModel.getDanhSachDonHang().observe(this, danhSach -> {
            danhSachGoc.clear();
            if (danhSach != null) {
                danhSachGoc.addAll(danhSach);
            }
            locDanhSachTheoTrangThai();
        });

        // Lắng nghe trạng thái loading
        donHangViewModel.getLoading().observe(this, isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        // Lắng nghe thông báo lỗi
        donHangViewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                donHangViewModel.resetTrangThai();
            }
        });

        // Lắng nghe khi cập nhật trạng thái đơn thành công
        donHangViewModel.getCapNhatThanhCong().observe(this, thanhCong -> {
            if (Boolean.TRUE.equals(thanhCong)) {
                Toast.makeText(this, "Cập nhật trạng thái đơn hàng thành công!", Toast.LENGTH_SHORT).show();
                donHangViewModel.resetTrangThai();
            }
        });
    }

    /**
     * Lọc danh sách theo Chip trạng thái được chọn
     */
    private void locDanhSachTheoTrangThai() {
        List<DonHang> danhSachLoc = new ArrayList<>();

        if ("Tất cả".equalsIgnoreCase(trangThaiLoc)) {
            danhSachLoc.addAll(danhSachGoc);
        } else {
            for (DonHang don : danhSachGoc) {
                String tt = don.getTrangThaiDon();
                if (tt != null) {
                    if ("Đã huỷ".equalsIgnoreCase(trangThaiLoc) && ("Đã huỷ".equalsIgnoreCase(tt) || "Đã hủy".equalsIgnoreCase(tt))) {
                        danhSachLoc.add(don);
                    } else if (tt.equalsIgnoreCase(trangThaiLoc)) {
                        danhSachLoc.add(don);
                    }
                }
            }
        }

        adapter.capNhatDanhSach(danhSachLoc);

        boolean coDon = !danhSachLoc.isEmpty();
        binding.rvDonHangAdmin.setVisibility(coDon ? View.VISIBLE : View.GONE);
        binding.layoutTrong.setVisibility(coDon ? View.GONE : View.VISIBLE);
        if (!coDon) {
            binding.tvThongBaoTrong.setText("Không có đơn hàng \"" + trangThaiLoc + "\"");
        }
    }

    /**
     * Hộp thoại cập nhật trạng thái đơn hàng (UC16)
     */
    @Override
    public void onCapNhatTrangThai(DonHang donHang) {
        if (donHang == null || donHang.getMaDonHang() == null) return;

        final String[] danhSachTrangThai = {
                "Chờ duyệt",
                "Đã xác nhận",
                "Đang giao",
                "Hoàn thành",
                "Đã huỷ"
        };

        // Xác định vị trí mặc định đang chọn
        selectedStatusIndex = 0;
        String trangThaiHienTai = donHang.getTrangThaiDon() != null ? donHang.getTrangThaiDon() : "Chờ duyệt";
        for (int i = 0; i < danhSachTrangThai.length; i++) {
            if (danhSachTrangThai[i].equalsIgnoreCase(trangThaiHienTai)
                    || ("Đã huỷ".equalsIgnoreCase(danhSachTrangThai[i]) && "Đã hủy".equalsIgnoreCase(trangThaiHienTai))) {
                selectedStatusIndex = i;
                break;
            }
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Cập nhật trạng thái #" + donHang.getMaDonHang())
                .setSingleChoiceItems(danhSachTrangThai, selectedStatusIndex, (dialog, which) -> {
                    selectedStatusIndex = which;
                })
                .setPositiveButton("Cập nhật", (dialog, which) -> {
                    String trangThaiMoi = danhSachTrangThai[selectedStatusIndex];
                    donHangViewModel.capNhatTrangThai(donHang.getMaDonHang(), trangThaiMoi);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}

