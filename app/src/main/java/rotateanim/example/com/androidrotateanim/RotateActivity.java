package rotateanim.example.com.androidrotateanim;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class RotateActivity extends AppCompatActivity {

    private ImageView mRotateImgv;
    private Button mSwitchAnimBtn1;
    private Button mSwitchAnimBtn2;
    private Button mSwitchAnimBtn3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rotate);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        initViewAndListener();
    }

    private void initViewAndListener() {
        mRotateImgv = (ImageView) findViewById(R.id.rotateview);
        mSwitchAnimBtn1 = (Button) findViewById(R.id.rotateanim_btn1);
        mSwitchAnimBtn2 = (Button) findViewById(R.id.rotateanim_btn2);
        mSwitchAnimBtn3 = (Button) findViewById(R.id.rotateanim_btn3);
        mSwitchAnimBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotateAnimHorizon();
            }
        });
        mSwitchAnimBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotateOnXCoordinate();
            }
        });
        mSwitchAnimBtn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotateOnYCoordinate();
            }
        });

        initRoundNumProgressViews();
    }

    private int mCurPic = 0;
    private Runnable mEndFlagRunnable = new Runnable() {
        @Override
        public void run() {
            mRotateImgv.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mCurPic = mCurPic % 3;
                    switch (mCurPic) {
                        case 0:
                            mRotateImgv.setImageResource(R.drawable.test1);
                            break;
                        case 1:
                            mRotateImgv.setImageResource(R.drawable.test2);
                            break;
                        case 2:
                            mRotateImgv.setImageResource(R.drawable.test3);
                            break;
                    }
                    mCurPic ++;
                }
            }, 50);
        }
    };

    private void initRoundNumProgressViews() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        final int numProgressView1Size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, metrics);
        final int numProgressView2Size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, metrics);

        final RoundNumProgressView numProgressView = (RoundNumProgressView) findViewById(R.id.roundnumprogressview);
        numProgressView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                numProgressView.setCurProgress(10);
                numProgressView.startFlickerArcProgress(mEndFlagRunnable);
            }
        });
        LinearLayout parent = (LinearLayout) findViewById(R.id.proress_parent);
        final RoundNumProgressView numProgressView1 = new RoundNumProgressView(getBaseContext());
        numProgressView1.setLayoutParams(new ViewGroup.LayoutParams(numProgressView1Size, numProgressView1Size));
        parent.addView(numProgressView1);
        ((LinearLayout.LayoutParams) numProgressView1.getLayoutParams()).leftMargin = 30;
        numProgressView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                numProgressView1.setCurProgress(50);
                numProgressView1.reRefreshToOriginProgress();
            }
        });
        final RoundNumProgressView numProgressView2 = new RoundNumProgressView(getBaseContext());
        numProgressView2.setLayoutParams(new ViewGroup.LayoutParams(numProgressView2Size, numProgressView2Size));
        parent.addView(numProgressView2);
        ((LinearLayout.LayoutParams) numProgressView2.getLayoutParams()).leftMargin = 30;
        numProgressView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                numProgressView2.setCurProgress(0);
                numProgressView2.startFlickerArcProgress(mEndFlagRunnable);
            }
        });
        final GradientArcView gradientArcView = new GradientArcView(getBaseContext());
        gradientArcView.setLayoutParams(new ViewGroup.LayoutParams(numProgressView2Size, numProgressView2Size));
        parent.addView(gradientArcView);
        ((LinearLayout.LayoutParams) gradientArcView.getLayoutParams()).leftMargin = 30;
        gradientArcView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gradientArcView.startFlickerArcProgress(mEndFlagRunnable);
            }
        });

        final GradientArcProgressView gradientArcProgressView = new GradientArcProgressView(getBaseContext());
        gradientArcProgressView.setLayoutParams(new ViewGroup.LayoutParams(numProgressView2Size, numProgressView2Size));
        parent.addView(gradientArcProgressView);
        ((LinearLayout.LayoutParams) gradientArcProgressView.getLayoutParams()).leftMargin = 30;
        gradientArcProgressView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gradientArcProgressView.startFlickerArcProgress(mEndFlagRunnable);
            }
        });

        final GradientArcProgressView2 gradientArcProgressView2 = new GradientArcProgressView2(getBaseContext());
        gradientArcProgressView2.setLayoutParams(new ViewGroup.LayoutParams(numProgressView2Size, numProgressView2Size));
        parent.addView(gradientArcProgressView2);
        ((LinearLayout.LayoutParams) gradientArcProgressView2.getLayoutParams()).leftMargin = 30;
        gradientArcProgressView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gradientArcProgressView2.startFlickerArcProgress(mEndFlagRunnable);
            }
        });
    }

    private void rotateOnXCoordinate() {
        float centerX = mRotateImgv.getWidth() / 2.0f;
        float centerY = mRotateImgv.getHeight() / 2.0f;
        float depthZ = 0f;
        Rotate3dAnimation rotate3dAnimationX = new Rotate3dAnimation(0, 180, centerX, centerY, depthZ, Rotate3dAnimation.ROTATE_X_AXIS, true);
        rotate3dAnimationX.setDuration(1000);
        mRotateImgv.startAnimation(rotate3dAnimationX);

        // 下面的代码，旋转的时候可以带透明度
//        float centerX = mRotateImgv.getWidth() / 2.0f;
//        float centerY = mRotateImgv.getHeight() / 2.0f;
//        float depthZ = 0f;
//        Rotate3dAnimationXY rotate3dAnimationX = new Rotate3dAnimationXY(0, 180, centerX, centerY, depthZ, true, (byte) 0);
//        rotate3dAnimationX.setDuration(1000);
//        mRotateImgv.startAnimation(rotate3dAnimationX);
    }

    private void rotateOnYCoordinate() {
        float centerX = mRotateImgv.getWidth() / 2.0f;
        float centerY = mRotateImgv.getHeight() / 2.0f;
        float centerZ = 0f;

        Rotate3dAnimation rotate3dAnimationX = new Rotate3dAnimation(0, 180, centerX, centerY, centerZ, Rotate3dAnimation.ROTATE_Y_AXIS, true);
        rotate3dAnimationX.setDuration(1000);
        mRotateImgv.startAnimation(rotate3dAnimationX);
    }

    private void rotateAnimHorizon() {
        float centerX = mRotateImgv.getWidth() / 2.0f;
        float centerY = mRotateImgv.getHeight() / 2.0f;
        float centerZ = 0f;

        Rotate3dAnimation rotate3dAnimationX = new Rotate3dAnimation(180, 0, centerX, centerY, centerZ, Rotate3dAnimation.ROTATE_Z_AXIS, true);
        rotate3dAnimationX.setDuration(1000);
        mRotateImgv.startAnimation(rotate3dAnimationX);

        // 下面是使用android自带的旋转动画
//        RotateAnimation rotateAnimation = new RotateAnimation(0, 180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
//        rotateAnimation.setDuration(1000);
//        mRotateImgv.startAnimation(rotateAnimation);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_rotate, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
