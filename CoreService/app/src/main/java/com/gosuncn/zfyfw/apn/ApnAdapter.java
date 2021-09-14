package com.gosuncn.zfyfw.apn;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gosuncn.zfyfw.R;

import java.util.List;

/**
 * @author: Administrator
 * @date: 2020/7/24
 */
public class ApnAdapter extends BaseAdapter {

    private List<APNModel> Datas;
    private Context mContext;

    public ApnAdapter(List<APNModel> datas, Context mContext) {
        Datas = datas;
        this.mContext = mContext;
    }

    /**
     * 返回item的个数
     * @return
     */
    @Override
    public int getCount() {
        return Datas.size();
    }

    /**
     * 返回每一个item对象
     * @param i
     * @return
     */
    @Override
    public Object getItem(int i) {
        return Datas.get(i);
    }

    /**
     * 返回每一个item的id
     * @param i
     * @return
     */
    @Override
    public long getItemId(int i) {
        return i;
    }

    /**
     * 暂时不做优化处理，后面会专门整理BaseAdapter的优化
     * @param i
     * @param view
     * @param viewGroup
     * @return
     */
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        if (view == null)
        {
            view = LayoutInflater.from(mContext).inflate(R.layout.item_read_apn,viewGroup,false);
        }

        TextView textView1 = (TextView) view.findViewById(R.id.apn_id);
        TextView textView2 = (TextView) view.findViewById(R.id.apn_name);
        TextView textView3 = (TextView) view.findViewById(R.id.apn_host);
        textView1.setText(""+Datas.get(i).getApnId());
        textView2.setText(Datas.get(i).getSp());
        textView3.setText(Datas.get(i).getHost());
        return view;
    }


}
