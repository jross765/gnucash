package org.gnucash.api.read;

import org.gnucash.api.basetypes.complex.GCshCmdtyCurrID;
import org.gnucash.api.basetypes.complex.InvalidCmdtyCurrIDException;
import org.gnucash.api.basetypes.complex.InvalidCmdtyCurrTypeException;
import org.gnucash.api.basetypes.simple.GCshID;
import org.gnucash.api.numbers.FixedPointNumber;
import org.gnucash.api.generated.GncAccount;
import org.gnucash.api.generated.GncTransaction;

import java.time.LocalDate;
import java.util.*;

/**
 * An account is a collection of transactions that start or end there. <br>
 * You can compare it's functionality to an abstracted bank account. <br>
 * It has a balance, may have a parent-account(@see #getParentAccount()) and child-accounts(@see #getSubAccounts()) to form
 * a tree. <br>
 * 
 * @see #getParentAccount()
 */
public interface GnucashAccount extends Comparable<GnucashAccount> {

    // For the following types cf.:
    // https://github.com/Gnucash/gnucash/blob/stable/libgnucash/engine/Account.h
    //
    // Examples (from German accounting):
    //
    // - TYPE_BANK = "BANK"; Girokonto, Tagesgeldkonto
    // - TYPE_CASH = "CASH"; Kasse
    // - TYPE_CREDIT = "CREDIT"; "Kreditkarte"
    // - TYPE_ASSET = "ASSET"; Vermögensgegenstaende, "1. Forderungen aus
    // Lieferungen und Leistungen"
    // - TYPE_LIABILITY = "LIABILITY"; Verbindlichkeiten ggueber Lieferanten
    // - TYPE_STOCK = "STOCK"; Aktie
    // - TYPE_MUTUAL = "MUTUAL"; Investment-Fonds
    // - TYPE_CURRENCY = "CURRENCY";
    // - TYPE_INCOME = "INCOME"; "Umsatzerloese 16% USt"
    // - TYPE_EXPENSE = "EXPENSE"; "private Ausgaben"
    // - TYPE_EQUITY = "EQUITY"; "Anfangsbestand"
    // - TYPE_RECEIVABLE = "RECEIVABLE"; "Forderungen aus Lieferungen und
    // Leistungen"
    // - TYPE_PAYABLE = "PAYABLE"; "Verbindlichkeiten ggueber Lieferant xyz"
    // - TYPE_ROOT = "ROOT"; guess ;-)
    // - TYPE_TRADING = "TRADING";

    public enum Type {
	BANK,
	CASH,
	CREDIT,
	ASSET,
	LIABILITY,
	STOCK,
	MUTUAL,
	CURRENCY,
	INCOME,
	EXPENSE,
	EQUITY,
	RECEIVABLE,
	PAYABLE,
	ROOT,
	TRADING
    }
    
    // -----------------------------------------------------------------
    
    public static String SEPARATOR = "::";

    // -----------------------------------------------------------------

    @SuppressWarnings("exports")
    GncAccount getJwsdpPeer();

    /**
     * The gnucash-file is the top-level class to contain everything.
     * @return the file we are associated with
     */
    GnucashFile getGnucashFile();
    
    // -----------------------------------------------------------------

    /**
     * @return the unique id for that account (not meaningfull to human users)
     */
    GCshID getId();

    /**
     * @return a user-defined description to acompany the name of the account. Can
     *         encompass many lines.
     */
    String getDescription();

    /**
     * @return the account-number
     */
    String getCode();

    /**
     * @return user-readable name of this account. Does not contain the name of
     *         parent-accounts
     */
    String getName();

    /**
     * get name including the name of the parent.accounts.
     *
     * @return e.g. "Aktiva::test::test2"
     */
    String getQualifiedName();

    /**
     * @return null if the account is below the root
     */
    GCshID getParentAccountId();

    /**
     * @return the parent-account we are a child of or null if we are a top-level
     *         account
     */
    GnucashAccount getParentAccount();

    /**
     * The returned collection is never null and is sorted by Account-Name.
     *
     * @return all child-accounts
     * @see #getChildren()
     */
    Collection<GnucashAccount> getSubAccounts();

    /**
     * The returned collection is never null and is sorted by Account-Name.
     *
     * @return all child-accounts
     */
    Collection<GnucashAccount> getChildren();

    /**
     * @param account the account to test
     * @return true if this is a child of us or any child's or us.
     */
    boolean isChildAccountRecursive(GnucashAccount account);

    // ----------------------------

    Type getType() throws UnknownAccountTypeException;

    GCshCmdtyCurrID getCmdtyCurrID() throws InvalidCmdtyCurrTypeException;

    // -----------------------------------------------------------------

    /**
     * The returned list ist sorted by the natural order of the Transaction-Splits.
     *
     * @return all splits
     * @link GnucashTransactionSplit
     */
    List<? extends GnucashTransactionSplit> getTransactionSplits();

    /**
     * @param id the split-id to look for
     * @return the identified split or null
     */
    GnucashTransactionSplit getTransactionSplitByID(final GCshID id);

    /**
     * Gets the last transaction-split before the given date.
     *
     * @param date if null, the last split of all time is returned
     * @return the last transaction-split before the given date
     */
    GnucashTransactionSplit getLastSplitBeforeRecursive(final LocalDate date);

    /**
     * @param split split to add to this transaction
     */
    void addTransactionSplit(final GnucashTransactionSplit split);

    // ----------------------------

    /**
     * @return true if ${@link #getTransactionSplits()}.size()>0
     */
    boolean hasTransactions();

    /**
     * @return true if ${@link #hasTransactions()} is true for this or any
     *         sub-accounts
     */
    boolean hasTransactionsRecursive();

    /**
     * The returned list ist sorted by the natural order of the Transaction-Splits.
     *
     * @return all splits
     * @link GnucashTransaction
     */
    List<GnucashTransaction> getTransactions();

    // -----------------------------------------------------------------

    /**
     * same as getBalance(new Date()).<br/>
     * ignores transactions after the current date+time<br/>
     * Be aware that the result is in the currency of this account!
     *
     * @return the balance
     */
    FixedPointNumber getBalance();

    /**
     * Be aware that the result is in the currency of this account!
     *
     * @param date if non-null transactions after this date are ignored in the
     *             calculation
     * @return the balance formatted using the current locale
     */
    FixedPointNumber getBalance(final LocalDate date);

    /**
     * Be aware that the result is in the currency of this account!
     *
     * @param date  if non-null transactions after this date are ignored in the
     *              calculation
     * @param after splits that are after date are added here.
     * @return the balance formatted using the current locale
     */
    FixedPointNumber getBalance(final LocalDate date, Collection<GnucashTransactionSplit> after);

    /**
     * @param lastIncludesSplit last split to be included
     * @return the balance up to and including the given split
     */
    FixedPointNumber getBalance(final GnucashTransactionSplit lastIncludesSplit);

    // ----------------------------

    /**
     * same as getBalance(new Date()). ignores transactions after the current
     * date+time
     *
     * @return the balance formatted using the current locale
     * @throws InvalidCmdtyCurrTypeException 
     */
    String getBalanceFormatted() throws InvalidCmdtyCurrTypeException;

    /**
     * same as getBalance(new Date()). ignores transactions after the current
     * date+time
     *
     * @param lcl the locale to use (does not affect the currency)
     * @return the balance formatted using the given locale
     * @throws InvalidCmdtyCurrTypeException 
     */
    String getBalanceFormatted(final Locale lcl) throws InvalidCmdtyCurrTypeException;

    // ----------------------------

    /**
     * same as getBalanceRecursive(new Date()).<br/>
     * ignores transactions after the current date+time<br/>
     * Be aware that the result is in the currency of this account!
     *
     * @return the balance including sub-accounts
     * @throws InvalidCmdtyCurrTypeException 
     * @throws InvalidCmdtyCurrIDException 
     */
    FixedPointNumber getBalanceRecursive() throws InvalidCmdtyCurrTypeException, InvalidCmdtyCurrIDException;

    /**
     * Gets the balance including all sub-accounts.
     *
     * @param date if non-null transactions after this date are ignored in the
     *             calculation
     * @return the balance including all sub-accounts
     * @throws InvalidCmdtyCurrTypeException 
     * @throws InvalidCmdtyCurrIDException 
     */
    FixedPointNumber getBalanceRecursive(final LocalDate date) throws InvalidCmdtyCurrTypeException, InvalidCmdtyCurrIDException;

    /**
     * Ignores accounts for which this conversion is not possible.
     *
     * @param date     ignores transactions after the given date
     * @param currency the currency the result shall be in
     * @return Gets the balance including all sub-accounts.
     * @throws InvalidCmdtyCurrTypeException 
     * @throws InvalidCmdtyCurrIDException 
     * @see GnucashAccount#getBalanceRecursive(LocalDate)
     */
    FixedPointNumber getBalanceRecursive(final LocalDate date, final Currency curr) throws InvalidCmdtyCurrTypeException, InvalidCmdtyCurrIDException;

    /**
     * Ignores accounts for which this conversion is not possible.
     *
     * @param date              ignores transactions after the given date
     * @param currencyNameSpace the currency the result shall be in
     * @param currencyName      the currency the result shall be in
     * @return Gets the balance including all sub-accounts.
     * @throws InvalidCmdtyCurrTypeException 
     * @throws InvalidCmdtyCurrIDException 
     * @see GnucashAccount#getBalanceRecursive(Date, Currency)
     */
    FixedPointNumber getBalanceRecursive(final LocalDate date, final GCshCmdtyCurrID secCurrID) throws InvalidCmdtyCurrTypeException, InvalidCmdtyCurrIDException;

    // ----------------------------

    /**
     * same as getBalanceRecursive(new Date()). ignores transactions after the
     * current date+time
     *
     * @return the balance including sub-accounts formatted using the current locale
     * @throws InvalidCmdtyCurrTypeException 
     * @throws InvalidCmdtyCurrIDException 
     */
    String getBalanceRecursiveFormatted() throws InvalidCmdtyCurrTypeException, InvalidCmdtyCurrIDException;

    /**
     * Gets the balance including all sub-accounts.
     *
     * @param date if non-null transactions after this date are ignored in the
     *             calculation
     * @return the balance including all sub-accounts
     * @throws InvalidCmdtyCurrTypeException 
     * @throws InvalidCmdtyCurrIDException 
     */
    String getBalanceRecursiveFormatted(final LocalDate date) throws InvalidCmdtyCurrTypeException, InvalidCmdtyCurrIDException;

    // -----------------------------------------------------------------

    /**
     * Examples: The user-defined-attribute "hidden"="true"/"false" was introduced
     * in gnucash2.0 to hide accounts.
     *
     * @param name the name of the user-defined attribute
     * @return the value or null if not set
     */
    String getUserDefinedAttribute(final String name);

    /**
     * @return all keys that can be used with
     *         ${@link #getUserDefinedAttribute(String)}}.
     */
    Collection<String> getUserDefinedAttributeKeys();
}