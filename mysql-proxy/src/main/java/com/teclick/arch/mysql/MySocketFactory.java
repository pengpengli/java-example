package com.teclick.arch.mysql;

import com.mysql.jdbc.StandardSocketFactory;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Properties;

/**
 * Created by pengli on 2017-11-06.
 */
public class MySocketFactory extends StandardSocketFactory {

    @Override
    public Socket connect(String hostname, int portNumber, Properties props) throws SocketException, IOException {
        return super.connect(hostname, portNumber, props);
    }

}
