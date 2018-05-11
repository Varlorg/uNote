package app.varlorg.unote;

import org.junit.Test;
import java.util.concurrent.TimeUnit;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class NoteTest {
    @Test
    public void testEmptyNote() throws Exception {
        Note n = new Note();
        SimpleDateFormat df   = new SimpleDateFormat("yyyy/MM/dd/HH:mm:ss");
        String           date = df.format(Calendar.getInstance().getTime());
        assertEquals("", n.getTitre());
        assertEquals("", n.getNote());
        assertEquals(null, n.getPassword());
        assertEquals(date, n.getDateCreation());
        assertEquals(date, n.getDateModification());
    }
    @Test
    public void testNonEmptyNote() throws Exception {
        Note n = new Note("titre", "note");
        SimpleDateFormat df   = new SimpleDateFormat("yyyy/MM/dd/HH:mm:ss");
        String           date = df.format(Calendar.getInstance().getTime());
        assertEquals("titre", n.getTitre());
        assertEquals("note", n.getNote());
        assertEquals(null, n.getPassword());
        assertEquals(date, n.getDateCreation());
        assertEquals(date, n.getDateModification());
    }
    @Test
    public void testSetTitre() throws Exception {
        Note n = new Note();
        assertEquals("", n.getTitre());
        n.setTitre("Test");
        assertEquals("Test", n.getTitre());
    }

    @Test
    public void testSetNote() throws Exception {
        Note n = new Note();
        assertEquals("", n.getNote());
        n.setNote("Test");
        assertEquals("Test", n.getNote());
    }

    @Test
    public void testSetDateCreation() throws Exception {
        Note n = new Note();
        SimpleDateFormat df   = new SimpleDateFormat("yyyy/MM/dd/HH:mm:ss");
        // years = 1900 + param => 2000 is 100
        // month starts from 0  => march is 02
        String           date = df.format(new Date(100, 02, 01, 17,42,31));
        n.setDateCreation(date);
        assertEquals("2000/03/01/17:42:31", n.getDateCreation());
    }

    @Test
    public void testGetDateCreationFormated() throws Exception {
        Note n = new Note();
        SimpleDateFormat df   = new SimpleDateFormat("yyyy/MM/dd/HH:mm:ss");
        // years = 1900 + param => 2000 is 100
        // month starts from 0  => march is 02
        String           date = df.format(new Date(100, 02, 01, 17,42,31));
        n.setDateCreation(date);
        assertEquals("Wed, 01 Mar 2000, 17:42", n.getDateCreationFormated());
    }

    @Test
    public void testSetDateModification() throws Exception {
        Note n = new Note();
        SimpleDateFormat df   = new SimpleDateFormat("yyyy/MM/dd/HH:mm:ss");
        // years = 1900 + param => 2000 is 100
        // month starts from 0  => march is 02
        String           date = df.format(new Date(100, 02, 01, 17,42,31));
        n.setDateModification(date);
        assertEquals("2000/03/01/17:42:31", n.getDateModification());
    }

    @Test
    public void testGetDateModificationFormated() throws Exception {
        Note n = new Note();
        SimpleDateFormat df   = new SimpleDateFormat("yyyy/MM/dd/HH:mm:ss");
        // years = 1900 + param => 2000 is 100
        // month starts from 0  => march is 02
        String           date = df.format(new Date(100, 02, 01, 17,42,31));
        n.setDateModification(date);
        assertEquals("Wed, 01 Mar 2000, 17:42", n.getDateModificationFormated());
    }

    @Test
    public void testSetPassword() throws Exception {
        Note n = new Note();
        assertEquals(null, n.getPassword());
        n.setPassword("Test");
        assertEquals("Test", n.getPassword());
        n.setPassword(null);
        assertEquals(null, n.getPassword());
    }

    @Test
    public void testGetHeadNote() throws Exception {
        String c = "This is a note";
        Note n = new Note("test",c);
        assertEquals(c.subSequence(0,4)+ "...", n.getNoteHead(4));
        assertEquals(c, n.getNoteHead(c.length()+2));
    }

    @Test
    public void testId() throws Exception {
        Note n = new Note();
        n.setId(4);
        assertEquals(4, n.getId());
    }
}