package edu.uw.rgm.dao;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.springframework.beans.BeansException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import edu.uw.ext.framework.account.Account;
import edu.uw.ext.framework.account.AccountException;


/**
 * Class for serializing instances of classes that implement the Account
 * interface.
 *
 * @author Russ Moul
 */
public final class AccountSer {
    /** Constant to be written to represent a null string reference.*/
    private static final String NULL_STR = "<null>";
    
    /**
     * Utility class - disable constructor.
     */
    private AccountSer() {
        // no-op
    };

    /**
     * Writes an Account object to an output stream.
     *
     * @param out the output stream to write to
     * @param acct the Account object to write
     *
     * @throws AccountException if an error occurs writing to stream
     */
    public static void write(final OutputStream out, final Account acct)
        throws AccountException {
        try {
            final DataOutputStream dos = new DataOutputStream(out);
            dos.writeUTF(acct.getName());
            writeByteArray(dos, acct.getPasswordHash());
            dos.writeInt(acct.getBalance());
            writeString(dos, acct.getFullName());
            writeString(dos, acct.getPhone());
            writeString(dos, acct.getEmail());
            dos.flush();
        } catch (final IOException ex) {
            throw new AccountException("Failed to write account data.", ex);
        }
    }

    /**
     * Writes the string to the out put stream, writing NULL_STR if the string
     * is null.
     * 
     * @param out DataOutputStream to write to
     * @param s the string to write
     * @throws IOException if an error occurs writing to stream
     */
    private static void writeString(final DataOutputStream out, final String s)
        throws IOException {
        out.writeUTF(s == null ? NULL_STR : s);
    }
    
    /**
     * Convenience method for reading strings.
     *
     * @param in DataInputStream to read from
     *
     * @return the read string
     *
     * @throws IOException if an error occurs reading from stream
     */
    private static String readString(final DataInputStream in) throws IOException {
        final String s = in.readUTF();
        return NULL_STR.equals(s) ? null : s;
    }


    /**
     * Convenience method for writing byte arrays.
     *
     * @param out DataOutputStream to write to
     * @param b the byte array to write
     *
     * @throws IOException if an error occurs writing to stream
     */
    private static void writeByteArray(final DataOutputStream out, final byte[] b)
        throws IOException {
        
        final int len = (b == null) ? -1 : b.length;
        out.writeInt(len);

        if (len > 0) {
            out.write(b);
        }
    }

    /**
     * Convenience method for reading byte arrays.
     *
     * @param in DataInputStream to read from
     *
     * @return the read bytes
     *
     * @throws IOException if an error occurs reading from stream
     */
    private static byte[] readByteArray(final DataInputStream in) throws IOException {
        byte[] bytes = null;
        final int len = in.readInt();

        if (len >= 0) {
            bytes = new byte[len];
            in.readFully(bytes);
        }

        return bytes;
    }

    /**
     * Reads an Account object from an input stream.
     *
     * @param in the input stream to read from
     *
     * @return the Account object read from stream
     *
     * @throws AccountException if an error occurs reading from stream or instantiating
     *                          the object
     */
    public static Account read(final InputStream in) throws AccountException {
        final DataInputStream din = new DataInputStream(in);
        try (ClassPathXmlApplicationContext appContext =
            new ClassPathXmlApplicationContext("context.xml")) {
            final Account acct = appContext.getBean(Account.class);

            acct.setName(din.readUTF());
            acct.setPasswordHash(readByteArray(din));
            acct.setBalance(din.readInt());
            acct.setFullName(readString(din));
            acct.setPhone(readString(din));
            acct.setEmail(readString(din));
            return acct;
        } catch (final BeansException ex) {
            throw new AccountException("Unable to create account instance.", ex);
        } catch (final IOException ex) {
            throw new AccountException("Unable to read persisted account data.", ex);
        }
    }
}

