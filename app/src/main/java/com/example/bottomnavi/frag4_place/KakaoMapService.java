package com.example.bottomnavi.frag4_place;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class KakaoMapService {
    private Context context;

    public KakaoMapService(Context context) {
        this.context = context;
    }

    public void searchPlace(String apiKey, String query, OnPlaceSearchListener listener) {
        // 카카오맵 서비스를 사용하여 장소를 검색하고 결과를 listener를 통해 반환하는 코드를 작성하세요.
        String apiUrl = "https://dapi.kakao.com/v2/local/search/address.json?query=" + query;

        // 네트워크 요청을 보내고 응답을 처리하는 코드 (HttpURLConnection 사용)
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "KakaoAK " + apiKey);

            int responseCode = conn.getResponseCode();
            Log.d("KakaoMapService", "HTTP 응답 코드: " + responseCode);        //로그 확인
            if (responseCode == 200) {
                // 요청이 성공하면 응답을 읽어옵니다.
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                //// 응답(JSON 형식) 로그
                Log.d("KakaoMapService", "응답 데이터: " + response.toString());

                // 응답(JSON 형식) 파싱하여 Place 객체로 변환
                List<Place> places = parseResponse(response.toString());

                // 결과를 listener.onPlaceSearchSuccess()로 전달
                listener.onPlaceSearchSuccess(places);
            } else {
                // 요청이 실패하면 오류 메시지를 전달
                listener.onPlaceSearchError("API 요청 실패 - HTTP 응답 코드: " + responseCode);
            }

            conn.disconnect();
        } catch (IOException e) {
            // 네트워크 오류 처리
            listener.onPlaceSearchError("네트워크 오류: " + e.getMessage());
        }
        // 검색 결과를 사용자 정의 Place 객체 리스트로 파싱하여 listener.onPlaceSearchSuccess()에 전달합니다.
    }

    public interface OnPlaceSearchListener {
        void onPlaceSearchSuccess(List<Place> places);
        void onPlaceSearchError(String errorMessage);
    }
    private List<Place> parseResponse(String jsonResponse) {
        List<Place> places = new ArrayList<>();

        try {
            JSONObject responseObject = new JSONObject(jsonResponse);
            JSONArray documentsArray = responseObject.getJSONArray("documents");

            for (int i = 0; i < documentsArray.length(); i++) {
                JSONObject placeObject = documentsArray.getJSONObject(i);
                String addressName = placeObject.getString("address_name");
                String x = placeObject.getString("x");
                String y = placeObject.getString("y");
                Place place = new Place(addressName, x, y);
                places.add(place);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return places;
    }
}
