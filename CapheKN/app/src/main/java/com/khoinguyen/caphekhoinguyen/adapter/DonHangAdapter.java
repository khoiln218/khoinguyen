package com.khoinguyen.caphekhoinguyen.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.khoinguyen.caphekhoinguyen.R;
import com.khoinguyen.caphekhoinguyen.controller.DBController;
import com.khoinguyen.caphekhoinguyen.fragment.BanHangFragment;
import com.khoinguyen.caphekhoinguyen.model.DonHang;
import com.khoinguyen.caphekhoinguyen.model.KhachHang;
import com.khoinguyen.caphekhoinguyen.model.SanPham;
import com.khoinguyen.caphekhoinguyen.utils.Constants;
import com.khoinguyen.caphekhoinguyen.utils.Utils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import me.next.tagview.TagCloudView;

public class DonHangAdapter extends RecyclerView.Adapter<DonHangAdapter.ViewHolder> {
    private List<DonHang> mValues;
    private Context mContext;
    private BanHangFragment.OnDonHangListerner mDonHangListerner;
    private BanHangFragment.OnBanHangInteractionListener mBanHangListerner;
    private int mTrangThai;
    private AlertDialog alertDialog;
    private List<String> itemStateArray = new ArrayList<>();

    public DonHangAdapter(Context context, List<DonHang> items, BanHangFragment.OnDonHangListerner donHangListerner, BanHangFragment.OnBanHangInteractionListener banHangListener) {
        this(context, items, donHangListerner, banHangListener, Constants.TRANG_THAI_DANG_XY_LY);
    }

    public DonHangAdapter(Context context, List<DonHang> items, BanHangFragment.OnDonHangListerner donHangListerner, BanHangFragment.OnBanHangInteractionListener banHangListener, int trangThai) {
        mContext = context;
        mValues = items;
        mDonHangListerner = donHangListerner;
        mBanHangListerner = banHangListener;
        mTrangThai = trangThai;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.don_hang_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.mItem = mValues.get(position);
        holder.mTvThoiGianTao.setText(Utils.convTimestamp(holder.mItem.getThoiGianTao(), "HH:mm"));
        if (holder.mItem.getIdKhachHang() != null) {
            KhachHang khachHang = DBController.getInstance(mContext).layKhachHangTheoId(holder.mItem.getIdKhachHang());
            holder.mTvKhachHang.setText(khachHang.getTenKH());
            TextDrawable drawable = TextDrawable.builder()
                    .round().build(String.valueOf(khachHang.getTenKH().charAt(0)), ColorGenerator.MATERIAL.getColor(khachHang.getTenKH()));
            holder.ivIcon.setImageDrawable(drawable);
        } else {
            holder.mTvKhachHang.setText(R.string.name_khach_vang_lai);
            TextDrawable drawable = TextDrawable.builder()
                    .round().build("K", ColorGenerator.MATERIAL.getColor("Khách vãng lai"));
            holder.ivIcon.setImageDrawable(drawable);
        }
        List<SanPham> sanPhams = new ArrayList<>();
        for (String id : holder.mItem.getIdSanPhams())
            sanPhams.add(DBController.getInstance(mContext).laySanPhamTheoId(id));
        if (!sanPhams.isEmpty()) {
            holder.tcvSanPham.setTags(getTagSanPhams(sanPhams));
            String formattedPrice = new DecimalFormat("##,##0VNĐ").format(holder.mItem.getTongTien(mContext));
            holder.mTvTongTien.setText(formattedPrice);
        }

        holder.mView.setBackgroundColor(itemStateArray.contains(holder.mItem.getId()) ? ContextCompat.getColor(mContext, R.color.colorDonHangSelect) : Color.WHITE);

        holder.mView.setOnClickListener(v -> {
            if (mTrangThai == Constants.TRANG_THAI_DANG_XY_LY) {
                if (itemStateArray.contains(holder.mItem.getId())) {
                    itemStateArray.remove(holder.mItem.getId());
                    holder.mView.setBackgroundColor(Color.WHITE);
                    if (itemStateArray.isEmpty())
                        mDonHangListerner.onHide();
                    else
                        mDonHangListerner.onShow();
                    selectChange();
                } else {
                    itemStateArray.add(holder.mItem.getId());
                    holder.mView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorDonHangSelect));
                    mDonHangListerner.onShow();
                    selectChange();
                }
            } else if (mTrangThai == Constants.TRANG_THAI_HOAN_THANH) {
                PopupMenu popup = new PopupMenu(mContext, holder.mView);
                popup.inflate(R.menu.menu_don_hang_hoan_thanh);
                popup.setOnMenuItemClickListener(item -> {
                    switch (item.getItemId()) {
                        case R.id.option_huy:
                            huy(holder.mItem);
                            break;
                    }
                    return false;
                });
                popup.show();
            }
        });

        holder.btnThanhToan.setOnClickListener(v -> thanhToan(holder.mItem));

        holder.btnChinhSua.setOnClickListener(v -> chinhSua(holder.mItem, holder.btnChinhSua));

        if (!TextUtils.equals(holder.mItem.getTrangThai(), mContext.getString(R.string.status_dang_xu_ly))) {
            holder.mSeparateLine.setVisibility(View.GONE);
            holder.mLayoutChinhSua.setVisibility(View.GONE);
        }
    }

    private void selectChange() {
        mDonHangListerner.onUpdateTongTien(tongTien());
    }

    public void clearSelect() {
        itemStateArray.clear();
        mDonHangListerner.onHide();
        notifyDataSetChanged();
        selectChange();
    }

    private void chinhSua(final DonHang donHang, View view) {
        PopupMenu popup = new PopupMenu(mContext, view);
        popup.inflate(R.menu.menu_don_hang);
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.option_chinh_sua:
                    capNhat(donHang);
                    break;
                case R.id.option_huy:
                    huy(donHang);
                    break;
            }
            return false;
        });
        popup.show();
    }

    private void huy(final DonHang donHang) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage("Hủy đơn hàng?")
                .setPositiveButton("Đồng ý", (dialog, id) -> {
                    donHang.setTrangThai(mContext.getString(R.string.status_da_huy));
                    DBController.getInstance(mContext).capNhatDonHang(donHang);
                    clearSelect();
                })
                .setNegativeButton("Hủy", (dialog, id) -> {
                    clearSelect();
                    dialog.cancel();
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void capNhat(final DonHang dh) {
        final DonHang donHang = new DonHang();
        donHang.setId(dh.getId());
        donHang.setThoiGianTao(dh.getThoiGianTao());
        donHang.setTrangThai(dh.getTrangThai());
        donHang.setIdKhachHang(dh.getIdKhachHang());

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Chỉnh sửa đơn hàng");
        builder.setCancelable(false);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.dialog_them_don_hang, null);

        final List<KhachHang> khachHangs = DBController.getInstance(mContext).layDanhSachKhachHang();
        KhachHangArrayAdapter adapterKH = new KhachHangArrayAdapter(mContext, android.R.layout.simple_dropdown_item_1line, khachHangs);
        AutoCompleteTextView etKhachHang = view.findViewById(R.id.etKhachHang);
        etKhachHang.setThreshold(1);
        etKhachHang.setAdapter(adapterKH);
        if (!TextUtils.isEmpty(donHang.getIdKhachHang()))
            etKhachHang.setText(DBController.getInstance(mContext).layKhachHangTheoId(donHang.getIdKhachHang()).getTenKH());
        etKhachHang.setOnItemClickListener((parent, v, position, id) -> {
            KhachHang khachHang = (KhachHang) parent.getItemAtPosition(position);
            donHang.setIdKhachHang(khachHang.getId());
        });

        final List<SanPham> sanPhams = DBController.getInstance(mContext).layDanhSachSanPham();
        SanPhamArrayAdapter adapterSP = new SanPhamArrayAdapter(mContext, android.R.layout.simple_dropdown_item_1line, sanPhams);
        final MultiAutoCompleteTextView etSanPham = view.findViewById(R.id.etSanPham);
        etSanPham.setThreshold(1);
        etSanPham.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        etSanPham.setAdapter(adapterSP);
        StringBuilder sanPhamString = new StringBuilder();
        for (String id : dh.getIdSanPhams()) {
            sanPhamString.append(DBController.getInstance(mContext).laySanPhamTheoId(id).getTenSP()).append(",");
        }
        etSanPham.setText(sanPhamString.toString());
        etSanPham.requestFocus();

        final Button btnThemKhachHang = view.findViewById(R.id.btnThemKhachHang);
        btnThemKhachHang.setOnClickListener(v -> {
            if (mBanHangListerner != null)
                mBanHangListerner.onThemKhachHangClick();
            alertDialog.cancel();
        });

        final Button btnThemSanPham = view.findViewById(R.id.btnThemSanPham);
        btnThemSanPham.setOnClickListener(v -> {
            if (mBanHangListerner != null)
                mBanHangListerner.onThemSanPhamClick();
            alertDialog.cancel();
        });

        builder.setView(view);

        builder.setPositiveButton("Sửa", (dialog, which) -> {
            String[] tenSPs = etSanPham.getText().toString().split(",");
            for (String tenSP : tenSPs) {
                for (SanPham sanPham : sanPhams) {
                    if (TextUtils.equals(tenSP.trim(), sanPham.getTenSP())) {
                        donHang.addSanPham(sanPham);
                        break;
                    }
                }
            }
            clearSelect();
            if (donHang.getIdSanPhams() != null) {
                DBController.getInstance(mContext).capNhatDonHang(donHang);
            } else {
                Utils.showToast(mContext, "Chỉnh sửa đơn hàng thất bại");
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> {
            clearSelect();
            dialog.cancel();
        });

        alertDialog = builder.create();
        alertDialog.show();
    }

    private void thanhToan(final DonHang donHang) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage("Thanh toán đơn hàng?")
                .setPositiveButton("Đồng ý", (dialog, id) -> {
                    donHang.setTrangThai(mContext.getString(R.string.status_hoan_thanh));
                    DBController.getInstance(mContext).capNhatDonHang(donHang);
                    clearSelect();
                })
                .setNegativeButton("Hủy", (dialog, id) -> {
                    clearSelect();
                    dialog.cancel();
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private List<String> getTagSanPhams(List<SanPham> sanPhams) {
        List<String> sanPhamString = new ArrayList<>();
        for (SanPham sanPham : sanPhams) {
            sanPhamString.add(sanPham.getTenSP());
        }
        return sanPhamString;
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public void thanhToanList() {
        if (itemStateArray.isEmpty()) {
            Utils.showToast(mContext, "Vui lòng chọn đơn hàng cần thanh toán ");
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            String formattedPrice = new DecimalFormat("##,##0VNĐ").format(tongTien());
            builder.setTitle("Thanh toán đơn hàng?")
                    .setMessage("Thành tiền " + formattedPrice)
                    .setPositiveButton("Đồng ý", (dialog, id) -> {
                        for (String idDonHang : itemStateArray) {
                            DonHang donHang = DBController.getInstance(mContext).layDonHangTheoId(idDonHang);
                            donHang.setTrangThai(mContext.getString(R.string.status_hoan_thanh));
                            DBController.getInstance(mContext).capNhatDonHang(donHang);
                        }
                        clearSelect();
                    })
                    .setNegativeButton("Hủy", (dialog, id) -> {
                        clearSelect();
                        dialog.cancel();
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    private long tongTien() {
        long tong = 0;
        for (String id : itemStateArray) {
            DonHang dh = DBController.getInstance(mContext).layDonHangTheoId(id);
            tong += dh.getTongTien(mContext);
        }
        return tong;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView mTvThoiGianTao;
        final ImageView ivIcon;
        final TextView mTvKhachHang;
        final TagCloudView tcvSanPham;
        final TextView mTvTongTien;
        final LinearLayout mSeparateLine;
        final LinearLayout mLayoutChinhSua;
        final ImageButton btnThanhToan;
        final ImageButton btnChinhSua;
        DonHang mItem;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mTvThoiGianTao = view.findViewById(R.id.tvThoiGianTao);
            ivIcon = view.findViewById(R.id.ivIcon);
            mTvKhachHang = view.findViewById(R.id.tvKhachHang);
            tcvSanPham = view.findViewById(R.id.tcvSanPham);
            mTvTongTien = view.findViewById(R.id.tvTongTien);
            mSeparateLine = view.findViewById(R.id.separateLine);
            mLayoutChinhSua = view.findViewById(R.id.layoutChinhSua);
            btnThanhToan = view.findViewById(R.id.btnThanhToan);
            btnChinhSua = view.findViewById(R.id.btnChinhSua);
        }
    }
}
