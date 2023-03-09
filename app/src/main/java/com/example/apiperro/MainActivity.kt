package com.example.apiperro

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.lifecycle.*
import com.bumptech.glide.Glide
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

class MainActivity : AppCompatActivity() {

    private lateinit var dogImageView: ImageView
    private lateinit var fetchDogButton: Button
    private lateinit var viewModel: DogViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dogImageView = findViewById(R.id.imageView)
        fetchDogButton = findViewById(R.id.button)

        viewModel = ViewModelProvider(this).get(DogViewModel::class.java)

        viewModel.dog.observe(this, Observer {
            Glide.with(this).load(it.imageUrl).into(dogImageView)
        })

        fetchDogButton.setOnClickListener {
            viewModel.viewDog()
        }
    }

    class DogViewModel : ViewModel() {

        private val retrofit = Retrofit.Builder()
            .baseUrl("https://dog.ceo/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        private val service = retrofit.create(DogService::class.java)

        private val _dog = MutableLiveData<Dog>()
        val dog: LiveData<Dog>
            get() = _dog

        fun viewDog() {
            service.getDog().enqueue(object : Callback<DogResponse> {
                override fun onResponse(call: Call<DogResponse>, response: Response<DogResponse>) {
                    if (response.isSuccessful) {
                        val message = response.body()?.message ?: ""
                        val breed = message.split("/")[4]
                        val subbreed = message.split("/")[5]
                        val dog = Dog(breed, subbreed, message)
                        _dog.value = dog
                    }
                }

                override fun onFailure(call: Call<DogResponse>, t: Throwable) {

                }
            })
        }
    }

    interface DogService {
        @GET("breeds/image/random")
        fun getDog(): Call<DogResponse>
    }

    data class DogResponse(val message: String)

    data class Dog(val breed: String, val subbreed: String, val imageUrl: String)
}
