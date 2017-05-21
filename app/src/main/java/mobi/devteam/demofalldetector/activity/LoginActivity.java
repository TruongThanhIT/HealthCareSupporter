package mobi.devteam.demofalldetector.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;
import mobi.devteam.demofalldetector.R;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.btnSignIn)
    Button btnSignIn;
    @BindView(R.id.txtUserName)
    EditText edtUserName;
    @BindView(R.id.txtPassword)
    EditText edtPassword;

    private static final String USER_NAME = "admin";
    private static final String PASSWORD = "123";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.btnSignIn)
    void sign_in() {
        if (edtUserName.getText().toString().compareTo(USER_NAME) == 0 &&
                edtPassword.getText().toString().compareTo(PASSWORD) == 0) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(this, getString(R.string.err_login), Toast.LENGTH_SHORT).show();
        }
    }
}
