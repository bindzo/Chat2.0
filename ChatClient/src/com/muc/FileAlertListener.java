package com.muc;

import java.io.IOException;

public interface FileAlertListener {

    void onFileAlert(String login, String fileName) throws IOException;
}
