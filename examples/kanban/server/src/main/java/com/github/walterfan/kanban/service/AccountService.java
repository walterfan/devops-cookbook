package com.github.walterfan.kanban.service;


import com.github.walterfan.kanban.dao.AccountDao;
import com.github.walterfan.kanban.domain.Account;


import com.github.walterfan.msa.common.util.EncodeUtils;
import com.github.walterfan.msa.common.util.Encryptor;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
public class AccountService {
	
	private Encryptor encryptor;
	private AccountDao accountDao;


	public AccountService() {

	}
	
	public AccountDao getAccountDao() {
		return accountDao;
	}


	public void setAccountDao(AccountDao accountDao) {
		this.accountDao = accountDao;
	}

	public Encryptor getEncryptor() {
		return encryptor;
	}

	public void setEncryptor(Encryptor encryptor) {
		this.encryptor = encryptor;
	}

	public void createAccount(Account account) throws Exception {
		if(!account.isEncrypted()) {
			String pwd = account.getPassword();
			byte[] encryptedPwdBytes = this.encryptor.encrypt(pwd.getBytes());
			account.setPassword(EncodeUtils.byte2Hex(encryptedPwdBytes));
			account.setEncrypted(true);
		}

		accountDao.createAccount(account);

	}
	
	
	public Account retrieveAccount(int accountID) throws Exception {

		Account account = accountDao.getAccount(accountID);
		if(account.isEncrypted()) {
			String pwd = account.getPassword();
			byte[] decryptedPwdBytes = this.encryptor.decrypt(EncodeUtils.hex2Byte(pwd));
			String originPwd = new String(decryptedPwdBytes);
			account.setPassword(originPwd.trim());
			account.setEncrypted(false);
		}
		return account;
	}
	
	public void updateAccount(Account account) {
		accountDao.updateAccount(account);
	}
	
	public void deleteAccount(int accountID) {
		accountDao.deleteAccount(accountID);
	}
	
	public List<Account> listAccount(int pageSize, int pageNum, String orderField, boolean isAsc) {
		return accountDao.getAllAccount();
	}
	
	public List<Account> listAccount() {
		return accountDao.getAllAccount();
	}
	
	public List<Account> searchAccount(String searchField, String searchValue, boolean isAny) {
		return null;
	}
}
