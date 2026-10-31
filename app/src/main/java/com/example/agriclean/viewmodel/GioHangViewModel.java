package com.example.agriclean.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.agriclean.data.local.GioHangItem;
import com.example.agriclean.data.repository.GioHangRepository;

import java.util.List;

/**
 * Lớp GioHangViewModel: Cầu nối giữa Repository và Giao diện (Activity/Fragment) theo kiến trúc MVVM.
 * Lưu trữ và quản lý trạng thái dữ liệu giỏ hàng, giữ dữ liệu không bị mất khi xoay màn hình.
 */
public class GioHangViewModel extends AndroidViewModel {

    // Lớp kho dữ liệu xử lý logic Room Database
    private final GioHangRepository repository;

    // Dữ liệu LiveData danh sách sản phẩm trong giỏ để Activity lắng nghe
    private final LiveData<List<GioHangItem>> danhSachGioHang;

    public GioHangViewModel(@NonNull Application application) {
        super(application);
        this.repository = new GioHangRepository(application);
        this.danhSachGioHang = repository.layDanhSachGioHang();
    }

    /**
     * Cung cấp danh sách giỏ hàng cho Activity quan sát (observe)
     */
    public LiveData<List<GioHangItem>> getDanhSachGioHang() {
        return danhSachGioHang;
    }

    /**
     * Thêm sản phẩm vào giỏ
     */
    public void themVaoGio(GioHangItem item) {
        repository.themVaoGio(item);
    }

    /**
     * Thay đổi số lượng mua của sản phẩm
     */
    public void capNhatSoLuong(String maSanPham, int soLuongMoi) {
        repository.capNhatSoLuong(maSanPham, soLuongMoi);
    }

    /**
     * Xóa sản phẩm được chọn khỏi giỏ
     */
    public void xoaKhoiGio(GioHangItem item) {
        repository.xoaKhoiGio(item);
    }

    /**
     * Xóa toàn bộ giỏ hàng
     */
    public void xoaToanBoGioHang() {
        repository.xoaToanBoGioHang();
    }
}

