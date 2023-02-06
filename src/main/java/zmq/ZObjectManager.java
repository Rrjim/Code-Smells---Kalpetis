package zmq;

import zmq.io.IEngine;
import zmq.pipe.Pipe;
import zmq.pipe.YPipeBase;

public class ZObjectManager {

    @SuppressWarnings("unchecked")
    final void processCommand(Command cmd)
    {
        //        System.out.println(Thread.currentThread().getName() + ": Processing command " + cmd);
        switch (cmd.type) {
        case ACTIVATE_READ:
            processActivateRead();
            break;

        case ACTIVATE_WRITE:
            processActivateWrite((Long) cmd.arg);
            break;

        case STOP:
            processStop();
            break;

        case PLUG:
            processPlug();
            processSeqnum();
            break;

        case OWN:
            processOwn((Own) cmd.arg);
            processSeqnum();
            break;

        case ATTACH:
            processAttach((IEngine) cmd.arg);
            processSeqnum();
            break;

        case BIND:
            processBind((Pipe) cmd.arg);
            processSeqnum();
            break;

        case HICCUP:
            processHiccup((YPipeBase<Msg>) cmd.arg);
            break;

        case PIPE_TERM:
            processPipeTerm();
            break;

        case PIPE_TERM_ACK:
            processPipeTermAck();
            break;

        case TERM_REQ:
            processTermReq((Own) cmd.arg);
            break;

        case TERM:
            processTerm((Integer) cmd.arg);
            break;

        case TERM_ACK:
            processTermAck();
            break;

        case REAP:
            processReap((SocketBase) cmd.arg);
            break;

        case REAP_ACK:
            processReapAck();
            break;

        case REAPED:
            processReaped();
            break;

        case INPROC_CONNECTED:
            processSeqnum();
            break;

        case CANCEL:
            processCancel();
            break;

        case DONE:
        default:
            throw new IllegalArgumentException();
        }
    }
    
    protected void processStop()
    {
        throw new UnsupportedOperationException();
    }

    protected void processPlug()
    {
        throw new UnsupportedOperationException();
    }

    protected void processOwn(Own object)
    {
        throw new UnsupportedOperationException();
    }

    protected void processAttach(IEngine engine)
    {
        throw new UnsupportedOperationException();
    }

    protected void processBind(Pipe pipe)
    {
        throw new UnsupportedOperationException();
    }

    protected void processActivateRead()
    {
        throw new UnsupportedOperationException();
    }

    protected void processActivateWrite(long msgsRead)
    {
        throw new UnsupportedOperationException();
    }

    protected void processHiccup(YPipeBase<Msg> hiccupPipe)
    {
        throw new UnsupportedOperationException();
    }

    protected void processPipeTerm()
    {
        throw new UnsupportedOperationException();
    }

    protected void processPipeTermAck()
    {
        throw new UnsupportedOperationException();
    }

    protected void processTermReq(Own object)
    {
        throw new UnsupportedOperationException();
    }

    protected void processTerm(int linger)
    {
        throw new UnsupportedOperationException();
    }

    protected void processTermAck()
    {
        throw new UnsupportedOperationException();
    }

    protected void processReap(SocketBase socket)
    {
        throw new UnsupportedOperationException();
    }

    protected void processReapAck()
    {
    }

    protected void processReaped()
    {
        throw new UnsupportedOperationException();
    }

    //  Special handler called after a command that requires a seqnum
    //  was processed. The implementation should catch up with its counter
    //  of processed commands here.
    protected void processSeqnum()
    {
        throw new UnsupportedOperationException();
    }

    protected void processCancel()
    {
    }

	
}
