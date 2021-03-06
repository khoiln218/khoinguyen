package com.khoinguyen.caphekhoinguyen.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;

import com.khoinguyen.caphekhoinguyen.R;
import com.khoinguyen.caphekhoinguyen.adapter.DonHangAdapter;
import com.khoinguyen.caphekhoinguyen.adapter.SimpleSectionedAdapter;
import com.khoinguyen.caphekhoinguyen.controller.DBController;
import com.khoinguyen.caphekhoinguyen.event.DonHangEvent;
import com.khoinguyen.caphekhoinguyen.model.DonHang;
import com.khoinguyen.caphekhoinguyen.utils.Constants;
import com.khoinguyen.caphekhoinguyen.utils.LogUtils;
import com.khoinguyen.caphekhoinguyen.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class LichSuGiaoDichFragment extends Fragment {
    private static final String TAG = "LichSuGiaoDichFragment";
    private BanHangFragment.OnBanHangInteractionListener mListener;

    private List<DonHang> mDonHangs;
    private RecyclerView mRecyclerView;
    private DonHangAdapter mAdapter;
    private SimpleSectionedAdapter mSectionedAdapter;
    private View layoutTotal;
    private View layoutMoney;
    private ImageButton btnGoUpDown;
    private TextView tvTotalCost;
    private Animation animGoUp;
    private Animation animGoDown;
    private MenuItem actionThanhToan;
    private DBController dbController;
    private String idKhachHang;
    private int trangThai;

    public LichSuGiaoDichFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lich_su_giao_dich, container, false);
        mRecyclerView = view.findViewById(R.id.rvLichSuGiaoDich);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        layoutTotal = view.findViewById(R.id.layoutTotal);
        btnGoUpDown = view.findViewById(R.id.btnGoUpDown);
        layoutMoney = view.findViewById(R.id.layoutMoney);
        tvTotalCost = view.findViewById(R.id.tvTotalCost);
        btnGoUpDown.setImageResource(layoutMoney.getVisibility() == View.VISIBLE ? R.drawable.orderlist_02 : R.drawable.orderlist_01);
        btnGoUpDown.setOnClickListener(v -> {
            if (layoutMoney.getVisibility() == View.GONE) {
                btnGoUpDown.setImageResource(R.drawable.orderlist_02);
                layoutTotal.startAnimation(animGoUp);
            } else {
                btnGoUpDown.setImageResource(R.drawable.orderlist_01);
                layoutTotal.startAnimation(animGoDown);
            }
        });

        animGoUp = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_up);
        animGoUp.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                layoutMoney.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        animGoDown = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_down);
        animGoDown.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                layoutMoney.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        idKhachHang = getArguments().getString("idKhachHang");
        trangThai = getArguments().getInt("trangThai");

        if (trangThai == Constants.TRANG_THAI_HOAN_THANH) {
            layoutTotal.setVisibility(View.GONE);
        }

        dbController = DBController.getInstance(getActivity());
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        actionThanhToan = menu.findItem(R.id.action_thanh_toan);
        actionThanhToan.setVisible(false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main_ban_hang, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_thanh_toan:
                thanhToan();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void thanhToan() {
        mAdapter.thanhToanList();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (null != mSectionedAdapter) {
            mRecyclerView.setAdapter(mSectionedAdapter);
        } else {
            getDonHangs();
        }
    }

    private void getDonHangs() {
        showLoading();
        if (actionThanhToan != null)
            actionThanhToan.setVisible(false);
        new MyTask().execute();
    }

    private void showLoading() {
//        Utils.showProgressDialog(getActivity());
    }

    private void hideLoading() {
//        Utils.hideProgressDialog();
    }

    private List<SimpleSectionedAdapter.Section> getSections() {
        if (mDonHangs.size() == 0) return new ArrayList<>();
        List<SimpleSectionedAdapter.Section> sections = new ArrayList<>();
        sections.add(new SimpleSectionedAdapter.Section(0, mDonHangs.get(0).getThoiGianTao()));
        for (int i = 1; i < mDonHangs.size(); i++) {
            if (!TextUtils.equals(Utils.convTimestamp(mDonHangs.get(i).getThoiGianTao(), "dd/MM/yyyy"), Utils.convTimestamp(mDonHangs.get(i - 1).getThoiGianTao(), "dd/MM/yyyy")))
                sections.add(new SimpleSectionedAdapter.Section(i, mDonHangs.get(i).getThoiGianTao()));
        }
        return sections;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof TrangChuFragment.OnTrangChuInteractionListener) {
            mListener = (BanHangFragment.OnBanHangInteractionListener) context;
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DonHangEvent event) {
        LogUtils.d(TAG, "onMessageEvent: " + event.getDonHang().getId());
        getDonHangs();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @SuppressLint("StaticFieldLeak")
    private class MyTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            mDonHangs = trangThai == Constants.TRANG_THAI_DANG_XY_LY ? dbController.layDonHangDangXuLyTheoKhachHang(idKhachHang) : dbController.layDonHangHoanThanhTheoKhachHang(idKhachHang);
            mAdapter = new DonHangAdapter(getActivity(), mDonHangs, new BanHangFragment.OnDonHangListerner() {
                @Override
                public void onShow() {
                    actionThanhToan.setVisible(true);
                }

                @Override
                public void onHide() {
                    actionThanhToan.setVisible(false);
                }

                @Override
                public void onUpdateTongTien(long tongTien) {
                    String formattedPrice = new DecimalFormat("##,##0VNĐ").format(tongTien);
                    tvTotalCost.setText(formattedPrice);
                }
            }, mListener, trangThai);
            mSectionedAdapter = new SimpleSectionedAdapter(getActivity(), getSections(), mAdapter);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mRecyclerView.setAdapter(mSectionedAdapter);
            hideLoading();
        }
    }
}
