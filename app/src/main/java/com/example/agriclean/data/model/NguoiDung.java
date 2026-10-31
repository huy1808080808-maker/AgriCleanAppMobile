package com.example.agriclean.data.model;

import com.google.firebase.Timestamp;

public class NguoiDung {
    private String maNguoiDung;
    private String hoVaTen;
    private String email;
    private String soDienThoai;
    private String vaiTro; // "customer" hoặc "admin"
    private Timestamp ngayTao;

    public NguoiDung() {
        // Bắt buộc để Firestore map dữ liệu tự động
    }

    public NguoiDung(String maNguoiDung, String hoVaTen, String email, String soDienThoai, String vaiTro, Timestamp ngayTao) {
        this.maNguoiDung = maNguoiDung;
        this.hoVaTen = hoVaTen;
        this.email = email;
        this.soDienThoai = soDienThoai;
        this.vaiTro = vaiTro;
        this.ngayTao = ngayTao;
    }

    public String getMaNguoiDung() { return maNguoiDung; }
    public void setMaNguoiDung(String maNguoiDung) { this.maNguoiDung = maNguoiDung; }

    public String getHoVaTen() { return hoVaTen; }
    public void setHoVaTen(String hoVaTen) { this.hoVaTen = hoVaTen; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }

    public String getVaiTro() { return vaiTro; }
    public void setVaiTro(String vaiTro) { this.vaiTro = vaiTro; }

    public Timestamp getNgayTao() { return ngayTao; }
    public void setNgayTao(Timestamp ngayTao) { this.ngayTao = ngayTao; }
}
