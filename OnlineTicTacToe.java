import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

/**
 * @author Karan Chopra
 */
public class OnlineTicTacToe implements ActionListener {

    private static int isAuto = 0;
    private static int i = 0;
    private final int INTERVAL = 1000;         // 1 second
    private final int NBUTTONS = 9;            // #bottons
    private final JButton[] button = new JButton[NBUTTONS]; // button[0] - button[9]
    private final boolean[] myTurn = new boolean[1]; // T: my turn, F: your turn
    private ObjectInputStream input = null;    // input from my counterpart
    private ObjectOutputStream output = null;  // output from my counterpart
    private JFrame window = null;              // the tic-tac-toe window
    private String myMark = null;              // "O" or "X"
    private String yourMark = null;            // "X" or "O"


    public static void main(String[] args) throws IOException {

        if (args.length == 0) {
            // if no arguments, this process was launched through JSCH
            try {
                OnlineTicTacToe game = new OnlineTicTacToe();
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else {
            // this process wa launched from the user console.

            // verify the number of arguments
            if (args.length != 2 && args.length != 3) {
                System.err.println("args.length = " + args.length);
                usage();
            }

            // verify the correctness of my counterpart address
            InetAddress addr = null;
            try {
                addr = InetAddress.getByName(args[0]);
            } catch (UnknownHostException e) {
                error(e);
            }

            // verify the correctness of my counterpart port
            int port = 0;
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                error(e);
            }
            if (port < 5000) {
                usage();
            }

            // check args[2] == "auto"
            if (args.length == 3 && args[2].equals("auto")) {
                // auto play
                isAuto = 1;
                OnlineTicTacToe game = new OnlineTicTacToe(args[0]);
            } else {
                // interactive play
                OnlineTicTacToe game = new OnlineTicTacToe(addr, port);
            }
        }
    }

    /**
     * Prints out the usage.
     */
    private static void usage() {
        System.err.
                println("Usage: java OnlineTicTacToe ipAddr ipPort(>=5000) [auto]");
        System.exit(-1);
    }

    private static void error(Exception e) {
        e.printStackTrace();
        System.exit(-1);
    }

    private void makeWindow(boolean amFormer) {
        myTurn[0] = amFormer;
        myMark = (amFormer) ? "O" : "X";    // 1st person uses "O"
        yourMark = (amFormer) ? "X" : "O";  // 2nd person uses "X"

        // create a window
        window = new JFrame("OnlineTicTacToe(" + ((amFormer) ? "former)" : "latter)") + myMark);
        window.setSize(300, 300);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setLayout(new GridLayout(3, 3));

        // initialize all nine cells.
        for (int i = 0; i < NBUTTONS; i++) {
            button[i] = new JButton();
            window.add(button[i]);
            button[i].addActionListener(this);
        }

        // make it visible
        window.setVisible(true);
    }

    private boolean markButton(int i, String mark) {
        if (button[i].getText().equals("")) {
            button[i].setText(mark);
            button[i].setEnabled(false);
            return true;
        }
        return false;
    }

    private int whichButtonClicked(ActionEvent event) {
        for (int i = 0; i < NBUTTONS; i++) {
            if (event.getSource() == button[i])
                return i;
        }
        return -1;
    }

    private boolean buttonMarkedWith(int i, String mark) {
        return button[i].getText().equals(mark);
    }

    private void showWon(String mark) {
        JOptionPane.showMessageDialog(null, mark + " won!");
    }


    /**
     * Starts the online tic-tac-toe game.
     *
     * @param: my counterpart's ip address, args[1]: his/her port, (arg[2]: "auto")
     * if args.length == 0, this Java program is remotely launched by JSCH.
     */
    /**

     Constructor for OnlineTicTacToe class.
     @throws IOException if there is an I/O error while creating the connection or writing to the log file.
     */
    public OnlineTicTacToe() throws IOException, ClassNotFoundException {
        // receive an ssh2 connection from a user-local master server.
        Connection connection = new Connection();
        input = connection.in;
        output = connection.out;
        // list to collect the available button numbers from the remaining buttons
        // available
        // ArrayList<Integer> list = new ArrayList<>();
        PrintWriter logs = new PrintWriter(new FileOutputStream("logs.txt"));
        logs.println("Autoplay: got started.");
        logs.flush();
        myMark = "X"; // auto player is always the 2nd.
        yourMark = "O";
        myTurn[0] = false;
        logs.flush();
// int val = input.readInt();
// int val =-1;
        synchronized (myTurn) {
            while (true) {
                // int val =0;
                if (myTurn[0]) {
                    int randomInteger;
                    randomInteger = new Random().nextInt(9);
                    output.writeObject(randomInteger);
                    output.flush();
                    logs.println("remote user typed" + i);
                    logs.flush();
                }

                if (!myTurn[0]) {
//                    int val = (int) connection.in.readObject();
                    int val = (int) input.readObject();
                    logs.println("local  user typed" + val);
                    logs.flush();
                }
                myTurn[0] = !myTurn[0];
            }
        }
    }

    /**
     Constructor for OnlineTicTacToe game with the given hostname.
     @param hostname the hostname to connect to for establishing an ssh2 connection.
     @throws IOException if there is an error in input/output operations.
     */
    public OnlineTicTacToe(String hostname) throws IOException {
        // Set the auto play flag to 1.
        isAuto = 1;

        // Read the user name and password for the ssh2 connection.
        Scanner keyboard = new Scanner(System.in);
        String username = null;
        String password = null;
        try {
        // Read the user name from console.
            System.out.print("User: ");
            username = keyboard.nextLine();
            // Read the password from console.
            Console console = System.console();
            password = new String(console.readPassword("Password: "));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        // Command to launch the remote game.
        String cur_dir = System.getProperty("user.dir");
        String command = "java -cp " + cur_dir + "/jsch-0.1.54.jar:" + cur_dir + " OnlineTicTacToe";

        // Establish the ssh2 connection and run the game on the remote host.
        Connection connection = new Connection(username, password, hostname, command);

        // Set the input and output streams.
        input = connection.in;
        output = connection.out;

        // Create the game window.
        makeWindow(true);

        // Start the counterpart thread.
        Counterpart counterpart = new Counterpart();
        counterpart.start();
    }

    // The constructor for the OnlineTicTacToe class that sets up a TCP connection with a counterpart on a given IP address and port number.
    public OnlineTicTacToe(InetAddress addr, int port) {
        // Set up a TCP connection with my counterpart
        boolean isServer; // flag to check if the current instance is the server or client
        ServerSocket server = null;
        try {
            server = new ServerSocket(port); // Prepare a server socket
            server.setSoTimeout(INTERVAL); // Make it non-blocking with a timeout
        } catch (Exception e) {
            error(e); // Handle exceptions
        }
        Socket client = null; // The socket that connects to the counterpart
        while (true) { // Loop until a connection is established
            try {
                client = server.accept(); // Try to accept a remote request
            } catch (SocketTimeoutException ste) {
                // Couldn't receive a connection request within INTERVAL
            } catch (IOException ioe) {
                error(ioe); // Handle exceptions
            }
            // Check if a connection was established. If so, leave the loop and set the flag
            if (client != null) {
                isServer = true; // Current instance is the server
                break;
            }

            try {
                client = new Socket(addr, port); // Try to send a connection request
            } catch (IOException ioe) {
                // Connection refused
            }
            // Check if a connection was established, If so, leave the loop and clear the flag
            if (client != null) {
                isServer = false; // Current instance is the client
                break;
            }
        }
        // Exchange a message with my counterpart.
        try {
            makeWindow(!isServer); // Set up a window and pass the flag
            // Set up object input and output streams for communication
            System.out.println("TCP connection established...");
            output = new ObjectOutputStream(client.getOutputStream());
            input = new ObjectInputStream(client.getInputStream());
        } catch (Exception e) {
            error(e); // Handle exceptions
        }
        // Start the counterpart thread for listening to incoming messages
        Counterpart counterpart = new Counterpart();
        counterpart.start();
    }

    // This method checks if a player has won or if the game is tied.
    private boolean winner(String player) {
        // Checking diagonally from left to right
        if (buttonMarkedWith(0, player) && buttonMarkedWith(4, player) && buttonMarkedWith(8, player)) {
            showWon(player);
            return true;
        }
        // Checking diagonally from right to left
        if (buttonMarkedWith(2, player) && buttonMarkedWith(4, player) && buttonMarkedWith(6, player)) {
            showWon(player);
            return true;
        }
        // Checking horizontally from beginning to end
        for (int i = 0; i < 7; i = i + 3) {
            if (buttonMarkedWith(i, player) && buttonMarkedWith(i + 1, player) && buttonMarkedWith(i + 2, player)) {
                showWon(player);
                return true;
            }
        }
        // Checking vertically from beginning to end
        for (int i = 0; i < 3; i++) {
            if (buttonMarkedWith(i, player) && buttonMarkedWith(i + 3, player) && buttonMarkedWith(i + 6, player)) {
                showWon(player);
                return true;
            }
        }
        // Checking for a tie between players
        for (int i = 0; i < NBUTTONS; i++) {
            if (button[i].getText().equals(""))
                break;
            if (i == NBUTTONS - 1) {
                JOptionPane.showMessageDialog(null, "Its a tie");
                return true;
            }
        }
        // If neither player has won and there is no tie, return false
        return false;
    }
    /**
     * Asks the user if they want to play again, and performs the appropriate action
     * based on their response.
     */
    private void replay() {
        // Display a confirmation dialog box with a "Yes" and "No" option
        int input = JOptionPane.showConfirmDialog(
                null,
                "Do you want to play again?",
                "Game over", JOptionPane.YES_NO_OPTION);
        // If the user clicks "Yes"
        if (input == JOptionPane.YES_OPTION) {
            // Reset the text and enable all the game buttons
            for (int i = 0; i < NBUTTONS; i++) {
                button[i].setText("");
                button[i].setEnabled(true);
            }
        }
        // If the user clicks "No"
        else if (input == JOptionPane.NO_OPTION) {
            // Display a message dialog box indicating the game is over and the loser must be slapped by the winner
            JOptionPane.showMessageDialog(null,
                    "Game over");
            // Exit the program
            System.exit(0);
        }
    }
    /**
     * Performs an action in response to an event.
     * @param event The event to respond to.
     */
    public void actionPerformed(ActionEvent event) {
        // Synchronize on the "myTurn" array to prevent multiple threads from accessing it at the same time
        synchronized (myTurn) {
            // If it's not the player's turn, wait until notified
            if (!myTurn[0]) {
                try {
                    myTurn.wait();
                } catch (InterruptedException e) {
                    System.err.println(e);
                }
            }
            // If it is the player's turn
            else {
                // Determine which button was clicked
                int clickedButtonId = whichButtonClicked(event);
                // Try to mark the button with the player's mark
                boolean markedSuccessfully = markButton(clickedButtonId, myMark);
                // Print a message indicating that the button was clicked successfully
                System.out.println("Button " + clickedButtonId + " clicked successfully");
                // If the button was marked successfully
                if (markedSuccessfully) {
                    try {
                        output.writeObject(clickedButtonId);
                        output.flush();
                    } catch (IOException e) {
                        System.err.println(e);
                    }
                    // Determine if the player has won
                    boolean won = winner(myMark);
                    // If the player has won, show a message indicating they won and ask if they want to play again
                    if (won) {
                        showWon(myMark);
                        replay();
                    }
                    // Switch to the other player's turn
                    myTurn[0] = !myTurn[0];
                    // Notify all threads waiting on "myTurn"
                    myTurn.notifyAll();

                }
            }
        }
    }
    /**
     * A thread that represents the other player in the game.
     */
    private class Counterpart extends Thread {
        /**
         * Runs the thread.
         */
        @Override
        public void run() {
            while (true) {
                // Synchronize on "myTurn" to prevent multiple threads from accessing it at the same time
                synchronized (myTurn) {
                    try {
                        // If it's not the other player's turn, wait until notified
                        if (myTurn[0]) {
                            myTurn.wait();
                        }
                        // If it is the other player's turn
                        else {
                            // Read the ID of the button that the other player clicked
                            Object object = null;
                            try {
                                object = input.readObject();
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();

                            }
                            int buttonClickedOpp = (Integer) object;
                            // Print a message indicating that the other player played a button
                            System.out.println("Opponent played button " + buttonClickedOpp);
                            // Try to mark the button with the other player's mark
                            boolean marked = markButton(buttonClickedOpp, yourMark);
                            // If the button was marked successfully
                            if (marked) {
                                // Switch to the other player's turn
                                myTurn[0] = !myTurn[0];
                                // Notify all threads waiting on "myTurn"
                                myTurn.notifyAll();
                                // Print a message indicating that the other player's turn is over
                                System.out.println("Opponent's turn over.");
                            }
                        }
                    } catch (IOException | InterruptedException e) {
                        System.err.println(e);
                    }
                }
            }
        }

    }
}