package com.msa.oneway.common

import android.content.Context
import android.graphics.Typeface
import androidx.annotation.ColorRes
import androidx.annotation.FontRes
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes

/**
 * Created by Abhi Muktheeswarar on 20-August-2020
 */

interface ResourceRepository {

    fun getContext(): Context

    fun getString(@StringRes id: Int): String

    fun getString(@StringRes id: Int, text: String): String

    fun getQuantityString(@PluralsRes id: Int, quantity: Int): String

    fun getQuantityString(@PluralsRes id: Int, quantity: Int, text: String): String

    fun getString(@StringRes id: Int, text1: String, text2: String): String

    fun getColor(@ColorRes id: Int): Int

    fun getTypeface(@FontRes id: Int): Typeface?

    fun loadJSONFromAsset(fileName: String): Any

}