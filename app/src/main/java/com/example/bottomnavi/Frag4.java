    package com.example.bottomnavi;

    import android.Manifest;
    import android.content.Context;
    import android.content.DialogInterface;
    import android.content.Intent;
    import android.content.pm.PackageInfo;
    import android.content.pm.PackageManager;
    import android.content.pm.Signature;
    import android.location.Address;
    import android.location.Geocoder;
    import android.location.Location;
    import android.location.LocationListener;
    import android.location.LocationManager;
    import android.net.Uri;
    import android.os.Bundle;
    import android.util.Base64;
    import android.util.Log;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.Button;
    import android.widget.EditText;
    import android.widget.Toast;

    import androidx.annotation.NonNull;
    import androidx.annotation.Nullable;
    import androidx.appcompat.app.AlertDialog;
    import androidx.core.app.ActivityCompat;
    import androidx.core.content.ContextCompat;
    import androidx.fragment.app.Fragment;

    import com.example.bottomnavi.frag4_place.CultureLocation;
    import com.example.bottomnavi.frag4_place.KakaoMapService;
    import com.example.bottomnavi.frag4_place.Place;
    import com.google.android.gms.location.FusedLocationProviderClient;
    import com.google.android.gms.location.LocationServices;

    import net.daum.mf.map.api.MapPOIItem;
    import net.daum.mf.map.api.MapPoint;
    import net.daum.mf.map.api.MapView;

    import org.xmlpull.v1.XmlPullParser;
    import org.xmlpull.v1.XmlPullParserException;
    import org.xmlpull.v1.XmlPullParserFactory;

    import java.io.IOException;
    import java.io.StringReader;
    import java.security.MessageDigest;
    import java.security.NoSuchAlgorithmException;
    import java.util.ArrayList;
    import java.util.List;
    import java.util.Locale;

    import okhttp3.Call;
    import okhttp3.Callback;
    import okhttp3.HttpUrl;
    import okhttp3.OkHttpClient;
    import okhttp3.Request;

    public class Frag4 extends Fragment {
        private View rootView; // rootView 선언
        private MapView mapView;
        private ViewGroup mapViewContainer;
        private MapPOIItem customMarker;
        private EditText searchEditText;
        private boolean isFirstLocationUpdate = true;
        private KakaoMapService mapService;
        private FusedLocationProviderClient fusedLocationClient; // 추가
        private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
        private static final String cultureHeritageAPI = "http://www.cha.go.kr/cha/SearchKindOpenapiList.do";       //문화재 open api base url
        private List<CultureLocation> CultureLocations = new ArrayList<>();                                                //문화재 class
        private LocationManager locationManager;
        private LocationListener locationListener;
        private double currentUserLatitude;
        private double currentUserLongitude;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.frag4, container, false);

            // 프래그먼트 내에서 액티비티의 컨텍스트를 가져옵니다.
            Context context = requireContext();

            try {
                PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
                for (Signature signature : info.signatures) {
                    MessageDigest md = MessageDigest.getInstance("SHA");
                    md.update(signature.toByteArray());
                    Log.d("키해시는 :", Base64.encodeToString(md.digest(), Base64.DEFAULT));
                }
            } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException e) {
                e.printStackTrace();
            }

            // 위치 권한 체크 및 요청
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
            } else {
                // 위치 권한이 없으면 요청
                ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            }

            // 권한이 열려있는지 확인
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                // 권한이 허용된 경우 지도를 띄움
                mapView = new MapView(context);
                mapViewContainer = rootView.findViewById(R.id.map_view);
                mapViewContainer.addView(mapView);
                mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
            }

            searchEditText = rootView.findViewById(R.id.search_edittext);
            Button searchButton = rootView.findViewById(R.id.search_button);
            // KakaoMapService 객체 초기화
            mapService = new KakaoMapService(requireContext());
            // 문화재 버튼 클릭 이벤트 처리
            onCultureHeritageButtonClick();
            onTreasureButtonClick();
            onMonumentButtonClick();
            onHistoricalButtonClick();
            searchButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String searchQuery = searchEditText.getText().toString();
                    if (!searchQuery.isEmpty()) {
                        // 검색어로 위치 검색
                        searchLocation(searchQuery);
                    }
                }
            });
            /*
            // 검색 버튼 클릭 이벤트 처리
            rootView.findViewById(R.id.search_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // 검색어를 가져와서 카카오 지도에서 해당 위치로 이동
                    String searchQuery = ((EditText) rootView.findViewById(R.id.search_edittext)).getText().toString();
                    if (!searchQuery.isEmpty()) {
                        searchLocation(searchQuery);
                    }
                }
            });             */
            // 위치 권한이 허용되었는지 체크
            if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                // 위치 업데이트를 요청할 LocationManager 객체 생성
                locationManager = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);
                // LocationListener 객체 생성
                locationListener = new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        handleLocationUpdate(location);
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {
                    }

                    @Override
                    public void onProviderEnabled(String provider) {
                    }

                    @Override
                    public void onProviderDisabled(String provider) {
                    }
                };
                // 위치 업데이트 요청
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
                // 최초 위치 표시
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastKnownLocation != null) {
                    handleLocationUpdate(lastKnownLocation);
                }

            }

            return rootView;
        }
        // onPause 메서드 대신 아래의 코드를 사용하여 지도를 숨깁니다.
        /* 실험
        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);
            // 사용자에게 보이지 않으면 지도를 숨깁니다.
            if (mapView != null) {
                mapView.setVisibility(isVisibleToUser ? View.VISIBLE : View.GONE);
            }
        }*/
        private void handleLocationUpdate(Location location) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            currentUserLatitude = latitude;
            currentUserLongitude = longitude;

            // 지도 위의 마커를 이동시키는 로직 구현
            // 마커 위치 업데이트
            MapPoint MARKER_POINT = MapPoint.mapPointWithGeoCoord(latitude, longitude);
            if (customMarker != null) {
                customMarker.setMapPoint(MARKER_POINT);
            } else {
                // customMarker가 null일 때는 처음 위치를 가리키는 마커를 추가합니다.
                showMyLocation(latitude, longitude);
            }

            // Geocoder 를 사용하여 주소 정보 가져오기
            Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                if (addresses != null && addresses.size() > 0) {
                    Address address = addresses.get(0);
                    String addressText = address.getAddressLine(0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // 내 위치를 제외한 모든 마커 제거
        private void removeAllMarkersExceptMyLocation() {
            if (mapView != null) {
                MapPOIItem[] poiItems = mapView.getPOIItems();
                for (MapPOIItem poiItem : poiItems) {
                    if (poiItem.getTag() != 1) { // 내 위치 마커는 제거하지 않음
                        mapView.removePOIItem(poiItem);
                    }
                }
                CultureLocations.clear(); // 리스트에 저장된 마커 정보 초기화
            }
        }
        // 내 위치를 지도 위에 표시하는 메서드
        private void showMyLocation(double latitude, double longitude) {
            if (mapView != null) {
                MapPoint MARKER_POINT = MapPoint.mapPointWithGeoCoord(latitude, longitude);
                if (customMarker == null) {
                    customMarker = new MapPOIItem();
                    customMarker.setItemName("내 위치");
                    customMarker.setTag(1);
                    customMarker.setMapPoint(MARKER_POINT);
                    customMarker.setMarkerType(MapPOIItem.MarkerType.CustomImage);
                    customMarker.setCustomImageResourceId(R.drawable.custom_marker_red);
                    customMarker.setCustomImageAutoscale(false);
                    customMarker.setCustomImageAnchor(0.5f, 1.0f);
                    mapView.addPOIItem(customMarker);
                } else {
                    customMarker.setMapPoint(MARKER_POINT);
                }
                mapView.setMapCenterPoint(MARKER_POINT, true);
                // 위치 업데이트는 한번만
                if (isFirstLocationUpdate) {
                    isFirstLocationUpdate = false;
                    mapView.setMapCenterPoint(MARKER_POINT, true); }
            }
        }

        // 사용자가 입력한 검색어로 위치 검색
        private void searchLocation(String query) {
            // 사용자의 검색어를 이용하여 카카오 지도에서 해당 위치 검색 및 이동
            mapService.searchPlace("904e8602dd13516eb005f4c980a95ec2", query, new KakaoMapService.OnPlaceSearchListener() { //rest api
                @Override
                public void onPlaceSearchSuccess(List<Place> places) {
                    if (places.size() > 0) {
                        // 검색 결과에서 장소 정보를 가져오고 마커로 표시
                        for (Place place : places) {
                            double latitude = Double.parseDouble(place.getY());
                            double longitude = Double.parseDouble(place.getX());
                            String placeName = place.getPlaceName(); // 인스턴스에서 메서드 호출
                            // 장소의 위치에 마커 표시
                            showLocationOnMap(placeName, latitude, longitude);
                        }
                    }
                }

                @Override
                public void onPlaceSearchError(String errorMessage) {
                    // Handle search error
                }
            });
        }
        // 권한 체크 이후 로직을 여기에 추가할 수 있습니다.
        /*
        private void searchLocation(String query) {
            // 검색어를 이용하여 카카오 지도에서 해당 위치 검색 및 이동
            Uri uri = Uri.parse("kakaomap://search?q=" + query);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }*/

        // 지도에 장소 위치를 마커로 표시
        private void showLocationOnMap(String name, double latitude, double longitude) {
            if (mapView != null) {
                MapPoint location = MapPoint.mapPointWithGeoCoord(latitude, longitude);
                MapPOIItem marker = new MapPOIItem();
                marker.setItemName(name);
                marker.setTag(0);
                marker.setMapPoint(location);
                marker.setMarkerType(MapPOIItem.MarkerType.CustomImage);
                marker.setCustomImageResourceId(R.drawable.custom_marker_red);
                marker.setCustomImageAnchor(0.5f, 1.0f);

                mapView.addPOIItem(marker);
            }
        }
        // 국보 버튼 클릭 시 호출되는 메서드
        private void onCultureHeritageButtonClick() {
            Button heritageButton = rootView.findViewById(R.id.btn_cultural_heritage);
            heritageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // API 요청
                    removeAllMarkersExceptMyLocation(); // 내 위치를 제외한 마커 제거
                    showMyLocation(currentUserLatitude, currentUserLongitude);
                    requestCultureHeritageData("11");
                }
            });
        }
        /*
        // 버튼 색상 변경을 위한 메서드
        private void changeButtonColor(Button button, boolean isSelected) {
            int selectedColor = ContextCompat.getColor(requireContext(), R.color.purple_200); // 파란색 계열의 색상

            // 선택 여부에 따라 색상을 조정
            int color = isSelected ? selectedColor : button.getBackgroundTintList().getDefaultColor();
            button.setBackgroundTintList(ColorStateList.valueOf(color));
        }*/
        // 보물 버튼 클릭 시 호출되는 메서드
        private void onTreasureButtonClick() {
            Button treasureButton = rootView.findViewById(R.id.btn_treasure);
            treasureButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // API 요청
                    removeAllMarkersExceptMyLocation(); // 내 위치를 제외한 마커 제거
                    showMyLocation(currentUserLatitude, currentUserLongitude);
                    requestCultureHeritageData("12");
                }
            });
        }
        // 시도 문화재 버튼 클릭 시 호출되는 메서드
        private void onMonumentButtonClick() {
            Button treasureButton = rootView.findViewById(R.id.btn_monument);
            treasureButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // API 요청
                    removeAllMarkersExceptMyLocation(); // 내 위치를 제외한 마커 제거
                    showMyLocation(currentUserLatitude, currentUserLongitude);
                    requestCultureHeritageData("23");
                }
            });
        }
        // 사적 버튼 클릭 시 호출되는 메서드
        private void onHistoricalButtonClick() {
            Button historicalButton = rootView.findViewById(R.id.btn_historical);
            historicalButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // API 요청
                    removeAllMarkersExceptMyLocation(); // 내 위치를 제외한 마커 제거
                    showMyLocation(currentUserLatitude, currentUserLongitude);
                    requestCultureHeritageData("13");
                }
            });
        }
        // 문화재 데이터를 요청하는 메서드
        private void requestCultureHeritageData(String ccbaKdcd) {
            OkHttpClient client = new OkHttpClient();

            //url 생성 및 호출R

            String ccbaCncl = "N"; // 지정해제 여부 (N: 미해제)
            String ccbaCtcd = getRegionCode(currentUserLatitude, currentUserLongitude); // 현재 위치의 시도군코드

            // 문화재 데이터 요청 API 엔드포인트 및 파라미터 설정
            HttpUrl.Builder urlBuilder = HttpUrl.parse(cultureHeritageAPI).newBuilder();
            urlBuilder.addQueryParameter("ccbaKdcd", ccbaKdcd);
            urlBuilder.addQueryParameter("ccbaCncl", ccbaCncl);
            urlBuilder.addQueryParameter("ccbaCtcd", ccbaCtcd);
            String url = urlBuilder.build().toString();
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            // API 호출
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onResponse(@NonNull Call call, @NonNull okhttp3.Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        // 받은 XML 데이터를 처리하는 메서드 호출
                        handleCultureHeritageData(responseBody);
                    } else {
                        // 응답 실패 시 처리 로직
                    }
                }
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    e.printStackTrace();
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "네트워크 요청 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }

            });
        }

        private String getRegionCode(double latitude, double longitude) {
            // 사용자의 현재 위치에 따라 지역 코드를 결정하는 로직을 구현합니다.

            if (latitude >= 37.55 && latitude <= 37.69 && longitude >= 126.98 && longitude <= 127.18) {
                return "11"; // 서울
            } else if (latitude >= 35.10 && latitude <= 35.35 && longitude >= 129.01 && longitude <= 129.15) {
                return "21"; // 부산
            } else if (latitude >= 35.80 && latitude <= 35.92 && longitude >= 128.50 && longitude <= 128.63) {
                return "22"; // 대구
            } else if (latitude >= 37.41 && latitude <= 37.58 && longitude >= 126.46 && longitude <= 126.64) {
                return "23"; // 인천
            } else if (latitude >= 35.12 && latitude <= 35.24 && longitude >= 126.78 && longitude <= 126.94) {
                return "24"; // 광주
            } else if (latitude >= 36.28 && latitude <= 36.43 && longitude >= 127.33 && longitude <= 127.46) {
                return "25"; // 대전
            } else if (latitude >= 35.52 && latitude <= 35.58 && longitude >= 129.31 && longitude <= 129.36) {
                return "26"; // 울산
            } else if (latitude >= 36.40 && latitude <= 36.64 && longitude >= 127.23 && longitude <= 127.45) {
                return "45"; // 세종
            } else if (latitude >= 37.13 && latitude <= 37.47 && longitude >= 126.46 && longitude <= 127.36) {
                return "31"; // 경기
            }
            return "ZZ"; // 위의 범위에 해당하지 않는 경우 전국일원으로 처리 (기본값)
        }
        // 받은 XML 데이터를 처리하는 메서드
        private void handleCultureHeritageData(String xmlData) {
            // 기존 마커들을 모두 제거
            try {
                // XML 파서를 사용하여 데이터 파싱
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                XmlPullParser parser = factory.newPullParser();
                parser.setInput(new StringReader(xmlData));

                String latitudeString = null;
                String longitudeString = null;
                String name = null;
                int eventType = parser.getEventType();

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        String tagName = parser.getName();
                        if (tagName.equals("latitude")) {
                            latitudeString = parser.nextText();
                            Log.d("Response 경도:",latitudeString);
                        } else if (tagName.equals("longitude")) {
                            longitudeString = parser.nextText();
                            Log.d("Response 위도:",longitudeString);
                        } else if (tagName.equals("ccbaMnm1")) {
                            name = parser.nextText();
                            Log.d("Response 이름:",name);
                        }
                    } else if (eventType == XmlPullParser.END_TAG) {
                        if (parser.getName().equals("item")) {
                            if (latitudeString != null && longitudeString != null && name != null) {
                                // Location 객체를 생성하고 리스트에 추가합니다.
                                double latitude = Double.parseDouble(latitudeString);
                                double longitude = Double.parseDouble(longitudeString);
                                CultureLocation location = new CultureLocation(latitude, longitude, name);
                                CultureLocations.add(location);
                            }
                            // 변수 초기화
                            latitudeString = null;
                            longitudeString = null;
                            name = null;
                        }
                    }
                    eventType = parser.next();
                }
            } catch (XmlPullParserException | IOException e) {
                e.printStackTrace();
            }
            requireActivity().runOnUiThread(() -> {
                showCultureLocationsOnMap();
            });
        }
        private void showCultureLocationsOnMap() {

            for (CultureLocation location : CultureLocations) {
                showCultureHeritageOnMap(location.getLatitude(), location.getLongitude(), location.getName());
                Log.d("Culture Location", "Name: " + location.getName() +
                        ", Latitude: " + location.getLatitude() +
                        ", Longitude: " + location.getLongitude());
            }
            mapView.setPOIItemEventListener(new MapView.POIItemEventListener() {
                @Override
                public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) {
                    // 위치 마커를 클릭했을 때 대화상자를 보여준다.
                    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                    builder.setTitle("길찾기")
                            .setMessage("선택한 위치로 길찾기를 하시겠습니까?")
                            .setPositiveButton("길찾기", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    double destinationLatitude = mapPOIItem.getMapPoint().getMapPointGeoCoord().latitude;
                                    double destinationLongitude = mapPOIItem.getMapPoint().getMapPointGeoCoord().longitude;

                                    // 카카오맵 URL 형식을 사용하여 길찾기 화면으로 이동
                                    String kakaoMapUrl = "kakaomap://route?sp=" + currentUserLatitude + "," + currentUserLongitude +
                                            "&ep=" + destinationLatitude + "," + destinationLongitude +
                                            "&by=FOOT";
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(kakaoMapUrl));
                                    startActivity(intent);
                                }
                            })
                            .setNegativeButton("취소", null)
                            .show();

                }

                @Override
                public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem) {}

                @Override
                public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem, MapPOIItem.CalloutBalloonButtonType calloutBalloonButtonType) {}

                @Override
                public void onDraggablePOIItemMoved(MapView mapView, MapPOIItem mapPOIItem, MapPoint mapPoint) {}
            });
        }

        // 지도에 문화재 위치를 마커로 표시하는 메서드
        private void showCultureHeritageOnMap(double latitude, double longitude, String heritageName) {
            if (mapView != null) {
                MapPoint location = MapPoint.mapPointWithGeoCoord(latitude, longitude);
                MapPOIItem marker = new MapPOIItem();
                marker.setItemName(heritageName);
                marker.setTag(0);
                marker.setMapPoint(location);
                marker.setMarkerType(MapPOIItem.MarkerType.CustomImage);
                marker.setCustomImageResourceId(R.drawable.custom_marker_red);
                marker.setCustomImageAnchor(0.5f, 1.0f);

                mapView.addPOIItem(marker);
            }
        }
    }
