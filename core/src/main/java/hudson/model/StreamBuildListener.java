package hudson.model;

import hudson.CloseProofOutputStream;
import hudson.remoting.RemoteOutputStream;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

/**
 * {@link BuildListener} that writes to an {@link OutputStream}.
 *
 * This class is remotable.
 * 
 * @author Kohsuke Kawaguchi
 */
public class StreamBuildListener implements BuildListener, Serializable {
    private PrintWriter w;

    private PrintStream ps;

    public StreamBuildListener(OutputStream w) {
        this(new PrintStream(w));
    }

    public StreamBuildListener(PrintStream w) {
        this(w,null);
    }

    public StreamBuildListener(PrintStream w, Charset charset) {
        this.ps = w;
        // unless we auto-flash, PrintStream will use BufferedOutputStream internally,
        // and break ordering
        this.w = new PrintWriter(new BufferedWriter(
                charset==null ? new OutputStreamWriter(w) : new OutputStreamWriter(w,charset)), true);
    }

    public void started() {
        w.println("started");
    }

    public PrintStream getLogger() {
        return ps;
    }

    public PrintWriter error(String msg) {
        w.println("ERROR: "+msg);
        return w;
    }

    public PrintWriter error(String format, Object... args) {
        return error(String.format(format,args));
    }

    public PrintWriter fatalError(String msg) {
        w.println("FATAL: "+msg);
        return w;
    }

    public PrintWriter fatalError(String format, Object... args) {
        return fatalError(String.format(format,args));
    }

    public void finished(Result result) {
        w.println("finished: "+result);
    }


    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(new RemoteOutputStream(new CloseProofOutputStream(ps)));
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        ps = new PrintStream((OutputStream)in.readObject(),true);
        w = new PrintWriter(ps,true);
    }

    private static final long serialVersionUID = 1L;
}
