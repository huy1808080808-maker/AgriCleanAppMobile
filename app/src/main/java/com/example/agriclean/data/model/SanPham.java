package com.example.agriclean.data.model;

import java.io.Serializable;

public class SanPham implements Serializable {
    private String maSanPham;
    private String maDanhMuc;
    private String tenSanPham;
    private String moTa;
    private double giaBan;
    private int soLuongTon;
    private String linkHinhAnh;
    
    // Thông tin nông trại và nguồn gốc
    private String tenTrangTrai;
    private String diaChiTrangTrai;
    private String chungChi; // Ví dụ: "VietGAP", "Hữu cơ"
    private String quyTrinhCanhTac;
    private double viDo;
    private double kinhDo;

    public SanPham() {
        // Firestore required
    }

    public SanPham(String maSanPham, String maDanhMuc, String tenSanPham, String moTa,
                   double giaBan, int soLuongTon, String linkHinhAnh, String tenTrangTrai,
                   String diaChiTrangTrai, String chungChi, String quyTrinhCanhTac,
                   double viDo, double kinhDo) {
        this.maSanPham = maSanPham;
        this.maDanhMuc = maDanhMuc;
        this.tenSanPham = tenSanPham;
        this.moTa = moTa;
        this.giaBan = giaBan;
        this.soLuongTon = soLuongTon;
        this.linkHinhAnh = linkHinhAnh;
        this.tenTrangTrai = tenTrangTrai;
        this.diaChiTrangTrai = diaChiTrangTrai;
        this.chungChi = chungChi;
        this.quyTrinhCanhTac = quyTrinhCanhTac;
        this.viDo = viDo;
        this.kinhDo = kinhDo;
    }

    public String getMaSanPham() { return maSanPham; }
    public void setMaSanPham(String maSanPham) { this.maSanPham = maSanPham; }

    public String getMaDanhMuc() { return maDanhMuc; }
    public void setMaDanhMuc(String maDanhMuc) { this.maDanhMuc = maDanhMuc; }

    public String getTenSanPham() { return tenSanPham; }
    public void setTenSanPham(String tenSanPham) { this.tenSanPham = tenSanPham; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    public double getGiaBan() { return giaBan; }
    public void setGiaBan(double giaBan) { this.giaBan = giaBan; }

    public int getSoLuongTon() { return soLuongTon; }
    public void setSoLuongTon(int soLuongTon) { this.soLuongTon = soLuongTon; }

    public String getLinkHinhAnh() { return linkHinhAnh; }
    public void setLinkHinhAnh(String linkHinhAnh) { this.linkHinhAnh = linkHinhAnh; }

    public String getTenTrangTrai() { return tenTrangTrai; }
    public void setTenTrangTrai(String tenTrangTrai) { this.tenTrangTrai = tenTrangTrai; }

    public String getDiaChiTrangTrai() { return diaChiTrangTrai; }
    public void setDiaChiTrangTrai(String diaChiTrangTrai) { this.diaChiTrangTrai = diaChiTrangTrai; }

    public String getChungChi() { return chungChi; }
    public void setChungChi(String chungChi) { this.chungChi = chungChi; }

    public String getQuyTrinhCanhTac() { return quyTrinhCanhTac; }
    public void setQuyTrinhCanhTac(String quyTrinhCanhTac) { this.quyTrinhCanhTac = quyTrinhCanhTac; }

    public double getViDo() { return viDo; }
    public void setViDo(double viDo) { this.viDo = viDo; }

    public double getKinhDo() { return kinhDo; }
    public void setKinhDo(double kinhDo) { this.kinhDo = kinhDo; }
}
