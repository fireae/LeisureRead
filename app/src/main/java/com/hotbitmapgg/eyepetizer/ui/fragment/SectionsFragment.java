package com.hotbitmapgg.eyepetizer.ui.fragment;

import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.hotbitmapgg.eyepetizer.base.LazyFragment;
import com.hotbitmapgg.eyepetizer.network.RetrofitHelper;
import com.hotbitmapgg.rxzhihu.R;
import com.hotbitmapgg.eyepetizer.adapter.SectionsAdapter;
import com.hotbitmapgg.eyepetizer.model.DailySections;
import com.hotbitmapgg.eyepetizer.ui.activity.SectionsDetailsActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by hcc on 16/4/23 14:09
 * 100332338@qq.com
 * <p/>
 * 知乎专栏列表
 */
public class SectionsFragment extends LazyFragment
{

    @Bind(R.id.recycle)
    RecyclerView mRecyclerView;

    @Bind(R.id.swipe_refresh)
    SwipeRefreshLayout mSwipeRefreshLayout;

    private List<DailySections.DailySectionsInfo> sectionsInfos = new ArrayList<>();

    public static SectionsFragment newInstance()
    {

        return new SectionsFragment();
    }


    @Override
    public int getLayoutId()
    {

        return R.layout.fragment_sections;
    }

    @Override
    public void initViews()
    {

        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        mSwipeRefreshLayout.setOnRefreshListener(this::getSections);
        showProgress();
    }

    private void getSections()
    {

        RetrofitHelper.getLastZhiHuApi().getZhiHuSections()
                .compose(bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(dailySections -> {

                    if (dailySections != null)
                    {
                        List<DailySections.DailySectionsInfo> data = dailySections.data;
                        if (data != null && data.size() > 0)
                        {
                            sectionsInfos.clear();
                            sectionsInfos.addAll(data);
                            finishGetSections();
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                    }
                }, throwable -> {

                    mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(false));

                    Snackbar.make(mRecyclerView, "加载失败,请重新下拉刷新数据.", Snackbar.LENGTH_SHORT).show();
                });
    }

    private void finishGetSections()
    {

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        SectionsAdapter mAdapter = new SectionsAdapter(mRecyclerView, sectionsInfos);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener((position, holder) -> {

            DailySections.DailySectionsInfo dailySectionsInfo = sectionsInfos.get(position);
            SectionsDetailsActivity.luancher(getActivity(), dailySectionsInfo.id);
        });
    }

    public void showProgress()
    {

        mSwipeRefreshLayout.postDelayed(() -> {

            mSwipeRefreshLayout.setRefreshing(true);
            getSections();
        }, 500);
    }
}