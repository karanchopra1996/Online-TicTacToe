import com.jcraft.jsch.*;
import java.io.*;
import java.net.*;

/**
 * Connection establishes a secured TCP connection from a user local to a
 * given remote machine and maintains a pair of ObjectInput and ObjectOutput
 * Streams with that remote machine.
 *
 * @author  Munehiro Fukuda
 * @version %I% %G%
 * @since   1.0
 */

public class Connection {
    public ObjectInputStream in = null;   // an output to a remote machiine
    public ObjectOutputStream out = null; // an input from a remote machine
    
    private final int JschPort = 22;
	public char[] hostname;
    private Session session = null;
    private Channel channel= null;

    /** 
     * This is a contructor executed at a user-local machine to set up a 
     * connection to each of remote machines.
     * 
     * @param username your user account name
     * @param password the password corresponding to username
     * @param hostname the name of a remote host computer to connect to
     * @param command  the remote java program to execute
     */
    public Connection( String username, String password, String hostname,
		       String command ) {
	try {
	    // ssh2 connection to a slave server
	    JSch jsch = new JSch( );
	    session = jsch.getSession( username, hostname, JschPort );
	    
	    // username and password will be given via UserInfo interface
	    UserInfo ui = new MyUserInfo( password );
	    session.setUserInfo( ui );
	    session.connect( );
	    System.err.println( "ssh2 connection to " + hostname );
	    
	    // execute a remote process
	    channel = session.openChannel( "exec" );
	    ( (ChannelExec)channel ).setCommand( command );
	    channel.connect( );
	    
	    // set up a pair of ObjectInputStream and ObjectOutputStream.
	    out = new ObjectOutputStream( channel.getOutputStream( ) );
	    out.flush( );
	    in = new ObjectInputStream( channel.getInputStream( ) );
	} catch( Exception e ) {
	    System.err.println( "upon a ssh2 connection to " + hostname );
	    e.printStackTrace( );
	    System.exit( -1 );
	}
    }

    /**
     * This is a constructor execute at a remote machine to receive a 
     * connection from a user-local machine.
     */
    public Connection( ) { 
	try {
	    // set up a pair of ObjectInput/OutputStream with a user-local
	    // machine.
	    out = new ObjectOutputStream( System.out );
	    in  = new ObjectInputStream( System.in );
	} catch( Exception e ) {
	}
    }

    /**
     * close() is called at a user-local machine to close the ssh connection 
     * with a given remote machine.
     */
    public void close( ) {
	try {
	    if ( session != null )
		session.disconnect( );
	} catch( Exception e ) {
	    System.err.println( );
	    e.printStackTrace( );
	    System.exit( -1 );
	}
    }

    /**
     * MyUserInfo stores a user account and password information. It is
     * used in libssh to establish a secured TCP connection from a user-local 
     * to a remote machine.
     */
    private class MyUserInfo implements UserInfo {
	// Private data members
	private String _passwd = null; // Users password
	
	// Constructor sets up password
	public MyUserInfo( String passwd )
	{
	    this._passwd = passwd;
	}
	
	// Because passphrase does not apply use null
	public String getPassphrase( ) { return null; };
	// Returns the password of the user
	public String getPassword( ) { return _passwd; };
	// You may only set password during construction of UserInfo
	public boolean promptPassword( String Message ) { return true; };
	// Because passphrase does not apply this function simply returns true
	public boolean promptPassphrase( String message ) { return true; };
	// Because this application runs remotely, no need to prompt the user
	public boolean promptYesNo( String message ) { return true; };
	public void showMessage( String message ) { };
    }
}