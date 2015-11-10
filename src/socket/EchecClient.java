package socket;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;


/**
 * A client for the TicTacToe game, modified and extended from the
 * class presented in Deitel and Deitel "Java How to Program" book.
 * I made a bunch of enhancements and rewrote large sections of the
 * code.  In particular I created the TTTP (Echec Protocol)
 * which is entirely text based.  Here are the strings that are sent:
 *
 *  Client -> Server           Server -> Client
 *  ----------------           ----------------
 *  MOVE <n>  (0 <= n <= 8)    WELCOME <char>  (char in {X, O})
 *  QUIT                       VALID_MOVE
 *                             OTHER_PLAYER_MOVED <n>
 *                             VICTORY
 *                             DEFEAT
 *                             TIE
 *                             MESSAGE <text>
 *
 */
public class EchecClient {
	public final static int XS=16;
	public final static int YS=12;
	public final static int DIM=70;//70
    private JFrame frame = new JFrame("Echec");
    
    private JLabel messageLabel = new JLabel("");
    private ImageIcon icon;
    private ImageIcon opponentIcon;

    private static Square[][] board = new Square[XS][YS];/////////////////////////////////////////////////////////////////////////
    private Square currentSquare=board[0][0];

    private static int PORT = 8901;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    public ImageIcon[][][] icons=new ImageIcon[2][6][2];

    public String outprint;
    public ArrayList<Square> clearNotify=new ArrayList<Square>();
    public boolean instron=true;
    public char mark='?';
    /**
     * Constructs the client by connecting to a server, laying out the
     * GUI and registering GUI listeners.
     */
    public EchecClient(String serverAddress) throws Exception {
        // Setup networking
    	try{
        socket = new Socket(serverAddress, PORT);
    	}
    	catch(Exception e){
    		
    		JOptionPane.showMessageDialog(null, e.toString());
    	}
        in = new BufferedReader(new InputStreamReader(
            socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        // Layout GUI
        Dimension d=new Dimension();
        d.setSize(14000,7000);
        //frame.setSize(d);
        frame.setBounds(0, 0, XS*DIM, YS*DIM);
        frame.setResizable(false);
        messageLabel.setBackground(Color.lightGray);/////////////////////////////////////////////////////////////////
        frame.getContentPane().add(messageLabel, "South");

        for(int i=0;i<2;i++){
        	for(int j=0;j<6;j++){
        		for(int k=0;k<2;k++){
        			String type="";
        			switch(j){
        			case 0:type="TP";break;
        			case 1:type="CV";break;
        			case 2:type="BB";break;
        			case 3:type="LC";break;
        			case 4:type="DD";break;
        			case 5:type="SS";break;
        			default:break;
        			}
        			try{
        			icons[i][j][k] = new ImageIcon("IMG/"+i+type+k+".gif"); // load the image to a imageIcon
        			//System.out.println("IMG/"+i+type+k+".gif");
        			Image image = icons[i][j][k].getImage(); // transform it 
                	Image newimg = image.getScaledInstance(DIM*4/5, DIM*4/5,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  
                	icons[i][j][k] = new ImageIcon(newimg);  // transform it back
                	//System.out.println("IMG/"+i+type+k+".gif");
        			}
        			catch(Exception e){
        				//System.out.println("IMG/"+i+type+k+".gif");
        				continue;
        			}
        		}
        	}
        }
        
        
        JPanel boardPanel = new JPanel();
        boardPanel.setBackground(new Color(10,10,80));
        boardPanel.setLayout(new GridLayout(YS, XS, 2, 2));
        for (int i = 0; i < board.length; i++) {
        	for(int j=0;j<board[j].length;j++){
        		final int i2=i;
        		final int j2=j;
        		if((i+j)%2==0){
        		board[i][j] = new Square(Color.white);
        		}
        		else{
        			board[i][j] = new Square(new Color(150,150,255));
        		}
                board[i][j].addMouseListener(new MouseAdapter() {
                	
                	public void mouseReleased(MouseEvent e) {
                		int mx=e.getX();
                		int my=e.getY();
                		if(mx<0){
                			mx-=DIM;
                		}
                		if(my<0){
                			my-=DIM;
                		}
                		int x2=mx/DIM;
                		int y2=my/DIM;
                    	int x=i2+x2;
                    	int y=j2+y2;
                    	//System.out.println(i2+","+j2+","+x2+","+y2+";"+x+","+y);
                        currentSquare = board[x][y];
                        String lx,ly;
                    	if(x>9){
                    		lx=""+x;
                    	}
                    	else{
                    		lx="0"+x;
                    	}
                    	if(j2>9){
                    		ly=""+y;
                    	}
                    	else{
                    		ly="0"+y;
                    	}
                        out.println("MOVE " +lx +" "+ ly);
                        }
                    public void mousePressed(MouseEvent e) {
                        currentSquare = board[i2][j2];
                        String lx,ly;
                    	if(i2>9){
                    		lx=""+i2;
                    	}
                    	else{
                    		lx="0"+i2;
                    	}
                    	if(j2>9){
                    		ly=""+j2;
                    	}
                    	else{
                    		ly="0"+j2;
                    	}
                        out.println("SEL " +lx +" "+ ly);
                        }
                });
                
                
        	}
        }
        for(int j=0;j<YS;j++){
        	for(int i=0;i<XS;i++){
        		boardPanel.add(board[i][j]);
        	}
        }
        frame.getContentPane().add(boardPanel, "Center");
        frame.addKeyListener(new KeyListener(){

			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
				if((int)e.getKeyChar()==10){
					out.println("DONE ");
				}
				if((int)e.getKeyChar()==104){
					JOptionPane.showMessageDialog(frame, "mouse control+\n" +
							"follow instructions shown at the botton and on the map\n" +
							"your goal to to have your transport ship reach the enemy's coast(the most left or right line)\n" +
							"keys:\n" +
							"enter:done,to next stage\n" +
							"h:help\n" +
							"q:admit defeated\n" +
							"x:close server\n" +
							"s:send message\n" +
							"c:cheat");
					out.println("HELP ");
				}
				if((int)e.getKeyChar()==113){//admit defeated
					out.println("FAIL ");
				}
				if((int)e.getKeyChar()==120){//close socket
					out.println("QUITCLOSE");
					frame.setVisible(false);
				}
				if((int)e.getKeyChar()==115){
					String msg = JOptionPane.showInputDialog(
		                    "send message");
					out.println("SEND"+mark+":"+msg);
				}
				if((int)e.getKeyChar()==99){//no way
					out.println("CHEAT ");
					JOptionPane.showMessageDialog(frame,"cheat request sent to your opponent");
				}
				if((int)e.getKeyChar()==105){//no way
					out.println("IO ");
					//JOptionPane.showMessageDialog(frame,"cheat request sent to your opponent");
				}
				if((int)e.getKeyChar()==116){//no way
					out.println("TU ");
					//JOptionPane.showMessageDialog(frame,"cheat request sent to your opponent");
				}
				System.out.println((int)e.getKeyChar()+"pressed");
			}

			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
        	
        });
        //////////////////////////////////////initialize
    }

    /**
     * The main thread of the client will listen for messages
     * from the server.  The first message will be a "WELCOME"
     * message in which we receive our mark.  Then we go into a
     * loop listening for "VALID_MOVE", "OPPONENT_MOVED", "VICTORY",
     * "DEFEAT", "TIE", "OPPONENT_QUIT or "MESSAGE" messages,
     * and handling each message appropriately.  The "VICTORY",
     * "DEFEAT" and "TIE" ask the user whether or not to play
     * another game.  If the answer is no, the loop is exited and
     * the server is sent a "QUIT" message.  If an OPPONENT_QUIT
     * message is recevied then the loop will exit and the server
     * will be sent a "QUIT" message also.
     */
    public void play() throws Exception {
        String response;
        try {
        	//repaintall();
            response = in.readLine();
            if (response.startsWith("WELCOME")) {
                mark  = response.charAt(8);
                icon = new ImageIcon(mark == 'X' ? "x.jpeg" : "o.jpeg");
                opponentIcon  = new ImageIcon(mark == 'X' ? "o.jpeg" : "x.jpeg");
                frame.setTitle("Echec - Player " + mark);
            }
            while (true) {
                response = in.readLine();
                if (response.startsWith("VALID_MOVE")) {
                	String[] splited = response.split(":");
                	repaintall(splited[1]);
                	String[] spl=splited[0].split(" ");
                   messageLabel.setText(spl[1]+", press h for help");
                } else if (response.startsWith("OPPONENT_MOVED")) {
                    String[] splited = response.split(":");
                	repaintall(splited[1]);
                    //messageLabel.setText("Valid move, press enter if you are done");
                    messageLabel.setText("Opponent moved");
                } else if (response.startsWith("VICTORY")) {
                    messageLabel.setText("You win");
                    break;
                }
                else if(response.startsWith("REFRESH")){
                	String[] splited = response.split(":");
                    if(splited.length>=2){
                    	refresh();
                    	repaintall(splited[1]);
                    }
                    //String msg=splited[0].substring(8);
                    messageLabel.setText("start");
                    
                } else if (response.startsWith("DEFEAT" +
                		"")) {
                
                    messageLabel.setText("You lose");
                    break;
                } else if (response.startsWith("TIE")) {
                    messageLabel.setText("You tied");
                    break;
                } else if (response.startsWith("MESSAGE")) {
                    String[] splited = response.split(":");
                    if(splited.length>=2){
                    	repaintall(splited[1]);
                    }
                    String msg=splited[0].substring(8);
                    messageLabel.setText(msg);
                }
                else if(response.startsWith("FRAMECHEAT")){
                	int reply=JOptionPane.showConfirmDialog(null,"your opponent wishes to cheat,do you allow"
                			,"cheat request",JOptionPane.YES_NO_OPTION);
                	if(JOptionPane.YES_OPTION==reply){
                		out.println("ACCEPT");
                	}
                	else{
                		out.println("DECLINE");
                	}
                }
                else if(response.startsWith("FRAME")){
                	String msg=response.substring(5);
                	String display=msg.replaceAll(" /n ", " \n ");
                	int reply=JOptionPane.showConfirmDialog(null,display
                			,"reply?",JOptionPane.YES_NO_OPTION);
                	if(JOptionPane.YES_OPTION==reply){
                		msg+=" /n "+mark+":"+ JOptionPane.showInputDialog(
                                display);
                		out.println("SEND "+msg);
                		
                	}
                }
                else if(response.startsWith("FRAMD")){
                	String msg=response.substring(5);
                	String display=msg.replaceAll("/n", "\n");
                	JOptionPane.showMessageDialog(frame,"your oponent died");
                }
            }
            out.println("QUIT");
        }
        finally {
            socket.close();
        }
    }

    private boolean wantsToPlayAgain() {
        int response = JOptionPane.showConfirmDialog(frame,
            "Want to play again?",
            "Echec is Fun Fun Fun",
            JOptionPane.YES_NO_OPTION);
        frame.dispose();
        return response == JOptionPane.YES_OPTION;
    }

    public void refresh(){
    	for(int i=0;i<XS;i++){
    		for(int j=0;j<YS;j++){
    			//board[i][j].setIcon(null);
    			//board[i][j].settxt("");
    			board[i][j].clearIcon();
    			board[i][j].repaint();
    		}
    	}
    }
    public void repaintall(String str){
    	/*for(int i=0;i<XS;i++){
    		for(int j=0;j<YS;j++){
    			board[i][j].notify=false;
    			board[i][j].repaint();
    		}
    	}*/
    	if(instron){
    	for(Square s:clearNotify){
    		s.notify=false;
    		s.repaint();
    	}
    	clearNotify=new ArrayList<Square>();
    	}
    	
    	String tc[]=str.split("_");
    	str=tc[1];
    	String toclear[]=str.split(";");
    	System.out.println(tc[1]);
    	for(int i=0;i<3;i++){
    		String p[]=toclear[i].split(",");
    		int x=Integer.parseInt(p[0]);
    		
    		int y=Integer.parseInt(p[1]);
    		int cl=Integer.parseInt(p[2]);
    		if(x>=0){
    			board[x][y].clearIcon();
    			if(cl!=0){
    				board[x][y].setclicked(i);
    				clearNotify.add(board[x][y]);
    				System.out.println(cl);
    			}
    			board[x][y].repaint();
    		}
    	}
    	str=tc[0];
    	String[] splited = str.split(";");
    	for(int i=0;i<22;i++){
    		//System.out.println(splited[i]+"!");
    		String[] spl=splited[i].split(",");
    		int[] num=new int[5];
    		for(int j=0;j<5;j++){
    			num[j]=Integer.parseInt(spl[j]);
    		}
    		if(num[0]!=-1){
    			String type="";
    			switch(num[3]){
    			case 0:type="TP";break;
    			case 1:type="CV";break;
    			case 2:type="BB";break;
    			case 3:type="LC";break;
    			case 4:type="DD";break;
    			case 5:type="SS";break;
    			default:break;
    			}
    			//board[num[0]][num[1]].settxt(""+num[2]+type+num[4]);
    			//board[num[0]][num[1]].texticon(""+num[2]+type+num[4]);
    			//System.out.println(num[2]+num[3]+num[4]);
    			board[num[0]][num[1]].setIcon(icons[num[2]][num[3]][num[4]]);
    			board[num[0]][num[1]].repaint();
    		}
    	}
    	if(instron){
    	if(tc.length>=3){
    	str=tc[2];
    	splited = str.split(";");
    	for(String s:splited){
    		String p[]=s.split(",");
    		int x=Integer.parseInt(p[0]);
    		int y=Integer.parseInt(p[1]);
    		board[x][y].setclicked(2);
    		board[x][y].repaint();
    		clearNotify.add(board[x][y]);
    	}
    	}
    	}
    }
    
    
    /**
     * Graphical square in the client window.  Each square is
     * a white panel containing.  A client calls setIcon() to fill
     * it with an Icon, presumably an X or O.
     */
    static class Square extends JPanel {
        JLabel label = new JLabel();//(Icon)null);
        boolean notify;
        Color c=Color.black;
        public Square(Color c) {
            setBackground(c);
            add(label);
            notify=false;
        }

        public void setIcon(Icon icon) {
        	//label.setText("a");
            label.setIcon(icon);
        }
        
        public void clearIcon(){
        	label.setIcon(null);
        }
        public void settxt(String txt){
        	label.setText(txt);
        }
        public void texticon(String string){
        	ImageIcon imageIcon = new ImageIcon("IMG/"+string+".gif"); // load the image to a imageIcon
        	Image image = imageIcon.getImage(); // transform it 
        	Image newimg = image.getScaledInstance(DIM*4/5, DIM*4/5,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way  
        	imageIcon = new ImageIcon(newimg);  // transform it back
        	label.setIcon(imageIcon);
        }
        public void paintComponent(Graphics g){
        	
        	//g.drawLine(0, 0, 0, 0);
        	super.paintComponent(g);
            //Graphics2D g2 = (Graphics2D) g;
            //g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            //myBackgroundRoutine(g2);
            if(notify){
            	Graphics2D g2d = (Graphics2D) g.create();
            	g2d.setColor(c);
                double thickness = 2;
                Stroke oldStroke = g2d.getStroke();
                g2d.setStroke(new BasicStroke((float) thickness));
                g2d.drawRect(1, 1, this.getWidth()-2, this.getHeight()-2);
                g2d.setStroke(oldStroke);
        	
        	//g.drawRect(4, 4, DIM, DIM);
            }
        }
        public void setclicked(int cl){
        	notify=true;
        	switch(cl){
        	case 0:c=new Color(20,100,20);break;
        	case
        	1:c=new Color(120,50,50);break;
        	case 2:c=new Color(50,120,50);;break;
        	case 3:c=new Color(225,225,225);break; 
        	default:break;
        	}
        	repaint();
        }
    }

    
    /**
     * Runs the client as an application.
     */
    public static void main(String[] args) throws Exception {
    	String serverAddress = (args.length == 0) ? "localhost" : args[1];
        //serverAddress="132.206.54.108";
        //serverAddress="132.206.51.22";
        serverAddress = JOptionPane.showInputDialog(
                "Enter IP Address \n try 132.206.51.22" +
                "\non port 8901:");
        if (serverAddress!=null){
        while (true) {
                //Socket s = new Socket(serverAddress, 9090);
            EchecClient client = new EchecClient(serverAddress);
            client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            //client.frame.setSize(240, 160);
            client.frame.setVisible(true);
            client.frame.setResizable(false);
            client.play();
            if (!client.wantsToPlayAgain()) {
                break;
            }
        }
        }
    }
}