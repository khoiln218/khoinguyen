package com.khoinguyen.caphekhoinguyen.fragment;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.khoinguyen.caphekhoinguyen.R;
import com.khoinguyen.caphekhoinguyen.adapter.SanPhamAdapter;
import com.khoinguyen.caphekhoinguyen.controller.DBController;
import com.khoinguyen.caphekhoinguyen.model.SanPham;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class SanPhamFragment extends Fragment {
    private static final String TAG = "SanPhamFragment";

    private List<SanPham> mSanPhams;
    private RecyclerView mRecyclerView;
    private SanPhamAdapter mAdapter;
    private DBController dbController;

    public SanPhamFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ban_hang, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                themSanPham();
            }
        });

        dbController = new DBController(getActivity());
        return view;
    }

    private void themSanPham() {
        final SanPham sanPham = new SanPham();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Thêm sản phẩm");
        builder.setCancelable(false);

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View view = inflater.inflate(R.layout.dialog_them_san_pham, null);

        final EditText etTenSanPham = (EditText) view.findViewById(R.id.etTenSanPham);
        final EditText etDonGia = (EditText) view.findViewById(R.id.etDonGia);

        builder.setView(view);

        builder.setPositiveButton("Thêm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sanPham.setTenSP(etTenSanPham.getText().toString());
                sanPham.setDonGia(Double.valueOf(etDonGia.getText().toString().trim()));
                dbController.themSanPham(sanPham);
                mSanPhams.add(sanPham);
                mAdapter.notifyDataSetChanged();
            }
        });

        builder.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (null != mAdapter) {
            mRecyclerView.setAdapter(mAdapter);
        } else {
            getSanPhams();
        }
    }

    private void getSanPhams() {
        mSanPhams = dbController.layDanhSachSanPham();
        mAdapter = new SanPhamAdapter(getActivity(), mSanPhams);
        mRecyclerView.setAdapter(mAdapter);
    }
}
