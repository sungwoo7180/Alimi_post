package com.example.bottomnavi;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class Frag2 extends Fragment {
    private static final int PICK_IMAGE = 1;
    private Uri selectedImageUri;
    private DatabaseReference databaseReference; //파이어베이스
    private StorageReference storageReference; // Firebase Storage 레퍼런스

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag2, container, false);
        Log.d("프래그2", "onCreateView() called"); // 추가된 로그
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 툴바 제목
        View toolbarView = view.findViewById(R.id.tb_frag2);
        TextView toolbarTitle = toolbarView.findViewById(R.id.textView_toolbar_title);
        toolbarTitle.setText("글 등록");

        // Firebase에서 현재 사용자 가져오기
        databaseReference = FirebaseDatabase.getInstance().getReference("posts");   // 파이어베이스에 POSTS라는 키로 값을 보냄
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userEmail = user.getEmail(); // 현재 사용자의 이메일 가져오기
        storageReference = FirebaseStorage.getInstance().getReference().child("images");    // 이미지를 업로드할 Firebase Storage 레퍼런스 설정
        Button chooseImageButton = view.findViewById(R.id.chooseImageButton2); //이미지 선택 버튼
        Button postButton = view.findViewById(R.id.postButton2); //게시 버튼
        ImageButton postButton3 = view.findViewById(R.id.postButton3); // 게시 이미지 버튼 3
        EditText editText = view.findViewById(R.id.editText2); //글작성 칸
        ImageView imageView = view.findViewById(R.id.imageView2); //이미지 미리보기
        EditText editTextLocation = view.findViewById(R.id.editTextLocation); // 위치정보
        // 사용자의 닉네임 가져오기
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users");
        Query query = userRef.orderByChild("email").equalTo(userEmail);

        chooseImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI); //갤러리 인텐트
                startActivityForResult(gallery, PICK_IMAGE); //갤러리에서 선택한 이미지 전송
                showToast("게시글이 성공적으로 저장되었습니다.");
            }
        });

        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userText = editText.getText().toString(); //게시글 작성한 부분을 userText에 저장
                String locationText = editTextLocation.getText().toString();

                if (selectedImageUri != null) {
                    // postId는 push() 메서드로 생성
                    String postId = databaseReference.push().getKey();

                    // 이미지를 Storage에 업로드
                    StorageReference imageRef = storageReference.child(postId + ".jpg");
                    imageRef.putFile(selectedImageUri)
                            .addOnSuccessListener(taskSnapshot -> {
                                // 이미지가 업로드된 경우
                                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                    // 이미지의 다운로드 URL 획득
                                    String imageUrl = uri.toString();

                                    // 데이터베이스에 저장할 PostData 객체 생성
                                    PostData post = new PostData(userText, imageUrl, locationText, userEmail);

                                    // 데이터베이스에 데이터 저장
                                    if (postId != null) {
                                        databaseReference.child(postId).setValue(post);
                                        showToast("게시글이 성공적으로 저장되었습니다.");
                                    }
                                });
                            })
                            .addOnFailureListener(e -> {
                                // 이미지 업로드 실패 시 처리
                                showToast("이미지 업로드에 실패했습니다.");
                            });
                }
            }
        });
        postButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userText = editText.getText().toString(); //게시글 작성한 부분을 userText에 저장
                String locationText = editTextLocation.getText().toString();

                if (selectedImageUri != null) {
                    String postId = databaseReference.push().getKey(); //postId에 파이어베이스 키 저장
                    String userEmail = user.getEmail(); // 현재 사용자의 이메일 가져오기
                    PostData post = new PostData(userText, selectedImageUri.toString(), locationText, userEmail); //PostData클래스 post생성자에 매개변수로 보낸 후 post에 저장.(파이어베이스로 보낼 데이터)
                    if (postId != null) {
                        databaseReference.child(postId).setValue(post); //키값과 데이터값을 파이어베이스에 전송
                        showToast("게시글이 성공적으로 저장되었습니다.");
                    }
                }
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK && requestCode == PICK_IMAGE) {
            selectedImageUri = data.getData();
            ImageView imageView = getView().findViewById(R.id.imageView2);
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageURI(selectedImageUri);
        }
    }
    // 알림을 표시하는 함수 정의
    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
