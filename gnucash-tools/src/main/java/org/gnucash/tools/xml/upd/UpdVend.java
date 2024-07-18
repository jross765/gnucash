package org.gnucash.tools.xml.upd;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.gnucash.api.write.GnuCashWritableVendor;
import org.gnucash.api.write.impl.GnuCashWritableFileImpl;
import org.gnucash.base.basetypes.simple.GCshID;
import org.gnucash.tools.CommandLineTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.schnorxoborx.base.beanbase.NoEntryFoundException;
import xyz.schnorxoborx.base.cmdlinetools.CouldNotExecuteException;
import xyz.schnorxoborx.base.cmdlinetools.InvalidCommandLineArgsException;

public class UpdVend extends CommandLineTool
{
  // Logger
  private static final Logger LOGGER = LoggerFactory.getLogger(UpdVend.class);
  
  // private static PropertiesConfiguration cfg = null;
  private static Options options;
  
  private static String gcshInFileName = null;
  private static String gcshOutFileName = null;
  private static GCshID vendID = null;

  private static String number = null;
  private static String name = null;
  private static String descr = null;

  private static GnuCashWritableVendor vend = null;

  public static void main( String[] args )
  {
    try
    {
      UpdVend tool = new UpdVend ();
      tool.execute(args);
    }
    catch (CouldNotExecuteException exc) 
    {
      System.err.println("Execution exception. Aborting.");
      exc.printStackTrace();
      System.exit(1);
    }
  }

  @Override
  protected void init() throws Exception
  {
    // vendID = UUID.randomUUID();

//    cfg = new PropertiesConfiguration(System.getProperty("config"));
//    getConfigSettings(cfg);

    // Options
    // The essential ones
    Option optFileIn = OptionBuilder
      .isRequired()
      .hasArg()
      .withArgName("file")
      .withDescription("GnuCash file (in)")
      .withLongOpt("gnucash-in-file")
      .create("if");
          
    Option optFileOut = OptionBuilder
      .isRequired()
      .hasArg()
      .withArgName("file")
      .withDescription("GnuCash file (out)")
      .withLongOpt("gnucash-out-file")
      .create("of");
      
    Option optID = OptionBuilder
      .isRequired()
      .hasArg()
      .withArgName("UUID")
      .withDescription("Vendor ID")
      .withLongOpt("vendor-id")
      .create("id");
            
    Option optNumber = OptionBuilder
      .hasArg()
      .withArgName("number")
      .withDescription("Vendor number")
      .withLongOpt("number")
      .create("num");
    	    
    Option optName = OptionBuilder
      .hasArg()
      .withArgName("name")
      .withDescription("Vendor name")
      .withLongOpt("name")
      .create("nam");
    
    Option optDescr = OptionBuilder
      .hasArg()
      .withArgName("descr")
      .withDescription("Vendor description")
      .withLongOpt("description")
      .create("desc");
      
    // The convenient ones
    // ::EMPTY
          
    options = new Options();
    options.addOption(optFileIn);
    options.addOption(optFileOut);
    options.addOption(optID);
    options.addOption(optNumber);
    options.addOption(optName);
    options.addOption(optDescr);
  }

  @Override
  protected void getConfigSettings(PropertiesConfiguration cs) throws Exception
  {
    // ::EMPTY
  }
  
  @Override
  protected void kernel() throws Exception
  {
    GnuCashWritableFileImpl gcshFile = new GnuCashWritableFileImpl(new File(gcshInFileName));

    try 
    {
      vend = gcshFile.getWritableVendorByID(vendID);
      System.err.println("Vendor before update: " + vend.toString());
    }
    catch ( Exception exc )
    {
      System.err.println("Error: Could not find/instantiate vendor with ID '" + vendID + "'");
      // ::TODO
//      throw new VendorNotFoundException();
      throw new NoEntryFoundException();
    }
    
    doChanges(gcshFile);
    System.err.println("Vendor after update: " + vend.toString());
    
    gcshFile.writeFile(new File(gcshOutFileName));
    
    System.out.println("OK");
  }

  private void doChanges(GnuCashWritableFileImpl gcshFile) throws Exception
  {
    if ( number != null )
    {
      System.err.println("Setting number");
      vend.setNumber(number);
    }

    if ( name != null )
    {
      System.err.println("Setting name");
      vend.setName(name);
    }

    if ( descr != null )
    {
      System.err.println("Setting description");
      vend.setNotes(descr);
    }
  }

  // -----------------------------------------------------------------

  @Override
  protected void parseCommandLineArgs(String[] args) throws InvalidCommandLineArgsException
  {
    CommandLineParser parser = new DefaultParser();
    CommandLine cmdLine = null;
    try
    {
      cmdLine = parser.parse(options, args);
    }
    catch (ParseException exc)
    {
      System.err.println("Parsing options failed. Reason: " + exc.getMessage());
    }

    // ---

    // <gnucash-in-file>
    try
    {
      gcshInFileName = cmdLine.getOptionValue("gnucash-in-file");
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <gnucash-in-file>");
      throw new InvalidCommandLineArgsException();
    }
    System.err.println("GnuCash file (in): '" + gcshInFileName + "'");
    
    // <gnucash-out-file>
    try
    {
      gcshOutFileName = cmdLine.getOptionValue("gnucash-out-file");
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <gnucash-out-file>");
      throw new InvalidCommandLineArgsException();
    }
    System.err.println("GnuCash file (out): '" + gcshOutFileName + "'");
    
    // <vendor-id>
    try
    {
      vendID = new GCshID( cmdLine.getOptionValue("vendor-id") );
    }
    catch ( Exception exc )
    {
      System.err.println("Could not parse <vendor-id>");
      throw new InvalidCommandLineArgsException();
    }
    System.err.println("Vendor ID: " + vendID);

    // <number>
    if ( cmdLine.hasOption("number") ) 
    {
      try
      {
        number = cmdLine.getOptionValue("number");
      }
      catch ( Exception exc )
      {
        System.err.println("Could not parse <number>");
        throw new InvalidCommandLineArgsException();
      }
    }
    System.err.println("Number: '" + number + "'");

    // <name>
    if ( cmdLine.hasOption("name") ) 
    {
      try
      {
        name = cmdLine.getOptionValue("name");
      }
      catch ( Exception exc )
      {
        System.err.println("Could not parse <name>");
        throw new InvalidCommandLineArgsException();
      }
    }
    System.err.println("Name: '" + name + "'");

    // <description>
    if ( cmdLine.hasOption("description") ) 
    {
      try
      {
        descr = cmdLine.getOptionValue("description");
      }
      catch ( Exception exc )
      {
        System.err.println("Could not parse <description>");
        throw new InvalidCommandLineArgsException();
      }
    }
    System.err.println("Description: '" + descr + "'");
  }
  
  @Override
  protected void printUsage()
  {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp( "UpdVend", options );
  }
}
