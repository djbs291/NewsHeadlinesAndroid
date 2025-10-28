package com.example.news.ui

import android.content.Context
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.CachePolicy
import okhttp3.OkHttpClient

fun provideImageLoader(context: Context, okHttpClient: OkHttpClient): ImageLoader =
    ImageLoader.Builder(context)
        .okHttpClient(okHttpClient)
        .crossfade(true)
        .respectCacheHeaders(false)
        .diskCachePolicy(CachePolicy.ENABLED)
        .components{
            if (android.os.Build.VERSION.SDK_INT >= 28){
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }
        .build()
