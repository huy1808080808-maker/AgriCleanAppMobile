package com.example.agriclean.ui.customer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.agriclean.data.local.GioHangItem;
import com.example.agriclean.databinding.ItemGioHangBinding;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter quản lý danh sách mặt hàng trong giỏ hàng Room Database (UC10, UC11).
 * Tối ưu hóa tải ảnh mượt mà với Glide (NFR09).
 */
public class GioHangAdapter extends RecyclerView.Adapter<GioHangAdapter.GioHangViewHolder> {

    private final Context context;
    private List<GioHangItem> danhSachGioHang = new ArrayList<>();
    private final OnGioHangItemListener listener;
    private final DecimalFormat formatter = new DecimalFormat("#,###");

    public interface OnGioHangItemListener {
        void onTangSoLuong(GioHangItem item);
        void onGiamSoLuong(GioHangItem item);
        void onXoaItem(GioHangItem item);
    }

    public GioHangAdapter(Context context, OnGioHangItemListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void capNhatDanhSach(List<GioHangItem> danhSachMoi) {
        this.danhSachGioHang = danhSachMoi != null ? danhSachMoi : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GioHangViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemGioHangBinding binding = ItemGioHangBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new GioHangViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull GioHangViewHolder holder, int position) {
        GioHangItem item = danhSachGioHang.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return danhSachGioHang.size();
    }

    class GioHangViewHolder extends RecyclerView.ViewHolder {

        private final ItemGioHangBinding binding;

        public GioHangViewHolder(@NonNull ItemGioHangBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(GioHangItem item) {
            // 1. Tên và nhà vườn
            binding.tvTenSanPham.setText(item.getTenSanPham());
            binding.tvTenTrangTrai.setText(item.getTenTrangTrai() != null ? item.getTenTrangTrai() : "Nhà vườn liên kết");

            // 2. Đơn giá và Thành tiền (giaBan * soLuong)
            binding.tvDonGia.setText("Đơn giá: " + formatter.format(item.getGiaBan()) + " đ");
            double thanhTien = item.getGiaBan() * item.getSoLuong();
            binding.tvThanhTien.setText(formatter.format(thanhTien) + " đ");

            // 3. Số lượng
            binding.tvSoLuong.setText(String.valueOf(item.getSoLuong()));

            // 4. Tải ảnh với Glide tối ưu NFR09
            Glide.with(context)
                    .load(item.getLinkHinhAnh())
                    .override(200, 200)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .into(binding.imgSanPham);

            // 5. Bắt các sự kiện
            binding.btnTang.setOnClickListener(v -> {
                if (listener != null) listener.onTangSoLuong(item);
            });

            binding.btnGiam.setOnClickListener(v -> {
                if (listener != null) listener.onGiamSoLuong(item);
            });

            binding.btnXoaItem.setOnClickListener(v -> {
                if (listener != null) listener.onXoaItem(item);
            });
        }
    }
}

