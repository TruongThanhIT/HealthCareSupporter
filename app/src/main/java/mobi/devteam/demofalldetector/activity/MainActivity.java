package mobi.devteam.demofalldetector.activity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import mobi.devteam.demofalldetector.R;
import mobi.devteam.demofalldetector.fragment.HomeFragment;
import mobi.devteam.demofalldetector.fragment.ProfileFragment;
import mobi.devteam.demofalldetector.fragment.RelativeListFragment;
import mobi.devteam.demofalldetector.myServices.DetectFallService;
import mobi.devteam.demofalldetector.myServices.GetLocationService;
import mobi.devteam.demofalldetector.myServices.ReminderService;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FragmentManager fragmentManager;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private NavigationView navigationView;
    private TextView txtUserName;
    private TextView txtUserEmail;
    private ImageView imgUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addControls();
        initData();


        startService(new Intent(this, GetLocationService.class));
    }

    private void initData() {
        txtUserEmail.setText(user.getEmail());
        String userName = user.getDisplayName();
        try {
            if (userName.equals("")) {
                String[] strName = txtUserEmail.getText().toString().split("@");
                userName = strName[0];
            }
            txtUserName.setText(userName);
        } catch (Exception e) {

        }
        Uri uriImageUser = user.getPhotoUrl();
        if (uriImageUser == null) {
            imgUser.setImageResource(R.drawable.ic_launcher);
        } else {
            Picasso.with(this).load(uriImageUser).into(imgUser);
        }
        onNavigationItemSelected(navigationView.getMenu().getItem(0));// select home for default
    }

    private void addControls() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);

        View header = navigationView.getHeaderView(0);
        txtUserName = (TextView) header.findViewById(R.id.txtUserNameNav);
        txtUserEmail = (TextView) header.findViewById(R.id.txtEmailUserNav);
        imgUser = (ImageView) header.findViewById(R.id.imgUser);
        fragmentManager = getSupportFragmentManager();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        navItemSelected(item.getItemId());
        return true;
    }

    public void navItemSelected(int id) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        switch (id) {
            case R.id.nav_home:
                setTitle(R.string.tittle_home);
                fragmentTransaction.replace(R.id.frame_container, HomeFragment.newInstance());
                fragmentTransaction.commit();
                navigationView.getMenu().getItem(0).setChecked(true);

                break;
            case R.id.nav_relatives:
                setTitle(R.string.tittle_relatives);
                fragmentTransaction.replace(R.id.frame_container, RelativeListFragment.
                        newInstance());
                fragmentTransaction.commit();
                navigationView.getMenu().getItem(1).setChecked(true);
                break;
            case R.id.nav_logout:
                try {
                    mAuth.signOut();
                    Intent detectService = new Intent(this, DetectFallService.class);
                    Intent getLocationService = new Intent(this, GetLocationService.class);
                    Intent reminderService = new Intent(this, ReminderService.class);
                    stopService(detectService);
                    stopService(getLocationService);
                    stopService(reminderService);
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                } catch (Exception e) {
                    Log.e(this.getLocalClassName(), e.toString());
                }
                break;
            case R.id.nav_profile:
                setTitle(R.string.tittle_profile);
                fragmentTransaction.replace(R.id.frame_container, ProfileFragment.newInstance());
                fragmentTransaction.commit();
                navigationView.getMenu().getItem(2).setChecked(true);
                break;
            case R.id.nav_rate:
                Uri uri = Uri.parse("market://details?id=" + getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                // To count with Play market backstack, After pressing back button,
                // to taken back to our application, we need to add following flags to intent.
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
                }
                break;
            case R.id.nav_share:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "Spending more time to take care of your relatives by downloading this app : https://play.google.com/store/apps/details?id=" + getPackageName());
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
                break;
            case R.id.nav_feedback:
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", "healthcare_app@gmail.com", null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Healthcare Supporter");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "Please give us your feedback to improve our application.");
                startActivity(Intent.createChooser(emailIntent, "Feedback..."));
                break;
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }
}
