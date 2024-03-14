package com.dullbluelab.pastweather.data

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

private const val BASE_URL = "https://dullbluelab.nobushi.jp"
private const val DIRECTORY_JSON = "/pastweather/data/directory.json"

class DirectoryJson {

    data class Data(
        val version: String,
        val update: String,
        val maxyear: String,
        val minyear: String,
        val points: List<Table>
    )

    data class Table(
        val code: String,
        val name: String
    )

    fun load(
        success: (Data) -> Unit,
        failed: (String) -> Unit
    ) {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(MyService::class.java)

        service.getData().enqueue(object : Callback<Data> {
            override fun onResponse(call: Call<Data>, response: Response<Data>) {
                if (response.isSuccessful) {
                    response.body()?.let { success(it) } ?: failed("error json response null")
                }
                else {
                    val message = "error json response ${response.code()}"
                    failed(message)
                }
            }

            override fun onFailure(call: Call<Data>, t: Throwable) {
                val message = t.message ?: "error"
                failed(message)
            }
        })
    }

    private interface MyService {
        @GET(DIRECTORY_JSON)
        fun getData(): Call<Data>
    }
}