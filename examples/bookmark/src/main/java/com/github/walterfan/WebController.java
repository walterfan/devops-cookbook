package com.github.walterfan;

/**
 * Created by yafan on 30/9/2017.
 */

import java.util.List;

import com.github.walterfan.msa.common.entity.Bookmark;
import com.github.walterfan.msa.common.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/")
public class WebController {
    private Logger logger = LoggerFactory.getLogger(WebController.class);
    private User defaultUser;

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @RequestMapping(method=RequestMethod.GET)
    public String getBookmarks(Model model) {

        List<Bookmark> bookmarks = bookmarkRepository.findByUser(defaultUser);
        if (bookmarks != null) {
            model.addAttribute("bookmarks", bookmarks);
        }
        model.addAttribute("bookmark", new Bookmark());
        return "bookmark";
    }

    @RequestMapping(value = "/bookmarks", method=RequestMethod.POST)
    public String addBookmark(@ModelAttribute Bookmark bookmark) {
        logger.info("add bookmark: " + bookmark.toString());
        bookmark.setUser(defaultUser);
        bookmarkRepository.save(bookmark);
        return "redirect:/";
    }

}

