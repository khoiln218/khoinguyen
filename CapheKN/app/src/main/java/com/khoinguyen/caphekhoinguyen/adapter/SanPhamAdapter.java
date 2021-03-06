package com.khoinguyen.caphekhoinguyen.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.khoinguyen.caphekhoinguyen.R;
import com.khoinguyen.caphekhoinguyen.controller.DBController;
import com.khoinguyen.caphekhoinguyen.model.SanPham;
import com.khoinguyen.caphekhoinguyen.utils.Utils;

import java.text.DecimalFormat;
import java.util.List;

public class SanPhamAdapter extends RecyclerView.Adapter<SanPhamAdapter.ViewHolder> {
    private final List<SanPham> mValues;
    private Context mContext;

    public SanPhamAdapter(Context context, List<SanPham> items) {
        this.mContext = context;
        this.mValues = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.san_pham_item, parent, false);
        return new SanPhamAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mTvTen.setText(holder.mItem.getTenSP());
        String formattedPrice = new DecimalFormat("##,##0VNĐ").format(holder.mItem.getDonGia());
        holder.mTvDonGia.setText(formattedPrice);

        holder.mView.setOnClickListener(v -> openMenu(holder.mItem, holder.mView));
    }

    private void openMenu(final SanPham sanPham, View view) {
        PopupMenu popup = new PopupMenu(mContext, view);
        popup.inflate(R.menu.menu_san_pham);
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.option_chinh_sua:
                    chinhSua(sanPham);
                    break;
            }
            return false;
        });
        popup.show();
    }

    private void chinhSua(final SanPham sp) {
        final SanPham sanPham = new SanPham();
        sanPham.setId(sp.getId());
        sanPham.setTenSP(sp.getTenSP());
        sanPham.setDonGia(sp.getDonGia());

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Chỉnh sửa sản phẩm");
        builder.setCancelable(false);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.dialog_them_san_pham, null);

        final EditText etTenSanPham = view.findViewById(R.id.etTenSanPham);
        final EditText etDonGia = view.findViewById(R.id.etDonGia);

        etTenSanPham.setText(sanPham.getTenSP());
        etTenSanPham.requestFocus();
        etDonGia.setText(String.valueOf(sanPham.getDonGia()));

        builder.setView(view);

        builder.setPositiveButton("Sửa", (dialog, which) -> {
            if (TextUtils.isEmpty(etTenSanPham.getText().toString().trim()) || TextUtils.isEmpty(etDonGia.getText().toString().trim())) {
                Utils.showToast(mContext, "Sửa thất bại. Vui lòng nhập tên và giá sản phẩm");
            } else if (DBController.getInstance(mContext).laySanPhamTheoTen(etTenSanPham.getText().toString().trim()) != null) {
                Utils.showToast(mContext, "Sửa thất bại. Bị trùng tên sản phẩm");
            } else {
                sanPham.setTenSP(etTenSanPham.getText().toString());
                sanPham.setDonGia(Long.valueOf(etDonGia.getText().toString().trim()));
                DBController.getInstance(mContext).capNhatSanPham(sanPham);
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView mTvTen;
        final TextView mTvDonGia;
        SanPham mItem;

        ViewHolder(@NonNull View view) {
            super(view);
            mView = view;
            mTvTen = view.findViewById(R.id.tvTen);
            mTvDonGia = view.findViewById(R.id.tvDonGia);
        }
    }
}
