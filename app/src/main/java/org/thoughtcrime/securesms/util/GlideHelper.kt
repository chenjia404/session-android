package org.thoughtcrime.securesms.util

import android.content.Context
import android.net.Uri
import android.text.TextUtils
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import network.qki.messenger.R
import org.thoughtcrime.securesms.conversation.v2.ViewUtil

/**
 * Created by Author on 2020/4/30
 */
object GlideHelper {
    private var mContext: Context? = null

    //在Application中初始化GlideHelper
    fun initGlideHelper(context: Context?) {
        mContext = context
    }

    //加载网络图片
    fun showImage(view: ImageView?, url: String?, isRound: Int) {
        val options =
            RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).timeout(60000).priority(
                Priority.HIGH
            )
        when (isRound) {
            1 -> {  //圆形带有边框效果
                options.transform(GlideCircleStrokeTransform())
            }

            2 -> {  //圆形无边框效果
                options.transform(CircleCrop())
            }

            3 -> {  //自定义所加载View的弧度
                options.transform(
                    CenterCrop(), RoundedCorners(
                        mContext!!.resources.getDimensionPixelOffset(R.dimen.text_view_corner_radius)
                    )
                )
            }
        }
        val builder: RequestBuilder<*> = Glide.with(mContext!!).load(url).apply(options)
        builder.into(view!!)
    }

    //加载本地资源图片
    fun showImage(view: ImageView?, resourceId: Int, isRound: Int) {
        val options =
            RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).timeout(60000).priority(
                Priority.HIGH
            )
        when (isRound) {
            1 -> {
                options.transform(GlideCircleStrokeTransform())
            }

            2 -> {
                options.transform(CircleCrop())
            }

            3 -> {
                options.transform(
                    CenterCrop(), RoundedCorners(
                        mContext!!.resources.getDimensionPixelOffset(R.dimen.text_view_corner_radius)
                    )
                )
            }
        }
        val builder: RequestBuilder<*> = Glide.with(mContext!!).load(resourceId).apply(options)
        builder.into(view!!)
    }

    //设置默认占位图和加载出现错误时的缺省图
    fun showImage(
        view: ImageView?,
        url: String,
        roundRadius: Int = 0,
        placeHolder: Int,
        errorHolder: Int
    ) {
        val options = RequestOptions().timeout(60000).centerCrop()
            .placeholder(placeHolder).error(errorHolder).priority(
                Priority.HIGH
            )
        if (roundRadius != 0) {
            options.transform(
                CenterCrop(), RoundedCorners(ViewUtil.dpToPx(roundRadius))
            )
        }
        val builder: RequestBuilder<*> = Glide.with(mContext!!).load(url).apply(options)
        builder.into(view!!)
    }

    fun showImage(
        view: ImageView?,
        uri: Uri,
        roundRadius: Int = 0,
        placeHolder: Int,
        errorHolder: Int
    ) {
        val options = RequestOptions().timeout(60000).centerCrop()
            .placeholder(placeHolder).error(errorHolder).priority(
                Priority.HIGH
            )
        if (roundRadius != 0) {
            options.transform(
                CenterCrop(), RoundedCorners(ViewUtil.dpToPx(roundRadius))
            )
        }
        val builder: RequestBuilder<*> = Glide.with(mContext!!).load(uri).apply(options)
        builder.into(view!!)
    }

    //加载Gif
    fun showGifImage(view: ImageView?, url: String?) {
        val options =
            RequestOptions().diskCacheStrategy(DiskCacheStrategy.RESOURCE).timeout(60000).priority(
                Priority.HIGH
            )
        Glide.with(mContext!!).load(url).apply(options).into(view!!)
    }

    //加载本地图片
    fun showImage(view: ImageView?, resourceId: Int) {
        val options =
            RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).timeout(60000).priority(
                Priority.HIGH
            )
        val builder: RequestBuilder<*> = Glide.with(mContext!!).load(resourceId).apply(options)
        builder.into(view!!)
    }

    //ImageView加载本地图片
    fun showImage(view: ImageView?, url: String?) {
        showImageWithTarget(view, url)
    }

    //View加载网络图片
    fun showImageWithTarget(view: ImageView?, url: String?) {
        if (!TextUtils.isEmpty(url)) {
            showImage(view, url, 0)
        }
    }

    //View加载本地图片
    fun showImageWithTarget(view: ImageView?, resourceId: Int, isRound: Int) {
        showImage(view, resourceId, isRound)
    }

}