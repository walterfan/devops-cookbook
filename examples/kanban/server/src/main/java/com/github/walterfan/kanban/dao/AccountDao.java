package com.github.walterfan.kanban.dao;

import com.github.walterfan.kanban.domain.Account;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface AccountDao {
	 
	@Select("SELECT * FROM account WHERE accountID = #{accountID}")
	Account getAccount(@Param("accountID") int accountID);
	
	@Select("SELECT * FROM account")
	List<Account> getAllAccount();


	@Insert("INSERT into account(username,password,sitename,siteurl,email, isEncrypted) " +
			"VALUES(#{userName}, #{password}, #{siteName}, #{siteUrl},  #{email}, #{isEncrypted})")
	void createAccount(Account account);

	@Update("UPDATE account SET userName=#{userName}, password =#{password}, siteName=#{siteName} , " +
			"siteUrl=#{siteUrl}, email=#{email} , isEncrypted=#{isEncrypted} WHERE accountID =#{accountID}")
	void updateAccount(Account account);

	@Delete("DELETE FROM account WHERE accountID =#{accountID}")
	void deleteAccount(int accountID);


}
