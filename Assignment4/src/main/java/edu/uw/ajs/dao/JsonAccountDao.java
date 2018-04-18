package main.java.edu.uw.ajs.dao;

import java.io.File;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import edu.uw.ext.framework.account.Account;
import edu.uw.ext.framework.account.AccountException;
import edu.uw.ext.framework.account.Address;
import edu.uw.ext.framework.account.CreditCard;
import edu.uw.ext.framework.dao.AccountDao;
import main.java.edu.uw.ajs.account.SimpleAccount;
import main.java.edu.uw.ajs.account.SimpleAddress;
import main.java.edu.uw.ajs.account.SimpleCreditCard;

public class JsonAccountDao implements AccountDao {

	String ACCOUNTFILENAME = "_Account.json";
	ObjectMapper mapper = new ObjectMapper();
	final File accountDir = new File("target", "/accounts");
	private static final Logger logger = LoggerFactory.getLogger(AccountDao.class);

	JsonAccountDao() {
		final SimpleModule module = new SimpleModule();
		module.addAbstractTypeMapping(Account.class, SimpleAccount.class);
		module.addAbstractTypeMapping(Address.class, SimpleAddress.class);
		module.addAbstractTypeMapping(CreditCard.class, SimpleCreditCard.class);
		mapper = new ObjectMapper();
		mapper.registerModule(module);

	}

	public Account getAccount(final String accountInput) {

		Account account = null;


		if (accountDir.exists() && this.accountDir.isDirectory()) {
			try {

				final File accountNameInput = new File(this.accountDir, accountInput + ACCOUNTFILENAME);
				logger.info(accountNameInput.getAbsolutePath());

				account = mapper.readValue(accountNameInput, Account.class);
			} catch (final IOException ex) {
				logger.info("Account unable to be found... " + accountInput + ACCOUNTFILENAME);
			}
		}

		return account;
	}

	public void setAccount(Account account) {

		String accountName = account.getName();

		if (!this.accountDir.exists()) {
			logger.info("Directory does not exist...attempting to create directory " + this.accountDir);
			this.accountDir.mkdirs();
		}

		final File accountOutput = new File(this.accountDir, accountName + ACCOUNTFILENAME);

		if (accountOutput.exists()) {
			logger.info("File exists, deleting file.... " + accountOutput);
			deleteFile(accountOutput);

		}

		try {
			mapper.writerWithDefaultPrettyPrinter().writeValue(accountOutput, account);
			logger.info("File " + accountOutput + " created and populated.");
		} catch (JsonGenerationException e) {
			logger.info("File unable to be written to ");
			e.printStackTrace();
		} catch (JsonMappingException e) {
			logger.info("File unable to be mapped ");
			e.printStackTrace();
		} catch (IOException e) {
			logger.info("File unable to find file.... Now creating ");
			e.printStackTrace();
		}

	}

	@Override
	public void deleteAccount(String accountName) throws AccountException {

		logger.info("Delete file has been requested for... " + accountName);

		final File account = new File(this.accountDir, accountName + ACCOUNTFILENAME);
		logger.info("Delete file has been requested for... " + account);

		if (!account.isDirectory()) {
			logger.info("NOT DIRECTORY DELETE " + account + ACCOUNTFILENAME);
			deleteFile(account);
			logger.info("DELETE SUCCESS? " + account.exists());

		} else {
			File[] directories = account.listFiles();
			for (int i = 0; i < directories.length; i++) {
				File fileDirectories = directories[i];
				logger.info("DIRECTORY DELETE " + fileDirectories.getName());
				logger.info(account.getName());

				deleteAccount(account.getName() + "/" + fileDirectories.getName());
				logger.info("DELETE SUCCESS? " + fileDirectories.exists());

			}
		}

	}

	@Override
	public void reset() throws AccountException {
		logger.info("RESETTING FOLDER");
		File dir = accountDir;
		logger.info("FILE DIRECTORY FOLDER " + dir);
		if (!dir.isDirectory()) {
			logger.info("This folder was already deleted.");
		} else {

			File[] files = dir.listFiles();
			for (int i = 0; i < files.length; i++) {
				File file = files[i];
				logger.info("DELETE FILE " + file.getName());
				file.delete();
			}
		}
	}

	@Override
	public void close() throws AccountException {
		// TODO Auto-generated method stub

	}

	private void deleteFile(File file) {
		logger.info("DELETE FILE ");

		if (file.exists()) {
			if (!file.isDirectory()) {
				logger.info("DELETE FILE " + file);

				file.delete();

				logger.info("DELETED FILE EXISTS " + (file.exists()));

			}
		}
	}
}
