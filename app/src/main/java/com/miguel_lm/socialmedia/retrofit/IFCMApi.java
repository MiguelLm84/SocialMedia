package com.miguel_lm.socialmedia.retrofit;

import com.miguel_lm.socialmedia.model.FCMBody;
import com.miguel_lm.socialmedia.model.FCMResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMApi {

    @Headers({
            "Content-Type:application/json",
            "Authorization:key=\tAAAAJeVVQdQ:APA91bGNXrE2tJZP32rg9QVGV9BWkWrAL9QhDrXrmUtqzl13u2OhosrFUb0B2Vk7v8JLefuq35pDiinUEiXXjwukJRdVEJS2qI4G8pgUcDXddapgafJkgeuNnkS5wk2GOtVipKgQdNtq"
    })

    @POST("fcm/send")
    Call<FCMResponse> send(@Body FCMBody body);
}
