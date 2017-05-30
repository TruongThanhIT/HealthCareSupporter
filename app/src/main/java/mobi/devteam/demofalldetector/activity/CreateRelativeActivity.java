package mobi.devteam.demofalldetector.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import mobi.devteam.demofalldetector.R;
import mobi.devteam.demofalldetector.model.Relative;
import mobi.devteam.demofalldetector.utils.Tools;

public class CreateRelativeActivity extends AppCompatActivity {
    @BindView(R.id.edtCreateRelativeName)
    EditText edtRelativeName;
    @BindView(R.id.edtCreateRelativePhone)
    EditText edtRelativePhone;
    @BindView(R.id.imgCreateRelative)
    ImageView imgCreateRelative;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_relative);
        ButterKnife.bind(this);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_create, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                this.finish();
                return true;
            case R.id.mnuSave:
                Relative relative = new Relative();
                relative.setId(Calendar.getInstance().getTimeInMillis());
                relative.setPhone(String.valueOf(edtRelativePhone.getText().toString()));
                relative.setName(edtRelativeName.getText().toString());
                try{
                    Bitmap bitmapAvatar = Tools.convertImageViewToBitmap(imgCreateRelative);
                    relative.setAvatar(Tools.convertBitmapToByteAray(bitmapAvatar));
                }catch (NullPointerException e){
                    Bitmap bitmapDefaultAvatar = BitmapFactory.decodeResource(this.getResources(),
                            R.drawable.image_user_login);
                    relative.setAvatar(Tools.convertBitmapToByteAray(bitmapDefaultAvatar));
                }

                Toast.makeText(this, relative.toString(), Toast.LENGTH_SHORT).show();
                finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
