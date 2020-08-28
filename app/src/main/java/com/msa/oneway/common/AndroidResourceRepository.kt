package com.msa.oneway.common

import android.content.Context
import android.graphics.Typeface
import androidx.annotation.ColorRes
import androidx.annotation.FontRes
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import java.io.IOException
import java.nio.charset.Charset

/**
 * Created by Abhi Muktheeswarar on 20-August-2020
 */

class AndroidResourceRepository(private val applicationContext: Context) : ResourceRepository {

    override fun getContext(): Context = applicationContext

    override fun getString(@StringRes id: Int): String = applicationContext.resources.getString(id)

    override fun getString(@StringRes id: Int, text: String): String =
        applicationContext.resources.getString(id, text)

    override fun getQuantityString(@PluralsRes id: Int, quantity: Int): String =
        applicationContext.resources.getQuantityString(id, quantity)

    override fun getQuantityString(@PluralsRes id: Int, quantity: Int, text: String): String =
        applicationContext.resources.getQuantityString(id, quantity, text)

    override fun getString(@StringRes id: Int, text1: String, text2: String): String =
        applicationContext.resources.getString(id, text1, text2)

    override fun getColor(@ColorRes id: Int): Int = ContextCompat.getColor(applicationContext, id)

    override fun getTypeface(@FontRes id: Int): Typeface? =
        ResourcesCompat.getFont(applicationContext, id)

    override fun loadJSONFromAsset(fileName: String): Any {
        var json: String? = null
        try {
            val inputStream = applicationContext.assets.open(fileName)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            json = String(buffer, Charset.forName("UTF-8"))
        } catch (ex: IOException) {
            ex.printStackTrace()
            return ex
        }

        return json
    }
}