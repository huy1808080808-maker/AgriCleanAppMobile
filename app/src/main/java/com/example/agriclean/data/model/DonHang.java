package com.example.agriclean.data.model;

import com.google.firebase.Timestamp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DonHang implements Serializable {
    private String maDonHang;
    private String maKhachHang;
    private String tenNguoiNhan;
    private String soDienThoaiNhan;
    private String diaChiGiaoHang;
    private double tongTien;
    private String trangThaiDon; // "Chờ duyệt", "Đã xác nhận", "Đang giao", "Hoàn thành", "Đã huỷ"
    private Timestamp ngayDatHang;
    private List<ChiTietDonHang> danhSachChiTiet = new ArrayList<>();

    public DonHang() {
    }

    public DonHang(String maDonHang, String maKhachHang, String tenNguoiNhan, String soDienThoaiNhan,
                   String diaChiGiaoHang, double tongTien, String trangThaiDon, Timestamp ngayDatHang,
                   List<ChiTietDonHang> danhSachChiTiet) {
        this.maDonHang = maDonHang;
        this.maKhachHang = maKhachHang;
        this.tenNguoiNhan = tenNguoiNhan;
        this.soDienThoaiNhan = soDienThoaiNhan;
        this.diaChiGiaoHang = diaChiGiaoHang;
        this.tongTien = tongTien;
        this.trangThaiDon = trangThaiDon;
        this.ngayDatHang = ngayDatHang;
        this.danhSachChiTiet = danhSachChiTiet != null ? danhSachChiTiet : new ArrayList<>();
    }

    public String getMaDonHang() { return maDonHang; }
    public void setMaDonHang(String maDonHang) { this.maDonHang = maDonHang; }

    public String getMaKhachHang() { return maKhachHang; }
    public void setMaKhachHang(String maKhachHang) { this.maKhachHang = maKhachHang; }

    public String getTenNguoiNhan() { return tenNguoiNhan; }
    public void setTenNguoiNhan(String tenNguoiNhan) { this.tenNguoiNhan = tenNguoiNhan; }

    public String getSoDienThoaiNhan() { return soDienThoaiNhan; }
    public void setSoDienThoaiNhan(String soDienThoaiNhan) { this.soDienThoaiNhan = soDienThoaiNhan; }

    public String getDiaChiGiaoHang() { return diaChiGiaoHang; }
    public void setDiaChiGiaoHang(String diaChiGiaoHang) { this.diaChiGiaoHang = diaChiGiaoHang; }

    public double getTongTien() { return tongTien; }
    public void setTongTien(double tongTien) { this.tongTien = tongTien; }

    public String getTrangThaiDon() { return trangThaiDon; }
    public void setTrangThaiDon(String trangThaiDon) { this.trangThaiDon = trangThaiDon; }

    public Timestamp getNgayDatHang() { return ngayDatHang; }
    public void setNgayDatHang(Timestamp ngayDatHang) { this.ngayDatHang = ngayDatHang; }

    public List<ChiTietDonHang> getDanhSachChiTiet() { return danhSachChiTiet; }
    public void setDanhSachChiTiet(List<ChiTietDonHang> danhSachChiTiet) { this.danhSachChiTiet = danhSachChiTiet; }
}
