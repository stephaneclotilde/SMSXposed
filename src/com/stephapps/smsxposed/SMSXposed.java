package com.stephapps.smsxposed;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

import java.io.ByteArrayInputStream;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.res.XModuleResources;
import android.content.res.XResources;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.Settings;
import android.telephony.SmsMessage;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

import com.stephapps.smsxposed.misc.Constants;
import com.stephapps.smsxposed.misc.ResourceTools;
import com.stephapps.smsxposed.misc.SMSTools;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class SMSXposed implements IXposedHookZygoteInit, IXposedHookLoadPackage, IXposedHookInitPackageResources
{
	private EditText mEditText ;
	private String[] mSources , mDestinations, mDelayedSources, mDelayedDestinations;
	private boolean mTextWillBeChanged=false ;
	private String mAfterMsg;
	private Object mComposeMsgActivityObject;
	private TextWatcher mOriginalTextWatcher;
	private Context mContext;
	private Drawable mSMSSmallIcon;
	private static final String PACKAGE_NAME = SMSXposed.class.getPackage().getName();
	private WakeLock mSMSWakeLock;
	private boolean mIsReceiverAlreadyRegistered=false;
	private static String MODULE_PATH = null;
	private int mSmsIconColor;
	private long mSendOrderedBroadcastTimeMillis=-1;
		
	
	@Override
	public void initZygote(StartupParam startupParam) throws Throwable 
	{
		MODULE_PATH = startupParam.modulePath;
        Log.i("SMSXposed","initZygote");
   
        XSharedPreferences prefs = new XSharedPreferences(PACKAGE_NAME);
        final boolean addButtons = prefs.getBoolean("add_buttons", false);
    	final boolean wakeOnNewSMS = prefs.getBoolean("wake_on_new_sms", false);
    	final boolean privacyMode = prefs.getBoolean("privacy_mode", false);
    	final boolean showSender = prefs.getBoolean("privacy_show_sender", false);
    	final boolean addShowBtn = prefs.getBoolean("privacy_add_show_btn", false);
    	final boolean noFullScreenWithKeyboard = prefs.getBoolean("no_fullscreen_with_keyboard", false);
    	final String[] notificationActions = loadArray(Constants.NOTIFICATION_ACTIONS, prefs);

    	final Class<?> notificationManagerClass = XposedHelpers.findClass("android.app.NotificationManager", null);
    	findAndHookMethod(notificationManagerClass, "notify", String.class, int.class, Notification.class, new XC_MethodHook() 
    	{
			@Override
    		protected void beforeHookedMethod(MethodHookParam param) throws Throwable 
    		{	
				Context context = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
   			
				if (context.getPackageName().equals("com.android.mms")||context.getPackageName().equals("com.google.android.talk"))
		    	{
//					 // Phone needs to always have this permission to write to the sms database
//					PackageManager packageManager = context.getPackageManager();
//					AppOpsManager appOps = (AppOpsManager)context.getSystemService(Context.APP_OPS_SERVICE);
//	                try {
//	                    PackageInfo info = packageManager.getPackageInfo(PHONE_PACKAGE_NAME, 0);
//	                    appOps.setMode(AppOpsManager.OP_WRITE_SMS, info.applicationInfo.uid,
//	                            PHONE_PACKAGE_NAME, AppOpsManager.MODE_ALLOWED);
//	                } catch (NameNotFoundException e) {
//	                    // No phone app on this device (unexpected, even for non-phone devices)
//	                    Log.e(LOG_TAG, "Unable to find phone package");
//	                }
					
					
					XSharedPreferences prefs = new XSharedPreferences("com.android.phone","smsXposedPreferences");
					final String smsMsg = prefs.getString("sms_msg", null);
			    	final String smsSender = prefs.getString("sms_sender", null);
			    	Log.i("SMSXposed","sms sender"+smsSender);
//			    	
//
//					Cursor c = context.getContentResolver().query(
//						    Uri.parse("content://sms/inbox"),
//						    null,
//						    "read = 0",//"read = 0 and address='"+smsSender+"' and body='"+smsMsg+"'"
//						    null,
//						    null
//						);
//						
					boolean specificMsgPresence = false;
//					boolean unreadSmsPresent = c.moveToFirst();
//					if (unreadSmsPresent)
//					{
//						long date = Long.valueOf(c.getString(c.getColumnIndex("date")));
//						if(Math.abs(date-System.currentTimeMillis())<500) 
//								 specificMsgPresence=true;
//						
//					}
//					Log.i("SMSXposed","sms presence:"+specificMsgPresence);	
			    	
			    	String smsReceived = ResourceTools.readFile("SMSXposed/SMSReceived");
			    	if (smsReceived.contains("1"))
			    	{
			    		specificMsgPresence= true;
			    		ResourceTools.generateNoteOnSD("SMSReceived","0");
			    	}
			    	
			    	Log.i("SMSXposed","msgPresence:"+specificMsgPresence);
					if (specificMsgPresence==true)
					{
						if (privacyMode) 
			    			param.args[2] = setPrivacyOnNotification(context, smsSender, smsMsg, (Notification)param.args[2], notificationActions[3], addShowBtn, addButtons, showSender, (Integer)param.args[1] );
			    		else
			    		{		    		
			    			if (addButtons) param.args[2] = addButtonsToSmsNotifications(context, smsSender, smsMsg, param.args[2], notificationActions, (Integer)param.args[1]);	
			    		}
					}
		    	}
				
				if (param.args[2]==null) param.setResult(0);
     		}
    	});
   	
		final Class<?> contextClass = XposedHelpers.findClass("android.content.ContextWrapper", null);
        XposedBridge.hookAllMethods(contextClass, "sendOrderedBroadcast", new XC_MethodHook() 
        {
        	protected void beforeHookedMethod(MethodHookParam param) throws Throwable 
        	{
        		boolean notificationHasBeenAlreadyIntercepted = false;
        		long currentTime = System.currentTimeMillis();
        		if (currentTime>(mSendOrderedBroadcastTimeMillis+250))
        		{
        			mSendOrderedBroadcastTimeMillis= currentTime;
        			notificationHasBeenAlreadyIntercepted = true;
        		}
        		
        		if (notificationHasBeenAlreadyIntercepted)
            	{
             		String permission = (String)param.args[1];
            		if ((permission!=null)&&permission.equals("android.permission.RECEIVE_SMS"))
            		{
    	        		Context context = (Context)param.thisObject;
    	        		
    	        		Log.i("SMSXposed","sendOrderedBroadcast hooked "+permission+" ,"+context.getPackageName());
    					
    					interceptAndSaveSMSInformations(context,(Intent)param.args[0]);
    					
    					if (wakeOnNewSMS)	wakeDevice(context);
            		}
            	}
			}
        });
        
//        if (noFullScreenWithKeyboard)
//        {
//	        final Class<?> contextClassTextView = XposedHelpers.findClass("android.widget.TextView", null);
//	        findAndHookMethod(contextClassTextView, "setImeOptions", Integer.class, new XC_MethodHook() 
//	        {
//	        	protected void beforeHookedMethod(MethodHookParam param) throws Throwable 
//	        	{
//	        		Log.i("SMSXposed","setImeOptions hooked");
//	        		param.args[0] = ((Integer)param.args[0])|EditorInfo.IME_FLAG_NO_FULLSCREEN;
//	        	}
//	        });
//        }
   
	}

	 @Override
	 public void handleInitPackageResources(InitPackageResourcesParam resparam) throws Throwable 
	 {
		if (!(resparam.packageName.equals("com.android.mms")))	return;

		XSharedPreferences prefs = new XSharedPreferences(PACKAGE_NAME);
    	
		if (prefs.getBoolean("sms_custom_icon_color_toggle", false))
		{
		  	mSmsIconColor = prefs.getInt("sms_icon_color", Color.WHITE);
	 		Resources tweakboxRes = XModuleResources.createInstance(MODULE_PATH, null);
			byte[] b = XposedHelpers.assetAsByteArray(tweakboxRes, "stat_notify_sms.png");
			ByteArrayInputStream is = new ByteArrayInputStream(b);
			mSMSSmallIcon = ResourceTools.resizDrawable(Drawable.createFromStream(is, "stat_notify_sms.png"),prefs.getFloat("density",2.0f));
		
			//XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, resparam.res);
			//resparam.res.setReplacement("com.android.mms", "drawable", "stat_notify_sms", modRes.fwd(R.drawable.stat_notify_sms));
			resparam.res.setReplacement("com.android.mms", "drawable", "stat_notify_sms", new XResources.DrawableLoader() {
				@Override
				public Drawable newDrawable(XResources res, int id) throws Throwable {
					Drawable d = mSMSSmallIcon.getConstantState().newDrawable();
					d.setColorFilter(mSmsIconColor,Mode.MULTIPLY );
					return d;
				}
			});
		}
	}
	
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable 
    { 
    	XSharedPreferences prefs = new XSharedPreferences(PACKAGE_NAME);
    	
    	if (lpparam.packageName.equals("com.android.mms"))
	    	hookSMS(lpparam, prefs);
    	if (lpparam.packageName.equals("com.google.android.talk"))
    		hookHangouts(lpparam, prefs);
    	return; 	
    }
    
    private void hookSMS(final LoadPackageParam lpparam, XSharedPreferences prefs)
    {
    	final boolean replaceSmileyWithEnterButton = prefs.getBoolean("replace_smiley_with_enter_button", false);
    	final boolean noFullScreenWithKeyboard = prefs.getBoolean("no_fullscreen_with_keyboard", false);
    	final boolean replacePuncutationInVoiceDictation = prefs.getBoolean("replace_punctuation_in_voice_dictation", false);
    	final boolean unlimitedTextbox = prefs.getBoolean("unlimited_textbox", false);
    	final boolean addButtons =  prefs.getBoolean("add_buttons", false);
    	
    	mSources 				= loadArray(Constants.SOURCES, prefs);
    	mDestinations 			= loadArray(Constants.DESTINATIONS, prefs);
    	mDelayedSources 		= loadArray(Constants.DELAYED_SOURCES, prefs);
    	mDelayedDestinations 	= loadArray(Constants.DELAYED_DESTINATIONS, prefs);
		
    	findAndHookMethod("com.android.mms.ui.ComposeMessageActivity", lpparam.classLoader, "initResourceRefs", new XC_MethodHook() {
    		@Override
    		protected void afterHookedMethod(MethodHookParam param) throws Throwable 
    		{
    			mContext = ((Activity)param.thisObject).getApplicationContext();

    			mEditText = (EditText) XposedHelpers.getObjectField(param.thisObject, "mTextEditor");
    			
    			if (replaceSmileyWithEnterButton) replaceSmileyKeyWithEnterKey();
    			
    			if (noFullScreenWithKeyboard) removeFullScreenInLandScapeMode();
    			
    			if (unlimitedTextbox) mEditText.setMaxLines(Integer.MAX_VALUE);
    			
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
    	
//    	findAndHookMethod("com.android.mms.ui.SmsReceiverService", lpparam.classLoader, "onStartCommand", Intent.class, int.class , int.class, new XC_MethodHook(){
//    		@Override
//    		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//    			Log.i("SMSXposed","SMSReceiverService hooked");
//    			Intent intent = (Intent)param.args[0];
//    			Bundle extras = intent.getExtras();
//    			if ((extras!=null)&&(extras.getString("sms_sender")!=null))
//    			{
//    				Log.i("SMSXposed","mark as read intent");
//    			}
//     		}
//    	});
    	
    	if (addButtons)
    	{
		    	findAndHookMethod("com.android.mms.transaction.MessageStatusReceiver", lpparam.classLoader, "onReceive", Context.class, Intent.class, new XC_MethodHook(){
		    		@Override
		    		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
		    			Log.i("SMSXposed","MessageStatusReceiver onReceive hooked");
		    			markAsReadAndCloseApp( param);
		     		}
		    	});
    	}	
    }
    
    private void hookHangouts(final LoadPackageParam lpparam , XSharedPreferences prefs)
    {
    	if ( prefs.getBoolean("add_buttons", false)==true)
    	{
	    	findAndHookMethod("com.google.android.apps.babel.realtimechat.NotificationReceiver", lpparam.classLoader,  "onReceive", Context.class, Intent.class,  new XC_MethodHook(){
	    		@Override
	    		protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
	    			Log.i("SMSXposed","NotificationReceiver onResume hooked");
	    			markAsReadAndCloseApp( param);
	    		}
	    	});		
    	}
    }
    
    private void markAsReadAndCloseApp(MethodHookParam param)
    {
    	Context context= (Context)param.args[0];
    	Intent intent = (Intent)param.args[1];
    	Bundle extras = intent.getExtras();
    	if ((extras!=null)&&(extras.getString("sms_sender")!=null))
		{
			SMSTools.markMessageRead(context, extras.getString("sms_sender"), extras.getString("sms_msg"));
			Toast.makeText(context, "marked as read", Toast.LENGTH_SHORT).show();
			intent.removeExtra("sms_sender");
			param.setResult(0);
			//((Activity)param.thisObject).finish();   
		}
    }
    
 	private Object addButtonsToSmsNotifications(Context context, String smsSender, String smsMsg, Object notification, String[] notificationsActions, Integer notificationId)
    {
 		Intent callIntent = new Intent(Intent.ACTION_CALL);
		callIntent.setData(Uri.parse("tel:" + smsSender));
		PendingIntent pendingCallIntent = PendingIntent.getActivity(context, 0, callIntent, 0);
		
		Intent respondIntent = new Intent(); 
		respondIntent.putExtra("sms_sender", smsSender);
		respondIntent.putExtra("sms_msg", smsMsg);
		respondIntent.putExtra("package_name", context.getPackageName());	
		respondIntent.setAction("com.stephapps.smsxposed.quickresponse_receiver");
		PendingIntent pendingRespondIntent = PendingIntent.getBroadcast(context, 0, respondIntent, PendingIntent.FLAG_UPDATE_CURRENT);		    	     

		Intent markAsReadIntent = new Intent();
		markAsReadIntent.putExtra("sms_sender", smsSender);
		markAsReadIntent.putExtra("sms_msg", smsMsg);
		markAsReadIntent.putExtra("notification_id", notificationId);
		markAsReadIntent.putExtra("package_name", context.getPackageName());	
		markAsReadIntent.setAction("com.stephapps.smsxposed.markasread_receiver");
	    PendingIntent pendingIntentMarkAsRead = PendingIntent.getBroadcast(context, 0, markAsReadIntent, PendingIntent.FLAG_UPDATE_CURRENT);

	    Notification paramNotif = (Notification)notification;
		
		CharSequence[] notificationsText = SMSTools.getTextFromNotificationView(paramNotif.contentView, context);
		Notification newNotif = new Notification.Builder(context)
		.setWhen(paramNotif.when)
        .setTicker(paramNotif.tickerText)
        .setLargeIcon(paramNotif.largeIcon)
        .setSmallIcon(paramNotif.icon)
        .setContentTitle(notificationsText[0])
        .setContentIntent(paramNotif.contentIntent)
        .setPriority(paramNotif.priority)
        .setSound(paramNotif.sound)
        .setDefaults(paramNotif.defaults)
        .setDeleteIntent(paramNotif.deleteIntent)
        .setContentText(notificationsText[2])
        .addAction(android.R.drawable.ic_menu_call, notificationsActions[0], pendingCallIntent)
        .addAction(android.R.drawable.ic_menu_send, notificationsActions[1], pendingRespondIntent)
        .addAction(android.R.drawable.checkbox_on_background, notificationsActions[2], pendingIntentMarkAsRead)
        .build();
		
		return newNotif;
    }
 	
 	private Notification setPrivacyOnNotification(Context context, String smsSender, String smsMsg, Notification notification, String showActionString, boolean addShowBtn, boolean showNotificationAction, boolean showSender, Integer notificationId)
 	{
 		Notification newNotif=null;
 		
 		CharSequence[] notificationsText = SMSTools.getTextFromNotificationView(notification.contentView, context);
 		
 		CharSequence sender;
 		if (!showSender) 	sender = notificationsText[0];
 		else 				sender = "    ";
 		
 		if (addShowBtn)
 		{
 			Intent intent = new Intent();
 	 		intent.setAction("com.stephapps.smsxposed.shownotification_receiver");
 	 		intent.putExtra("notification_id", notificationId);
 	 		intent.putExtra("notification", (Notification)notification);
 	 		intent.putExtra("show_notification_action", showNotificationAction);
 	 		intent.putExtra("show_sender", showSender);
 	 		intent.putExtra("add_show_btn", true);
 	 		intent.putExtra("sms_sender", smsSender);
 	 		intent.putExtra("sms_msg", smsMsg);
 	 		intent.putExtra("package_name", context.getPackageName());
 	 		intent.putExtra("ticker", notification.tickerText);
 	 		intent.putExtra("content_title", notificationsText[0]);
 	 		intent.putExtra("content_text", notificationsText[2]);
 	 		context.sendBroadcast(intent);
 	 		
 	 		
 		}
 		else
 		{
 	 		Notification paramNotif = (Notification)notification;
 	 		//TODO: in theory we could just modify the original notification since there's no additionnal action button (addable only via Builder)
 			//but this would require to mess with ContentView for ContentText, will leave that for later
 			newNotif = new Notification.Builder(context)
			.setWhen(paramNotif.when)
	        .setTicker("    ")
	        .setLargeIcon(paramNotif.largeIcon)
	        .setSmallIcon(paramNotif.icon)
	        .setContentTitle(sender)
	        .setContentIntent(paramNotif.contentIntent)
	        .setPriority(paramNotif.priority)
	        .setSound(paramNotif.sound)
	        .setDefaults(paramNotif.defaults)
	        .setDeleteIntent(paramNotif.deleteIntent)
	        .setContentText("    ")
	        .build();
 		}
		
		return newNotif;
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
    
    private static String[] loadArray(String arrayName, XSharedPreferences prefs)
    {
    	int size = prefs.getInt(arrayName + "_size", 0);  
        String array[] = new String[size];  
        for(int i=0;i<size;i++)  
            array[i] = prefs.getString(arrayName + "_" + i, null);  
        return array; 
    }

   
    
    private void interceptAndSaveSMSInformations(Context context,Intent intent)
	{
		SmsMessage[] msgs = SMSTools.getMessagesFromIntent(intent);
        SmsMessage sms = msgs[0];
        Log.i("SMSXposed", "handleSmsReceived" + (sms.isReplace() ? "(replace)" : "") +
                ", address: " + sms.getOriginatingAddress() +
                ", body: " + sms.getMessageBody()
                + ""+context.getPackageName());

       SharedPreferences prefs = context.getSharedPreferences("smsXposedPreferences",Context.MODE_WORLD_READABLE);
       Editor edit = prefs.edit();
       edit.putString("sms_sender", sms.getOriginatingAddress());
       edit.putString("sms_msg", sms.getMessageBody());
       edit.commit();
       
       ResourceTools.generateNoteOnSD("SMSReceived","1");
	}
	
	private void wakeDevice(Context context)
	{
		Log.i("SMSXposed","wakeDevice");
		PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
		if (!pm.isScreenOn())
		{
			mSMSWakeLock = pm.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE), "TAG");
			mSMSWakeLock.acquire();
			mSMSWakeLock.release();
		}
	}
	
	
}