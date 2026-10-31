package com.example.agriclean.data.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;

import java.io.Serializable;

/**
 * Model NguoiDung đại diện cho đối tượng người dùng trong hệ thống AgriClean.
 * Lưu trữ trên Cloud Firestore tại collection "Users".
 * Hệ thống chỉ phân 2 quyền (Role): "admin" và "customer".
 */
public class NguoiDung implements Serializable {

    // Định nghĩa hằng số vai trò (Role)
    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_CUSTOMER = "customer";

    private String uid;
    private String email;
    private String role; // "admin" hoặc "customer"
    private String hoTen;
    private String sdt;
    private String diaChi;

    /**
     * Constructor mặc định không tham số (Bắt buộc cho Firebase Firestore mapping)
     */
    public NguoiDung() {
    }

    /**
     * Constructor khởi tạo nhanh khi đăng ký (chỉ cần Email và Role mặc định)
     */
    public NguoiDung(String uid, String email, String role) {
        this.uid = uid;
        this.email = email;
        this.role = role;
        this.hoTen = "";
        this.sdt = "";
        this.diaChi = "";
    }

    /**
     * Constructor đầy đủ thông tin
     */
    public NguoiDung(String uid, String email, String role, String hoTen, String sdt, String diaChi) {
        this.uid = uid;
        this.email = email;
        this.role = role;
        this.hoTen = hoTen != null ? hoTen : "";
        this.sdt = sdt != null ? sdt : "";
        this.diaChi = diaChi != null ? diaChi : "";
    }

    /**
     * Constructor tương thích ngược cho các màn hình hiện hữu
     */
    public NguoiDung(String uid, String hoTen, String email, String sdt, String role, Timestamp ngayTao) {
        this.uid = uid;
        this.hoTen = hoTen != null ? hoTen : "";
        this.email = email;
        this.sdt = sdt != null ? sdt : "";
        this.role = role;
        this.diaChi = "";
    }

    // --- Getter và Setter chính thức được lưu trên Firestore ---

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getSdt() {
        return sdt;
    }

    public void setSdt(String sdt) {
        this.sdt = sdt;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    // --- Phương thức tiện ích (đánh dấu @Exclude để Firestore không lưu thành field riêng) ---

    @Exclude
    public boolean isAdmin() {
        return ROLE_ADMIN.equalsIgnoreCase(this.role);
    }

    @Exclude
    public boolean isCustomer() {
        return ROLE_CUSTOMER.equalsIgnoreCase(this.role);
    }

    // Alias hỗ trợ tương thích mã cũ (đánh dấu @Exclude)
    @Exclude
    public String getMaNguoiDung() {
        return uid;
    }

    @Exclude
    public void setMaNguoiDung(String maNguoiDung) {
        this.uid = maNguoiDung;
    }

    @Exclude
    public String getHoVaTen() {
        return hoTen;
    }

    @Exclude
    public void setHoVaTen(String hoVaTen) {
        this.hoTen = hoVaTen;
    }

    @Exclude
    public String getVaiTro() {
        return role;
    }

    @Exclude
    public void setVaiTro(String vaiTro) {
        this.role = vaiTro;
    }

    @Exclude
    public String getSoDienThoai() {
        return sdt;
    }

    @Exclude
    public void setSoDienThoai(String soDienThoai) {
        this.sdt = soDienThoai;
    }
}
