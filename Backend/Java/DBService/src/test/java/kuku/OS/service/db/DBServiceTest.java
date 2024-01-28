package kuku.OS.service.db;

import kuku.OS.Models.exceptions.user.UserAlreadySignedUpException;
import org.javatuples.Pair;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;


class DBServiceTest {
    DBService service = DBService.instance();

    @Test
    @Ignore
    public void getUserTest() {
        var user = service.getUser("test");
        Assert.assertEquals("test", user.getUserId());
    }

    @Test
    @Ignore
    public void createUserTest() {
        try {
            Pair<String, Boolean> result = service.createUser("testUser", "testPassword");
            Assert.assertTrue(result.getValue1());
        } catch (UserAlreadySignedUpException e) {
            Assert.assertTrue(true);
        }
    }
}