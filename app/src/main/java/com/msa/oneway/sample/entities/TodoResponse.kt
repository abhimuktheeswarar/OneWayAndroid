package com.msa.oneway.sample.entities


import com.google.gson.annotations.SerializedName

/**
 * Created by Abhi Muktheeswarar on 20-August-2020
 */

class TodoResponse : ArrayList<TodoResponse.TodoItem>() {
    data class TodoItem(
        @SerializedName("completed")
        val completed: Boolean,
        @SerializedName("id")
        val id: Int,
        @SerializedName("title")
        val title: String,
        @SerializedName("userId")
        val userId: Int
    )
}