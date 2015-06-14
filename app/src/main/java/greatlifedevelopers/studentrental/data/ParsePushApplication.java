package greatlifedevelopers.studentrental.data;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseAnalytics;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseUser;
import com.parse.PushService;
import com.parse.SaveCallback;

import java.text.ParseException;

import greatlifedevelopers.studentrental.activitys.MainActivity;

/**
 * Created by ecs_kenny on 4/11/15.
 */
public class ParsePushApplication  extends Application{

    @Override
    public void onCreate() {
        Parse.initialize(this, "gkh3At3iyvJXs8IR2nekTTKGKPtUPnnqHAHcLSMr", "JiTecjeL7KNDRxQ9pJfvrwUcjH4GyjVTKrw7Y8wS");


        PushService.subscribe(this, "", MainActivity.class);
        PushService.setDefaultPushCallback(this,MainActivity.class);


        /*
        ParseUser.enableAutomaticUser();
        ParseACL defaultACL = new ParseACL();

        // If you would like all objects to be private by default, remove this line.
        defaultACL.setPublicReadAccess(true);


        ParseACL.setDefaultACL(defaultACL, true);*/

    }
}
