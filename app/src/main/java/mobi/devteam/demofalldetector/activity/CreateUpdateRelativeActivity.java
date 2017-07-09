package mobi.devteam.demofalldetector.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickResult;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import mobi.devteam.demofalldetector.R;
import mobi.devteam.demofalldetector.model.Relative;
import mobi.devteam.demofalldetector.utils.Tools;

public class CreateUpdateRelativeActivity extends AppCompatActivity implements IPickResult {
    public static final String EXTRA_IS_ADD_MODE = "is_add_mode";
    public static final String EXTRA_RELATIVE_DATA = "relative_data";
    public static final String TAG = "RelativeActivity";
    ;
    @BindView(R.id.edtCreateRelativeName)
    EditText edtRelativeName;
    @BindView(R.id.edtCreateRelativePhone)
    EditText edtRelativePhone;
    @BindView(R.id.imgCreateRelative)
    ImageView imgCreateRelative;
    private boolean is_add_mode = true;
    private Relative relative;
    private FirebaseAuth mAuth;
    private DatabaseReference relative_data;
    private StorageReference mStorageRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_update_relative);
        ButterKnife.bind(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        initdata();
    }

    private void initdata() {
        mAuth = FirebaseAuth.getInstance();
        relative_data = FirebaseDatabase.getInstance().getReference("relatives");
        mStorageRef = FirebaseStorage.getInstance().getReference();

        if (getIntent().hasExtra(EXTRA_IS_ADD_MODE)) {
            Intent intent = getIntent();

            is_add_mode = intent.getBooleanExtra(EXTRA_IS_ADD_MODE, true);
            relative = intent.getParcelableExtra(EXTRA_RELATIVE_DATA);

            if (relative != null) {
                edtRelativeName.setText(relative.getName());
                edtRelativePhone.setText(relative.getPhone());

                Picasso.with(this)
                        .load(relative.getThumb())
                        .into(imgCreateRelative);
            }
        }
    }

    @OnClick(R.id.imgCreateRelative)
    void select_image() {
        PickImageDialog.build(new PickSetup()).show(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_create, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            case R.id.mnuSave:
                save_relative();
        }
        return super.onOptionsItemSelected(item);
    }

    private void save_relative() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        final DatabaseReference child = relative_data.child(currentUser.getUid());
        final StorageReference relatives_images = mStorageRef.child("relatives_images").child(currentUser.getUid());

        long tsLong = System.currentTimeMillis();

        if (relative == null)
            relative = new Relative();
        if (edtRelativeName.getText().toString().equals("") || edtRelativePhone.getText().toString().equals("")) {
            Toast.makeText(this, R.string.err_empty_info_relative, Toast.LENGTH_SHORT).show();
        } else {
            relative.setName(edtRelativeName.getText().toString());
            relative.setPhone(edtRelativePhone.getText().toString());

            if (is_add_mode) {
                relative.setId(tsLong);
                child.child(tsLong + "").setValue(relative);
            } else {
                child.child(relative.getId() + "").setValue(relative);
            }

            try {
                Bitmap bitmapAvatar = Tools.convertImageViewToBitmap(imgCreateRelative);
                byte[] bytes = Tools.convertBitmapToByteAray(bitmapAvatar);
                relatives_images.child(relative.getId() + "").putBytes(bytes)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                @SuppressWarnings("VisibleForTests")
                                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                child.child(relative.getId() + "").child("thumb").setValue(downloadUrl.toString());
                            }
                        });
            } catch (NullPointerException e) {
                Log.e(TAG, e.toString());
            }
            finish();
        }
    }

    @Override
    public void onPickResult(PickResult r) {
        if (r.getError() == null) {

            Bitmap scaledBitmap = Tools.scaleCenterCrop(r.getBitmap(), 300, 300);
            imgCreateRelative.setImageBitmap(scaledBitmap);

        } else {
            Toast.makeText(this, r.getError().getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
