package zmq;

import org.junit.Test;
import zmq.util.Utils;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

public class TestPeer
{
	
	public int serverRoutingId(SocketBase client, String address) {
		return ZMQ.connectPeer(client, address);
	}
	
	public boolean rc(SocketBase bind, String address) {
		return ZMQ.bind(bind, address);
	}
	
	public Msg getMsg(byte[] b) {
		return new Msg(b);
	}
	
	public Msg getMsg(Msg m) {
		return new Msg(m);
	}
	
	public int sizeOfSendMessageFromCl(SocketBase client, Msg msg, int num, String address) {
		msg.setRoutingId(serverRoutingId(client, address));
		return ZMQ.send(client, msg, 0);
	}
	
	
	public String recvMessageOnBind(Msg msg, SocketBase bind) {
		
		return new String(msg.data());
	}
	
	public int sizeOfSendMessageBackCl(SocketBase bind, Msg msg, int num) {
		int clientRoutingId = msg.getRoutingId();
		msg = new Msg("HELLO".getBytes());
		msg.setRoutingId(clientRoutingId);
		return ZMQ.send(bind, msg, 0);
	}
	
	public String recvMessageFromBind(SocketBase client, Msg msg) {
		msg = ZMQ.recv(client, 0);
		return new String(msg.data());
	}
	
	public Ctx getContext() {
        Ctx context = ZMQ.createContext();
        return context;
	}
	public SocketBase getBind() {
        SocketBase bind = ZMQ.socket(getContext(), ZMQ.ZMQ_PEER);
        return bind;
	}
	
	public SocketBase getClient() {
        SocketBase client = ZMQ.socket(getContext(), ZMQ.ZMQ_PEER);
        return client;
	}
	
	public void closeAndTerm(SocketBase client, SocketBase bind, Ctx context) {
        ZMQ.close(client);
        ZMQ.close(bind);
        ZMQ.term(context);
	}
	
	   public void assertions(String addressText) throws Exception
	    {

	        int port = Utils.findOpenPort();

	        assertThat(getContext(), notNullValue());

	        assertThat(getBind(), notNullValue());

	        assertThat(rc(getBind(), addressText), is(true));

	        assertThat(getClient(), notNullValue());
	        assertThat(serverRoutingId(getClient(), addressText), not(0));
	        // Send a message from client
	        assertThat(sizeOfSendMessageFromCl(getClient(), getMsg("X".getBytes()), 0, "tcp://localhost:" + port), is(1));


	        // Recv message on the bind side
	        assertThat(recvMessageOnBind(getMsg(ZMQ.recv(getBind(), 0)), getBind()), notNullValue());
	        assertThat(recvMessageOnBind(getMsg(ZMQ.recv(getBind(), 0)), getBind()), is("X"));

	        // Send message back to client using routing id
	        assertThat(sizeOfSendMessageBackCl(getBind(), getMsg("HELLO".getBytes()), 0), is(5));

	        // Client recv message from bind

	        assertThat(getMsg(ZMQ.recv(getClient(), 0)), notNullValue());
	        assertThat(recvMessageFromBind(getClient(), ZMQ.recv(getClient(), 0)), is("HELLO"));

	        closeAndTerm(getClient(), getBind(), getContext());
	    }
	
    @Test
    public void testInproc() throws Exception
    {
        System.out.println("Scenario 1");
        
        assertions("inproc://peer-to-peer");

    }

    @Test
    public void testTcp() throws Exception
    {
        System.out.println("Scenario 2");
  
        assertions("tcp://*:" + Utils.findOpenPort());
    }
    

    @Test
    public void testTcpDisconnect() throws Exception
    {
        System.out.println("Scenario 3");

        int port = Utils.findOpenPort();

        Ctx context = ZMQ.createContext();
        assertThat(context, notNullValue());

        // Create a bind
        SocketBase bind = ZMQ.socket(context, ZMQ.ZMQ_PEER);
        assertThat(bind, notNullValue());

        // bind bind
        boolean rc = ZMQ.bind(bind, "tcp://*:" + port);
        assertThat(rc, is(true));

        // create a client
        SocketBase client = ZMQ.socket(context, ZMQ.ZMQ_PEER);
        assertThat(client, notNullValue());
        int serverRoutingId = ZMQ.connectPeer(client, "tcp://localhost:" + port);
        assertThat(serverRoutingId, not(0));
        // Send a message from client
        Msg msg = new Msg("X".getBytes());
        msg.setRoutingId(serverRoutingId);
        int size = ZMQ.send(client, msg, 0);
        assertThat(size, is(1));

        // Recv message on the bind side
        msg = ZMQ.recv(bind, 0);
        assertThat(msg, notNullValue());
        assertThat(new String(msg.data()), is("X"));
        int clientRoutingId = msg.getRoutingId();
        assertThat(serverRoutingId, not(0));

        boolean disconnectResult = ZMQ.disconnectPeer(bind, clientRoutingId);
        assertThat(disconnectResult, is(true));

        // Send message back to client fails after disconnect
        msg = new Msg("HELLO".getBytes());
        msg.setRoutingId(clientRoutingId);
        size = ZMQ.send(bind, msg, 0);
        assertThat(size, is(-1));

        ZMQ.close(client);
        ZMQ.close(bind);
        ZMQ.term(context);
    }
}
