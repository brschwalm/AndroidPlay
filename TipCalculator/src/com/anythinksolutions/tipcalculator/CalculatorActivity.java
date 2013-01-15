package com.anythinksolutions.tipcalculator;

import com.anythinksolutions.tipcalculator.R;

import android.os.Bundle;
import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class CalculatorActivity extends Activity {

	private static final String BILL_TOTAL = "BILL_TOTAL";
	private static final String CUSTOM_PERCENT = "CUSTOM_PERCENT";
	private static final String currencyFormat = "$%.02f";
	
	private double currentBillTotal;
	private int customTipPercent;
	private double[] tips = {0.1, 0.15, 0.2 };
	
	private EditText tip10; private EditText tip15; private EditText tip20;
	private EditText total10; private EditText total15; private EditText total20;
	private EditText billTotal;
	private EditText tipCustom; private EditText totalCustom;
	private TextView tipCustomDisplay;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);
        
        if(savedInstanceState == null){
        	currentBillTotal = 0.0;
        	customTipPercent = 18;
        }
        else{
        	currentBillTotal = savedInstanceState.getDouble(BILL_TOTAL);
        	customTipPercent = savedInstanceState.getInt(CUSTOM_PERCENT);
        }
        
        //Get references to the UI components
        tip10 = (EditText)findViewById(R.id.tip10);
        tip15 = (EditText)findViewById(R.id.tip15);
        tip20 = (EditText)findViewById(R.id.tip20);
        total10 = (EditText)findViewById(R.id.total10);
        total15 = (EditText)findViewById(R.id.total15);
        total20 = (EditText)findViewById(R.id.total20);
        tipCustom = (EditText)findViewById(R.id.tipCustom);
        totalCustom = (EditText)findViewById(R.id.totalCustom);
        tipCustomDisplay = (TextView)findViewById(R.id.tipCustomDisplay);
        
        billTotal = (EditText)findViewById(R.id.billTotal);
        billTotal.addTextChangedListener(billTotalWatcher);
        
        SeekBar customSeek = (SeekBar)findViewById(R.id.tipSeekBar);
        customSeek.setOnSeekBarChangeListener(customTipListener);        
    }

    @Override
    protected void onSaveInstanceState(Bundle outState){
    	super.onSaveInstanceState(outState);
    	
    	outState.putDouble(BILL_TOTAL, currentBillTotal);
    	outState.putInt(CUSTOM_PERCENT, customTipPercent);
    }
 
    private void updateStandard(){
    	double tip1 = currentBillTotal * tips[0];
    	double tip2 = currentBillTotal * tips[1];
    	double tip3 = currentBillTotal * tips[2];
    	
    	tip10.setText(String.format(currencyFormat, tip1));
    	tip15.setText(String.format(currencyFormat, tip2));
    	tip20.setText(String.format(currencyFormat, tip3));
    	
    	total10.setText(String.format(currencyFormat, currentBillTotal + tip1));
    	total15.setText(String.format(currencyFormat, currentBillTotal + tip2));
    	total20.setText(String.format(currencyFormat, currentBillTotal + tip3));
    }
    
    private void updateCustom(){    	
    	tipCustomDisplay.setText(customTipPercent + "%");
    	
    	double customTip = currentBillTotal * (customTipPercent*0.01);
    	tipCustom.setText(String.format(currencyFormat, customTip));
    	totalCustom.setText(String.format(currencyFormat, currentBillTotal + customTip));    	
    }

    private OnSeekBarChangeListener customTipListener = new OnSeekBarChangeListener(){
    	@Override
    	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
    		customTipPercent = seekBar.getProgress();
    		updateCustom();
    	}
    	
    	@Override
		public void onStartTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStopTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub
			
		}
    };

	private TextWatcher billTotalWatcher = new TextWatcher(){
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count){
			
			//Try to convert the current text to a double
			try{
				currentBillTotal = Double.parseDouble(s.toString());
			}
			catch(NumberFormatException e){
				currentBillTotal = 0.0;	//default to 0 if not a valid number
			}
			
			//Update everything
			updateStandard();
			updateCustom();
		}
		
		@Override
		public void afterTextChanged(Editable s){}
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after){}
	};
}