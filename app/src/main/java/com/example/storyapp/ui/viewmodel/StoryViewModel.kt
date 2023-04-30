package com.example.storyapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.storyapp.api.ApiConfig
import com.example.storyapp.api.ListStoryResponse
import com.example.storyapp.api.StoryResponse
import retrofit2.Call
import retrofit2.Response

class StoryViewModel : ViewModel() {

    private val _message = MutableLiveData<String>()
    private val _isLoading = MutableLiveData<Boolean>()

    val message: LiveData<String> = _message
    val isLoading: LiveData<Boolean> = _isLoading
    var story: List<ListStoryResponse> = listOf()
    var isError: Boolean = false

    fun getStories(token: String) {
        _isLoading.value = true
        val api = ApiConfig.getApiService().getStories("Bearer $token")
        api.enqueue(object : retrofit2.Callback<StoryResponse> {
            override fun onResponse(call: Call<StoryResponse>, response: Response<StoryResponse>) {
                _isLoading.value = false
                val responseBody = response.body()
                if (response.isSuccessful) {
                    isError = false
                    if (responseBody != null) {
                        story = responseBody.listStory
                    }
                    _message.value = responseBody?.message.toString()

                } else {
                    isError = true
                    _message.value = response.message()
                }
            }

            override fun onFailure(call: Call<StoryResponse>, t: Throwable) {
                isError = true
                _isLoading.value = false
                _message.value = t.message.toString()
            }
        })
    }
}