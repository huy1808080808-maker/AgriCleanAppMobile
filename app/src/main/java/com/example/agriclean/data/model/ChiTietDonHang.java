package com.example.agriclean.data.model;

import java.io.Serializable;

public class ChiTietDonHang implements Serializable {
    private String maDonHang;
    private String maSanPham;
    private String tenSanPham;
    private double donGia;
    private int soLuongMua;

    public ChiTietDonHang() {
    }

    public ChiTietDonHang(String maDonHang, String maSanPham, String tenSanPham, double donGia, int soLuongMua) {
        this.maDonHang = maDonHang;
        this.maSanPham = maSanPham;
        this.tenSanPham = tenSanPham;
        this.donGia = donGia;
        this.soLuongMua = soLuongMua;
    }

    public String getMaDonHang() { return maDonHang; }
    public void setMaDonHang(String maDonHang) { this.maDonHang = maDonHang; }

    public String getMaSanPham() { return maSanPham; }
    public void setMaSanPham(String maSanPham) { this.maSanPham = maSanPham; }

    public String getTenSanPham() { return tenSanPham; }
    public void setTenSanPham(String tenSanPham) { this.tenSanPham = tenSanPham; }

    public double getDonGia() { return donGia; }
    public void setDonGia(double donGia) { this.donGia = donGia; }

    public int getSoLuongMua() { return soLuongMua; }
    public void setSoLuongMua(int soLuongMua) { this.soLuongMua = soLuongMua; }
}
