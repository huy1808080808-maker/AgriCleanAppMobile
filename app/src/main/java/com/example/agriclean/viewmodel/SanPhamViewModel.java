package com.example.agriclean.viewmodel;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.agriclean.data.model.SanPham;
import com.example.agriclean.data.repository.SanPhamRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Lớp SanPhamViewModel: Cầu nối MVVM quản lý dữ liệu nông sản sạch.
 * Phục vụ đồng thời cho:
 * - Admin (UC14 - Quản lý sản phẩm: thêm, sửa, xóa, tải ảnh Storage)
 * - Khách hàng (UC04 - Danh sách nông sản, UC05 - Tìm kiếm, UC06 - Lọc danh mục, UC08 - Quét QR xem chi tiết)
 */
public class SanPhamViewModel extends ViewModel {

    private final SanPhamRepository repository;

    private final MutableLiveData<List<SanPham>> danhSachHienThiLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<SanPham>> tatCaSanPhamLiveData = new MutableLiveData<>();
    private final MutableLiveData<SanPham> sanPhamChiTietLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> actionSuccessLiveData = new MutableLiveData<>();

    private String tuKhoaHienTai = "";
    private String danhMucHienTai = SanPham.DANH_MUC_TAT_CA;

    public SanPhamViewModel() {
        this.repository = SanPhamRepository.getInstance();
    }

    public SanPhamViewModel(SanPhamRepository repository) {
        this.repository = repository;
    }

    public LiveData<List<SanPham>> getDanhSachHienThiLiveData() {
        return danhSachHienThiLiveData;
    }

    public LiveData<SanPham> getSanPhamChiTietLiveData() {
        return sanPhamChiTietLiveData;
    }

    public LiveData<Boolean> getLoadingLiveData() {
        return loadingLiveData;
    }

    public LiveData<String> getErrorMessageLiveData() {
        return errorMessageLiveData;
    }

    public LiveData<Boolean> getActionSuccessLiveData() {
        return actionSuccessLiveData;
    }

    public void taiDanhSachSanPhamRealtime() {
        loadingLiveData.setValue(true);
        repository.layTatCaSanPham(danhSach -> {
            loadingLiveData.setValue(false);
            tatCaSanPhamLiveData.setValue(danhSach);
            apDungBoLoc();
        });
    }

    public void timKiem(String tuKhoa) {
        this.tuKhoaHienTai = tuKhoa != null ? tuKhoa.trim().toLowerCase() : "";
        apDungBoLoc();
    }

    public void locTheoDanhMuc(String danhMuc) {
        this.danhMucHienTai = danhMuc != null ? danhMuc : SanPham.DANH_MUC_TAT_CA;
        apDungBoLoc();
    }

    private void apDungBoLoc() {
        List<SanPham> goc = tatCaSanPhamLiveData.getValue();
        if (goc == null) {
            danhSachHienThiLiveData.setValue(new ArrayList<>());
            return;
        }

        List<SanPham> ketQua = new ArrayList<>();
        for (SanPham sp : goc) {
            boolean khopDanhMuc = danhMucHienTai.equals(SanPham.DANH_MUC_TAT_CA)
                    || (sp.getDanhMuc() != null && sp.getDanhMuc().equalsIgnoreCase(danhMucHienTai));

            boolean khopTuKhoa = tuKhoaHienTai.isEmpty()
                    || (sp.getTenSanPham() != null && sp.getTenSanPham().toLowerCase().contains(tuKhoaHienTai))
                    || (sp.getTenTrangTrai() != null && sp.getTenTrangTrai().toLowerCase().contains(tuKhoaHienTai));

            if (khopDanhMuc && khopTuKhoa) {
                ketQua.add(sp);
            }
        }
        danhSachHienThiLiveData.setValue(ketQua);
    }

    public void layChiTietSanPham(String productId) {
        loadingLiveData.setValue(true);
        repository.laySanPhamTheoId(productId, new SanPhamRepository.OnSingleSanPhamCallback() {
            @Override
            public void onSuccess(SanPham sanPham) {
                loadingLiveData.setValue(false);
                sanPhamChiTietLiveData.setValue(sanPham);
            }

            @Override
            public void onFailure(String errorMessage) {
                loadingLiveData.setValue(false);
                errorMessageLiveData.setValue(errorMessage);
            }
        });
    }

    /**
     * Lưu sản phẩm (Thêm mới hoặc Cập nhật) kèm tải ảnh lên Storage (NFR10)
     */
    public void luuSanPham(SanPham sp, Uri imageUri, boolean isUpdate) {
        if (!kiemTraHopLeSanPham(sp, imageUri, isUpdate)) {
            return;
        }

        loadingLiveData.setValue(true);
        repository.luuSanPham(sp, imageUri, isUpdate, new SanPhamRepository.OnActionCallback() {
            @Override
            public void onSuccess() {
                loadingLiveData.setValue(false);
                actionSuccessLiveData.setValue(true);
            }

            @Override
            public void onFailure(String loi) {
                loadingLiveData.setValue(false);
                errorMessageLiveData.setValue(loi);
            }
        });
    }

    public void xoaSanPham(String productId) {
        loadingLiveData.setValue(true);
        repository.xoaSanPham(productId, new SanPhamRepository.OnActionCallback() {
            @Override
            public void onSuccess() {
                loadingLiveData.setValue(false);
                actionSuccessLiveData.setValue(true);
            }

            @Override
            public void onFailure(String loi) {
                loadingLiveData.setValue(false);
                errorMessageLiveData.setValue(loi);
            }
        });
    }

    /**
     * Kiểm tra tính hợp lệ của dữ liệu nông sản theo NFR10
     */
    private boolean kiemTraHopLeSanPham(SanPham sp, Uri imageUri, boolean isUpdate) {
        if (sp == null) {
            errorMessageLiveData.setValue("Dữ liệu sản phẩm không hợp lệ");
            return false;
        }

        if (sp.getTenSanPham() == null || sp.getTenSanPham().trim().isEmpty()) {
            errorMessageLiveData.setValue("Vui lòng nhập tên nông sản");
            return false;
        }

        if (sp.getGia() <= 0) {
            errorMessageLiveData.setValue("Giá bán phải lớn hơn 0");
            return false;
        }

        if (sp.getTonKho() < 0) {
            errorMessageLiveData.setValue("Số lượng tồn kho không được âm");
            return false;
        }

        if (sp.getDanhMuc() == null || sp.getDanhMuc().trim().isEmpty()) {
            errorMessageLiveData.setValue("Vui lòng chọn danh mục nông sản");
            return false;
        }

        if (sp.getTenTrangTrai() == null || sp.getTenTrangTrai().trim().isEmpty()) {
            errorMessageLiveData.setValue("Vui lòng nhập tên trang trại");
            return false;
        }

        if (sp.getDiaChiTrangTrai() == null || sp.getDiaChiTrangTrai().trim().isEmpty()) {
            errorMessageLiveData.setValue("Vui lòng nhập địa chỉ trang trại");
            return false;
        }

        // Khi thêm mới, bắt buộc phải chọn ảnh hoặc đã có link ảnh
        if (!isUpdate && imageUri == null && (sp.getHinhAnhUrl() == null || sp.getHinhAnhUrl().trim().isEmpty())) {
            errorMessageLiveData.setValue("Vui lòng chọn ảnh nông sản từ thiết bị");
            return false;
        }

        return true;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        repository.huyListener();
    }
}
