package com.vermeg.app.services;

    import org.json.simple.JSONObject;
    import org.json.simple.parser.JSONParser;

    import java.io.BufferedReader;
    import java.io.IOException;
    import java.io.InputStreamReader;
    import java.net.HttpURLConnection;
    import java.net.MalformedURLException;
    import java.net.URL;
    import java.text.DecimalFormat;
    import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TestService {




    public static Integer getAge(String birthd)
    {
        Calendar now = Calendar.getInstance();
        Date birthdate=null;

        if(birthd==null)
        {
            return 0;
        }

        try {


           birthdate = new SimpleDateFormat("yyyy-MM-dd").parse(birthd);
            Calendar birthDay = Calendar.getInstance();
            birthDay.setTimeInMillis(birthdate.getTime());
            //Get difference between years
            int years = now.get(Calendar.YEAR) - birthDay.get(Calendar.YEAR);
            return years;
           }

        catch (ParseException e) {

         System.out.println(e);
         return 0;

        }




    }





    public  static String formatNumber(Double nb, String pattern) {

        if(nb==null)
        {
            nb=Double.valueOf(0);
        }
        DecimalFormat myFormatter = new DecimalFormat(pattern);

        return myFormatter.format(nb);
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


public static String FormatDate(String Date, String pattern)
{
    SimpleDateFormat sm = new SimpleDateFormat(pattern);
    SimpleDateFormat dp= new SimpleDateFormat("yyyy-MM-dd");
    Date dt = null;
    if(Date==null)
    {
        return "";
    }

    try {

        dt = dp.parse(Date);
        String strDate = sm.format(dt);
        return strDate;


    } catch (ParseException e) {
        e.printStackTrace();
        return "";
    }
}



}
