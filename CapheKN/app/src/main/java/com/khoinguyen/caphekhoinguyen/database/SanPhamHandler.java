package com.khoinguyen.caphekhoinguyen.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.khoinguyen.caphekhoinguyen.model.SanPham;

import java.util.ArrayList;
import java.util.List;

public class SanPhamHandler {
    private static SanPhamHandler INSTANCE = null;

    private SQLiteDatabase db;
    private DBHelper dbHelper;
    private ContentValues values;

    private SanPhamHandler(Context context) {
        this.dbHelper = DBHelper.getInstance(context);
    }

    public static SanPhamHandler getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new SanPhamHandler(context);
        }
        return (INSTANCE);
    }

    public void insertOrUpdateSanPham(SanPham sanPham) {
        if (checkExits(sanPham.getId())) {
            updateSanPham(sanPham);
        } else {
            db = dbHelper.getWritableDatabase();

            values = new ContentValues();
            values.put(DBConstant.SAN_PHAM_ID, sanPham.getId());
            values.put(DBConstant.SAN_PHAM_TEN, sanPham.getTenSP());
            values.put(DBConstant.SAN_PHAM_DON_GIA, sanPham.getDonGia());

            db.insert(DBConstant.TABLE_NAME_SAN_PHAM, null, values);
            values = null;
            db.close();
        }
    }

    public long updateSanPham(SanPham sanPham) {
        db = dbHelper.getWritableDatabase();
        values = new ContentValues();
        values.put(DBConstant.SAN_PHAM_TEN, sanPham.getTenSP());
        values.put(DBConstant.SAN_PHAM_DON_GIA, sanPham.getDonGia());

        long rowUpdate = db.update(DBConstant.TABLE_NAME_SAN_PHAM, values, DBConstant.SAN_PHAM_ID + "=?", new String[]{String.valueOf(sanPham.getId())});
        values = null;
        db.close();
        return rowUpdate;
    }

    public SanPham getSanPhamById(String id) {
        String selectQuery = "SELECT  * FROM " + DBConstant.TABLE_NAME_SAN_PHAM
                + " WHERE " + DBConstant.SAN_PHAM_ID + " = " + "'" + id + "'";

        db = dbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {

            SanPham sanPham = new SanPham();
            sanPham.setId(cursor.getString(0));
            sanPham.setTenSP(cursor.getString(1));
            sanPham.setDonGia(Long.valueOf(cursor.getString(2)));
            cursor.close();
            db.close();
            return sanPham;
        } else {
            cursor.close();
            db.close();
            return null;
        }
    }

    public List<SanPham> getAllSanPham() {
        List<SanPham> sanPhams = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + DBConstant.TABLE_NAME_SAN_PHAM
                + " ORDER BY " + DBConstant.SAN_PHAM_TEN + " ASC";

        db = dbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                SanPham sanPham = new SanPham();
                sanPham.setId(cursor.getString(0));
                sanPham.setTenSP(cursor.getString(1));
                sanPham.setDonGia(Long.valueOf(cursor.getString(2)));
                sanPhams.add(sanPham);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return sanPhams;
    }

    public void deleteSanPham(String id) {
        db = dbHelper.getWritableDatabase();
        db.delete(DBConstant.TABLE_NAME_SAN_PHAM, DBConstant.SAN_PHAM_ID + " = ?",
                new String[]{String.valueOf(id)});
        db.close();
    }

    public int getSanPhamsCount() {
        String countQuery = "SELECT  * FROM " + DBConstant.TABLE_NAME_SAN_PHAM;
        db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int rowCount = cursor.getCount();
        cursor.close();
        db.close();
        return rowCount;
    }

    private boolean checkExits(String id) {
        return getSanPhamById(id) != null;
    }
}
