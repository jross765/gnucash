package org.gnucash.read.impl.spec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;

import org.gnucash.ConstTest;
import org.gnucash.basetypes.simple.GCshID;
import org.gnucash.read.GnucashFile;
import org.gnucash.read.GnucashGenerJob;
import org.gnucash.read.impl.GnucashFileImpl;
import org.gnucash.read.impl.TestGnucashGenerJobImpl;
import org.gnucash.read.spec.GnucashCustomerJob;
import org.junit.Before;
import org.junit.Test;

import junit.framework.JUnit4TestAdapter;

public class TestGnucashCustomerJobImpl
{
  private GnucashFile        gcshFile = null;
  private GnucashGenerJob    jobGener = null;
  private GnucashCustomerJob jobSpec = null;
  
  private static final String JOB_1_ID = TestGnucashGenerJobImpl.JOB_1_ID;

  // -----------------------------------------------------------------
  
  public static void main(String[] args) throws Exception
  {
    junit.textui.TestRunner.run(suite());
  }

  @SuppressWarnings("exports")
  public static junit.framework.Test suite() 
  {
    return new JUnit4TestAdapter(TestGnucashCustomerJobImpl.class);  
  }
  
  @Before
  public void initialize() throws Exception
  {
    ClassLoader classLoader = getClass().getClassLoader();
    // URL gcshFileURL = classLoader.getResource(Const.GCSH_FILENAME);
    // System.err.println("GnuCash test file resource: '" + gcshFileURL + "'");
    InputStream gcshFileStream = null;
    try 
    {
      gcshFileStream = classLoader.getResourceAsStream(ConstTest.GCSH_FILENAME);
    } 
    catch ( Exception exc ) 
    {
      System.err.println("Cannot generate input stream from resource");
      return;
    }
    
    try
    {
      gcshFile = new GnucashFileImpl(gcshFileStream);
    }
    catch ( Exception exc )
    {
      System.err.println("Cannot parse GnuCash file");
      exc.printStackTrace();
    }
  }

  // -----------------------------------------------------------------

  @Test
  public void test01() throws Exception
  {
    jobGener = gcshFile.getGenerJobByID(JOB_1_ID);
    assertNotEquals(null, jobGener);
    jobSpec  = new GnucashCustomerJobImpl(jobGener);
    assertNotEquals(null, jobSpec);

    assertTrue(jobSpec instanceof GnucashCustomerJob);
    assertEquals(JOB_1_ID, jobSpec.getId());
    assertEquals("000001", jobSpec.getNumber());
    assertEquals("Do more for others", jobSpec.getName());
  }

  @Test
  public void test02() throws Exception
  {
    jobGener = gcshFile.getGenerJobByID(JOB_1_ID);
    assertNotEquals(null, jobGener);
    jobSpec  = new GnucashCustomerJobImpl(jobGener);
    assertNotEquals(null, jobSpec);
      
    // Note: That the following two return the same result
    // is *not* trivial (in fact, a serious implementation error was
    // found with this test)
    assertEquals(0, jobGener.getPaidInvoices().size());
    assertEquals(0, jobSpec.getPaidInvoices().size());

    // Note: That the following two return the same result
    // is *not* trivial (in fact, a serious implementation error was
    // found with this test)
    assertEquals(1, jobGener.getUnpaidInvoices().size());
    assertEquals(1, jobSpec.getUnpaidInvoices().size());
  }

  @Test
  public void test03() throws Exception
  {
    jobGener = gcshFile.getGenerJobByID(JOB_1_ID);
    assertNotEquals(null, jobGener);
    jobSpec  = new GnucashCustomerJobImpl(jobGener);
    assertNotEquals(null, jobSpec);
      
    // Note: That the following three return the same result
    // is *not* trivial (in fact, a serious implementation error was
    // found with this test)
    GCshID custID = new GCshID("f44645d2397946bcac90dff68cc03b76");
    assertEquals(custID.toString(), jobGener.getOwnerId());
    assertEquals(custID.toString(), jobSpec.getOwnerId());
    assertEquals(custID, jobSpec.getCustomerId());
  }
}
