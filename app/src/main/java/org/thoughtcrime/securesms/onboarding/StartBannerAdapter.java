package org.thoughtcrime.securesms.onboarding;


import com.zhpan.bannerview.BaseBannerAdapter;
import com.zhpan.bannerview.BaseViewHolder;

import network.qki.messenger.R;

/**
 * Created by yuxiang.huang on 3/3/21 : 5:59 PM
 */
public class StartBannerAdapter extends BaseBannerAdapter<StartBanner> {
    @Override
    protected void bindData(BaseViewHolder<StartBanner> holder, StartBanner data, int position, int pageSize) {
        holder.setImageResource(R.id.ivSrc, data.getImgResId());
        holder.setText(R.id.tvDesc, data.getTitleResId());
    }

    @Override
    public int getLayoutId(int viewType) {
        return R.layout.item_start_banner;
    }


}
