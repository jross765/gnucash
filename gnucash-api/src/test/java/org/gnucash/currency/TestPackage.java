package org.gnucash.currency;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestPackage extends TestCase
{
  public static void main(String[] args) throws Exception
  {
    junit.textui.TestRunner.run(suite());
  }

  @SuppressWarnings("exports")
  public static Test suite() throws Exception
  {
    TestSuite suite = new TestSuite();
    
    suite.addTest(org.gnucash.currency.TestCmdtyCurrID.suite());
    suite.addTest(org.gnucash.currency.TestCurrencyID.suite());
    suite.addTest(org.gnucash.currency.TestCommodityID.suite());
    suite.addTest(org.gnucash.currency.TestCommodityID_Exchange.suite());
    suite.addTest(org.gnucash.currency.TestCommodityID_MIC.suite());

    return suite;
  }
}