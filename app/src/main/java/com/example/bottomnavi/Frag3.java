package com.example.bottomnavi;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Frag3 extends Fragment {
    private FirebaseAuth mFirebaseAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 이 프래그먼트의 레이아웃을 inflate합니다.
        View view = inflater.inflate(R.layout.frag3, container, false);
        TextView textViewEmail = view.findViewById(R.id.nickname);
        // 툴바 제목
        View toolbarView = view.findViewById(R.id.tb_frag3);
        TextView toolbarTitle = toolbarView.findViewById(R.id.textView_toolbar_title);
        toolbarTitle.setText("MyPage");
        mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userEmail = user.getEmail();
            if (userEmail != null) {
                // TextViewv 에 가져온 이메일 설정하기
                String[] emailParts = userEmail.split("@");
                if (emailParts.length > 0) {
                    String username = emailParts[0]; // "@" 이전의 부분(사용자 이름)을 가져옵니다.
                    textViewEmail.setText("닉네임: " + username); }
            }
        }
        Button viewMyPostsButton = view.findViewById(R.id.viewMyPostsButton); // 내가 쓴 글 모아보기 버튼
        /* 미구현
        viewMyPostsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ViewMyPostsActivity.class);
                startActivity(intent);
            }
        });*/

        Button btn_logout = view.findViewById(R.id.btn_logout);
        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("로그아웃");
                builder.setMessage("로그아웃 하시겠습니까?");
                builder.setPositiveButton("로그아웃", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mFirebaseAuth.signOut();
                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                        startActivity(intent);
                    }
                });
                builder.setNegativeButton("취소", null);
                builder.show();
            }
        });

        Button btn_deleteAcc = view.findViewById(R.id.btn_deleteAcc);
        btn_deleteAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("계정 삭제");
                builder.setMessage("정말로 계정을 삭제하시겠습니까?");
                builder.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseUser user = mFirebaseAuth.getCurrentUser();
                        if (user != null) {
                            user.delete()
                                    .addOnCompleteListener(getActivity(), task -> {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(getActivity(), "계정이 성공적으로 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(getActivity(), LoginActivity.class);
                                            startActivity(intent);
                                        } else {
                                            Toast.makeText(getActivity(), "계정 삭제 실패: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                });
                builder.setNegativeButton("취소", null);
                builder.show();
            }
        });

        Button btn_changePassword = view.findViewById(R.id.btn_changePassword);
        btn_changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangePasswordDialog();
            }
        });

        return view;
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("새로운 비밀번호 입력");

        // 다이얼로그의 레이아웃을 inflate합니다.
        View viewInflated = LayoutInflater.from(getActivity()).inflate(R.layout.change_password_dialog, null);
        // 입력란 설정
        final EditText input = viewInflated.findViewById(R.id.input_new_password);
        // 예상되는 입력 유형 지정
        builder.setView(viewInflated);

        // 버튼 설정
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newPassword = input.getText().toString().trim();

                FirebaseUser user = mFirebaseAuth.getCurrentUser();

                if (user != null) {
                    user.updatePassword(newPassword)
                            .addOnCompleteListener(getActivity(), task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getActivity(), "비밀번호가 성공적으로 변경되었습니다.", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getActivity(), "비밀번호 변경 실패: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }
}
