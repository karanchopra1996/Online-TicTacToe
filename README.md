# Online-TicTacToe
This project exercises how to write a peer-to-peer communicating program using non-blocking accept( ), multiple threads, (specifically saying, the main and the slave threads), and JSCH (Java secure shell).
This is an online Internet program that involves two users in the same tic-tac-toe game or allows a single
user to play with an automated remote user.

In a two-user interactive play, each user starts the game locally and operates on a local 3-by-3 tic-tac-toe
window that however interacts with his/her remote counterpart’s window through the Internet, so that the
two users can view the same ongoing progress in their game. On the other hand, in an automated play, a
single user gets a local window that interacts with an automated remote player. This remote player doesn’t
have to pop out any window but should write its on-going status in a “log.txt” file.
The game itself is as simple as needless to say. Therefore, the following specifications focus on only how
to start the program and how to manage the game window:

In a two-user interactive play:
(a) Each of two users starts a game with his/her counterpart’s IP address and port as follows:
java OnlineTicTacToe IP_Address
They do not care which of their programs would behave as a TCP client and a server. The users may
be sitting at the same computer, which thus allows the IP address to be “localhost”.
(b) After (at most) one TCP connection has been established, each program must decide which will play
first with the mark “O” and second with the mark “X”.
(c) A graphics window marked with “O” must play first, thus accepting its user’s choice of nine buttons.
The selected button is marked with “O”, which should be reflected to the same button on the
counterpart’s graphics window. Similarly, a graphics window marked with “X” must play second,
mark its user’s choice of nine buttons with “X”, and reflect it to the counterpart’s graphics window.
(d) While the counterpart is playing, the local user cannot click any button of his/her game window.
Such an action must be ignored or postponed until the local user gets a turn to play. Ignoring or
postponing a too early action is up to your design.
(e) Every time a user (regardless of local or remote) clicks a button, your program needs to check if the
user has won the current game, in which case a winning message such as “O won!” or “X won!”
should come out on the monitor.
In a automated single-user automated play:
(a) A user starts a game with his automated player’s IP address, as specifying the “auto” parameter as
follows:
java OnlineTicTacToe IP_ Address auto
An auto play is initiated through JSCH (which uses port 22) and thus needs no port number. However,
for an easier argument-parsing purpose, let OnlineTicTacToe still receive a port number as its second
argument.
(b) Once a connection has been established through JSCH, this real user may assume that s/he will play
first with the mark “O” and the automated player will play second with the mark “X”.
(c) Only a graphics window marked with “O” will pop out for the real user and let her/him play first.
The automated player should prints out its on-going status in a file named “logs.txt” under your home
directory. The auto player is dumb enough to randomly choose an available button.
(d) The same as the two-user interactive play.
(e) The same as the two-user interactive play.


Graphics
This online tic-tac-toe game needs to display a 3-by-3 graphics window with which the local user can play.
Since the main purpose of this programming project is peer-to-peer communication using non-blocking
accept and multithreads, we do not have to spend too much time for graphics. 


Main() Verifies the arguments:
1) If no arguments are passed, this program has been invoked by
JSCH remotely. It will instantiate an OnlineTicTacToe object
without any arguments that behaves as an automated
counterpart player.
2) If two arguments are passed, this program starts a two-user
interactive game. It will instantiate an OnlineTicTacToe
object with the counterart’s InetAddress and port.
3) If three arguments are passed and the 3 rd argument is “auto”,
this program starts a single-user automated game. It will
instantiate an OnlineTicTacToe object with a counter part’s IP
adderss (in String)


[OnlineTicTacToe(InetAddress addr, int port): Sets up a TCP connection with the counterpart on the specified port and InetAddress using sockets. 
The implementation approach is to create a server socket that waits for a connection request from the counterpart. 
If the connection request is received within the specified INTERVAL, the current instance is set as the server, and the connection is established. 
If the connection request is not received within the interval, a new socket is created to send a connection request to the counterpart. 
If the connection is established, the current instance is set as the client, and the connection is established. After the connection is established,
a window is set up for the Tic-Tac-Toe game, and object input and output streams are set up for communication between the server and client. 
The counterpart thread is then started to listen for incoming messages.

OnlineTicTacToe(String hostname): The public OnlineTicTacToe constructor establishes an SSH2 connection to the hostname, 
launches the game using the specified command, and sets up the input and output streams for communication. 
The implementation strategy is to read the user's username and password for the SSH2 connection from the console. 
Then, the command to launch the remote game is constructed using the current directory and the required classpath. 
The Connection object is created with the username, password, hostname, and command, which establishes the SSH2 connection and runs the game on the remote host.
Once the connection is established, the input and output streams are set up for communication, and the game window is created using the makeWindow method.
The counterpart thread is started to listen for incoming messages.

OnlineTicTacToe(): establishes an ssh2 connection with a user-local master server and uses input and output streams to communicate. 
The auto player's mark is set to "X" and the opponent's mark is set to "O". 
The code uses a synchronized block to alternate between the auto player's turn and the opponent's turn. 
During the auto player's turn, a random integer between 0 and 8 is generated and sent to the opponent. 
During the opponent's turn, the code waits for the opponent's move and logs it to a file. The turn is then switched to the auto player's turn.

The winner() method checks if a player has won the game or not. 
It first checks diagonally from left to right and then from right to left. 
Then it checks horizontally and vertically by iterating through the buttons of the game board. 
If there is a winning combination of buttons, it shows a message indicating the winner and returns true. 
If there is no winning combination and all buttons have been marked, it shows a message indicating a tie and returns true.
If neither player has won nor there is a tie, it returns false. The method uses a simple brute-force approach to check all possible winning combinations.

The actionPerformed() method is called when the player clicks on a button on the Tic Tac Toe game board. 
The method first synchronizes on the "myTurn" array to prevent multiple threads from accessing it at the same time. 
If it is not the player's turn, the method waits until notified. If it is the player's turn, the method determines which button was clicked,
marks the button with the player's mark, and sends the clicked button ID to the output stream. 
The method then determines if the player has won and shows a message if they have. 
Finally, the method switches to the other player's turn, notifies all threads waiting on "myTurn", and exits.

The Counterpart class extends Thread and overrides the run() method. Within the run() method, 
it runs an infinite loop that synchronizes on the "myTurn" array to prevent multiple threads from accessing it at the same time. 
If it is not the other player's turn, it waits until notified. Otherwise, it reads the ID of the button that the other player clicked, 
marks the button with the other player's mark, and switches to the other player's turn by setting myTurn[0] to !myTurn[0] and notifying all threads waiting on "myTurn". If any exceptions occur, it catches them and prints an error message to the console.
