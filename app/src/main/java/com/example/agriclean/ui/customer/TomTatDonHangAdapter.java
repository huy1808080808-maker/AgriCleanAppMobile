package com.example.agriclean.ui.customer;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agriclean.data.local.GioHangItem;
import com.example.agriclean.databinding.ItemTomTatDonHangBinding;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter hiển thị danh sách rút gọn các món hàng trong màn hình Đặt hàng (Checkout - UC12).
 */
public class TomTatDonHangAdapter extends RecyclerView.Adapter<TomTatDonHangAdapter.TomTatViewHolder> {

    private final List<GioHangItem> danhSach = new ArrayList<>();
    private final DecimalFormat formatter = new DecimalFormat("#,###");

    public void capNhatDanhSach(List<GioHangItem> danhSachMoi) {
        this.danhSach.clear();
        if (danhSachMoi != null) {
            this.danhSach.addAll(danhSachMoi);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TomTatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTomTatDonHangBinding binding = ItemTomTatDonHangBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new TomTatViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TomTatViewHolder holder, int position) {
        holder.bind(danhSach.get(position));
    }

    @Override
    public int getItemCount() {
        return danhSach.size();
    }

    class TomTatViewHolder extends RecyclerView.ViewHolder {
        private final ItemTomTatDonHangBinding binding;

        public TomTatViewHolder(@NonNull ItemTomTatDonHangBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(GioHangItem item) {
            binding.tvTenSanPham.setText(item.getTenSanPham());
            binding.tvSoLuong.setText("x " + item.getSoLuong());
            double thanhTien = item.getGiaBan() * item.getSoLuong();
            binding.tvThanhTien.setText(formatter.format(thanhTien) + " đ");
        }
    }
}

