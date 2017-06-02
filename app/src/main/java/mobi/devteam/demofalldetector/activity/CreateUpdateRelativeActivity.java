package mobi.devteam.demofalldetector.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import mobi.devteam.demofalldetector.R;
import mobi.devteam.demofalldetector.model.Relative;
import mobi.devteam.demofalldetector.utils.Tools;

public class CreateUpdateRelativeActivity extends AppCompatActivity {
    public static final String EXTRA_IS_ADD_MODE = "is_add_mode";
    public static final String EXTRA_RELATIVE_DATA = "relative_data";
    private boolean is_add_mode = true;
    private Relative relative;

    @BindView(R.id.edtCreateRelativeName)
    EditText edtRelativeName;
    @BindView(R.id.edtCreateRelativePhone)
    EditText edtRelativePhone;
    @BindView(R.id.imgCreateRelative)
    ImageView imgCreateRelative;

    private FirebaseAuth mAuth;
    private DatabaseReference relative_data;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_update_relative);

        mAuth = FirebaseAuth.getInstance();
        relative_data = FirebaseDatabase.getInstance().getReference("relatives");

        if (getIntent().hasExtra(EXTRA_IS_ADD_MODE)) {
            Intent intent = getIntent();

            is_add_mode = intent.getBooleanExtra(EXTRA_IS_ADD_MODE, true);
            relative_data = intent.getParcelableExtra(EXTRA_RELATIVE_DATA);
        }

        initdata();
        ButterKnife.bind(this);
    }

    private void initdata() {

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

                /*
                try {
                    Bitmap bitmapAvatar = Tools.convertImageViewToBitmap(imgCreateRelative);
                    relative.setAvatar(Tools.convertBitmapToByteAray(bitmapAvatar));
                } catch (NullPointerException e) {
                    Bitmap bitmapDefaultAvatar = BitmapFactory.decodeResource(this.getResources(),
                            R.drawable.image_user_login);
                    relative.setAvatar(Tools.convertBitmapToByteAray(bitmapDefaultAvatar));
                }*/

                FirebaseUser currentUser = mAuth.getCurrentUser();
                DatabaseReference child = relative_data.child(currentUser.getUid());
                long tsLong = System.currentTimeMillis();

                if (relative == null)
                    relative = new Relative();

                relative.setName(edtRelativeName.getText().toString());
                relative.setPhone(edtRelativePhone.getText().toString());

                if (is_add_mode){
                    relative.setId(tsLong);
                    child.child(tsLong+"").setValue(relative);

                }else{
                    child.child(relative.getId()+"").setValue(relative);
                }

                finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
