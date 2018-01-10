package com.teclick.arch.mysql;

import com.mysql.jdbc.StandardSocketFactory;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Properties;

/**
 * Created by 581854 on 2017-11-06 13:56.
 */
public class MySocketFactory extends StandardSocketFactory {

    @Override
    public Socket connect(String hostname, int portNumber, Properties props) throws SocketException, IOException {
        return super.connect(hostname, portNumber, props);
    }

}
