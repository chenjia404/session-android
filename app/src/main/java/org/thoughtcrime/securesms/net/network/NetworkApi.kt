package org.thoughtcrime.securesms.net.network

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import org.session.libsession.BuildConfig
import org.thoughtcrime.securesms.constants.AppConst
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


//双重校验锁式-单例 封装NetApiService 方便直接快速调用简单的接口
val apiService: Api by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
    NetworkApi.INSTANCE.getApi(Api::class.java, AppConst.URLS.BASE)
}

class NetworkApi : BaseNetworkApi() {

    companion object {
        val INSTANCE: NetworkApi by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            NetworkApi()
        }
    }

    /**
     * 实现重写父类的setHttpClientBuilder方法，
     * 在这里可以添加拦截器，可以对 OkHttpClient.Builder 做任意操作
     */
    override fun setHttpClientBuilder(builder: OkHttpClient.Builder): OkHttpClient.Builder {
        builder.apply {
            //设置缓存配置 缓存最大10M
//            cache(Cache(File(appCtx.cacheDir, "cxk_cache"), 10 * 1024 * 1024))
//            //添加Cookies自动持久化
//            cookieJar(cookieJar)
            //示例：添加公共heads 注意要设置在日志拦截器之前，不然Log中会不显示head信息
            addInterceptor(UrlInterceptor())
            // 日志拦截器
            if (BuildConfig.DEBUG) {
                addInterceptor(NormalInterceptor())
            }
            //超时时间 连接、读、写
            connectTimeout(60, TimeUnit.SECONDS)
            readTimeout(5, TimeUnit.SECONDS)
            writeTimeout(60, TimeUnit.SECONDS)
        }
        return builder
    }

    /**
     * 实现重写父类的setRetrofitBuilder方法，
     * 在这里可以对Retrofit.Builder做任意操作，比如添加GSON解析器，protobuf等
     */
    override fun setRetrofitBuilder(builder: Retrofit.Builder): Retrofit.Builder {
        return builder.apply {
            addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
        }
    }

}



