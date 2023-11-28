package com.example.bottomnavi;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bottomnavi.frag1_place.Frag1ViewModel;
import com.example.bottomnavi.frag1_place.YourAdapter;
import com.example.bottomnavi.frag1_place.YourItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Frag1 extends Fragment {

    private DatabaseReference databaseReference;
    private RecyclerView recyclerView;
    private YourAdapter adapter;
    private List<YourItem> items; // 누적해서 저장할 리스트
    private Frag1ViewModel viewModel;
    private String currentUserEmail;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag1, container, false);

        // 툴바 제목
        View toolbarView = view.findViewById(R.id.tb_frag1);
        TextView toolbarTitle = toolbarView.findViewById(R.id.textView_toolbar_title);
        toolbarTitle.setText("홈 화면");
        // Firebase에서 현재 사용자 가져오기
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserEmail = currentUser.getEmail();
            // currentUserEmail을 기반으로 데이터베이스에서 사용자 정보 가져오는 메서드 호출
            fetchUserData(currentUserEmail);
        }
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 뷰모델 초기화
        viewModel = new ViewModelProvider(this).get(Frag1ViewModel.class);

        // "posts" 아래에 있는 특정 폴더를 지정
        databaseReference = FirebaseDatabase.getInstance().getReference("posts");

        // 누적해서 저장할 리스트 초기화
        items = new ArrayList<>();

        adapter = new YourAdapter(items);
        recyclerView.setAdapter(adapter);

        Button button = view.findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            // Frag1.java

        // ... (이미 있는 코드는 생략)

            @Override
            public void onClick(View v) {
                Query lastQuery = databaseReference.orderByKey().limitToLast(5);
                lastQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        try {
                            if (snapshot.exists()) {
                                // 이전 데이터를 모두 지우고 새로 불러올 데이터를 저장할 리스트 초기화
                                items.clear();
                                Log.v("Firebase", "Data clearing completed");

                                // 데이터 순회
                                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                    String imageUrl = postSnapshot.child("imageUrl").getValue(String.class);
                                    String text = postSnapshot.child("text").getValue(String.class);
                                    String location = postSnapshot.child("location").getValue(String.class);
                                    String email = postSnapshot.child("userEmail").getValue(String.class);

                                    if (imageUrl != null && text != null) {
                                        items.add(new YourItem(imageUrl, text, location, email));
                                        Log.v("Firebase", "Data added: " + text);
                                    }
                                }

                                // 뷰모델에 데이터 설정
                                viewModel.setItems(items);
                                adapter.setItems(items);  // 추가: 어댑터에도 데이터 설정
                                Log.v("Firebase", "Adapter data set completed");

                            } else {
                                Log.v("Firebase", "No data exists.");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e("Firebase", "Error fetching data");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Firebase", "Database query canceled: " + error.getMessage());
                    }
                });
            }

// ... (나머지 코드는 생략)

        });

        return view;
    }
    // 현재 사용자의 이메일을 기반으로 데이터베이스에서 사용자 정보 가져오는 메서드
    private void fetchUserData(String email) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users");
        Query query = userRef.orderByChild("email").equalTo(email); // 현재 사용자의 이메일로 쿼리 생성

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        // 사용자 정보에서 필요한 데이터 가져오기
                        String imageUrl = snapshot.child("imageUrl").getValue(String.class);
                        String text = snapshot.child("text").getValue(String.class);
                        String location = snapshot.child("location").getValue(String.class);
                        // 가져온 데이터를 YourItem 객체로 생성하여 리스트에 추가
                        YourItem item = new YourItem(imageUrl, text, location, currentUserEmail);
                        items.add(item);
                    }

                    // 데이터를 ViewModel과 Adapter에 설정
                    viewModel.setItems(items);
                    adapter.setItems(items);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // 에러 처리
                Log.e("Firebase", "Failed to fetch user data: " + databaseError.getMessage());
            }
        });
    }
}