package com.codekarhu.whatswithme;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import com.ibm.watson.developer_cloud.android.speech_common.v1.TokenProvider;
import com.ibm.watson.developer_cloud.android.speech_to_text.v1.ISpeechDelegate;
import com.ibm.watson.developer_cloud.android.speech_to_text.v1.SpeechToText;
import com.ibm.watson.developer_cloud.android.speech_to_text.v1.dto.SpeechConfiguration;
import com.ibm.watson.developer_cloud.android.text_to_speech.v1.TextToSpeech;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    FloatingActionButton mFab;
    RecordCircle recordCircle;
    AnimatorSet runningAnimationAudio;
    private Handler mHandler = null;

    FragmentTabSTT fragmentTabSTT = new FragmentTabSTT();
    FragmentTabTTS fragmentTabTTS = new FragmentTabTTS();
    DocTalkFragment talkFragment = new DocTalkFragment();

    boolean isRecording;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.init(this);
        setContentView(R.layout.activity_main);
        // Strictmode needed to run the http/wss request for devices > Gingerbread
        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.GINGERBREAD) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        mHandler = new Handler();

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        recordCircle = (RecordCircle) findViewById(R.id.record);
        recordCircle.setVisibility(View.INVISIBLE);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateRecordIntefrace();
                initView(true);
            }
        });
        recordCircle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateRecordIntefrace();
                initView(false);
            }
        });

        if(savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(fragmentTabSTT, "STT")
                    .add(fragmentTabTTS, "TTS")
                    .commit();
        }
    }

    private void initView(boolean show) {
        Log.w(TAG, "init view");
        talkFragment = (DocTalkFragment) getSupportFragmentManager().findFragmentByTag("dialog");
        if(talkFragment != null && talkFragment.getView() != null) {
            if(isRecording && show) {
                talkFragment.getView().setVisibility(View.VISIBLE);
                Log.w(TAG, "visible fragment");
            } else {
                talkFragment.getView().setVisibility(View.INVISIBLE);
                Log.w(TAG, "invisible fragment");
            }
        } else {
            final Runnable runnableUi = new Runnable(){
                @Override
                public void run() {
                    talkFragment = new DocTalkFragment();
                    getSupportFragmentManager().beginTransaction().
                            replace(R.id.container, talkFragment, "dialog")
                            .addToBackStack(null)
                            .commit();
                }
            };

            new Thread(){
                public void run(){
                    mHandler.post(runnableUi);
                }
            }.start();

        }
    }

    private void updateRecordIntefrace() {
        isRecording = !isRecording;
        if (isRecording) {
            mFab.setVisibility(View.INVISIBLE);
            recordCircle.setVisibility(View.VISIBLE);
            recordCircle.setAmplitude(0);
            ViewCompat.setTranslationX(recordCircle, 0);
            if (runningAnimationAudio != null) {
                runningAnimationAudio.cancel();
            }
            runningAnimationAudio = new AnimatorSet();
            float slide = (Utils.getScreenHeight(MainActivity.this) / 2) - ( Utils.dp(72));
            Log.i(TAG, "slide= "+slide);
            runningAnimationAudio.playTogether(
                    ObjectAnimator.ofFloat(recordCircle, "scale", 1),
                    ObjectAnimator.ofFloat(recordCircle, "translationY", slide));
            runningAnimationAudio.setDuration(300);
            runningAnimationAudio.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {}
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (runningAnimationAudio != null && runningAnimationAudio.equals(animation)) {
                        runningAnimationAudio = null;
                        fragmentTabSTT.record(true);
                    }
                }
                @Override
                public void onAnimationCancel(Animator animation) {}
                @Override
                public void onAnimationRepeat(Animator animation) {}
            });
            runningAnimationAudio.setInterpolator(new DecelerateInterpolator());
            runningAnimationAudio.start();
            startFoo();
        } else {
            fragmentTabSTT.record(false);
            if (runningAnimationAudio != null) {
                runningAnimationAudio.cancel();
            }
            runningAnimationAudio = new AnimatorSet();
            runningAnimationAudio.playTogether(
                    ObjectAnimator.ofFloat(recordCircle, "scale", 0.0f),
                    ObjectAnimator.ofFloat(recordCircle, "translationY", 0));
            runningAnimationAudio.setDuration(300);
            runningAnimationAudio.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {}
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (runningAnimationAudio != null && runningAnimationAudio.equals(animation)) {
                        recordCircle.setVisibility(View.INVISIBLE);
                        mFab.setVisibility(View.VISIBLE);
                        runningAnimationAudio = null;
                    }
                }
                @Override
                public void onAnimationCancel(Animator animation) {}
                @Override
                public void onAnimationRepeat(Animator animation) {}
            });
            runningAnimationAudio.setInterpolator(new AccelerateInterpolator());
            runningAnimationAudio.start();
        }
    }


    private static String mRecognitionResults = "";

    private enum ConnectionState {
        IDLE, CONNECTING, CONNECTED
    }

    ConnectionState mState = ConnectionState.IDLE;

    int phase = 0;

//For presentation only, remove and replace with connection to Watson api
    String[] fooData = new String[] {"Do you have a temperature?", "Do you have dry cough", "Do you have sneezing" };
    String[] fooTalk = new String[] {"Doc, what is wrong with me, I have sharp headache", "I think it's quite high"};

    private void startFoo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    phase = 0;
                    Thread.sleep(10000);
                    fragmentTabSTT.record(false);
                    playTTS(fooData[phase]);
                    phase = 1;
                    Thread.sleep(6000);
                    fragmentTabSTT.record(true);
                    Thread.sleep(8000);
                    fragmentTabSTT.record(false);
                    playTTS(fooData[phase]);
                    phase = 2;
                    Thread.sleep(6000);
                    fragmentTabSTT.record(true);
                    Thread.sleep(8000);
                    fragmentTabSTT.record(false);
                    playTTS(fooData[phase]);
                    phase = 3;
                    Thread.sleep(6000);
                    fragmentTabSTT.record(true);
                    Thread.sleep(6000);
                    playTTS("Probably you have got a cold");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.i(TAG, "10 sec time");
            }
        }).start();
    }

    public void displayResult(final String result) {
        final Runnable runnableUi = new Runnable(){
            @Override
            public void run() {
                TextView textResult = (TextView) findViewById(R.id.textState);
                textResult.setText(result);
            }
        };
        new Thread(){
            public void run(){
                mHandler.post(runnableUi);
            }
        }.start();
    }



    public static class FragmentTabSTT extends Fragment implements ISpeechDelegate {

        // session recognition results
        private static String mRecognitionResults = "";

        ConnectionState mState = ConnectionState.IDLE;
        public Context mContext = null;
        private Handler mHandler = null;

        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
            mHandler = new Handler();
            if (initSTT() == false) {
                displayResult("Error: no authentication credentials/token available, please enter your authentication information");
                return;
            }

            displayStatus("please, press the button to start speaking");
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            mContext = getActivity().getApplicationContext();
            if(mHandler ==null)
                mHandler = new Handler();
        }

        public void record(boolean isRecording) {
            if((isRecording && mState == ConnectionState.CONNECTED) ||
                    (!isRecording && mState == ConnectionState.IDLE)) return;
            if (mState == ConnectionState.IDLE) {
                mState = ConnectionState.CONNECTING;
                Log.d(TAG, "onClickRecord: IDLE -> CONNECTING");
                mRecognitionResults = "";
                displayResult(mRecognitionResults);

                SpeechToText.sharedInstance().setModel("en-US_BroadbandModel");
                displayStatus("connecting to the STT service...");
                // start recognition
                new AsyncTask<Void, Void, Void>(){
                    @Override
                    protected Void doInBackground(Void... none) {
                        SpeechToText.sharedInstance().recognize();
                        return null;
                    }
                }.execute();
                setButtonLabel(R.id.buttonRecord, "Connecting...");
                setButtonState(true);
            }
            else if (mState == ConnectionState.CONNECTED) {
                mState = ConnectionState.IDLE;
                Log.d(TAG, "onClickRecord: CONNECTED -> IDLE");
                SpeechToText.sharedInstance().stopRecognition();
                if(((MainActivity)getActivity()).phase < 2)
                ((MainActivity)getActivity()).talkFragment.addOrUpdate(
                        ((MainActivity)getActivity()).fooTalk[((MainActivity)getActivity()).phase]);
                setButtonState(false);
            }
        }

        public URI getHost(String url){
            try {
                return new URI(url);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            return null;
        }

        // initialize the connection to the Watson STT service
        private boolean initSTT() {

            // DISCLAIMER: please enter your credentials or token factory in the lines below
            String username = getString(R.string.STTUsername);
            String password = getString(R.string.STTPassword);

            String tokenFactoryURL = getString(R.string.defaultTokenFactory);
            String serviceURL = "wss://stream.watsonplatform.net/speech-to-text/api";

            SpeechConfiguration sConfig = new SpeechConfiguration(SpeechConfiguration.AUDIO_FORMAT_OGGOPUS);
            //SpeechConfiguration sConfig = new SpeechConfiguration(SpeechConfiguration.AUDIO_FORMAT_DEFAULT);

            SpeechToText.sharedInstance().initWithContext(this.getHost(serviceURL), getActivity().getApplicationContext(), sConfig);

            // token factory is the preferred authentication method (service credentials are not distributed in the client app)
            if (tokenFactoryURL.equals(getString(R.string.defaultTokenFactory)) == false) {
                SpeechToText.sharedInstance().setTokenProvider(new MyTokenProvider(tokenFactoryURL));
            }
            // Basic Authentication
            else if (username.equals(getString(R.string.defaultUsername)) == false) {
                SpeechToText.sharedInstance().setCredentials(username, password);
            } else {
                // no authentication method available
                return false;
            }

            SpeechToText.sharedInstance().setModel(getString(R.string.modelDefault));
            SpeechToText.sharedInstance().setDelegate(this);

            return true;
        }


        public void displayResult(final String result) {
            final Runnable runnableUi = new Runnable(){
                @Override
                public void run() {
//                    TextView textResult = (TextView)mView.findViewById(R.id.textResult);
//                    textResult.setText(result);
                    Log.i(TAG, "result = "+result);
                    if(result.startsWith("ssl")) return;
                    ((MainActivity)getActivity()).talkFragment.addOrUpdate(result);
                }
            };

            new Thread(){
                public void run(){
                    mHandler.post(runnableUi);
                }
            }.start();
        }

        public void displayStatus(final String status) {
            /*final Runnable runnableUi = new Runnable(){
                @Override
                public void run() {
                    TextView textResult = (TextView)mView.findViewById(R.id.sttStatus);
                    textResult.setText(status);
                }
            };
            new Thread(){
                public void run(){
                    mHandler.post(runnableUi);
                }
            }.start();*/
        }

        /**
         * Change the button's label
         */
        public void setButtonLabel(final int buttonId, final String label) {
            final Runnable runnableUi = new Runnable(){
                @Override
                public void run() {
//                    Button button = (Button)mView.findViewById(buttonId);
//                    button.setText(label);
                }
            };
            new Thread(){
                public void run(){
                    mHandler.post(runnableUi);
                }
            }.start();
        }

        /**
         * Change the button's drawable
         */
        public void setButtonState(final boolean bRecording) {

//            final Runnable runnableUi = new Runnable(){
//                @Override
//                public void run() {
//                    int iDrawable = bRecording ? R.drawable.button_record_stop : R.drawable.button_record_start;
//                    Button btnRecord = (Button)mView.findViewById(R.id.buttonRecord);
//                    btnRecord.setBackground(getResources().getDrawable(iDrawable));
//                }
//            };
//            new Thread(){
//                public void run(){
//                    mHandler.post(runnableUi);
//                }
//            }.start();
        }

        // delegages ----------------------------------------------

        public void onOpen() {
            Log.d(TAG, "onOpen");
            displayStatus("successfully connected to the STT service");
            setButtonLabel(R.id.buttonRecord, "Stop recording");
            mState = ConnectionState.CONNECTED;
        }

        public void onError(String error) {

            Log.e(TAG, error);
            displayResult(error);
            mState = ConnectionState.IDLE;
        }

        public void onClose(int code, String reason, boolean remote) {
            Log.d(TAG, "onClose, code: " + code + " reason: " + reason);
            displayStatus("connection closed");
            setButtonLabel(R.id.buttonRecord, "Record");
            mState = ConnectionState.IDLE;
        }

        public void onMessage(String message) {

            Log.d(TAG, "onMessage, message: " + message);
            try {
                JSONObject jObj = new JSONObject(message);
                // state message
                if(jObj.has("state")) {
                    Log.d(TAG, "Status message: " + jObj.getString("state"));
                }
                // results message
                else if (jObj.has("results")) {
                    //if has result
                    Log.d(TAG, "Results message: ");
                    JSONArray jArr = jObj.getJSONArray("results");
                    for (int i=0; i < jArr.length(); i++) {
                        JSONObject obj = jArr.getJSONObject(i);
                        JSONArray jArr1 = obj.getJSONArray("alternatives");
                        String str = jArr1.getJSONObject(0).getString("transcript");
                        // remove whitespaces if the language requires it
//                        String model = this.getModelSelected();
//                        if (model.startsWith("ja-JP") || model.startsWith("zh-CN")) {
//                            str = str.replaceAll("\\s+","");
//                        }
                        String strFormatted = Character.toUpperCase(str.charAt(0)) + str.substring(1);
                        if (obj.getString("final").equals("true")) {
                            String stopMarker = ". ";
                            mRecognitionResults += strFormatted.substring(0,strFormatted.length()-1) + stopMarker;
                            displayResult(mRecognitionResults);
                        } else {
                            displayResult(mRecognitionResults + strFormatted);
                        }
                        break;
                    }
                } else {
                    displayResult("unexpected data coming from stt server: \n" + message);
                }

            } catch (JSONException e) {
                Log.e(TAG, "Error parsing JSON");
                e.printStackTrace();
            }
        }

        public void onAmplitude(final double amplitude, final double volume) {
            Runnable myRunnable = new Runnable() {
                @Override
                public void run() {
                    ((MainActivity)getActivity()).recordCircle.setAmplitude(amplitude / volume);
                }
            };
            mHandler.post(myRunnable);
//            Log.e(TAG, "amplitude=" + amplitude + ", volume=" + volume);
        }
    }

    public static class FragmentTabTTS extends Fragment {

        public Context mContext = null;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
            Log.d(TAG, "onCreateTTS");
            mContext = getActivity().getApplicationContext();
            initTTS();
            try {
                ((MainActivity)getActivity()).playTTS("Hello, I am doctor Watson, can I help you?");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public URI getHost(String url){
            try {
                return new URI(url);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            return null;
        }

        private boolean initTTS() {

            // DISCLAIMER: please enter your credentials or token factory in the lines below

            String username = getString(R.string.TTSUsername);
            String password = getString(R.string.TTSPassword);
            String tokenFactoryURL = getString(R.string.defaultTokenFactory);
            String serviceURL = "https://stream.watsonplatform.net/text-to-speech/api";

            TextToSpeech.sharedInstance().initWithContext(this.getHost(serviceURL));

            // token factory is the preferred authentication method (service credentials are not distributed in the client app)
            if (tokenFactoryURL.equals(getString(R.string.defaultTokenFactory)) == false) {
                TextToSpeech.sharedInstance().setTokenProvider(new MyTokenProvider(tokenFactoryURL));
            }
            // Basic Authentication
            else if (username.equals(getString(R.string.defaultUsername)) == false) {
                TextToSpeech.sharedInstance().setCredentials(username, password);
            } else {
                // no authentication method available
                return false;
            }

            TextToSpeech.sharedInstance().setVoice(getString(R.string.voiceDefault));

            return true;
        }


    }


    static class MyTokenProvider implements TokenProvider {

        String m_strTokenFactoryURL = null;

        public MyTokenProvider(String strTokenFactoryURL) {
            m_strTokenFactoryURL = strTokenFactoryURL;
        }

        public String getToken() {

            Log.d(TAG, "attempting to get a token from: " + m_strTokenFactoryURL);
            try {
                // DISCLAIMER: need implement an authentication mechanism from the mobile app to the
                // server side app so the token factory in the server only provides tokens to authenticated clients
                HttpClient httpClient = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(m_strTokenFactoryURL);
                HttpResponse executed = httpClient.execute(httpGet);
                InputStream is = executed.getEntity().getContent();
                StringWriter writer = new StringWriter();
                IOUtils.copy(is, writer, "UTF-8");
                String strToken = writer.toString();
                Log.d(TAG, strToken);
                return strToken;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }


    String text;
    TextView textTTS;

    /**
     * Play TTS Audio data
     *
     * @param ttsText
     */
    public void playTTS(String ttsText) throws JSONException {

        TextToSpeech.sharedInstance().setVoice("en-US_MichaelVoice");
//        Log.d(TAG, fragmentTabTTS.getSelectedVoice());

        //Get text from text box
//        textTTS = (TextView)fragmentTabTTS.mView.findViewById(R.id.prompt);
//        ttsText = textTTS.getText().toString();
        Log.d(TAG, ttsText);
//        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.hideSoftInputFromWindow(textTTS.getWindowToken(),
//                InputMethodManager.HIDE_NOT_ALWAYS);

        //Call the sdk function
        TextToSpeech.sharedInstance().synthesize(ttsText);
        if(talkFragment!= null) talkFragment.add(ttsText, true);

    }


    @Override
    public void onBackPressed() {
        if(isRecording) {
            updateRecordIntefrace();
            fragmentTabSTT.record(false);
        }
        super.onBackPressed();
    }
}
