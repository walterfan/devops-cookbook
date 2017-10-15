package com.github.walterfan;

import com.github.walterfan.msa.common.entity.User;
import com.github.walterfan.msa.common.entity.Bookmark;
import com.github.walterfan.msa.common.entity.Category;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created by yafan on 24/9/2017.
 */
@RunWith(SpringRunner.class)
@DataJpaTest
public class BookmarkRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    BookmarkRepository repository;

    private User createUser() {
        User user1 = new User();
        user1.setUsername("alice");
        user1.setEmail("aa@bb.cc");
        user1.setPassword("pass1");
        entityManager.persist(user1);
        return user1;
    }

    private Category createCategory() {
        Category category = new Category();
        category.setName("tools");
        return category;
    }

    private Bookmark createBookmark(User user1, Category category, String url) {
        Bookmark bookmark = new Bookmark();
        bookmark.setCategory(category);
        bookmark.setUser(user1);
        bookmark.setUrl(url);
        return bookmark;
    }

    @Test
    public void findNoBookmarksTest() {
        List<Bookmark> bookmarks = repository.findAll();

        assertTrue(bookmarks.isEmpty());
    }

    @Test
    public void findBookmarkByIdTest() {
        User user1 = createUser();

        Category category = createCategory();

        entityManager.persist(category);

        String url = "http://websequencediagrams.com";
        Bookmark bookmark = createBookmark(user1, category, url );

        Bookmark savedBookmark = repository.save(bookmark);

        Bookmark foundBookmark = repository.findOne(savedBookmark.getId());

        assertTrue(foundBookmark.equals(savedBookmark));
        assertTrue(foundBookmark.getUrl().equals(url));
    }



    @Test
    public void findBookmarkByCategoryTest() {
        User user1 = createUser();

        Category category = createCategory();

        entityManager.persist(category);

        String url = "http://qq.com";

        Bookmark bookmark = createBookmark(user1, category, url);

        Bookmark savedBookmark = repository.save(bookmark);

        List<Bookmark> foundBookmarks = repository.findByCategory(category);

        assertTrue(foundBookmarks.get(0).getUrl().equals(url));
    }



    @Test
    public void findBookmarkByUserTest() {
        User user1 = createUser();

        Category category = createCategory();

        entityManager.persist(category);

        String url = "http://xx.com";

        Bookmark bookmark = createBookmark(user1, category, url);

        Bookmark savedBookmark = repository.save(bookmark);

        List<Bookmark> foundBookmarks = repository.findByUser(user1);

        assertTrue(foundBookmarks.get(0).getUrl().equals(url));
    }

}
