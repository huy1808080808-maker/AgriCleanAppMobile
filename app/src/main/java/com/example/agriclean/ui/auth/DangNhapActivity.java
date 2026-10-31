package com.example.agriclean.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.agriclean.MainActivity;
import com.example.agriclean.databinding.ActivityDangNhapBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Màn hình Đăng nhập (DangNhapActivity):
 * Xây dựng theo mẫu chuẩn thầy dạy (giống PetCareAppMobile).
 * - Xác thực Email & Mật khẩu bằng FirebaseAuth.
 * - Truy vấn Firestore kiểm tra vai trò (Role: "admin" hoặc "customer").
 * - Điều hướng đúng màn hình theo quyền hạn.
 */
public class DangNhapActivity extends AppCompatActivity {

    private ActivityDangNhapBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDangNhapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Chuyển sang màn hình Đăng ký
        binding.tvChuyenDangKy.setOnClickListener(v -> {
            startActivity(new Intent(DangNhapActivity.this, DangKyActivity.class));
        });

        // Nút Đăng nhập
        binding.btnDangNhap.setOnClickListener(v -> xuLyDangNhap());

        // Nếu đã đăng nhập trước đó và còn phiên, kiểm tra vai trò và chuyển màn hình luôn
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            kiemTraVaiTroVaDieuHuong(currentUser.getUid());
        }
    }

    private void xuLyDangNhap() {
        String email = binding.edtEmail.getText().toString().trim();
        String matKhau = binding.edtMatKhau.getText().toString().trim();

        // Kiểm tra dữ liệu đầu vào (Validation)
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

        if (matKhau.isEmpty()) {
            binding.edtMatKhau.setError("Vui lòng nhập Mật khẩu");
            binding.edtMatKhau.requestFocus();
            return;
        }

        if (matKhau.length() < 6) {
            binding.edtMatKhau.setError("Mật khẩu phải từ 6 ký tự trở lên");
            binding.edtMatKhau.requestFocus();
            return;
        }

        // Đăng nhập qua Firebase Authentication
        binding.btnDangNhap.setEnabled(false);
        mAuth.signInWithEmailAndPassword(email, matKhau)
                .addOnCompleteListener(task -> {
                    binding.btnDangNhap.setEnabled(true);
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Toast.makeText(DangNhapActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                            kiemTraVaiTroVaDieuHuong(user.getUid());
                        }
                    } else {
                        Toast.makeText(DangNhapActivity.this, "Đăng nhập thất bại: Sai email hoặc mật khẩu", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Hàm kiểm tra vai trò của người dùng trên Firestore để phân quyền điều hướng
     */
    private void kiemTraVaiTroVaDieuHuong(String uid) {
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String vaiTro = doc.getString("vaiTro");

                        if ("admin".equalsIgnoreCase(vaiTro)) {
                            Toast.makeText(this, "Đăng nhập với quyền Quản Trị Viên (Admin)", Toast.LENGTH_SHORT).show();
                            // Điều hướng sang màn hình Admin (MainActivity tạm thời hoặc AdminDashboardActivity)
                            startActivity(new Intent(DangNhapActivity.this, MainActivity.class));
                        } else {
                            Toast.makeText(this, "Chào mừng Khách hàng trở lại!", Toast.LENGTH_SHORT).show();
                            // Điều hướng sang màn hình Khách hàng
                            startActivity(new Intent(DangNhapActivity.this, MainActivity.class));
                        }
                        finish();
                    } else {
                        Toast.makeText(this, "Không tìm thấy hồ sơ người dùng trong hệ thống", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi kiểm tra quyền: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}

