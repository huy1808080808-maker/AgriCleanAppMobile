package com.example.agriclean.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.agriclean.data.model.DonHang;
import com.example.agriclean.data.repository.DonHangRepository;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

/**
 * Lớp DonHangViewModel: Quản lý trạng thái và luồng dữ liệu cho quy trình Đặt hàng (UC12),
 * Lịch sử đơn hàng Realtime (UC13) và Quản lý đơn hàng Admin Realtime (UC15, UC16).
 */
public class DonHangViewModel extends ViewModel {

    private final DonHangRepository repository;

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>(null);
    private final MutableLiveData<String> taoDonThanhCong = new MutableLiveData<>(null);
    private final MutableLiveData<List<DonHang>> danhSachDonHang = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> capNhatThanhCong = new MutableLiveData<>(false);

    private ListenerRegistration donHangListener;

    public DonHangViewModel() {
        this.repository = DonHangRepository.getInstance();
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<String> getTaoDonThanhCong() {
        return taoDonThanhCong;
    }

    public LiveData<List<DonHang>> getDanhSachDonHang() {
        return danhSachDonHang;
    }

    public LiveData<Boolean> getCapNhatThanhCong() {
        return capNhatThanhCong;
    }

    /**
     * Tiến hành tạo đơn hàng và đẩy lên Firestore collection "Orders" (UC12)
     */
    public void datHang(DonHang donHang) {
        if (donHang == null) {
            errorMessage.setValue("Dữ liệu đơn hàng không hợp lệ!");
            return;
        }

        loading.setValue(true);
        repository.taoDonHang(donHang, new DonHangRepository.OnTaoDonCallback() {
            @Override
            public void onSuccess(String maDonHang) {
                loading.setValue(false);
                taoDonThanhCong.setValue(maDonHang);
            }

            @Override
            public void onFailure(String loi) {
                loading.setValue(false);
                errorMessage.setValue("Lỗi khi tạo đơn hàng: " + loi);
            }
        });
    }

    /**
     * Lắng nghe Realtime lịch sử đơn hàng của khách hàng hiện tại (UC13)
     */
    public void batDauLangNgheKhachHang(String maKhachHang) {
        if (donHangListener != null) {
            donHangListener.remove();
        }

        loading.setValue(true);
        donHangListener = repository.langNgheDonHangKhachHang(maKhachHang, new DonHangRepository.OnDonHangCallback() {
            @Override
            public void onResult(List<DonHang> list) {
                loading.setValue(false);
                danhSachDonHang.setValue(list != null ? list : new ArrayList<>());
            }

            @Override
            public void onFailure(String loi) {
                loading.setValue(false);
                errorMessage.setValue("Không thể tải đơn hàng: " + loi);
            }
        });
    }

    /**
     * Lắng nghe Realtime toàn bộ đơn hàng cho Admin (UC15, UC16)
     */
    public void batDauLangNgheAdmin() {
        if (donHangListener != null) {
            donHangListener.remove();
        }

        loading.setValue(true);
        donHangListener = repository.langNgheTatCaDonHang(new DonHangRepository.OnDonHangCallback() {
            @Override
            public void onResult(List<DonHang> list) {
                loading.setValue(false);
                danhSachDonHang.setValue(list != null ? list : new ArrayList<>());
            }

            @Override
            public void onFailure(String loi) {
                loading.setValue(false);
                errorMessage.setValue("Không thể tải danh sách đơn hàng: " + loi);
            }
        });
    }

    /**
     * Tải thủ công lịch sử đơn hàng (phục vụ SwipeRefresh)
     */
    public void taiDonHangKhachHang(String maKhachHang) {
        loading.setValue(true);
        repository.layDonHangTheoKhachHang(maKhachHang, new DonHangRepository.OnDonHangCallback() {
            @Override
            public void onResult(List<DonHang> list) {
                loading.setValue(false);
                danhSachDonHang.setValue(list != null ? list : new ArrayList<>());
            }

            @Override
            public void onFailure(String loi) {
                loading.setValue(false);
                errorMessage.setValue("Không thể tải đơn hàng: " + loi);
            }
        });
    }

    /**
     * Cập nhật trạng thái đơn hàng (Dành cho Admin duyệt/hủy đơn)
     */
    public void capNhatTrangThai(String maDonHang, String trangThaiMoi) {
        loading.setValue(true);
        repository.capNhatTrangThaiDonHang(maDonHang, trangThaiMoi, new DonHangRepository.OnCapNhatTrangThaiCallback() {
            @Override
            public void onSuccess() {
                loading.setValue(false);
                capNhatThanhCong.setValue(true);
            }

            @Override
            public void onFailure(String loi) {
                loading.setValue(false);
                errorMessage.setValue("Lỗi cập nhật trạng thái: " + loi);
            }
        });
    }

    /**
     * Reset các thông báo sau khi đã xử lý xong
     */
    public void resetTrangThai() {
        taoDonThanhCong.setValue(null);
        errorMessage.setValue(null);
        capNhatThanhCong.setValue(false);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (donHangListener != null) {
            donHangListener.remove();
            donHangListener = null;
        }
    }
}
