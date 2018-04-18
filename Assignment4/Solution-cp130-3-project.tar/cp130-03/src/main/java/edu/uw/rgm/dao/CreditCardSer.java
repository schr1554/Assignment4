package edu.uw.rgm.dao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.springframework.beans.BeansException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import edu.uw.ext.framework.account.AccountException;
import edu.uw.ext.framework.account.CreditCard;


/**
 * Class for serializing instances of classes that implement the CreditCard
 * interface.  Implemented using a simple text file, one property per line.
 *
 * @author Russ Moul
 */
public final class CreditCardSer {
    /** Constant to be written to represent a null string reference.*/
    private static final String NULL_STR = "<null>";

    /**
     * Utility class - disable constructor.
     */
    private CreditCardSer() {
        // no-op
    };
    /**
     * Writes a CreditCard object to an output stream.
     *
     * @param out the output stream to write to
     * @param cc the CreditCard object to write
     */
    public static void write(final OutputStream out, final CreditCard cc) {
        final PrintWriter wtr = new PrintWriter(out);

        if (cc != null) {
            String s;
            s = cc.getIssuer();
            wtr.println(s == null ? NULL_STR : s);
            s = cc.getType();
            wtr.println(s == null ? NULL_STR : s);
            s = cc.getHolder();
            wtr.println(s == null ? NULL_STR : s);
            s = cc.getAccountNumber();
            wtr.println(s == null ? NULL_STR : s);
            s = cc.getExpirationDate();
            wtr.println(s == null ? NULL_STR : s);
        }
        wtr.flush();
    }

     /**
     * Reads a CreditCard object from an input stream.
     *
     * @param in the input stream to read from
     *
     * @return the CreditCard object read from stream
     *
     * @throws AccountException if an error occurs reading from stream or instantiating
     *                   the object
     */
    public static CreditCard read(final InputStream in) throws AccountException {
        final BufferedReader rdr = new BufferedReader(new InputStreamReader(in));
        try (ClassPathXmlApplicationContext appContext =
            new ClassPathXmlApplicationContext("context.xml")) {
            final CreditCard cc = appContext.getBean(CreditCard.class);
            String tmp = null;

            tmp = rdr.readLine();
            cc.setIssuer((NULL_STR.equals(tmp)) ? null : tmp);

            tmp = rdr.readLine();
            cc.setType((NULL_STR.equals(tmp)) ? null : tmp);

            tmp = rdr.readLine();
            cc.setHolder((NULL_STR.equals(tmp)) ? null : tmp);

            tmp = rdr.readLine();
            cc.setAccountNumber((NULL_STR.equals(tmp)) ? null : tmp);

            tmp = rdr.readLine();
            cc.setExpirationDate((NULL_STR.equals(tmp)) ? null : tmp);
            return cc;
        } catch (final BeansException ex) {
            throw new AccountException("Unable to create credit card instance.", ex);
        } catch (final IOException ex) {
            throw new AccountException("Unable to read persisted credit card data.", ex);
        }
    }
}

