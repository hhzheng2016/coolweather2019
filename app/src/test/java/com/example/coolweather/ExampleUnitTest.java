package com.example.coolweather;

import com.weather.android.util.HttpUtil;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void httpTest(){
        HttpUtil.sendOkHttpRequest( "http://guolin.tech/api/china", new Callback(){
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                System.out.println("onFailure");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                System.out.println("success");
                System.out.println(response.body().toString());
            }
        });
    }
}