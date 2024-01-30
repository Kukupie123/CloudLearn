package kuku.OS.util;

public class ErrorMsgGenerator {
    private ErrorMsgGenerator() {
    }

    public static String generateErrorString(Throwable e) {
        String msg = e.getMessage();
        msg = msg + "---TYPE= " + e.getClass().toString();
        for (var s : e.getStackTrace()) {
            msg = msg + "------" + s.toString();
        }
        return msg;
    }
}
