package com.example.bottomnavi;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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

public class Frag2 extends Fragment {
    private static final int PICK_IMAGE = 1;
    private Uri selectedImageUri;
    private DatabaseReference databaseReference; //파이어베이스

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate (R.layout.frag2, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 툴바 제목
        View toolbarView = view.findViewById(R.id.tb_frag2);
        TextView toolbarTitle = toolbarView.findViewById(R.id.textView_toolbar_title);
        toolbarTitle.setText("글 등록");
        databaseReference = FirebaseDatabase.getInstance().getReference("posts"); // 파이어베이스에 POSTS라는 키로 값을 보냄
        // Firebase에서 현재 사용자 가져오기
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userEmail = user.getEmail(); // 현재 사용자의 이메일 가져오기
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

                if (selectedImageUri != null && user != null) {
                    String postId = databaseReference.push().getKey(); //postId에 파이어베이스 키 저장

                    PostData post = new PostData(userText, selectedImageUri.toString(), locationText, userEmail); //PostData클래스 post생성자에 매개변수로 보낸 후 post에 저장.(파이어베이스로 보낼 데이터)
                    if (postId != null) {
                        databaseReference.child(postId).setValue(post); //키값과 데이터값을 파이어베이스에 전송
                        showToast("게시글이 성공적으로 저장되었습니다.");
                    }
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
