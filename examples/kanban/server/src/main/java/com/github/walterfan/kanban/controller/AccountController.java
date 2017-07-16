package com.github.walterfan.kanban.controller;


import com.github.walterfan.kanban.domain.Account;
import com.github.walterfan.kanban.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;


@RestController
@RequestMapping("/api/v1")
public class AccountController {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private AccountService accountService;


    @RequestMapping(value = "/accounts", method = RequestMethod.POST)
    public Account createAccount(@RequestBody Account account) throws Exception {
        logger.info("got post request: " + account.toString());
        accountService.createAccount(account);
        return account;
    }

    @RequestMapping(value = "/accounts", method = RequestMethod.GET)
    public List<Account> getAccounts() {
        logger.info("got all accounts request");
        List<Account> AccountList = accountService.listAccount();
        return AccountList;
    }

    @RequestMapping(value = "accounts/{id}", method = GET)
    public Account getAccount(@PathVariable("id") int accountId) throws Exception {
        return accountService.retrieveAccount(accountId);
    }


    @RequestMapping(value = "accounts/{id}", method = PUT)
    public Account updateAccount(@PathVariable("id") int accountId, @RequestBody Account account) {
        account.setAccountID(accountId);
        accountService.updateAccount(account);
        return account;
    }

    @RequestMapping(value = "accounts/{id}", method = DELETE)
    public void deleteAccount(@PathVariable("id") int accountId) {
        accountService.deleteAccount(accountId);

    }
}
