package kuku.OS.service.db;

import org.junit.*;


class DBServiceTest {

    @Test
    @Ignore
    void getUser() {

        var service = DBService.instance();
        //service.setupConnectionTEST("TESTUSERNAME", "TESTPASSWORD");
        var user = service.getUser("test");
        Assert.assertEquals("test", user.getUserID());
    }
}