package com.github.walterfan;


import java.util.List;
import java.util.UUID;

import com.github.walterfan.msa.common.entity.Bookmark;
import com.github.walterfan.msa.common.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping("/api/v1/bookmarks")
public class BookmarkController {

    private User user;


    public BookmarkController() {
        //TODO: get user from session
        user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setEmail("walter.fan@gmail.com");
        user.setUsername("walterfan");
    }

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @RequestMapping(method=RequestMethod.GET, value="/fail")
    public void fail() {
        throw new RuntimeException();
    }

    @ExceptionHandler(value=RuntimeException.class)
    @ResponseStatus(value=HttpStatus.BANDWIDTH_LIMIT_EXCEEDED)
    public String error() {
        return "error";
    }


    @RequestMapping(method=RequestMethod.GET)
    public List<Bookmark> userBookmarks( Model model) {
        return bookmarkRepository.findByUser(user);
    }

    @RequestMapping(method=RequestMethod.POST)
    public Bookmark createBookmark(@ModelAttribute Bookmark bookmark) {
        bookmark.setUser(user);
        return bookmarkRepository.save(bookmark);

    }

}
