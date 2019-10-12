package com.sudoajay.duplication_data.DuplicationData;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.sudoajay.duplication_data.R;

import java.io.File;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;


public class ExpandableDuplicateListAdapter extends BaseExpandableListAdapter {
    private Context context;
    private List<String> list_Header;
    private LinkedHashMap<String, List<String>> list_Header_Child;
    private List<Integer> arrow_Image_Resource;
    private LinkedHashMap<Integer, List<Boolean>> checkBoxArray;
    private ShowDuplicate showDuplicate;

    ExpandableDuplicateListAdapter(final ShowDuplicate showDuplicate, List<String> list_Header, LinkedHashMap<String, List<String>> list_Header_Child, List<Integer> arrow_Image_Resource,
                                   final LinkedHashMap<Integer, List<Boolean>> checkBoxArray) {
        this.showDuplicate = showDuplicate;
        this.context = showDuplicate.getApplicationContext();
        this.list_Header = list_Header;
        this.list_Header_Child = list_Header_Child;
        this.arrow_Image_Resource = arrow_Image_Resource;
        this.checkBoxArray = checkBoxArray;
    }

    @Override
    public int getGroupCount() {
        return this.list_Header.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return Objects.requireNonNull(this.list_Header_Child.get(this.list_Header.get(groupPosition)))
                .size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.list_Header.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return Objects.requireNonNull(this.list_Header_Child.get(this.list_Header.get(groupPosition))).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @SuppressLint({"SetTextI18n", "InflateParams"})
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infaltInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert infaltInflater != null;
            convertView = infaltInflater.inflate(R.layout.activity_duplication_list_view, null);
        }
        ImageView arrow_Image_View = convertView.findViewById(R.id.arrow_Image_View);
        TextView group_Heading_Text_View = convertView.findViewById(R.id.group_Heading_Text_View);
        TextView count_Text_View = convertView.findViewById(R.id.count_Text_View);
        TextView group_Size_Text_View = convertView.findViewById(R.id.group_Size_Text_View);

        count_Text_View.setText("" + getChildrenCount(groupPosition));
        group_Heading_Text_View.setText(headerTitle);
        arrow_Image_View.setImageResource(arrow_Image_Resource.get(groupPosition));

        // long data
        long dataSize = 0;
        for (int i = 0; i < Objects.requireNonNull(list_Header_Child.get(list_Header.get(groupPosition))).size(); i++) {
            dataSize += getFileSizeInBytes((String) getChild(groupPosition, i));
        }
        group_Size_Text_View.setText("(" + Convert_It(dataSize) + ")");

        return convertView;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {


        final String headerTitle = (String) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater infaltInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert infaltInflater != null;
            convertView = infaltInflater.inflate(R.layout.activity_duplication_under_list_view, null);
        }
        final TextView nameTextView = convertView.findViewById(R.id.nameTextView);
        final ImageView coverImageView = convertView.findViewById(R.id.coverImageView);
        final TextView pathTextView = convertView.findViewById(R.id.pathTextView);
        final CheckBox checkBoxView = convertView.findViewById(R.id.checkBoxView);

        File file = new File(headerTitle);
        pathTextView.setText(headerTitle);
        nameTextView.setText(file.getName());
        Check_For_Extension(headerTitle, coverImageView);

        checkBoxView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkBoxView.isChecked()) {
                    checkBoxView.setChecked(false);
                    Objects.requireNonNull(checkBoxArray.get(groupPosition)).set(childPosition, false);
                    showDuplicate.setTotal_Size("sub", new File(Objects.requireNonNull(list_Header_Child.get(list_Header.get(groupPosition))).get(childPosition)).length());

                } else {
                    checkBoxView.setChecked(true);
                    Objects.requireNonNull(checkBoxArray.get(groupPosition)).set(childPosition, true);
                    showDuplicate.setTotal_Size("add", new File(Objects.requireNonNull(list_Header_Child.get(list_Header.get(groupPosition))).get(childPosition)).length());
                }
            }
        });

        checkBoxView.setChecked(Objects.requireNonNull(checkBoxArray.get(groupPosition)).get(childPosition));
        return convertView;

    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    private static long getFileSizeInBytes(String fileName) {
        long ret = 0;
        File f = new File(fileName);
        if (f.exists()) {
            if (f.isFile()) {
                return f.length();
            } else if (f.isDirectory()) {
                File[] contents = f.listFiles();
                for (File content : Objects.requireNonNull(contents)) {
                    if (content.isFile()) {
                        ret += content.length();
                    } else if (content.isDirectory())
                        ret += getFileSizeInBytes(content.getPath());
                }
            }
        }
        return ret;
    }

    private String Convert_It(long size) {
        if (size > (1024 * 1024 * 1024)) {
            // GB
            return Convert_To_Decimal((float) size / (1024 * 1024 * 1024)) + " GB";
        } else if (size > (1024 * 1024)) {
            // MB
            return Convert_To_Decimal((float) size / (1024 * 1024)) + " MB";

        } else {
            // KB
            return Convert_To_Decimal((float) size / (1024)) + " KB";
        }

    }

    private String Convert_To_Decimal(float value) {
        String size = value + "";
        if (value >= 1000) {
            return size.substring(0, 4);
        } else if (value >= 100) {
            return size.substring(0, 3);
        } else {
            if (size.length() == 2 || size.length() == 3) {
                return size.substring(0, 1);
            }
            return size.substring(0, 4);

        }

    }

    private void Check_For_Extension(String path, ImageView imageView) {
        int i = path.lastIndexOf('.');
        String extension = "";
        if (i > 0) {
            extension = path.substring(i + 1);
        }
        switch (extension) {
            case "jpg":
            case "mp4":
            case "jpeg":
            case "png":
            case "gif":
            case "webp":
                // Images || Videos
                Glide.with(context)
                        .asBitmap()
                        .load(Uri.fromFile(new File(path)))
                        .into(imageView);
                break;
            case "mp3":
            case "m4a":
            case "amr":
            case "aac":
                // Audiio
                getAudioAlbumImageContentUri(imageView, path);

                break;
            case "pptx":
            case "pdf":
            case "docx":
            case "txt":
                imageView.setImageResource(R.drawable.document_icon);
                break;
            case "opus":
                imageView.setImageResource(R.drawable.voice_icon);
                break;
            default:
                imageView.setImageResource(R.drawable.file_icon);
                break;
        }

    }

    private void getAudioAlbumImageContentUri(ImageView imageView, String filePath) {
        try {
            Uri audioUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            String selection = MediaStore.Audio.Media.DATA + "=? ";
            String[] projection = new String[]{MediaStore.Audio.Media._ID, MediaStore.Audio.Media.ALBUM_ID};

            Cursor cursor = context.getContentResolver().query(
                    audioUri,
                    projection,
                    selection,
                    new String[]{filePath}, null);

            if (cursor != null && cursor.moveToFirst()) {

                long albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                if (get_Cover(albumId) != null) {
                    Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
                    Uri imgUri = ContentUris.withAppendedId(sArtworkUri,
                            albumId);

                    Glide.with(context)
                            .load(imgUri)
                            .into(imageView);
                } else {
                    imageView.setImageResource(R.drawable.audio_icon);
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e("Exception", Objects.requireNonNull(e.getMessage()));
        }
    }

    private Bitmap get_Cover(long album_id) {
        Bitmap artwork;
        Bitmap resizedBitmap = null;
        try {
            Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
            Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);
            ContentResolver res = context.getContentResolver();
            InputStream in = res.openInputStream(uri);
            artwork = BitmapFactory.decodeStream(in);

            int width = artwork.getWidth();
            int height = artwork.getHeight();
            float scaleWidth = ((float) 500) / width;
            float scaleHeight = ((float) 500) / height;
            // CREATE A MATRIX FOR THE MANIPULATION
            Matrix matrix = new Matrix();
            // RESIZE THE BIT MAP
            matrix.postScale(scaleWidth, scaleHeight);

            // "RECREATE" THE NEW BITMAP
            resizedBitmap = Bitmap.createBitmap(
                    artwork, 0, 0, width, height, matrix, false);

        } catch (Exception ignored) {

        }

        return resizedBitmap;
    }

    LinkedHashMap<Integer, List<Boolean>> getCheckBoxArray() {
        return checkBoxArray;
    }




}
