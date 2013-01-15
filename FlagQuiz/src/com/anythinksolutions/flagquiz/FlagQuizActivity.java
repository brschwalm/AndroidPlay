package com.anythinksolutions.flagquiz;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class FlagQuizActivity extends Activity {

	private static final String TAG = "FlagQuizActivity";
	private final int CHOICES_MENU_ID = Menu.FIRST;
	private final int REGIONS_MENU_ID = Menu.FIRST + 1;
	
	private List<String> fileNameList;
	private List<String> quizCountriesList;
	private Map<String, Boolean> regionsMap;
	private String correctAnswer;
	private int totalGuesses;
	private int correctAnswers;
	private int guessRows;
	private Random random;
	private Handler handler;			//used to delay loading the next flag
	private Animation shakeAnimation;
	
	private TextView answerTextView;
	private TextView questionNumberTextView;
	private ImageView flagImageView;
	private TableLayout buttonTableLayout;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_flag_quiz);
		
		fileNameList = new ArrayList<String>();
		quizCountriesList = new ArrayList<String>();
		regionsMap = new HashMap<String, Boolean>();
		guessRows = 1;
		random = new Random();
		handler = new Handler();
		
		shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.incorrect_shake);
		shakeAnimation.setRepeatCount(3);
		
		String[] regionNames = getResources().getStringArray(R.array.regions_list);
		
		for(String region : regionNames)
			regionsMap.put(region, true);
		
		answerTextView = (TextView)findViewById(R.id.answerTextView);
		questionNumberTextView = (TextView)findViewById(R.id.questionNumberTextView);
		flagImageView = (ImageView)findViewById(R.id.flagImageView);
		buttonTableLayout = (TableLayout)findViewById(R.id.buttonTableLayout);
		
		setQuestionNumber(0);		
		resetQuiz();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		menu.add(Menu.NONE, CHOICES_MENU_ID, Menu.NONE, R.string.choices);
		menu.add(Menu.NONE, REGIONS_MENU_ID, Menu.NONE, R.string.regions);
		
		return true;		//display the menu
	}
	
	private void setQuestionNumber(int number){
		questionNumberTextView.setText(
				getResources().getString(R.string.question) + " " + number + " " + 
				getResources().getString(R.string.of) + " 10");
	}
	
	private void resetQuiz(){
		//Get flags using the Asset Manager
		AssetManager assets = getAssets();
		fileNameList.clear();
		
		try{
			Set<String> regions = regionsMap.keySet();
			for(String region : regions){
				if(regionsMap.get(region)){
					String[] paths = assets.list(region);
					for(String path : paths){
						fileNameList.add(path.replace(".png", ""));
					}
				}
			}
		}
		catch(IOException e){
			Log.e(TAG, "Error loading image file names", e);
		}
		
		correctAnswers = 0; totalGuesses = 0; quizCountriesList.clear();
		
		//Add 10 random file names to the quiz countries list
		int flagCounter = 1;
		int numberOfFlags = fileNameList.size();		
		while (flagCounter <= 10){
			int index = random.nextInt(numberOfFlags);
			String flag = fileNameList.get(index);
			if(!quizCountriesList.contains(flag)){
				quizCountriesList.add(flag);
				flagCounter++;
			}
		}
		
		loadNextFlag();
	}

	private void loadNextFlag(){
		
		String nextImageName = quizCountriesList.remove(0);
		correctAnswer = nextImageName;
		
		answerTextView.setText("");
		setQuestionNumber(correctAnswers + 1);
		
		//Load the flag for this country
		String region = nextImageName.substring(0, nextImageName.indexOf('-'));
		AssetManager assets = getAssets();
		InputStream stream;		
		try{
			stream = assets.open(region + "/" + nextImageName + ".png");
			Drawable flag = Drawable.createFromStream(stream, nextImageName);
			flagImageView.setImageDrawable(flag);
		}
		catch(IOException e){
			Log.e(TAG, "Error loading " + nextImageName, e);
		}
		
		//Clear prior answer buttons
		for(int row = 0; row < buttonTableLayout.getChildCount(); ++row)
			getTableRow(row).removeAllViews();
		
		Collections.shuffle(fileNameList);
		int correct = fileNameList.indexOf(correctAnswer);
		fileNameList.add(fileNameList.remove(correct));
		
		LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		for(int row = 0; row < guessRows; row++){
			TableRow currentRow = getTableRow(row);
			for(int col = 0; col < 3; col++){
				Button guess = (Button)inflater.inflate(R.layout.guess_button, null);
				String fileName = fileNameList.get((row * 3) + col);
				guess.setText(getCountryName(fileName));
				
				guess.setOnClickListener(guessListener);
				currentRow.addView(guess);
			}
		}

		//Randomly choose a button to hold the correct answer
		Button correctButton = (Button)getTableRow(random.nextInt(guessRows)).getChildAt(random.nextInt(3));
		correctButton.setText(getCountryName(correctAnswer));		
	}
	
	private TableRow getTableRow(int row){
		return (TableRow)buttonTableLayout.getChildAt(row);
	}
	
	private String getCountryName(String name){
		return name.substring(name.indexOf('-') + 1).replace('_', ' ');
	}
	
	private void submitGuess(Button guessButton){
		String guess = guessButton.getText().toString();
		String answer = getCountryName(correctAnswer);
		++totalGuesses;
		
		if(guess.equals(answer)){
			onCorrectAnswer();
		}
		else{
			onIncorrectAnswer(guessButton);
		}
	}
	
	private OnClickListener guessListener = new OnClickListener() {		
		@Override
		public void onClick(View v) {
			submitGuess((Button)v);			
		}
	};
	
	private void onCorrectAnswer(){
		++correctAnswers;
		
		answerTextView.setText(R.string.correct);
		answerTextView.setTextColor(getResources().getColor(R.color.correct_answer));
		
		disableButtons();
		
		if(correctAnswers == 10){
			onQuizFinished();
		}
		else{
			handler.postDelayed(
					new Runnable(){
						@Override
						public void run(){
							loadNextFlag();
						}
					}, 1000);
		}
		
	}
	
	private void onIncorrectAnswer(Button guessButton){
		flagImageView.startAnimation(shakeAnimation);
		answerTextView.setText(R.string.incorrect_answer);
		answerTextView.setTextColor(getResources().getColor(R.color.incorrect_answer));
		guessButton.setEnabled(false);
	}
	
	private void onQuizFinished(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.reset_quiz);
		builder.setMessage(String.format("%d %s, %.02f%% %s", 
				totalGuesses, 
				getResources().getString(R.string.guesses),
				(1000/(double)totalGuesses), 
				getResources().getString(R.string.correct)));
		builder.setCancelable(false);
		
		//Reset Quiz Button
		builder.setPositiveButton(R.string.reset_quiz,
				new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						resetQuiz();						
					}
				});
		builder.show();
	}
	
	private void disableButtons(){
		for(int row = 0; row < buttonTableLayout.getChildCount(); ++row){
			TableRow currentRow = getTableRow(row);
			for(int i = 0; i < currentRow.getChildCount(); ++i)
				currentRow.getChildAt(i).setEnabled(false);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case CHOICES_MENU_ID:
				final String[] possibleChoices = getResources().getStringArray(R.array.guesses_list);
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.choices);
				builder.setItems(R.array.guesses_list,
						new DialogInterface.OnClickListener() {							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								guessRows = Integer.parseInt(possibleChoices[which].toString()) /3;
								resetQuiz();
							}
						});
				builder.show();
				return true;
				
			case REGIONS_MENU_ID:
				int size = regionsMap.size();
				
				//Setup the currently selected regions
				final String[] regionNames = regionsMap.keySet().toArray(new String[size]);
				boolean[] enabledRegions = new boolean[size];
				for(int i = 0; i < size; i++)
					enabledRegions[i] = regionsMap.get(regionNames[i]);
				
				//Build the dialog for selecting regions
				AlertDialog.Builder regionBuilder = new AlertDialog.Builder(this);
				regionBuilder.setTitle(R.string.regions);
				String[] displayNames = new String[regionNames.length];
				for(int i = 0; i < regionNames.length; i++)
					displayNames[i] = regionNames[i].replace('_', ' ');
				regionBuilder.setMultiChoiceItems(displayNames, enabledRegions,
						new DialogInterface.OnMultiChoiceClickListener() {							
							@Override
							public void onClick(DialogInterface dialog, int which, boolean isChecked) {
								regionsMap.put(regionNames[which], isChecked);
							}
						});
				regionBuilder.setPositiveButton(R.string.reset_quiz, 
						new DialogInterface.OnClickListener() {							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								resetQuiz();
							}
						});
				regionBuilder.show();
				return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
}
