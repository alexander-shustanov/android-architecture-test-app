package com.example.alexander.library.network

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okio.Buffer
import retrofit2.Converter
import retrofit2.Retrofit
import java.io.OutputStreamWriter
import java.lang.reflect.Type
import java.nio.charset.Charset

class ConverterFactory(private val gson: Gson) : Converter.Factory() {

    override fun responseBodyConverter(
        type: Type, annotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *>? = null

    override fun requestBodyConverter(
        type: Type,
        parameterAnnotations: Array<Annotation>, methodAnnotations: Array<Annotation>, retrofit: Retrofit
    ): Converter<*, RequestBody>? {
        val fit = parameterAnnotations.any { it is UpdateBody }
        return if (fit) {
            val adapter = gson.getAdapter(TypeToken.get(type))
            RequestConverter(gson, adapter)
        } else null
    }

    private class RequestConverter<T>(val gson: Gson, val adapter: TypeAdapter<T>) : Converter<T, RequestBody> {
        override fun convert(value: T): RequestBody {
            val buffer = Buffer()
            val writer = OutputStreamWriter(buffer.outputStream(), UTF_8)
            val jsonWriter = gson.newJsonWriter(writer)
            adapter.write(jsonWriter, value)
            jsonWriter.close()
            return RequestBody.create(MEDIA_TYPE, buffer.readByteString())
        }
    }

    companion object {
        private val MEDIA_TYPE = MediaType.parse("application/json; charset=UTF-8")
        private val UTF_8 = Charset.forName("UTF-8")
    }
}