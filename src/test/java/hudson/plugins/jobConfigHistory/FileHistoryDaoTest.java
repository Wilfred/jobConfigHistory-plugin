package hudson.plugins.jobConfigHistory;

import hudson.Util;
import hudson.XmlFile;
import hudson.model.AbstractItem;
import hudson.model.User;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.SortedMap;
import java.util.concurrent.atomic.AtomicReference;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.*;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * @author Mirko Friedenhagen
 */
public class FileHistoryDaoTest {

    @Rule
    public final TemporaryFolder tempFolder = new TemporaryFolder(new File("target"));
    
    @Rule
    public final UnpackResourceZip unpackResourceZip = UnpackResourceZip.INSTANCE;
    
    private final User mockedUser = mock(User.class);

    public FileHistoryDaoTest() {
    }
    
    
    /**
     * Test of createNewHistoryEntry method, of class FileHistoryDao.
     */
    @Test
    public void testCreateNewHistoryEntry() throws IOException {
        final XmlFile xmlFile = new XmlFile(tempFolder.newFile());
        FileHistoryDao sut = new FileHistoryDao(new File("config-history"), tempFolder.getRoot(), null) {
            @Override
            JobConfigHistory getPlugin() {
                final JobConfigHistory mockPlugin = mock(JobConfigHistory.class);
                try {
                    when(mockPlugin.getHistoryDir(xmlFile)).thenReturn(tempFolder.newFolder());
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                return mockPlugin;
            }
        };
        sut.createNewHistoryEntry(xmlFile, "foo");
        assertTrue(xmlFile.getFile().exists());
    }

    /**
     * Test of createNewHistoryEntry method, of class FileHistoryDao.
     */
    @Test
    public void testCreateNewHistoryEntryRTE() throws IOException {
        final XmlFile xmlFile = new XmlFile(tempFolder.newFile());
        FileHistoryDao sut = new FileHistoryDao(new File("config-history"), tempFolder.getRoot(), null) {
            @Override
            File getRootDir(XmlFile xmlFile, AtomicReference<Calendar> timestampHolder) {
                throw new RuntimeException("oops");
            }
        };
        sut.createNewHistoryEntry(xmlFile, "foo");
    }

    /**
     * Test of getIdFormatter method, of class FileHistoryDao.
     */
    @Test
    public void testGetIdFormatter() {
        SimpleDateFormat result = FileHistoryDao.getIdFormatter();
        final String formattedDate = result.format(new Date(0));
        // workaround for timezone issues, as cloudbees is in the far east :-) and returns 1969 :-).
        assertThat(formattedDate, startsWith("19"));
        assertThat(formattedDate, endsWith("00-00"));
    }

    /**
     * Test of copyConfigFile method, of class FileHistoryDao.
     */
    @Test
    public void testCopyConfigFile() throws Exception {
        File currentConfig = new File(FileHistoryDaoTest.class.getResource("file1.txt").getPath());
        File timestampedDir = tempFolder.newFolder();
        FileHistoryDao.copyConfigFile(currentConfig, timestampedDir);
        final File copy = new File(timestampedDir, currentConfig.getName());
        assertTrue(copy.exists());
    }

    /**
     * Test of createHistoryXmlFile method, of class FileHistoryDao.
     */
    @Test
    public void testCreateHistoryXmlFile() throws Exception {
        final String fullName = "Full Name";
        when(mockedUser.getFullName()).thenReturn(fullName);
        when(mockedUser.getId()).thenReturn("userId");
        FileHistoryDao sut = new FileHistoryDao(new File("config-history"), tempFolder.getRoot(), mockedUser);
        testCreateHistoryXmlFile(sut, fullName);
    }

    /**
     * Test of createHistoryXmlFile method, of class FileHistoryDao.
     */
    @Test
    public void testCreateHistoryXmlFileAnonym() throws Exception {
        final String fullName = "Anonym";
        FileHistoryDao sut = new FileHistoryDao(new File("config-history"), tempFolder.getRoot(), null);
        testCreateHistoryXmlFile(sut, fullName);
    }
    /**
     * Test of createNewHistoryDir method, of class FileHistoryDao.
     */
    @Test
    public void testCreateNewHistoryDir() throws IOException {
        final File itemHistoryDir = tempFolder.newFolder();
        final AtomicReference<Calendar> timestampHolder = new AtomicReference<Calendar>();
        final File result = FileHistoryDao.createNewHistoryDir(itemHistoryDir, timestampHolder);
        assertTrue(result.exists());
        assertTrue(result.isDirectory());
        // Should provoke clash
        final File result2 = FileHistoryDao.createNewHistoryDir(itemHistoryDir, timestampHolder);
        assertTrue(result2.exists());
        assertTrue(result2.isDirectory());
        assertNotEquals(result, result2);
    }

    private void testCreateHistoryXmlFile(FileHistoryDao sut, final String fullName) throws IOException {
        Calendar timestamp = new GregorianCalendar();
        File timestampedDir = tempFolder.newFolder();
        sut.createHistoryXmlFile(timestamp, timestampedDir, "foo");
        final File historyFile = new File(timestampedDir, JobConfigHistoryConsts.HISTORY_FILE);
        assertTrue(historyFile.exists());
        final String historyContent = Util.loadFile(historyFile, Charset.forName("utf-8"));
        assertThat(historyContent, startsWith("<?xml"));
        assertThat(historyContent, endsWith("HistoryDescr>"));
        assertThat(historyContent, containsString("<user>"+fullName));
        assertThat(historyContent, containsString("foo"));
    }

    /**
     * Test of getRootDir method, of class FileHistoryDao.
     */
    @Test
    @Ignore
    public void testGetRootDir() {
        System.out.println("getRootDir");
        XmlFile xmlFile = null;
        AtomicReference<Calendar> timestampHolder = null;
        FileHistoryDao instance = null;
        File expResult = null;
        File result = instance.getRootDir(xmlFile, timestampHolder);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getPlugin method, of class FileHistoryDao.
     */
    @Test
    @Ignore
    public void testGetPlugin() {
        System.out.println("getPlugin");
        FileHistoryDao instance = null;
        JobConfigHistory expResult = null;
        JobConfigHistory result = instance.getPlugin();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of createNewItem method, of class FileHistoryDao.
     */
    @Test
    @Ignore
    public void testCreateNewItem() {
        System.out.println("createNewItem");
        AbstractItem item = null;
        FileHistoryDao instance = null;
        instance.createNewItem(item);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of saveItem method, of class FileHistoryDao.
     */
    @Test
    @Ignore
    public void testSaveItem_AbstractItem() {
        System.out.println("saveItem");
        AbstractItem item = null;
        FileHistoryDao instance = null;
        instance.saveItem(item);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of saveItem method, of class FileHistoryDao.
     */
    @Test
    @Ignore
    public void testSaveItem_XmlFile() {
        System.out.println("saveItem");
        XmlFile file = null;
        FileHistoryDao instance = null;
        instance.saveItem(file);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of deleteItem method, of class FileHistoryDao.
     */
    @Test
    @Ignore
    public void testDeleteItem() {
        System.out.println("deleteItem");
        AbstractItem item = null;
        FileHistoryDao instance = null;
        instance.deleteItem(item);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of renameItem method, of class FileHistoryDao.
     */
    @Test
    @Ignore
    public void testRenameItem() {
        System.out.println("renameItem");
        AbstractItem item = null;
        String newName = "";
        FileHistoryDao instance = null;
        instance.renameItem(item, newName);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getRevisions method, of class FileHistoryDao.
     */
    @Test
    @Ignore
    public void testGetRevisions() {
        System.out.println("getRevisions");
        AbstractItem item = null;
        FileHistoryDao instance = null;
        SortedMap<String, XmlFile> expResult = null;
        SortedMap<String, XmlFile> result = instance.getRevisions(item);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getOldRevision method, of class FileHistoryDao.
     */
    @Test
    @Ignore
    public void testGetOldRevision() {
        System.out.println("getOldRevision");
        AbstractItem item = null;
        String identifier = "";
        FileHistoryDao instance = null;
        XmlFile expResult = null;
        XmlFile result = instance.getOldRevision(item, identifier);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getHistoryDir method, of class FileHistoryDao.
     */
    @Test
    @Ignore
    public void testGetHistoryDir() {
        System.out.println("getHistoryDir");
        XmlFile xmlFile = null;
        FileHistoryDao instance = null;
        File expResult = null;
        File result = instance.getHistoryDir(xmlFile);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getJobHistoryRootDir method, of class FileHistoryDao.
     */
    @Test
    @Ignore
    public void testGetJobHistoryRootDir() {
        System.out.println("getJobHistoryRootDir");
        FileHistoryDao instance = null;
        File expResult = null;
        File result = instance.getJobHistoryRootDir();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of purgeOldEntries method, of class FileHistoryDao.
     */
    @Test
    public void testPurgeOldEntriesNoEntriesToDelete() {        
        final File itemHistoryRoot = unpackResourceZip.getResource("config-history/jobs/Test1/");
        final int oldLength = itemHistoryRoot.list().length;
        int maxEntries = 0;
        FileHistoryDao.purgeOldEntries(itemHistoryRoot, maxEntries);
        final int newLength = itemHistoryRoot.list().length;
        assertEquals(oldLength, newLength);
    }

    /**
     * Test of purgeOldEntries method, of class FileHistoryDao.
     */
    @Test
    public void testPurgeOldEntriesOnlyOneExisting() {        
        final File itemHistoryRoot = unpackResourceZip.getResource("config-history/jobs/Test1/");
        int maxEntries = 2;
        FileHistoryDao.purgeOldEntries(itemHistoryRoot, maxEntries);
        final int newLength = itemHistoryRoot.list().length;
        assertEquals(1, newLength);
    }
}