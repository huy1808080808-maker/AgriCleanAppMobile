package com.example.agriclean.ui.customer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.agriclean.data.model.SanPham;
import com.example.agriclean.databinding.ItemSanPhamCustomerBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter hiển thị danh sách nông sản dành riêng cho Khách hàng (UC04 - Bước 3).
 * Hỗ trợ hiển thị dạng lưới (Grid), tải ảnh mượt mà với Glide theo chuẩn NFR09.
 */
public class SanPhamCustomerAdapter extends RecyclerView.Adapter<SanPhamCustomerAdapter.CustomerViewHolder> {

    private final Context context;
    private List<SanPham> danhSachSanPham = new ArrayList<>();
    private final OnSanPhamCustomerListener listener;

    public interface OnSanPhamCustomerListener {
        void onXemChiTiet(SanPham sanPham);
        void onThemVaoGio(SanPham sanPham);
    }

    public SanPhamCustomerAdapter(Context context, OnSanPhamCustomerListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void capNhatDanhSach(List<SanPham> danhSachMoi) {
        this.danhSachSanPham = danhSachMoi != null ? danhSachMoi : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CustomerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSanPhamCustomerBinding binding = ItemSanPhamCustomerBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new CustomerViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomerViewHolder holder, int position) {
        SanPham sp = danhSachSanPham.get(position);
        holder.bind(sp);
    }

    @Override
    public int getItemCount() {
        return danhSachSanPham.size();
    }

    class CustomerViewHolder extends RecyclerView.ViewHolder {

        private final ItemSanPhamCustomerBinding binding;

        public CustomerViewHolder(@NonNull ItemSanPhamCustomerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(SanPham sp) {
            // 1. Tên, trang trại và chứng nhận
            binding.tvTenSanPham.setText(sp.getTenSanPham());
            binding.tvTenTrangTrai.setText(sp.getTenTrangTrai() != null ? sp.getTenTrangTrai() : "Nhà vườn liên kết");

            String chungNhan = sp.getChungNhan();
            if (chungNhan != null && !chungNhan.isEmpty()) {
                binding.tvBadgeChungNhan.setText(chungNhan);
                binding.tvBadgeChungNhan.setVisibility(View.VISIBLE);
            } else {
                binding.tvBadgeChungNhan.setVisibility(View.GONE);
            }

            // 2. Giá hiển thị kèm đơn vị tính
            String donVi = (sp.getDonViTinh() != null && !sp.getDonViTinh().isEmpty()) ? sp.getDonViTinh() : "kg";
            binding.tvGia.setText(sp.getGiaDinhDang() + " / " + donVi);

            // 3. Xử lý trạng thái tồn kho
            boolean conHang = sp.getTonKho() > 0;
            binding.tvHetHangOverlay.setVisibility(conHang ? View.GONE : View.VISIBLE);
            binding.btnThemNhanh.setEnabled(conHang);

            // 4. Tải ảnh với Glide tối ưu theo NFR09 (300x300, diskCache ALL, placeholder)
            Glide.with(context)
                    .load(sp.getHinhAnhUrl())
                    .override(300, 300)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .into(binding.imgSanPham);

            // 5. Sự kiện click xem chi tiết sản phẩm (UC07)
            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) listener.onXemChiTiet(sp);
            });

            // 6. Sự kiện click thêm nhanh vào giỏ hàng (UC10)
            binding.btnThemNhanh.setOnClickListener(v -> {
                if (listener != null && conHang) {
                    listener.onThemVaoGio(sp);
                }
            });
        }
    }
}

