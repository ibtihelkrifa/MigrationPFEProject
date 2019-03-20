package com.vermeg.testapp.services;

    import org.json.simple.JSONObject;
    import org.json.simple.parser.JSONParser;

    import java.io.BufferedReader;
    import java.io.DataOutput;
    import java.io.IOException;
    import java.io.InputStreamReader;
    import java.net.HttpURLConnection;
    import java.net.MalformedURLException;
    import java.net.URL;
    import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TestService {




    public static String getAge(String birthd)
    {
        Calendar now = Calendar.getInstance();
        Date birthdate=null;
        try {
         birthdate = new SimpleDateFormat("mm-dd-yy").parse(birthd);
    } catch (ParseException e) {
        e.printStackTrace();
    }



        Calendar birthDay = Calendar.getInstance();
        birthDay.setTimeInMillis(birthdate.getTime());
        //Get difference between years
        int years = now.get(Calendar.YEAR) - birthDay.get(Calendar.YEAR);
        return String.valueOf(years);
    }

    public  static String formatNumber(double nb) {

        NumberFormat numberFormatter = NumberFormat.getNumberInstance(Locale.FRENCH);
         numberFormatter.setMaximumFractionDigits(2);
        return String.valueOf(numberFormatter.format(nb));

    }

public static Double getcurrency(String url)
{
    try {
        URL urll = new URL(url);
          HttpURLConnection conn=(HttpURLConnection) urll.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Failed : HTTP Error code : "
                    + conn.getResponseCode());
        }
        InputStreamReader in = new InputStreamReader(conn.getInputStream());
        BufferedReader br = new BufferedReader(in);
        String output;
        String result="";
        while((output = br.readLine()) != null) {
             result+=output;
        }
        conn.disconnect();
        JSONParser parser= new JSONParser();
        JSONObject jsoncurrency= (JSONObject) parser.parse(result);
        return (Double) jsoncurrency.get("tauxr");
    } catch (MalformedURLException e) {
        e.printStackTrace();
    } catch (IOException e) {
        e.printStackTrace();
    } catch (org.json.simple.parser.ParseException e) {
        e.printStackTrace();
    }

    return Double.valueOf(0);

}



}
