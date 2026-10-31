package com.example.agriclean.data.model;

import java.io.Serializable;

public class DanhMuc implements Serializable {
    private String maDanhMuc;
    private String tenDanhMuc;
    private String linkBieuTuong;

    public DanhMuc() {
    }

    public DanhMuc(String maDanhMuc, String tenDanhMuc, String linkBieuTuong) {
        this.maDanhMuc = maDanhMuc;
        this.tenDanhMuc = tenDanhMuc;
        this.linkBieuTuong = linkBieuTuong;
    }

    public String getMaDanhMuc() { return maDanhMuc; }
    public void setMaDanhMuc(String maDanhMuc) { this.maDanhMuc = maDanhMuc; }

    public String getTenDanhMuc() { return tenDanhMuc; }
    public void setTenDanhMuc(String tenDanhMuc) { this.tenDanhMuc = tenDanhMuc; }

    public String getLinkBieuTuong() { return linkBieuTuong; }
    public void setLinkBieuTuong(String linkBieuTuong) { this.linkBieuTuong = linkBieuTuong; }
}
