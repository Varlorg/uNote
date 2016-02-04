package app.varlorg.unote;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.util.Log;

public class Note
{
    private int id;
    private String titre;
    private String note;
    private String dateCreation;
    private String dateModification;
    private String tag;

    public Note()
    {
        //this.dateCreation = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());;
    }

    public Note(String t, String c)
    {
        this.titre = t;
        this.note = c;
        //SimpleDateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy, HH:mm");
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd/HH:mm:ss");
        String date = df.format(Calendar.getInstance().getTime());
        Log.v("date creation note",date);
        this.dateCreation = date; //java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());;
        this.dateModification = date;
    }

    public int getId()
    {
        return this.id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getTitre()
    {
        return this.titre;
    }

    public void setTitre(String titre)
    {
        this.titre = new String(titre);
    }

    public String getNote()
    {
        return this.note;
    }

    public String getNoteHead()
    {
        final int MAX = 30;
        int min = Math.min(MAX,this.note.length());
        if (MAX < this.note.length())
            return this.note.substring(0, min)+ "...";
        else
            return this.note.substring(0, min);
    }

    public void setNote(String c)
    {
        this.note = new String(c);
    }

    public String getDateCreationFormated()
    {
    	Log.v("dateCreation",this.dateCreation.toString());
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd/HH:mm");
    	Date d = null;
    	
		try {
			d = sdf.parse(new String(this.dateCreation.toString()));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	sdf.applyPattern("EEE, dd MMM yyyy, HH:mm");
		Log.v("DAte ",sdf.format(d));
		String date_formated = sdf.format(d);
		//sdf.applyPattern("yyyy/MM/dd/HH:mm");
        return date_formated;
    }
    
    public String getDateCreation()
    {
        return this.dateCreation;
    }
    
    public void setDateCreation(String dc)
    {
        this.dateCreation = new String(dc);
    }
    
    public String getDateModificationFormated()
    {
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd/HH:mm");
    	Date d = null;
    	
		try {
			d = sdf.parse(new String(this.dateModification.toString()));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	sdf.applyPattern("EEE, dd MMM yyyy, HH:mm");
		String date_formated = sdf.format(d);
		//sdf.applyPattern("yyyy/MM/dd/HH:mm");
        return date_formated;
    }

    public String getDateModification()
    {
        return this.dateModification;
    }
    
    public void setDateModification(String dc)
    {
        this.dateModification = new String(dc);
    }
    
    public String toString()
    {
    	//return (String) Html.fromHtml("<b>"+this.getTitre() + "</b> <br/>"+this.getNoteHead());
    	return "Titre : "+titre+"\nNote : "+ this.getNoteHead() ;
    	//return "Titre : "+titre+"\nNote : "+ note+"\nDate de création : " + dateCreation ;
        //return "ID : "+id+"\nTitre : "+titre+"\nNote : "+ note+"\nDate de création : " + dateCreation ;
    }
}
