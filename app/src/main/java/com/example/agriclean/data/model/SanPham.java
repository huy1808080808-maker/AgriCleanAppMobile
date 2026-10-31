package com.example.agriclean.data.model;

import com.google.firebase.firestore.Exclude;

import java.io.Serializable;
import java.text.DecimalFormat;

/**
 * Model SanPham đại diện cho nông sản sạch trên hệ thống AgriClean.
 * Lưu trữ tại Firestore collection "Products".
 * Gộp toàn bộ thông tin nông trại và nguồn gốc trực tiếp vào sản phẩm.
 */
public class SanPham implements Serializable {

    // Các hằng số danh mục chuẩn (UC06)
    public static final String DANH_MUC_TAT_CA = "Tất cả";
    public static final String DANH_MUC_RAU_XANH = "Rau xanh";
    public static final String DANH_MUC_CU_QUA = "Củ quả";
    public static final String DANH_MUC_TRAI_CAY = "Trái cây";
    public static final String DANH_MUC_NAM = "Nấm";

    // Hằng số chứng nhận
    public static final String CHUNG_NHAN_VIETGAP = "Đạt chuẩn VietGAP";
    public static final String CHUNG_NHAN_HUU_CO = "Hữu cơ";

    // 1. Thông tin cơ bản của nông sản
    private String productId;         // Mã định danh sản phẩm (dùng tạo mã QR & ID Firestore)
    private String tenSanPham;        // Tên nông sản sạch
    private String moTa;              // Mô tả chi tiết
    private double gia;               // Giá bán (VND)
    private String donViTinh;         // Đơn vị tính: ví dụ "hộp 500g", "kg", "bó"
    private String danhMuc;           // Danh mục: "Rau xanh", "Củ quả", "Trái cây", "Nấm"
    private int tonKho;               // Số lượng tồn kho
    private String hinhAnhUrl;        // Link ảnh lưu trên Firebase Storage

    // 2. Thông tin nguồn gốc & nông trại (gộp trực tiếp từ trang trại)
    private String tenTrangTrai;      // Ví dụ: "Trang trại Đà Lạt Eco Farm"
    private String chuTrangTrai;      // Ví dụ: "Ông Nguyễn Văn An"
    private String diaChiTrangTrai;   // Địa chỉ nhà vườn (phục vụ hiển thị & chỉ đường Google Maps UC09)
    private String chungNhan;         // Ví dụ: "Đạt chuẩn VietGAP" / "Hữu cơ"
    private String quyTrinhCanhTac;   // Nhật ký canh tác, ngày gieo trồng và ngày thu hoạch

    /**
     * Constructor mặc định không đối số (Bắt buộc cho Firebase Firestore mapping)
     */
    public SanPham() {
        this.donViTinh = "kg";
        this.chungNhan = CHUNG_NHAN_VIETGAP;
    }

    /**
     * Constructor đầy đủ thông tin phục vụ thêm/sửa sản phẩm (UC14)
     */
    public SanPham(String productId, String tenSanPham, String moTa, double gia,
                   String donViTinh, String danhMuc, int tonKho, String hinhAnhUrl,
                   String tenTrangTrai, String chuTrangTrai, String diaChiTrangTrai,
                   String chungNhan, String quyTrinhCanhTac) {
        this.productId = productId;
        this.tenSanPham = tenSanPham;
        this.moTa = moTa;
        this.gia = gia;
        this.donViTinh = donViTinh != null ? donViTinh : "kg";
        this.danhMuc = danhMuc;
        this.tonKho = tonKho;
        this.hinhAnhUrl = hinhAnhUrl;
        this.tenTrangTrai = tenTrangTrai;
        this.chuTrangTrai = chuTrangTrai;
        this.diaChiTrangTrai = diaChiTrangTrai;
        this.chungNhan = chungNhan != null ? chungNhan : CHUNG_NHAN_VIETGAP;
        this.quyTrinhCanhTac = quyTrinhCanhTac;
    }

    // --- Getter & Setter chính thức lưu trên Firestore ---

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getTenSanPham() {
        return tenSanPham;
    }

    public void setTenSanPham(String tenSanPham) {
        this.tenSanPham = tenSanPham;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public double getGia() {
        return gia;
    }

    public void setGia(double gia) {
        this.gia = gia;
    }

    public String getDonViTinh() {
        return donViTinh;
    }

    public void setDonViTinh(String donViTinh) {
        this.donViTinh = donViTinh;
    }

    public String getDanhMuc() {
        return danhMuc;
    }

    public void setDanhMuc(String danhMuc) {
        this.danhMuc = danhMuc;
    }

    public int getTonKho() {
        return tonKho;
    }

    public void setTonKho(int tonKho) {
        this.tonKho = tonKho;
    }

    public String getHinhAnhUrl() {
        return hinhAnhUrl;
    }

    public void setHinhAnhUrl(String hinhAnhUrl) {
        this.hinhAnhUrl = hinhAnhUrl;
    }

    public String getTenTrangTrai() {
        return tenTrangTrai;
    }

    public void setTenTrangTrai(String tenTrangTrai) {
        this.tenTrangTrai = tenTrangTrai;
    }

    public String getChuTrangTrai() {
        return chuTrangTrai;
    }

    public void setChuTrangTrai(String chuTrangTrai) {
        this.chuTrangTrai = chuTrangTrai;
    }

    public String getDiaChiTrangTrai() {
        return diaChiTrangTrai;
    }

    public void setDiaChiTrangTrai(String diaChiTrangTrai) {
        this.diaChiTrangTrai = diaChiTrangTrai;
    }

    public String getChungNhan() {
        return chungNhan;
    }

    public void setChungNhan(String chungNhan) {
        this.chungNhan = chungNhan;
    }

    public String getQuyTrinhCanhTac() {
        return quyTrinhCanhTac;
    }

    public void setQuyTrinhCanhTac(String quyTrinhCanhTac) {
        this.quyTrinhCanhTac = quyTrinhCanhTac;
    }

    // --- Các phương thức tiện ích & Alias tương thích (đánh dấu @Exclude) ---

    @Exclude
    public boolean isVietGAP() {
        return chungNhan != null && chungNhan.toLowerCase().contains("vietgap");
    }

    @Exclude
    public boolean isConHang() {
        return tonKho > 0;
    }

    @Exclude
    public String getGiaDinhDang() {
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(gia) + " đ";
    }

    // Alias tương thích ngược
    @Exclude
    public String getMaSanPham() {
        return productId;
    }

    @Exclude
    public void setMaSanPham(String maSanPham) {
        this.productId = maSanPham;
    }

    @Exclude
    public double getGiaBan() {
        return gia;
    }

    @Exclude
    public void setGiaBan(double giaBan) {
        this.gia = giaBan;
    }

    @Exclude
    public int getSoLuongTon() {
        return tonKho;
    }

    @Exclude
    public void setSoLuongTon(int soLuongTon) {
        this.tonKho = soLuongTon;
    }

    @Exclude
    public String getLinkHinhAnh() {
        return hinhAnhUrl;
    }

    @Exclude
    public void setLinkHinhAnh(String linkHinhAnh) {
        this.hinhAnhUrl = linkHinhAnh;
    }

    @Exclude
    public String getMaDanhMuc() {
        return danhMuc;
    }

    @Exclude
    public void setMaDanhMuc(String maDanhMuc) {
        this.danhMuc = maDanhMuc;
    }
}
