package com.example.agriclean.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.agriclean.MainActivity;
import com.example.agriclean.data.model.NguoiDung;
import com.example.agriclean.databinding.ActivityDangKyBinding;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Màn hình Đăng ký tài khoản (DangKyActivity):
 * - Tạo tài khoản bằng Firebase Authentication.
 * - Lưu tự động thực thể NguoiDung vào Firestore Collection "users" với vai trò mặc định "customer".
 */
public class DangKyActivity extends AppCompatActivity {

    private ActivityDangKyBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDangKyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Quay lại màn hình Đăng nhập
        binding.tvChuyenDangNhap.setOnClickListener(v -> finish());

        // Bấm nút Đăng ký
        binding.btnDangKy.setOnClickListener(v -> xuLyDangKy());
    }

    private void xuLyDangKy() {
        String hoTen = binding.edtHoTen.getText().toString().trim();
        String email = binding.edtEmail.getText().toString().trim();
        String soDienThoai = binding.edtSoDienThoai.getText().toString().trim();
        String matKhau = binding.edtMatKhau.getText().toString().trim();

        if (hoTen.isEmpty()) {
            binding.edtHoTen.setError("Vui lòng nhập Họ và Tên");
            binding.edtHoTen.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            binding.edtEmail.setError("Vui lòng nhập Email");
            binding.edtEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.edtEmail.setError("Email không đúng định dạng");
            binding.edtEmail.requestFocus();
            return;
        }

        if (soDienThoai.isEmpty()) {
            binding.edtSoDienThoai.setError("Vui lòng nhập Số điện thoại");
            binding.edtSoDienThoai.requestFocus();
            return;
        }

        if (matKhau.isEmpty() || matKhau.length() < 6) {
            binding.edtMatKhau.setError("Mật khẩu phải từ 6 ký tự trở lên");
            binding.edtMatKhau.requestFocus();
            return;
        }

        binding.btnDangKy.setEnabled(false);

        // 1. Tạo tài khoản trên FirebaseAuth
        mAuth.createUserWithEmailAndPassword(email, matKhau)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // 2. Lưu thông tin người dùng vào Firestore
                            luuThongTinNguoiDung(user.getUid(), hoTen, email, soDienThoai);
                        }
                    } else {
                        binding.btnDangKy.setEnabled(true);
                        Toast.makeText(DangKyActivity.this, "Đăng ký thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void luuThongTinNguoiDung(String uid, String hoTen, String email, String soDienThoai) {
        NguoiDung nguoiDungMoi = new NguoiDung(
                uid,
                hoTen,
                email,
                soDienThoai,
                "customer", // Mặc định là khách hàng
                Timestamp.now()
        );

        db.collection("users").document(uid)
                .set(nguoiDungMoi)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(DangKyActivity.this, "Đăng ký tài khoản thành công!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(DangKyActivity.this, MainActivity.class));
                    finishAffinity(); // Đóng tất cả Activity trước đó
                })
                .addOnFailureListener(e -> {
                    binding.btnDangKy.setEnabled(true);
                    Toast.makeText(DangKyActivity.this, "Lỗi lưu thông tin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}

