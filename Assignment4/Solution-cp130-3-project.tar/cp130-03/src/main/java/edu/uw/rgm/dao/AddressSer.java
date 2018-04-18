package edu.uw.rgm.dao;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.springframework.beans.BeansException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import edu.uw.ext.framework.account.AccountException;
import edu.uw.ext.framework.account.Address;


/**
 * Class for serializing instances of classes that implement the Address
 * interface.  Implementation uses a Properties file.
 *
 * @author Russ Moul
 */
public final class AddressSer {
    /** Name for the streetAddress property. */
    private static final String STREET_ADDRESS_PROP_NAME = "streetAddress";

    /** Name for the city property. */
    private static final String STREET_CITY_PROP_NAME = "city";

    /** Name for the state property. */
    private static final String STREET_STATE_PROP_NAME = "state";

    /** Name for the zipCode property. */
    private static final String STREET_ZIP_CODE_PROP_NAME = "zipCode";

    /**
     * Utility class - disable constructor.
     */
    private AddressSer() {
        // no-op
    };

    /**
     * Writes an Address object to an output stream.
     *
     * @param out the output stream to write to
     * @param addr the Address object to write
     *
     * @throws AccountException if an error occurs writing to stream
     */
    public static void write(final OutputStream out, final Address addr)
        throws AccountException {

        final Properties props = new Properties();
        if (addr != null) {
            String tmp;
            tmp = addr.getStreetAddress();
            if (tmp != null) {
                props.put(STREET_ADDRESS_PROP_NAME, tmp);
            }

            tmp = addr.getCity();
            if (tmp != null) {
                props.put(STREET_CITY_PROP_NAME, tmp);
            }

            tmp = addr.getState();
            if (tmp != null) {
                props.put(STREET_STATE_PROP_NAME, tmp);
            }

            tmp = addr.getZipCode();
            if (tmp != null) {
                props.put(STREET_ZIP_CODE_PROP_NAME, tmp);
            }
        }
        try {
            props.store(out, "Address data");
        } catch (final IOException ex) {
            throw new AccountException("Failed to write address data.", ex);
        }

    }


    /**
     * Reads an Address object from an input stream.
     *
     * @param in the input stream to read from
     *
     * @return the Address object read from stream
     *
     * @throws AccountException if an error occurs reading from stream or 
     *                          instantiating the object
     */
    public static Address read(final InputStream in) throws AccountException {
        final Properties props = new Properties();
        try (ClassPathXmlApplicationContext appContext =
            new ClassPathXmlApplicationContext("context.xml")) {
            props.load(in);
    
            final Address addr = appContext.getBean(Address.class);
            addr.setStreetAddress(props.getProperty(STREET_ADDRESS_PROP_NAME));
            addr.setCity(props.getProperty(STREET_CITY_PROP_NAME));
            addr.setState(props.getProperty(STREET_STATE_PROP_NAME));
            addr.setZipCode(props.getProperty(STREET_ZIP_CODE_PROP_NAME));
            return addr;
        } catch (final BeansException ex) {
            throw new AccountException("Unable to create address instance.", ex);
        } catch (final IOException ex) {
            throw new AccountException("Unable to read persisted address data.", ex);
        }

    }
}

