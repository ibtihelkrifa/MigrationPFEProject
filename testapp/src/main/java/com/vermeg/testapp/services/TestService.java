package com.vermeg.testapp.services;

    import org.json.simple.JSONObject;
    import org.json.simple.parser.JSONParser;

    import javax.xml.bind.annotation.XmlType;
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

        if(birthd==null)
        {
            return String.valueOf(0);
        }

        try {


           birthdate = new SimpleDateFormat("yyyy-MM-dd").parse(birthd);
            Calendar birthDay = Calendar.getInstance();
            birthDay.setTimeInMillis(birthdate.getTime());
            //Get difference between years
            int years = now.get(Calendar.YEAR) - birthDay.get(Calendar.YEAR);
            return String.valueOf(years);
           }

        catch (ParseException e) {

         System.out.println(e);
         return String.valueOf(0);

        }




    }





    public  static String formatNumber(Double nb) {

        if(nb==null)
        {
            nb=Double.valueOf(0);
        }
        NumberFormat numberFormatter = NumberFormat.getNumberInstance(Locale.FRENCH);
         numberFormatter.setMaximumFractionDigits(2);
        return numberFormatter.format(nb);
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


public static String FormatDate(String Date)
{
    SimpleDateFormat sm = new SimpleDateFormat("MM/dd/yyyy");
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
