package com.codekarhu.whatswithme;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by @author ${user} on ${date}
 * <p/>
 * ${file_name}
 */
public class DocTalkFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private ChatRecyclerAdapter mRecyclerAdapter;
    private LinearLayoutManager mLayoutManager;
    private View mRootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mRootView = inflater.inflate(R.layout.fragment_doc_talk, container, false);
        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.recycler);
        return mRootView;
    }


    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerAdapter = new ChatRecyclerAdapter(getActivity());
        mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, true);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mRecyclerAdapter.add(new Message(true, "Can I help you?"));
    }

    public void add(String text, boolean isWatson) {
        mRecyclerAdapter.add(new Message(isWatson, text));
    }

    public void addOrUpdate(String text) {
        mRecyclerAdapter.addOrUpdate(text);
    }


}
