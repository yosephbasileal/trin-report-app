package com.trinreport.m.app.tor;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import cz.msebera.android.httpclient.HttpHost;
import cz.msebera.android.httpclient.conn.socket.ConnectionSocketFactory;
import cz.msebera.android.httpclient.protocol.HttpContext;


/**
 * Tor connection socket library
 * Source: https://github.com/jehy/Tor-Onion-Proxy-Library
 */
public class MyConnectionSocketFactory implements ConnectionSocketFactory {

    @Override
    public Socket createSocket(final HttpContext context) throws IOException {
        return new Socket();
    }

    @Override
    public Socket connectSocket(
            int connectTimeout,
            Socket socket,
            final HttpHost host,
            final InetSocketAddress remoteAddress,
            final InetSocketAddress localAddress,
            final HttpContext context) throws IOException {

        InetSocketAddress socksaddr = (InetSocketAddress) context.getAttribute("socks.address");
        socket = new Socket();
        connectTimeout = 100000;
        socket.setSoTimeout(connectTimeout);
        socket.connect(new InetSocketAddress(socksaddr.getHostName(),
                socksaddr.getPort()), connectTimeout);


        DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
        outputStream.write((byte) 0x04);
        outputStream.write((byte) 0x01);
        outputStream.writeShort((short) host.getPort());
        outputStream.writeInt(0x01);
        outputStream.write((byte) 0x00);
        outputStream.write(host.getHostName().getBytes());
        outputStream.write((byte) 0x00);

        DataInputStream inputStream = new DataInputStream(socket.getInputStream());
        if (inputStream.readByte() != (byte) 0x00 || inputStream.readByte() != (byte) 0x5a) {
            throw new IOException("SOCKS4a connect failed");
        } else
            Log.v("SSLConnectionSF", "SOCKS4a connect ok!");
        inputStream.readShort();
        inputStream.readInt();
        return socket;
    }
}