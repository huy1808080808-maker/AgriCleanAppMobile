package com.example.agriclean.ui.admin;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.agriclean.R;
import com.example.agriclean.data.model.SanPham;
import com.example.agriclean.databinding.ItemSanPhamAdminBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter hiển thị danh sách nông sản dành riêng cho Quản trị viên (UC14 - Bước 2).
 * Tối ưu hóa tải ảnh với Glide theo tiêu chuẩn NFR09.
 */
public class SanPhamAdminAdapter extends RecyclerView.Adapter<SanPhamAdminAdapter.SanPhamAdminViewHolder> {

    private final Context context;
    private List<SanPham> danhSachSanPham = new ArrayList<>();
    private final OnSanPhamAdminListener listener;

    public interface OnSanPhamAdminListener {
        void onSua(SanPham sanPham);
        void onXoa(SanPham sanPham);
        void onXemQR(SanPham sanPham);
    }

    public SanPhamAdminAdapter(Context context, OnSanPhamAdminListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void capNhatDanhSach(List<SanPham> danhSachMoi) {
        this.danhSachSanPham = danhSachMoi != null ? danhSachMoi : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SanPhamAdminViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSanPhamAdminBinding binding = ItemSanPhamAdminBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new SanPhamAdminViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SanPhamAdminViewHolder holder, int position) {
        SanPham sp = danhSachSanPham.get(position);
        holder.bind(sp);
    }

    @Override
    public int getItemCount() {
        return danhSachSanPham.size();
    }

    class SanPhamAdminViewHolder extends RecyclerView.ViewHolder {

        private final ItemSanPhamAdminBinding binding;

        public SanPhamAdminViewHolder(@NonNull ItemSanPhamAdminBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(SanPham sp) {
            // 1. Tên và danh mục
            binding.tvTenSanPham.setText(sp.getTenSanPham());
            binding.tvDanhMuc.setText(sp.getDanhMuc() != null ? sp.getDanhMuc() : "Rau sạch");

            // 2. Giá và đơn vị tính (VD: 45.000 đ / hộp 500g)
            String donVi = (sp.getDonViTinh() != null && !sp.getDonViTinh().isEmpty()) ? sp.getDonViTinh() : "kg";
            String giaHienThi = sp.getGiaDinhDang() + " / " + donVi;
            binding.tvGia.setText(giaHienThi);

            // 3. Trạng thái tồn kho (B2 - Bước 2)
            if (sp.getTonKho() > 0) {
                binding.tvTrangThaiTonKho.setText("Còn hàng (" + sp.getTonKho() + ")");
                binding.tvTrangThaiTonKho.setTextColor(Color.parseColor("#2E7D32")); // primary_green
            } else {
                binding.tvTrangThaiTonKho.setText("Hết hàng");
                binding.tvTrangThaiTonKho.setTextColor(Color.parseColor("#D32F2F")); // Đỏ cảnh báo
            }

            // 4. Tối ưu Glide theo NFR09 (kích thước cố định 300x300, bộ nhớ đệm ALL, placeholder tránh OOM)
            Glide.with(context)
                    .load(sp.getHinhAnhUrl())
                    .override(300, 300)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .into(binding.imgHinhAnh);

            // 5. Bắt các sự kiện thao tác
            binding.btnXemQR.setOnClickListener(v -> {
                if (listener != null) listener.onXemQR(sp);
            });

            binding.btnSua.setOnClickListener(v -> {
                if (listener != null) listener.onSua(sp);
            });

            binding.btnXoa.setOnClickListener(v -> {
                if (listener != null) listener.onXoa(sp);
            });
        }
    }
}

