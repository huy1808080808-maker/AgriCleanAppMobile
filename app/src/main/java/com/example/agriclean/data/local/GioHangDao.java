package com.example.agriclean.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface GioHangDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void themVaoGio(GioHangItem item);

    @Update
    void capNhatGio(GioHangItem item);

    @Delete
    void xoaKhoiGio(GioHangItem item);

    @Query("SELECT * FROM gio_hang")
    LiveData<List<GioHangItem>> layDanhSachGioHang();

    @Query("SELECT * FROM gio_hang WHERE maSanPham = :maSp LIMIT 1")
    GioHangItem laySanPhamTheoMa(String maSp);

    @Query("DELETE FROM gio_hang")
    void xoaToanBoGioHang();
}
