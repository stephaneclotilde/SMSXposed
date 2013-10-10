package com.stephapps.smsxposed;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

import java.lang.reflect.Field;
import java.util.List;

import com.stephapps.smsxposed.misc.Constants;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class SMSXposed implements IXposedHookLoadPackage
{
	private EditText mEditText ;
	private String[] mSources , mDestinations, mDelayedSources, mDelayedDestinations;
	private boolean mTextWillBeChanged=false ;
	private String mAfterMsg;
	private Object mComposeMsgActivityObject;
	private TextWatcher mOriginalTextWatcher;
	private Context mContext;
	private String mPoint;
	
	private static final String PACKAGE_NAME = SMSXposed.class.getPackage().getName();

    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable 
    {  	
    	if (!(lpparam.packageName.equals("com.android.mms")))	return;
    	
    	
    	XSharedPreferences prefs = new XSharedPreferences(PACKAGE_NAME);
    	final boolean replaceSmileyWithEnterButton = prefs.getBoolean("replace_smiley_with_enter_button", false);
    	final boolean noFullScreenWithKeyboard = prefs.getBoolean("no_fullscreen_with_keyboard", false);
    	final boolean replacePuncutationInVoiceDictation = prefs.getBoolean("replace_punctuation_in_voice_dictation", false);
    	mSources 				= prefs.getStringSet(Constants.SOURCES, null).toArray(new String[0]);
    	mDestinations 			= prefs.getStringSet(Constants.DESTINATIONS, null).toArray(new String[0]);
    	mDelayedSources 		= prefs.getStringSet(Constants.DELAYED_SOURCES, null).toArray(new String[0]);
    	mDelayedDestinations 	= prefs.getStringSet(Constants.DELAYED_DESTINATIONS, null).toArray(new String[0]);
		
    	findAndHookMethod("com.android.mms.ui.ComposeMessageActivity", lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
    		@Override
    		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
    			Activity activity = (Activity)param.thisObject  ;  
    			activity.setTheme(android.R.style.Theme_Holo);

     		}
    		
    		@Override
    		protected void afterHookedMethod(MethodHookParam param) throws Throwable 
    		{
    			    		}
    	});

    	findAndHookMethod("com.android.mms.ui.ComposeMessageActivity", lpparam.classLoader, "initResourceRefs", new XC_MethodHook() {
    		@Override
    		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
    			
     		}
    		@Override
    		protected void afterHookedMethod(MethodHookParam param) throws Throwable 
    		{
    			mContext = ((Activity)param.thisObject).getApplicationContext();
    			
    			
    			mEditText = (EditText) XposedHelpers.getObjectField(param.thisObject, "mTextEditor");
    			
    			if (replaceSmileyWithEnterButton) replaceSmileyKeyWithEnterKey();
    			
    			if (noFullScreenWithKeyboard) removeFullScreenInLandScapeMode();
    			
    			mEditText.setMaxLines(Integer.MAX_VALUE);
    			
    			if (replacePuncutationInVoiceDictation)
    			{
	    			mOriginalTextWatcher = (TextWatcher) XposedHelpers.getObjectField(param.thisObject, "mTextEditorWatcher");
	    			switchTextChangedListener(mOriginalTextWatcher, mNewTextEditorWatcher);
	    			
	    			mComposeMsgActivityObject = param.thisObject;
    			}
    			
//    			IntentFilter filter = new IntentFilter(Intent.ACTION_INPUT_METHOD_CHANGED);
//    	    	BroadcastReceiver mReceiver = new BroadcastReceiver() {
//    				
//    				@Override
//    				public void onReceive(Context context, Intent intent) {
//    					String action = intent.getAction();
//    		            if (action.equals(Intent.ACTION_INPUT_METHOD_CHANGED)) {
//    		                Log.i("SMSXposed","input method changed");
//    		                getInputInfos();
//    		            }
//    				}
//    			};
//    	    	mContext.registerReceiver(mReceiver, filter);
    		}
    	});
    	
    	 Class<?> contactClass = XposedHelpers.findClass("com.android.mms.data.Contact", lpparam.classLoader);
    	findAndHookMethod("com.android.mms.transaction.MessagingNotification", lpparam.classLoader, "getNewMessageNotificationInfo", Context.class, boolean.class, String.class, String.class, String.class, long.class, long.class, Bitmap.class, contactClass, int.class, new XC_MethodHook() {
    		@Override
    		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
    			Toast.makeText(mContext, "no sms text", Toast.LENGTH_SHORT).show();
    			param.args[2] = "    ";
    			param.args[3] = "    ";
    			return;
     		}
    		@Override
    		protected void afterHookedMethod(MethodHookParam param) throws Throwable 
    		{
    			
    		}
    	});
    	
    	if (replacePuncutationInVoiceDictation)
    	{
	    	//meant to avoid errors , might be useless
	    	findAndHookMethod("com.android.mms.ui.ComposeMessageActivity", lpparam.classLoader, "resetMessage", new XC_MethodHook() {
	    		@Override
	    		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
	    			switchTextChangedListener(mNewTextEditorWatcher, mOriginalTextWatcher);
	     		}
	    		@Override
	    		protected void afterHookedMethod(MethodHookParam param) throws Throwable 
	    		{
	    			switchTextChangedListener(mOriginalTextWatcher, mNewTextEditorWatcher);    			
	    		}
	    	});
    	}
    }
    
    private final TextWatcher mNewTextEditorWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) 
        {
        	if (isVoiceInputMethodEnabled()==false) return;
        	
        	String beforeMsg = mEditText.getText().toString();
        	Log.i("SMSXposed","beforeTextChanged"+beforeMsg);
        	mAfterMsg = new String(beforeMsg); //TextUtils.replace(beforeMsg, mSources, mDestinations);
			
        	detectAndReplacePunctuation();
			
			Log.i("SMSXposed","afterMsg"+mAfterMsg);
			if ((mAfterMsg.trim().equals(beforeMsg.trim()))==false) 
			{
				Log.i("SMSXposed","not equals");
				mTextWillBeChanged=true;
			}
			else
			{
				mTextWillBeChanged=false;//strangely necessary or else will always be true even after 'afterTextChanged'
				delayedDetectAndReplacePunctuation();
			}
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) 
        {
            //if there's no change we call the original methods 
        	if (mTextWillBeChanged==false)
            {
	        	// This is a workaround for bug 1609057. Since onUserInteraction() is
	            // not called when the user touches the soft keyboard, we pretend it was
	            // called when textfields changes. This should be removed when the bug
	            // is fixed.
            	XposedHelpers.callMethod(mComposeMsgActivityObject, "onUserInteraction"); 

				Object workingMessage = XposedHelpers.getObjectField(mComposeMsgActivityObject, "mWorkingMessage");
				XposedHelpers.callMethod(workingMessage, "setText", s);  	
	           
	            XposedHelpers.callMethod(mComposeMsgActivityObject, "updateSendButtonState"); 

	            XposedHelpers.callMethod(mComposeMsgActivityObject, "updateCounter", s, start, before, count); 

	            XposedHelpers.callMethod(mComposeMsgActivityObject, "ensureCorrectButtonHeight"); 	           
            }
        }

        @Override
        public void afterTextChanged(Editable s) 
        {
        	if (mTextWillBeChanged)
        	{
        		Log.i("SMSXposed","afterTextChanged"+mAfterMsg);
            			
        		replaceInEditText();

				mTextWillBeChanged=false;
        	}
        }
    };
    
    private void detectAndReplacePunctuation()
    {
		int nbSources = mSources.length;
		for (int i=0;i<nbSources;i++)
		{
			mAfterMsg = mAfterMsg.replace(mSources[i], mDestinations[i]);
		}

    }
    
    //this method is used so some words are not replaced too fast, 
    //breaking other replacements (Ex : "point" "point d'interrogation" gives ". d'interrogation" or even "." the other word is not taken in account.
    private void delayedDetectAndReplacePunctuation()
    {
    	Handler handler= new Handler();
    	handler.postDelayed(new Runnable() {
			
			@Override
			public void run() 
			{
				int nbSources = mDelayedSources.length;
				if (nbSources>0)
				{
					String beforeMsg = mEditText.getText().toString();
		        	Log.i("SMSXposed","beforeTextChanged"+beforeMsg);
		        	mAfterMsg = new String(beforeMsg);
		        	
		        	for (int i=0;i<nbSources;i++)
		    		{
		        		mAfterMsg = mAfterMsg.replace(mDelayedSources[i], mDelayedDestinations[i]);
		    		}
					
					if ((mAfterMsg.trim().equals(beforeMsg.trim()))==false) 
					{
						replaceInEditText();
					}
				}
			}
		}, 1000);
    }
    
    private void replaceInEditText()
    {
    	mEditText.setSelection(0);//needed to avoid IndexOutOfBoundsException
		mEditText.setText(mAfterMsg);
		mEditText.setSelection(mEditText.getText().length());
    }
    
    private void switchTextChangedListener(TextWatcher oldTxtWatcher, TextWatcher newTxtWatcher)
    {
    	mEditText.removeTextChangedListener(oldTxtWatcher);
		mEditText.addTextChangedListener(newTxtWatcher);
    }
    
    private void replaceSmileyKeyWithEnterKey()
    {
    	mEditText.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES|InputType.TYPE_TEXT_FLAG_MULTI_LINE|InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE|InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
    }
    
    private void removeFullScreenInLandScapeMode()
    {
    	mEditText.setImeOptions(mEditText.getImeOptions()|(EditorInfo.IME_FLAG_NO_FULLSCREEN));
    }
    
    public boolean isVoiceInputMethodEnabled() 
    {
        String id = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);

        ComponentName defaultInputMethod = ComponentName.unflattenFromString(id);
//      ComponentName myInputMethod = new ComponentName(mContext, VoiceInputMethodService.class);

        return defaultInputMethod.getClassName().equals("com.google.android.voicesearch.ime.VoiceInputMethodService");
    }
    
//    private void getInputInfos()
//    {
//    	InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
//        List<InputMethodInfo> mInputMethodProperties = imm.getEnabledInputMethodList();
//
//        final int N = mInputMethodProperties.size();
//
//        for (int i = 0; i < N; i++) {
//
//            InputMethodInfo imi = mInputMethodProperties.get(i);
//
//            if (imi.getId().equals(Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD))) {
//            	if (imi.getComponent().toString().equals("ComponentInfo{com.google.android.googlequicksearchbox/com.google.android.voicesearch.ime.VoiceInputMethodService}"))
//            		mVoiceInputComponent = imi.getComponent();
//            	Log.i("SMSXposed",""+imi.getComponent().toString());
//                //imi contains the information about the keyboard you are using
//                break;
//            }
//        }
//    }
    
}