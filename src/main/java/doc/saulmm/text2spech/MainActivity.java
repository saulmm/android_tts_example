package doc.saulmm.text2spech;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.View;
import android.widget.*;

import java.util.ArrayList;

import static android.util.Log.d;

public class MainActivity extends Activity implements View.OnClickListener, TextToSpeech.OnInitListener, SeekBar.OnSeekBarChangeListener {
	// TTS Stuff
	private UtteranceProgressListener uteranceListener;
	private int TTS_DATA_CHECK_CODE = 0;
	private int RESULT_TALK_CODE = 1;
	private TextToSpeech myTTS;


	// UI Stuff
	private EditText speechEdit;
	private ImageButton talkToTextButton;
	private Button speechButton;
	private SeekBar speechRateSeekBar;
	private SeekBar pitchSeekBar;


	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		initUI();
		configureTTS();
	}


	/**
	 * Starts ui components
	 */
	private void initUI () {
		setContentView(R.layout.activity_main);
		speechButton = (Button) findViewById(R.id.speech_button);
		speechEdit = (EditText) findViewById(R.id.input_text);
		speechEdit.setText("Android text to speech is working !!!");
		speechButton.setOnClickListener(this);

		talkToTextButton = (ImageButton) findViewById(R.id.talk_text);
		talkToTextButton.setOnClickListener(this);

		speechRateSeekBar = (SeekBar) findViewById(R.id.speech_rate);
		speechRateSeekBar.setOnSeekBarChangeListener(this);

		pitchSeekBar = (SeekBar) findViewById(R.id.pitch);
		pitchSeekBar.setOnSeekBarChangeListener(this);
	}


	/**
	 * Starts activity with the check tts intent
	 * calling to the startActivityForResult method
	 */
	private void configureTTS () {
		Intent checkTTSIntent = new Intent();
		checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkTTSIntent, TTS_DATA_CHECK_CODE);
	}


	@Override
	protected void onActivityResult (int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		d("[DEBUG] doc.saulmm.text2spech.MainActivity.onActivityResult ", "Request_code: " + requestCode);

		if(requestCode == TTS_DATA_CHECK_CODE) {

			// The user has the TTS data installed
			if(resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
				myTTS = new TextToSpeech(this, this);
				configureTTSCalbacks();

			// The app will prompt the user to install TTS data
			} else {
				disableUI();
				Intent installTTSIntent = new Intent();
				installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(installTTSIntent);
			}

		} else if(requestCode == RESULT_TALK_CODE && data != null) {
				ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

				showResultDialog(text.get(0));
		}

	}

	/**
	 * Shows a dialog with the result text
	 * @param text
	 */
	private void showResultDialog (String text) {
		AlertDialog.Builder dBuilder = new AlertDialog.Builder(this);
		dBuilder.setTitle("Speect to text example");
		dBuilder.setMessage(text);
		dBuilder.setPositiveButton("Accept",null);
		dBuilder.create().show();
	}


	private void configureTTSCalbacks () {
		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			uteranceListener = new UtteranceProgressListener() {
				@Override
				public void onStart (String utteranceId) {
					d("[DEBUG] doc.saaulmm.text2spech.MainActivity.onStart ", "started");
				}


				@Override
				public void onDone (String utteranceId) {
					d("[DEBUG] doc.saulmm.text2spech.MainActivity.onDone ", "done");
				}


				@Override
				public void onError (String utteranceId) {
					d("[DEBUG] doc.saulmm.text2spech.MainActivity.onError ", "error");
				}
			};

			myTTS.setOnUtteranceProgressListener(uteranceListener);
		}
	}


	/**
	 * Disable ui input
	 */
	private void disableUI () {
		speechEdit.setText("Android text to speech is not working :(");
		speechEdit.setEnabled(false);
		speechButton.setEnabled(false);
	}


	@Override
	public void onClick (View v) {
		switch (v.getId()) {
			case R.id.speech_button:
				speech(speechEdit.getText().toString());
				break;


			case R.id.talk_text:
				talkToText();
				break;
		}
	}


	/**
	 * Inits the intent with text recognition
	 */
	private void talkToText () {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

		// Extra options
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
//		intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 2000000);
//      intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 2000000);
//		intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 20000000);
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speech to text example"); // Text message in the recognition dialog

		try {
			startActivityForResult(intent, RESULT_TALK_CODE);

		} catch (ActivityNotFoundException a) {
			Toast t = Toast.makeText(getApplicationContext(),
					"Opps! Your device doesn't support Speech to Text",
					Toast.LENGTH_SHORT);
			t.show();
		}
	}


	/**
	 * Speech the text parameter
	 * @param speechText the text to speech
	 */
	private void speech (String speechText) {
		if(myTTS != null) {

			myTTS.speak(speechText,
					TextToSpeech.QUEUE_FLUSH, //
					null);
		}

	}


	@Override
	public void onInit (int status) {

		switch (status) {
			case TextToSpeech.SUCCESS:
				configureTTSLocale();
				break;

			case TextToSpeech.ERROR:
				Toast.makeText(this, "TTS Failed :(", Toast.LENGTH_SHORT).show();
				Log.e("[ERROR] doc.saulmm.text2speech.MainActivity.onInit ", "TTS Failed");
				break;
		}
	}


	/**
	 * Useful tts configurations
	 */
	private void configureTTSLocale () {
		/*
		Locale deviceLocale = Locale.getDefault();

		if(myTTS.isLanguageAvailable(deviceLocale) == TextToSpeech.LANG_AVAILABLE)
			myTTS.setLanguage(deviceLocale);
		*/
	}


	@Override
	public void onProgressChanged (SeekBar seekBar, int progress, boolean fromUser) {

		switch(seekBar.getId()) {
			case R.id.speech_rate:
				manageSpeechRate(progress);

				break;

			case R.id.pitch:
				managePitch(progress);
				break;
		}
	}


	@Override
	public void onStartTrackingTouch (SeekBar seekBar) {}


	@Override
	public void onStopTrackingTouch (SeekBar seekBar) {}


	private void manageSpeechRate (int progress) {
		if(myTTS != null) {
			float value  = (progress / 10f) -10f;
			myTTS.setSpeechRate(value);
		}
	}


	private void managePitch (int progress) {
		if(myTTS != null) {
			float value = (progress / 10f) -1f;
			myTTS.setPitch(value);
		}
	}


}
