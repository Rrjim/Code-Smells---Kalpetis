package zmq.io.net.tcp;

import java.io.IOException;
import java.net.SocketOption;
import java.net.StandardSocketOptions;
import java.nio.channels.Channel;
import java.nio.channels.NetworkChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;

import zmq.ZError;
import zmq.io.net.Address;
import zmq.util.Utils;

public class TcpUtils
{
    public static final boolean WITH_EXTENDED_KEEPALIVE = SocketOptionsProvider.WITH_EXTENDED_KEEPALIVE;

    @SuppressWarnings("unchecked")
    private static final class SocketOptionsProvider
    {
        // Wrapped in an inner class, to avoid the @SuppressWarnings for the whole class
        private static final SocketOption<Integer> TCP_KEEPCOUNT;
        private static final SocketOption<Integer> TCP_KEEPIDLE;
        private static final SocketOption<Integer> TCP_KEEPINTERVAL;
        private static final boolean WITH_EXTENDED_KEEPALIVE;
        static {
            SocketOption<Integer> tryCount = null;
            SocketOption<Integer> tryIdle = null;
            SocketOption<Integer> tryInterval = null;

            boolean extendedKeepAlive = false;
            try {
                Class<?> eso = TcpUtils.class.getClassLoader().loadClass("jdk.net.ExtendedSocketOptions");
                tryCount = (SocketOption<Integer>) eso.getField("TCP_KEEPCOUNT").get(null);
                tryIdle = (SocketOption<Integer>) eso.getField("TCP_KEEPIDLE").get(null);
                tryInterval = (SocketOption<Integer>) eso.getField("TCP_KEEPINTERVAL").get(null);
                extendedKeepAlive = true;
            }
            catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
                // If failing, will keep extendedKeepAlive to false
            }
            TCP_KEEPCOUNT = tryCount;
            TCP_KEEPIDLE = tryIdle;
            TCP_KEEPINTERVAL = tryInterval;
            WITH_EXTENDED_KEEPALIVE = extendedKeepAlive;
        }
   }

    private TcpUtils()
    {
    }

    // The explicit IOException is useless, but kept for API compatibility
    public static void tuneTcpSocket(Channel channel) throws IOException
    {
        // Disable Nagle's algorithm. We are doing data batching on 0MQ level,
        // so using Nagle wouldn't improve throughput in any way, but it would
        // hurt latency.
        setOption(channel, StandardSocketOptions.TCP_NODELAY, true);
    }

    public static void tuneTcpKeepalives(Channel channel, int tcpKeepAlive, int tcpKeepAliveCnt,
            int tcpKeepAliveIdle, int tcpKeepAliveIntvl)
    {
        if (tcpKeepAlive != -1) {
            if (channel instanceof SocketChannel) {
                setOption(channel, StandardSocketOptions.SO_KEEPALIVE, tcpKeepAlive == 1);
            }
            if (WITH_EXTENDED_KEEPALIVE && tcpKeepAlive == 1) {
                if (tcpKeepAliveCnt > 0) {
                    setOption(channel, SocketOptionsProvider.TCP_KEEPCOUNT, tcpKeepAliveCnt);
                }
                if (tcpKeepAliveIdle > 0) {
                    setOption(channel, SocketOptionsProvider.TCP_KEEPIDLE, tcpKeepAliveIdle);
                }
                if (tcpKeepAliveIntvl > 0) {
                    setOption(channel, SocketOptionsProvider.TCP_KEEPINTERVAL, tcpKeepAliveIntvl);
                }
            }
        }
    }

    public static boolean setTcpReceiveBuffer(Channel channel, final int rcvbuf)
    {
        setOption(channel, StandardSocketOptions.SO_RCVBUF, rcvbuf);
        return true;
    }

    public static boolean setTcpSendBuffer(Channel channel, final int sndbuf)
    {
        setOption(channel, StandardSocketOptions.SO_SNDBUF, sndbuf);
        return true;
    }

    public static boolean setIpTypeOfService(Channel channel, final int tos)
    {
        setOption(channel, StandardSocketOptions.SO_SNDBUF, tos);
        return true;
    }

    public static boolean setReuseAddress(Channel channel, final boolean reuse)
    {
        setOption(channel, StandardSocketOptions.SO_REUSEADDR, reuse);
        return true;
    }

    private static <T> void setOption(Channel channel, SocketOption<T> option, T value)
    {
        try {
            if (channel instanceof NetworkChannel) {
                ((NetworkChannel) channel).setOption(option, value);
            }
            else {
                throw new IllegalArgumentException("Channel " + channel + " is not a network channel");
            }
        }
        catch (IOException e) {
            throw new ZError.IOException(e);
        }
    }

    public static void unblockSocket(SelectableChannel... channels) throws IOException
    {
        for (SelectableChannel ch : channels) {
            ch.configureBlocking(false);
        }
    }

    public static void enableIpv4Mapping(SelectableChannel channel)
    {
        // TODO V4 enable ipv4 mapping
    }

    /**
     * Return the {@link  Address} of the channel
     * @param channel the channel, should be a TCP socket channel
     * @return The {@link Address} of the channel
     * @deprecated Use {@link zmq.util.Utils#getPeerIpAddress(SocketChannel)} instead
     * @throws ZError.IOException if the channel is closed or an I/O errors occurred
     * @throws IllegalArgumentException if the SocketChannel is not a TCP channel
     */
    @Deprecated
    public static Address getPeerIpAddress(SocketChannel channel)
    {
        return Utils.getPeerIpAddress(channel);
    }
}
