package com.example.agriclean.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.agriclean.data.local.AppDatabase;
import com.example.agriclean.data.local.GioHangDao;
import com.example.agriclean.data.local.GioHangItem;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Lớp GioHangRepository: Đóng vai trò trung gian giữa cơ sở dữ liệu nội bộ (Room GioHangDao) và ViewModel.
 * Quản lý các tác vụ truy vấn và cập nhật giỏ hàng trên luồng nền (Background Thread).
 */
public class GioHangRepository {

    // Đối tượng truy xuất cơ sở dữ liệu
    private final GioHangDao gioHangDao;

    // Danh sách dữ liệu giỏ hàng dạng LiveData để quan sát thời gian thực
    private final LiveData<List<GioHangItem>> danhSachGioHang;

    // Luồng thực thi riêng biệt cho các tác vụ ghi/xóa Room Database để không đơ giao diện
    private final ExecutorService executorService;

    public GioHangRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        this.gioHangDao = database.gioHangDao();
        this.danhSachGioHang = gioHangDao.layDanhSachGioHang();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    /**
     * Lấy toàn bộ danh sách sản phẩm có trong giỏ hàng
     */
    public LiveData<List<GioHangItem>> layDanhSachGioHang() {
        return danhSachGioHang;
    }

    /**
     * Thêm sản phẩm vào giỏ hàng.
     * Nếu sản phẩm đã tồn tại thì cộng dồn số lượng, nếu chưa thì thêm mới.
     */
    public void themVaoGio(GioHangItem item) {
        executorService.execute(() -> {
            GioHangItem sanPhamCu = gioHangDao.laySanPhamTheoMa(item.getMaSanPham());
            if (sanPhamCu != null) {
                sanPhamCu.setSoLuong(sanPhamCu.getSoLuong() + item.getSoLuong());
                gioHangDao.capNhatGio(sanPhamCu);
            } else {
                gioHangDao.themVaoGio(item);
            }
        });
    }

    /**
     * Cập nhật số lượng mặt hàng nông sản trong giỏ.
     * Nếu số lượng giảm về <= 0 thì tự động xóa mặt hàng khỏi giỏ.
     */
    public void capNhatSoLuong(String maSanPham, int soLuongMoi) {
        executorService.execute(() -> {
            GioHangItem sanPham = gioHangDao.laySanPhamTheoMa(maSanPham);
            if (sanPham != null) {
                if (soLuongMoi <= 0) {
                    gioHangDao.xoaKhoiGio(sanPham);
                } else {
                    sanPham.setSoLuong(soLuongMoi);
                    gioHangDao.capNhatGio(sanPham);
                }
            }
        });
    }

    /**
     * Xóa một sản phẩm xác định khỏi giỏ hàng
     */
    public void xoaKhoiGio(GioHangItem item) {
        executorService.execute(() -> gioHangDao.xoaKhoiGio(item));
    }

    /**
     * Dọn sạch toàn bộ giỏ hàng (sau khi đặt hàng thành công hoặc người dùng nhấn xóa hết)
     */
    public void xoaToanBoGioHang() {
        executorService.execute(gioHangDao::xoaToanBoGioHang);
    }
}

