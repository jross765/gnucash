package org.gnucash.api.write.impl.spec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.gnucash.api.ConstTest;
import org.gnucash.api.basetypes.simple.GCshID;
import org.gnucash.api.read.GnucashAccount;
import org.gnucash.api.read.GnucashCustomer;
import org.gnucash.api.read.GnucashGenerInvoice;
import org.gnucash.api.read.GnucashTransaction;
import org.gnucash.api.read.impl.GnucashFileImpl;
import org.gnucash.api.read.impl.TestGnucashCustomerImpl;
import org.gnucash.api.read.impl.TestGnucashGenerJobImpl;
import org.gnucash.api.read.impl.TestGnucashVendorImpl;
import org.gnucash.api.read.impl.spec.GnucashCustomerInvoiceImpl;
import org.gnucash.api.read.spec.GnucashCustomerInvoice;
import org.gnucash.api.write.impl.GnucashWritableFileImpl;
import org.gnucash.api.write.spec.GnucashWritableCustomerInvoice;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import junit.framework.JUnit4TestAdapter;

public class TestGnucashWritableCustomerInvoiceImpl
{
    private static final GCshID CUST_1_ID = TestGnucashCustomerImpl.CUST_1_ID;
    private static final GCshID CUST_2_ID = TestGnucashCustomerImpl.CUST_2_ID;
    private static final GCshID CUST_3_ID = TestGnucashCustomerImpl.CUST_3_ID;

    private static final GCshID VEND_1_ID = TestGnucashVendorImpl.VEND_1_ID;
    private static final GCshID VEND_2_ID = TestGnucashVendorImpl.VEND_2_ID;
    private static final GCshID VEND_3_ID = TestGnucashVendorImpl.VEND_3_ID;

    private static final GCshID JOB_1_ID  = TestGnucashGenerJobImpl.JOB_1_ID;
    private static final GCshID JOB_2_ID  = TestGnucashGenerJobImpl.JOB_2_ID;

    static final GCshID INCOME_ACCT_ID     = new GCshID("fed745c4da5c49ebb0fde0f47222b35b"); // Root Account::Erträge::Sonstiges
    static final GCshID RECEIVABLE_ACCT_ID = new GCshID("7e223ee2260d4ba28e8e9e19ce291f43"); // Root Account::Aktiva::Forderungen::Unfug_Quatsch
    
    // ----------------------------

    private GnucashWritableFileImpl gcshInFile = null;
    private GnucashFileImpl         gcshOutFile = null;

    private GnucashCustomer cust1 = null;
    
    private GnucashAccount  incomeAcct = null;
    private GnucashAccount  receivableAcct = null;
    
    // ----------------------------

    // https://stackoverflow.com/questions/11884141/deleting-file-and-directory-in-junit
    @SuppressWarnings("exports")
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    
    // -----------------------------------------------------------------
  
  public static void main(String[] args) throws Exception
  {
    junit.textui.TestRunner.run(suite());
  }

  @SuppressWarnings("exports")
  public static junit.framework.Test suite() 
  {
    return new JUnit4TestAdapter(TestGnucashWritableCustomerInvoiceImpl.class);  
  }
  
  @Before
  public void initialize() throws Exception
  {
    ClassLoader classLoader = getClass().getClassLoader();
    // URL gcshFileURL = classLoader.getResource(Const.GCSH_FILENAME);
    // System.err.println("GnuCash test file resource: '" + gcshFileURL + "'");
    InputStream gcshInFileStream = null;
    try 
    {
      gcshInFileStream = classLoader.getResourceAsStream(ConstTest.GCSH_FILENAME_IN);
    } 
    catch ( Exception exc ) 
    {
      System.err.println("Cannot generate input stream from resource");
      return;
    }
    
    try
    {
      gcshInFile = new GnucashWritableFileImpl(gcshInFileStream);
    }
    catch ( Exception exc )
    {
      System.err.println("Cannot parse GnuCash in-file");
      exc.printStackTrace();
    }
    
    // ----------------------------
    
    cust1 = gcshInFile.getCustomerByID(CUST_1_ID);
    
    incomeAcct     = gcshInFile.getAccountByID(INCOME_ACCT_ID);
    receivableAcct = gcshInFile.getAccountByID(RECEIVABLE_ACCT_ID);
  }

  // -----------------------------------------------------------------

  @Test
  public void test01() throws Exception
  {
      LocalDate postDate = LocalDate.of(2023, 8, 1);
      LocalDate openedDate = LocalDate.of(2023, 8, 3);
      LocalDate dueDate = LocalDate.of(2023, 8, 10);
      GnucashWritableCustomerInvoice invc = gcshInFile.createWritableCustomerInvoice("19327", 
	      							cust1, 
	      							incomeAcct, receivableAcct, 
	      							openedDate, postDate, dueDate);
      
//      GnucashWritableCustomerInvoiceEntry entr = invc.createEntry(acct2, 
//                                                                  new FixedPointNumber(12), 
//                                                                  new FixedPointNumber(13));

      assertNotEquals(null, invc);
      GCshID newInvcID = invc.getId();
//      System.out.println("New Invoice ID (1): " + newInvcID);
      
      assertEquals("19327", invc.getNumber());

      File outFile = folder.newFile(ConstTest.GCSH_FILENAME_OUT);
//      System.err.println("Outfile for TestGnucashWritableCustomerImpl.test01_1: '" + outFile.getPath() + "'");
      outFile.delete(); // sic, the temp. file is already generated (empty), 
                        // and the GnuCash file writer does not like that.
      gcshInFile.writeFile(outFile);
      
      // test01_2();
      test01_3(outFile, newInvcID);
      test01_4(outFile, newInvcID);
      
      // post invoice
      invc.post(incomeAcct, receivableAcct, postDate, dueDate);
      
      // write to file
      outFile.delete();
      gcshInFile.writeFile(outFile);
      
      test01_5(outFile, newInvcID);
  }

  private void test01_2(File outFile, String newInvcID) throws ParserConfigurationException, SAXException, IOException 
  {
      // ::TODO
      // Check if generated XML file is valid
  }
  
  private void test01_3(File outFile, GCshID newInvcID) throws ParserConfigurationException, SAXException, IOException 
  {
      //    assertNotEquals(null, outFileGlob);
      //    assertEquals(true, outFileGlob.exists());

      // Build document
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document = builder.parse(outFile);
//      System.err.println("xxxx XML parsed");

      // Normalize the XML structure
      document.getDocumentElement().normalize();
//      System.err.println("xxxx XML normalized");
      
      NodeList nList = document.getElementsByTagName("gnc:GncInvoice");
      assertEquals(8, nList.getLength());

      // Last (new) node
      Node lastNode = nList.item(nList.getLength() - 1);
      assertEquals(lastNode.getNodeType(), Node.ELEMENT_NODE);
      
      Element elt = (Element) lastNode;
      assertEquals("19327", elt.getElementsByTagName("invoice:id").item(0).getTextContent());
      String locNewInvcID = elt.getElementsByTagName("invoice:guid").item(0).getTextContent();
//      System.out.println("New Invoice ID (2): " + locNewInvcID);
      assertEquals(newInvcID.toString(), locNewInvcID);
  }

  // Before post
  private void test01_4(File outFile, GCshID newInvcID) throws Exception
  {
//      assertNotEquals(null, outFileGlob);
//      assertEquals(true, outFileGlob.exists());

      gcshOutFile = new GnucashFileImpl(outFile);
      
//      System.out.println("New Invoice ID (3): " + newInvcID);
      GnucashGenerInvoice invcGener = gcshOutFile.getGenerInvoiceByID(newInvcID);
      assertNotEquals(null, invcGener);
      GnucashCustomerInvoice invcSpec = new GnucashCustomerInvoiceImpl(invcGener);
      assertNotEquals(null, invcSpec);
      
      assertEquals("19327", invcSpec.getNumber());
      assertEquals(null, invcSpec.getPostAccountId());      
      assertEquals(null, invcSpec.getPostTransactionId());
  }

  // After post
  private void test01_5(File outFile, GCshID newInvcID) throws Exception
  {
//      assertNotEquals(null, outFileGlob);
//      assertEquals(true, outFileGlob.exists());

      gcshOutFile = new GnucashFileImpl(outFile);
      
//      System.out.println("New Invoice ID (3): " + newInvcID);
      GnucashGenerInvoice invcGener = gcshOutFile.getGenerInvoiceByID(newInvcID);
      assertNotEquals(null, invcGener);
      GnucashCustomerInvoice invcSpec = new GnucashCustomerInvoiceImpl(invcGener);
      assertNotEquals(null, invcSpec);
      
      assertEquals("19327", invcSpec.getNumber());
      assertEquals(RECEIVABLE_ACCT_ID, invcSpec.getPostAccountId());
      
      assertNotEquals(null, invcSpec.getPostTransactionId());
      GnucashTransaction postTrx = gcshOutFile.getTransactionByID(invcSpec.getPostTransactionId());
      assertNotEquals(null, postTrx);
      assertEquals(2, postTrx.getSplits().size());
      GCshID postTrxFirstSpltId = postTrx.getFirstSplit().getId();
      assertNotEquals(postTrxFirstSpltId, postTrx);
      GCshID postTrxFirstSpltAcctId = postTrx.getFirstSplit().getAccount().getId();
      assertNotEquals(postTrxFirstSpltAcctId, postTrx);
      GCshID postTrxSecondSpltAcctId = postTrx.getSecondSplit().getAccount().getId();
      assertNotEquals(postTrxSecondSpltAcctId, postTrx);
//      System.out.println("ptrx1 " + postTrxFirstSpltAcctId);
//      System.out.println("ptrx2 " + postTrxSecondSpltAcctId);
  }

//  @AfterClass
//  public void after() throws Exception
//  {
//      FileUtils.delete(outFileGlob);
//  }

}