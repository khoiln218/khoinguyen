package com.khoinguyen.caphekhoinguyen.fragment;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.khoinguyen.caphekhoinguyen.R;
import com.khoinguyen.caphekhoinguyen.adapter.KhachHangAdapter;
import com.khoinguyen.caphekhoinguyen.controller.DBController;
import com.khoinguyen.caphekhoinguyen.model.DonHang;
import com.khoinguyen.caphekhoinguyen.model.KhachHang;
import com.khoinguyen.caphekhoinguyen.model.SanPham;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class CongNoFragment extends Fragment {
    private KhachHangFragment.OnKhachHangInteractionListener mListener;

    private List<KhachHang> mKhachHangs;
    private RecyclerView mRecyclerView;
    private KhachHangAdapter mAdapter;
    private DBController dbController;

    public CongNoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cong_no, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.rvKhachHang);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        dbController = new DBController(getActivity());
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (null != mAdapter) {
            mRecyclerView.setAdapter(mAdapter);
        } else {
            getKhachHangs();
        }
    }

    private void getKhachHangs() {
        List<KhachHang> khachHangs = dbController.layDanhSachKhachHang();
        mKhachHangs = new ArrayList<>();
        for (KhachHang khachHang : khachHangs) {
            if (getTongTien(khachHang.getId()) > 0)
                mKhachHangs.add(khachHang);
        }
        mAdapter = new KhachHangAdapter(getActivity(), mKhachHangs, mListener);
        mRecyclerView.setAdapter(mAdapter);
    }

    private long getTongTien(int id) {
        List<DonHang> donHangs = dbController.layDonHangDangXuLyTheoKhachHang(id);
        long tongTien = 0;
        for (DonHang donHang : donHangs) {
            for (SanPham sanPham : donHang.getSanPhams()) {
                tongTien += sanPham.getDonGia();
            }
        }
        return tongTien;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof TrangChuFragment.OnTrangChuInteractionListener) {
            mListener = (KhachHangFragment.OnKhachHangInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnOrderInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
