package com.example.agriclean.utils;

import androidx.activity.result.ActivityResultLauncher;

import com.journeyapps.barcodescanner.CaptureActivity;
import com.journeyapps.barcodescanner.ScanOptions;

/**
 * Lớp tiện ích QuetQRHelper: Hỗ trợ mở Camera quét mã QR truy xuất nguồn gốc nông sản bằng thư viện ZXing.
 */
public class QuetQRHelper {

    /**
     * Mở camera quét mã QR
     * @param launcher Trình kích hoạt nhận kết quả trả về từ màn hình quét
     * @param loiNhan Dòng chữ hướng dẫn hiển thị trên màn hình quét camera
     */
    public static void batDauQuetQR(ActivityResultLauncher<ScanOptions> launcher, String loiNhan) {
        ScanOptions options = new ScanOptions();
        options.setPrompt(loiNhan != null ? loiNhan : "Hướng camera vào mã QR nông sản");
        options.setBeepEnabled(true);              // Phát âm thanh khi quét thành công
        options.setBarcodeImageEnabled(false);     // Không lưu ảnh mã vạch vào bộ nhớ
        options.setOrientationLocked(true);        // Khóa hướng màn hình dọc khi quét
        options.setCaptureActivity(CaptureActivity.class);

        launcher.launch(options);
    }
}

