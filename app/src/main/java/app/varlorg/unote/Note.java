package app.varlorg.unote;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Note
{
    private int id;
    private String titre;
    private String noteContent;
    private String dateCreation;
    private String dateModification;
    private String password;
    private boolean selected = false;

    public Note(String t, String c)
    {
        SimpleDateFormat df   = new SimpleDateFormat("yyyy/MM/dd/HH:mm:ss");
        String           date = df.format(Calendar.getInstance().getTime());

        this.dateCreation     = date;
        this.dateModification = date;
        this.titre            = t;
        this.noteContent      = c;
        this.password         = null;
    }

    public Note()
    {
        this("", "");
    }

    public int getId()
    {
        return(this.id);
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getTitre()
    {
        return(this.titre);
    }

    public void setTitre(String titre)
    {
        this.titre = titre;
    }

    public String getNote()
    {
        return(this.noteContent);
    }

    public String getNoteHead(int nbChar)
    {
        int max = nbChar;
        int min = Math.min(max, this.noteContent.length());

        if (max < this.noteContent.length())
        {
            return(this.noteContent.substring(0, min) + "...");
        }
        else
        {
            return(this.noteContent.substring(0, min));
        }
    }

    public void setNote(String c)
    {
        this.noteContent = c;
    }

    public String getDateCreationFormated()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd/HH:mm");
        Date             d   = null;

        try {
            d = sdf.parse(this.dateCreation);
        } catch (ParseException e) {
            Log.e(BuildConfig.APPLICATION_ID, "exception getDateCreationFormated", e);
        }
        sdf.applyPattern("EEE, dd MMM yyyy, HH:mm");
        return(sdf.format(d));
    }

    public String getDateCreation()
    {
        return(this.dateCreation);
    }

    public void setDateCreation(String dc)
    {
        this.dateCreation = dc;
    }

    public String getDateModificationFormated()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd/HH:mm");
        Date             d   = null;

        try {
            d = sdf.parse(this.dateModification);
        } catch (ParseException e) {
            Log.e(BuildConfig.APPLICATION_ID, "exception getDateModificationFormated", e);
        }
        sdf.applyPattern("EEE, dd MMM yyyy, HH:mm");
        return(sdf.format(d));
    }

    public String getDateModification()
    {
        return(this.dateModification);
    }

    public void setDateModification(String dc)
    {
        this.dateModification = dc;
    }

    public String getPassword()
    {
        return(this.password);
    }

    public void setPassword(String pw)
    {
        // pw is null for removing password
        this.password = pw;
    }
    public boolean isSelected()
    {
        return(this.selected);
    }
    public void setSelected(boolean checked)
    {
        this.selected  = checked;
    }
}
