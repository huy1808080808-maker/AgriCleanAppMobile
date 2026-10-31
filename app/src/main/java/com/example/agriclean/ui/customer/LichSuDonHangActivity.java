package com.example.agriclean.ui.customer;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.agriclean.databinding.ActivityLichSuDonHangBinding;
import com.example.agriclean.viewmodel.DonHangViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Activity Màn hình Lịch sử Đơn hàng Khách hàng (UC13).
 * Lắng nghe Realtime trạng thái các đơn hàng từ collection "Orders",
 * hiển thị badge trực quan, hỗ trợ làm mới và giao diện trống (Empty state).
 */
public class LichSuDonHangActivity extends AppCompatActivity {

    private ActivityLichSuDonHangBinding binding;
    private DonHangViewModel donHangViewModel;
    private DonHangCustomerAdapter adapter;
    private String currentUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLichSuDonHangBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        donHangViewModel = new ViewModelProvider(this).get(DonHangViewModel.class);

        caiDatRecyclerView();
        caiDatSuKien();
        quanSatDuLieu();

        // Lấy thông tin khách hàng hiện tại
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUid = currentUser.getUid();
            donHangViewModel.batDauLangNgheKhachHang(currentUid);
        } else {
            Toast.makeText(this, "Vui lòng đăng nhập để xem lịch sử đơn hàng!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void caiDatRecyclerView() {
        adapter = new DonHangCustomerAdapter();
        binding.rvLichSuDonHang.setLayoutManager(new LinearLayoutManager(this));
        binding.rvLichSuDonHang.setAdapter(adapter);
    }

    private void caiDatSuKien() {
        // Nút quay lại
        binding.btnBack.setOnClickListener(v -> finish());

        // Nút khám phá nông sản khi giỏ trống
        binding.btnKhamPhaNgay.setOnClickListener(v -> finish());

        // Nút làm mới thủ công
        binding.btnLamMoi.setOnClickListener(v -> {
            if (currentUid != null) {
                Toast.makeText(this, "Đang đồng bộ đơn hàng mới nhất...", Toast.LENGTH_SHORT).show();
                donHangViewModel.batDauLangNgheKhachHang(currentUid);
            }
        });
    }

    private void quanSatDuLieu() {
        // Lắng nghe danh sách đơn hàng Realtime
        donHangViewModel.getDanhSachDonHang().observe(this, danhSach -> {
            adapter.capNhatDanhSach(danhSach);

            boolean coDon = danhSach != null && !danhSach.isEmpty();
            binding.rvLichSuDonHang.setVisibility(coDon ? View.VISIBLE : View.GONE);
            binding.layoutTrong.setVisibility(coDon ? View.GONE : View.VISIBLE);
        });

        // Lắng nghe trạng thái loading
        donHangViewModel.getLoading().observe(this, isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        // Lắng nghe thông báo lỗi
        donHangViewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
