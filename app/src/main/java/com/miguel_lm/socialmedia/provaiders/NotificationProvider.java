package com.miguel_lm.socialmedia.provaiders;

import com.miguel_lm.socialmedia.model.FCMBody;
import com.miguel_lm.socialmedia.model.FCMResponse;
import com.miguel_lm.socialmedia.retrofit.IFCMApi;
import com.miguel_lm.socialmedia.retrofit.RetrofitClient;

import retrofit2.Call;


public class NotificationProvider {

    private String url = "https://fcm.googleapis.com";

    public NotificationProvider() {
    }

    public Call<FCMResponse> sendNotification(FCMBody body) {
        return RetrofitClient.getClient(url).create(IFCMApi.class).send(body);
    }
}
