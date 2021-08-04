package com.example.d_bee_7.ui;

import android.Manifest;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.example.d_bee_7.MyApplication;
import com.example.d_bee_7.R;
import com.example.d_bee_7.logic.model.Note;

import org.jetbrains.annotations.NotNull;
import org.litepal.LitePal;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * 点击item之后的编辑界面
 */
public class NoteFragment extends Fragment {

    private static final int SELECT_FROM_GALLERY = 1;
    private static final int TAKE_PHOTO = 2;
    private Note note;
    private LinearLayout imageSelect;
    private ImageView imageView;
    private TextView selectFromGallery;
    private TextView takePhoto;
    private EditText editTextTitle;
    private EditText editTextContent;
    private Uri imageUri;

    public NoteFragment() {
        // Required empty public constructor
    }

    public NoteFragment(Note note) {
        this.note = note;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_note, container, false);

        imageSelect = view.findViewById(R.id.ll_image_select);
        imageView = view.findViewById(R.id.iv_image);
        editTextContent = view.findViewById(R.id.et_content);
        editTextTitle = view.findViewById(R.id.et_title);
        selectFromGallery = view.findViewById(R.id.tv_gallery);
        takePhoto = view.findViewById(R.id.tv_photo);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!imageSelect.isShown())
                    imageSelect.setVisibility(View.VISIBLE);
                else imageSelect.setVisibility(View.GONE);
            }
        });
        selectFromGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPicFromGallery();
            }
        });
        takePhoto = view.findViewById(R.id.tv_photo);
        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeAShot();
            }
        });
        initView();
        return view;
    }

    private void initView() {
        if (note != null) {
            editTextTitle.setText(note.getTitle());
            editTextContent.setText(note.getContent());
            imageView.setImageBitmap(BitmapFactory.decodeByteArray(note.getImage(), 0, note.getImage().length));
        }
    }


    private void takeAShot() {
        File outputImage = new File(MyApplication.context.getExternalCacheDir(), "output_image.jpg");
        try {
            if (outputImage.exists()) {
                outputImage.delete();
            }
            outputImage.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (Build.VERSION.SDK_INT >= 24) {
            imageUri = FileProvider.getUriForFile(MyApplication.context, "com.example.d_bee_7.fileprovider", outputImage);
        } else {
            imageUri = Uri.fromFile(outputImage);
        }
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, TAKE_PHOTO);
    }

    private void selectPicFromGallery() {
        if (ContextCompat.checkSelfPermission(MyApplication.context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else openAlbums();
    }

    private void openAlbums() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, SELECT_FROM_GALLERY);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openAlbums();
                } else
                    Toast.makeText(MyApplication.context, "你拒绝了授权", Toast.LENGTH_SHORT).show();
                break;
            default:
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SELECT_FROM_GALLERY:
                if (resultCode == RESULT_OK) {
                    handleImageOnKitKat(data);
                }
                break;
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(MyApplication.context.getContentResolver().openInputStream(imageUri));
                        imageView.setImageBitmap(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
            default:
                break;
        }
    }

    private void handleImageOnKitKat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(MyApplication.context, uri)) {
//如果是document类型的uri,则通过document Id处理
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);

            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            //如果是content类型的uri,则按普通方式处理
            imagePath = getImagePath(uri, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            //如果是file类型的uri,直接获取图片就行
            imagePath = uri.getPath();
        }
        displayImage(imagePath);
    }

    private void displayImage(String imagePath) {
        if (imagePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            imageView.setImageBitmap(bitmap);
        } else Toast.makeText(MyApplication.context, "未能获取到图片", Toast.LENGTH_SHORT).show();
    }

    private String getImagePath(Uri externalContentUri, String selection) {
        String path = null;
        Cursor cursor = MyApplication.context.getContentResolver().query(externalContentUri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString((cursor.getColumnIndex(MediaStore.Images.Media.DATA)));
                cursor.close();
            }
        }
        return path;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Date date = new Date();
        String time = date.toLocaleString();
        Note note = new Note();
        String title = editTextTitle.getText().toString();
        String content = editTextContent.getText().toString();

        ByteArrayOutputStream baos;
        byte[] bytes;
        try {
            baos = new ByteArrayOutputStream();
            ((BitmapDrawable) imageView.getDrawable()).getBitmap().compress(Bitmap.CompressFormat.PNG, 10, baos);
            bytes = baos.toByteArray();
            note.setImage(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!"".equals(title))
            note.setTitle(title);
        if (!"".equals(content))
            note.setContent(content);


        note.setDate(time);
        if (!note.isSaved()) {
            note.save();
        }
        if (this.note!=null){
            LitePal.delete(Note.class,this.note.getId());
        }
        Toast.makeText(MyApplication.context, "保存成功", Toast.LENGTH_SHORT).show();

    }
}