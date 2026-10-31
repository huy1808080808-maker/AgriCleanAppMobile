package com.example.agriclean.ui.customer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agriclean.R;
import com.example.agriclean.data.model.ChiTietDonHang;
import com.example.agriclean.data.model.DonHang;
import com.example.agriclean.databinding.ItemDonHangCustomerBinding;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter hiển thị danh sách lịch sử đơn hàng của khách hàng (UC13).
 * Trực quan hóa màu sắc badge trạng thái (Chờ duyệt, Đang giao, Hoàn thành, Đã huỷ).
 */
public class DonHangCustomerAdapter extends RecyclerView.Adapter<DonHangCustomerAdapter.DonHangViewHolder> {

    private final List<DonHang> danhSach = new ArrayList<>();
    private final DecimalFormat formatter = new DecimalFormat("#,###");
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault());

    public void capNhatDanhSach(List<DonHang> danhSachMoi) {
        this.danhSach.clear();
        if (danhSachMoi != null) {
            this.danhSach.addAll(danhSachMoi);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DonHangViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDonHangCustomerBinding binding = ItemDonHangCustomerBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new DonHangViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DonHangViewHolder holder, int position) {
        holder.bind(danhSach.get(position));
    }

    @Override
    public int getItemCount() {
        return danhSach.size();
    }

    class DonHangViewHolder extends RecyclerView.ViewHolder {
        private final ItemDonHangCustomerBinding binding;

        public DonHangViewHolder(@NonNull ItemDonHangCustomerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(DonHang donHang) {
            Context context = itemView.getContext();

            // 1. Mã đơn hàng
            String ma = donHang.getMaDonHang() != null ? donHang.getMaDonHang() : "N/A";
            binding.tvMaDonHang.setText(ma.startsWith("#") ? ma : "#" + ma);

            // 2. Ngày giờ đặt hàng
            if (donHang.getNgayDatHang() != null) {
                binding.tvNgayDat.setText(dateFormat.format(donHang.getNgayDatHang().toDate()));
            } else {
                binding.tvNgayDat.setText("Vừa đặt xong");
            }

            // 3. Trạng thái đơn hàng & Màu sắc Badge
            String trangThai = donHang.getTrangThaiDon() != null ? donHang.getTrangThaiDon() : "Chờ duyệt";
            binding.tvTrangThai.setText(trangThai);
            caiDatMauBadge(context, trangThai);

            // 4. Danh sách món hàng tóm tắt
            StringBuilder sb = new StringBuilder();
            if (donHang.getDanhSachChiTiet() != null && !donHang.getDanhSachChiTiet().isEmpty()) {
                for (int i = 0; i < donHang.getDanhSachChiTiet().size(); i++) {
                    ChiTietDonHang ct = donHang.getDanhSachChiTiet().get(i);
                    sb.append("• ").append(ct.getTenSanPham()).append(" (x").append(ct.getSoLuongMua()).append(")");
                    if (i < donHang.getDanhSachChiTiet().size() - 1) {
                        sb.append("\n");
                    }
                }
            } else {
                sb.append("Chi tiết đơn hàng đang cập nhật");
            }
            binding.tvDanhSachMon.setText(sb.toString());

            // 5. Địa chỉ nhận hàng
            String diaChi = donHang.getDiaChiGiaoHang() != null ? donHang.getDiaChiGiaoHang() : "";
            binding.tvDiaChiNhan.setText("Giao tới: " + diaChi);

            // 6. Phương thức thanh toán
            String pttt = donHang.getPhuongThucThanhToan() != null ? donHang.getPhuongThucThanhToan() : "COD";
            binding.tvPhuongThuc.setText(pttt);

            // 7. Tổng tiền
            binding.tvTongTien.setText(formatter.format(donHang.getTongTien()) + " đ");
        }

        private void caiDatMauBadge(Context context, String trangThai) {
            if ("Hoàn thành".equalsIgnoreCase(trangThai)) {
                binding.tvTrangThai.setBackgroundResource(R.drawable.bg_badge_status_green);
                binding.tvTrangThai.setTextColor(ContextCompat.getColor(context, R.color.primary_green));
            } else if ("Đã xác nhận".equalsIgnoreCase(trangThai) || "Đang giao".equalsIgnoreCase(trangThai)) {
                binding.tvTrangThai.setBackgroundResource(R.drawable.bg_badge_status_blue);
                binding.tvTrangThai.setTextColor(ContextCompat.getColor(context, R.color.status_blue));
            } else if ("Đã huỷ".equalsIgnoreCase(trangThai) || "Đã hủy".equalsIgnoreCase(trangThai)) {
                binding.tvTrangThai.setBackgroundResource(R.drawable.bg_badge_status_red);
                binding.tvTrangThai.setTextColor(ContextCompat.getColor(context, R.color.status_red));
            } else {
                // "Chờ duyệt" hoặc mặc định
                binding.tvTrangThai.setBackgroundResource(R.drawable.bg_badge_status_orange);
                binding.tvTrangThai.setTextColor(ContextCompat.getColor(context, R.color.accent_orange));
            }
        }
    }
}

