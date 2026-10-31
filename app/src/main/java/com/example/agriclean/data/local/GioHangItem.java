package com.example.agriclean.data.local;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "gio_hang")
public class GioHangItem implements Serializable {
    @PrimaryKey
    @NonNull
    private String maSanPham;
    private String tenSanPham;
    private double giaBan;
    private int soLuong;
    private String linkHinhAnh;
    private String tenTrangTrai;

    public GioHangItem(@NonNull String maSanPham, String tenSanPham, double giaBan, int soLuong, String linkHinhAnh, String tenTrangTrai) {
        this.maSanPham = maSanPham;
        this.tenSanPham = tenSanPham;
        this.giaBan = giaBan;
        this.soLuong = soLuong;
        this.linkHinhAnh = linkHinhAnh;
        this.tenTrangTrai = tenTrangTrai;
    }

    @NonNull
    public String getMaSanPham() { return maSanPham; }
    public void setMaSanPham(@NonNull String maSanPham) { this.maSanPham = maSanPham; }

    public String getTenSanPham() { return tenSanPham; }
    public void setTenSanPham(String tenSanPham) { this.tenSanPham = tenSanPham; }

    public double getGiaBan() { return giaBan; }
    public void setGiaBan(double giaBan) { this.giaBan = giaBan; }

    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }

    public String getLinkHinhAnh() { return linkHinhAnh; }
    public void setLinkHinhAnh(String linkHinhAnh) { this.linkHinhAnh = linkHinhAnh; }

    public String getTenTrangTrai() { return tenTrangTrai; }
    public void setTenTrangTrai(String tenTrangTrai) { this.tenTrangTrai = tenTrangTrai; }
}
