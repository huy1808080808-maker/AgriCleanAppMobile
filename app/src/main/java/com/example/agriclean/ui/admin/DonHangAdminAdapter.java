package com.example.agriclean.ui.admin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agriclean.R;
import com.example.agriclean.data.model.ChiTietDonHang;
import com.example.agriclean.data.model.DonHang;
import com.example.agriclean.databinding.ItemDonHangAdminBinding;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter hiển thị danh sách đơn hàng toàn hệ thống cho Quản trị viên (UC15, UC16).
 * Hỗ trợ cập nhật trạng thái đơn hàng (Duyệt đơn, Đang giao, Hoàn thành, Huỷ).
 */
public class DonHangAdminAdapter extends RecyclerView.Adapter<DonHangAdminAdapter.DonHangAdminViewHolder> {

    public interface OnDonHangAdminListener {
        void onCapNhatTrangThai(DonHang donHang);
    }

    private final List<DonHang> danhSach = new ArrayList<>();
    private final OnDonHangAdminListener listener;
    private final DecimalFormat formatter = new DecimalFormat("#,###");
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault());

    public DonHangAdminAdapter(OnDonHangAdminListener listener) {
        this.listener = listener;
    }

    public void capNhatDanhSach(List<DonHang> danhSachMoi) {
        this.danhSach.clear();
        if (danhSachMoi != null) {
            this.danhSach.addAll(danhSachMoi);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DonHangAdminViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDonHangAdminBinding binding = ItemDonHangAdminBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new DonHangAdminViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DonHangAdminViewHolder holder, int position) {
        holder.bind(danhSach.get(position));
    }

    @Override
    public int getItemCount() {
        return danhSach.size();
    }

    class DonHangAdminViewHolder extends RecyclerView.ViewHolder {
        private final ItemDonHangAdminBinding binding;

        public DonHangAdminViewHolder(@NonNull ItemDonHangAdminBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(DonHang donHang) {
            Context context = itemView.getContext();

            // 1. Mã đơn hàng
            String ma = donHang.getMaDonHang() != null ? donHang.getMaDonHang() : "N/A";
            binding.tvMaDonHang.setText(ma.startsWith("#") ? ma : "#" + ma);

            // 2. Ngày giờ đặt
            if (donHang.getNgayDatHang() != null) {
                binding.tvNgayDat.setText(dateFormat.format(donHang.getNgayDatHang().toDate()));
            } else {
                binding.tvNgayDat.setText("Vừa đặt xong");
            }

            // 3. Trạng thái đơn hàng & Màu sắc Badge
            String trangThai = donHang.getTrangThaiDon() != null ? donHang.getTrangThaiDon() : "Chờ duyệt";
            binding.tvTrangThai.setText(trangThai);
            caiDatMauBadge(context, trangThai);

            // 4. Khách hàng & Số điện thoại
            String tenKhach = donHang.getTenNguoiNhan() != null ? donHang.getTenNguoiNhan() : "Khách vãng lai";
            binding.tvTenKhachHang.setText(tenKhach);

            String sdt = donHang.getSoDienThoaiNhan() != null ? donHang.getSoDienThoaiNhan() : "";
            binding.tvSoDienThoai.setText(!sdt.isEmpty() ? "(" + sdt + ")" : "");

            // 5. Địa chỉ nhận hàng
            String diaChi = donHang.getDiaChiGiaoHang() != null ? donHang.getDiaChiGiaoHang() : "";
            binding.tvDiaChiGiaoHang.setText("Địa chỉ: " + diaChi);

            // 6. Ghi chú (nếu có)
            String ghiChu = donHang.getGhiChu();
            if (ghiChu != null && !ghiChu.trim().isEmpty()) {
                binding.tvGhiChu.setVisibility(View.VISIBLE);
                binding.tvGhiChu.setText("Ghi chú: " + ghiChu);
            } else {
                binding.tvGhiChu.setVisibility(View.GONE);
            }

            // 7. Danh sách món hàng
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

            // 8. Phương thức thanh toán & Tổng tiền
            String pttt = donHang.getPhuongThucThanhToan() != null ? donHang.getPhuongThucThanhToan() : "COD";
            binding.tvPhuongThuc.setText(pttt);
            binding.tvTongTien.setText(formatter.format(donHang.getTongTien()) + " đ");

            // 9. Nút Cập nhật trạng thái đơn (UC16)
            binding.btnCapNhatTrangThai.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCapNhatTrangThai(donHang);
                }
            });
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

