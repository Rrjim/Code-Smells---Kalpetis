package zmq;

import zmq.io.IEngine;
import zmq.io.IOThread;
import zmq.io.SessionBase;
import zmq.pipe.Pipe;
import zmq.pipe.YPipeBase;

//  Base class for all objects that participate in inter-thread
//  communication.
public abstract class ZObject extends ZObjectManager
{
    //  Context provides access to the global state.
    private final Ctx ctx;

    //  Thread ID of the thread the object belongs to.
    private int tid;

    protected ZObject(Ctx ctx, int tid)
    {
        this.ctx = ctx;
        this.tid = tid;
    }

    protected ZObject(ZObject parent)
    {
        this(parent.ctx, parent.tid);
    }

    public final int getTid()
    {
        return tid;
    }

    protected final void setTid(int tid)
    {
        this.tid = tid;
    }

    protected final Ctx getCtx()
    {
        return ctx;
    }


    protected final boolean registerEndpoint(String addr, Ctx.Endpoint endpoint)
    {
        return ctx.registerEndpoint(addr, endpoint);
    }

    protected final boolean unregisterEndpoint(String addr, SocketBase socket)
    {
        return ctx.unregisterEndpoint(addr, socket);
    }

    protected final void unregisterEndpoints(SocketBase socket)
    {
        ctx.unregisterEndpoints(socket);
    }

    protected final Ctx.Endpoint findEndpoint(String addr)
    {
        return ctx.findEndpoint(addr);
    }

    protected final void pendConnection(String addr, Ctx.Endpoint endpoint, Pipe[] pipes)
    {
        ctx.pendConnection(addr, endpoint, pipes);
    }

    protected final void connectPending(String addr, SocketBase bindSocket)
    {
        ctx.connectPending(addr, bindSocket);
    }

    protected final void destroySocket(SocketBase socket)
    {
        ctx.destroySocket(socket);
    }

    //  Chooses least loaded I/O thread.
    protected final IOThread chooseIoThread(long affinity)
    {
        return ctx.chooseIoThread(affinity);
    }

    protected final void sendStop()
    {
        //  'stop' command goes always from administrative thread to
        //  the current object.
        Command cmd = new Command(this, Command.Type.STOP);
        ctx.sendCommand(tid, cmd);
    }

    protected final void sendPlug(Own destination)
    {
        sendPlug(destination, true);
    }

    protected final void sendPlug(Own destination, boolean incSeqnum)
    {
        if (incSeqnum) {
            destination.incSeqnum();
        }

        Command cmd = new Command(destination, Command.Type.PLUG);
        sendCommand(cmd);
    }

    protected final void sendOwn(Own destination, Own object)
    {
        destination.incSeqnum();
        Command cmd = new Command(destination, Command.Type.OWN, object);
        sendCommand(cmd);
    }

    protected final void sendAttach(SessionBase destination, IEngine engine)
    {
        sendAttach(destination, engine, true);
    }

    protected final void sendAttach(SessionBase destination, IEngine engine, boolean incSeqnum)
    {
        if (incSeqnum) {
            destination.incSeqnum();
        }

        Command cmd = new Command(destination, Command.Type.ATTACH, engine);
        sendCommand(cmd);
    }

    protected final void sendBind(Own destination, Pipe pipe)
    {
        sendBind(destination, pipe, true);
    }

    protected final void sendBind(Own destination, Pipe pipe, boolean incSeqnum)
    {
        if (incSeqnum) {
            destination.incSeqnum();
        }

        Command cmd = new Command(destination, Command.Type.BIND, pipe);
        sendCommand(cmd);
    }

    protected final void sendActivateRead(Pipe destination)
    {
        Command cmd = new Command(destination, Command.Type.ACTIVATE_READ);
        sendCommand(cmd);
    }

    protected final void sendActivateWrite(Pipe destination, long msgsRead)
    {
        Command cmd = new Command(destination, Command.Type.ACTIVATE_WRITE, msgsRead);
        sendCommand(cmd);
    }

    protected final void sendHiccup(Pipe destination, YPipeBase<Msg> pipe)
    {
        Command cmd = new Command(destination, Command.Type.HICCUP, pipe);
        sendCommand(cmd);
    }

    protected final void sendPipeTerm(Pipe destination)
    {
        Command cmd = new Command(destination, Command.Type.PIPE_TERM);
        sendCommand(cmd);
    }

    protected final void sendPipeTermAck(Pipe destination)
    {
        Command cmd = new Command(destination, Command.Type.PIPE_TERM_ACK);
        sendCommand(cmd);
    }

    protected final void sendTermReq(Own destination, Own object)
    {
        Command cmd = new Command(destination, Command.Type.TERM_REQ, object);
        sendCommand(cmd);
    }

    protected final void sendTerm(Own destination, int linger)
    {
        Command cmd = new Command(destination, Command.Type.TERM, linger);
        sendCommand(cmd);
    }

    protected final void sendTermAck(Own destination)
    {
        Command cmd = new Command(destination, Command.Type.TERM_ACK);
        sendCommand(cmd);
    }

    protected final void sendReap(SocketBase socket)
    {
        Command cmd = new Command(ctx.getReaper(), Command.Type.REAP, socket);
        sendCommand(cmd);
    }

    protected final void sendReapAck()
    {
        Command cmd = new Command(this, Command.Type.REAP_ACK);
        sendCommand(cmd);
    }

    protected final void sendReaped()
    {
        Command cmd = new Command(ctx.getReaper(), Command.Type.REAPED);
        sendCommand(cmd);
    }

    protected final void sendInprocConnected(SocketBase socket)
    {
        Command cmd = new Command(socket, Command.Type.INPROC_CONNECTED);
        sendCommand(cmd);
    }

    protected final void sendDone()
    {
        Command cmd = new Command(null, Command.Type.DONE);
        ctx.sendCommand(Ctx.TERM_TID, cmd);
    }

    protected final void sendCancel()
    {
        Command cmd = new Command(this, Command.Type.CANCEL);
        sendCommand(cmd);
    }

    private void sendCommand(Command cmd)
    {
        ctx.sendCommand(cmd.destination.getTid(), cmd);
    }
    
    
}
