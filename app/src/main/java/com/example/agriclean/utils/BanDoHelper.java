package com.example.agriclean.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

/**
 * Lớp tiện ích BanDoHelper: Mở ứng dụng Google Maps hoặc trình duyệt để định vị và dẫn đường tới trang trại nông sản.
 */
public class BanDoHelper {

    /**
     * Mở ứng dụng Google Maps định vị trang trại
     * @param context Ngữ cảnh ứng dụng (Activity)
     * @param viDo Vĩ độ địa lý của trang trại
     * @param kinhDo Kinh độ địa lý của trang trại
     * @param tenTrangTrai Tên nông trại / hợp tác xã để gắn nhãn đánh dấu
     */
    public static void moViTriTrangTrai(Context context, double viDo, double kinhDo, String tenTrangTrai) {
        String tenMaHoa = Uri.encode(tenTrangTrai);
        Uri uri = Uri.parse("geo:" + viDo + "," + kinhDo + "?q=" + viDo + "," + kinhDo + "(" + tenMaHoa + ")");

        Intent mapIntent = new Intent(Intent.ACTION_VIEW, uri);
        mapIntent.setPackage("com.google.android.apps.maps");

        try {
            if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(mapIntent);
            } else {
                // Fallback nếu máy người dùng chưa cài app Google Maps
                Uri browserUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + viDo + "," + kinhDo);
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, browserUri);
                context.startActivity(browserIntent);
            }
        } catch (Exception e) {
            Toast.makeText(context, "Không thể mở ứng dụng bản đồ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}

