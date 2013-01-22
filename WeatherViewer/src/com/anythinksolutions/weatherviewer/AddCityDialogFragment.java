package com.anythinksolutions.weatherviewer;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class AddCityDialogFragment extends DialogFragment implements OnClickListener{
	
	//Interface for results from the AddCityDIalog
	public interface DialogFinishedListener{
		//Called when the AddCityDialog is dismissed
		void onDialogFinished(String zipCodeString, boolean preferred);
	}
	
	private EditText addCityEditText;
	private CheckBox addCityCheckBox;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setCancelable(true);			//Allow the user to exit with the back button
	}
	
	//Inflates the DialogFragment's layout
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.add_city_dialog, container, false);
		
		//Get the edit text
		addCityEditText = (EditText)rootView.findViewById(R.id.add_city_edit_text);
		addCityCheckBox = (CheckBox)rootView.findViewById(R.id.add_city_checkbox);
		
		if(savedInstanceState != null){
			addCityEditText.setText(savedInstanceState.getString(getResources().getString(R.string.add_city_dialog_bundle_key)));
		}
		
		getDialog().setTitle(R.string.add_city_dialog_title);
		
		Button okButton = (Button)rootView.findViewById(R.id.add_city_button);
		okButton.setOnClickListener(this);
		return rootView;
	}
	
	//Save the state of the dialog box
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putCharSequence(getResources().getString(R.string.add_city_dialog_bundle_key), addCityEditText.getText().toString());
		super.onSaveInstanceState(outState);
	}

	//Handle the Click event of the OK Button in the Dialog
	@Override
	public void onClick(View clickedView) {

		if(clickedView.getId() == R.id.add_city_button){
			DialogFinishedListener listener = (DialogFinishedListener)getActivity();
			listener.onDialogFinished(addCityEditText.getText().toString(), addCityCheckBox.isChecked());
		}
		
	}

}
