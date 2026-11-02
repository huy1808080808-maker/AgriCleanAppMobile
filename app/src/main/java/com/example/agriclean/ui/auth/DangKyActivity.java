package com.example.agriclean.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.agriclean.MainActivity;
import com.example.agriclean.databinding.ActivityDangKyBinding;

/**
 * Màn hình Đăng ký tài khoản (DangKyActivity):
 */
public class DangKyActivity extends AppCompatActivity {

    private ActivityDangKyBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDangKyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        // Quay lại màn hình Đăng nhập
        binding.tvChuyenDangNhap.setOnClickListener(v -> finish());

        // Bấm nút Đăng ký
        binding.btnDangKy.setOnClickListener(v -> xuLyDangKy());
    }

    private void xuLyDangKy() {

            binding.edtSoDienThoai.requestFocus();
            return;
        }

                    binding.edtMatKhau.requestFocus();
                } else {
                }
            }
        });
    }
}
