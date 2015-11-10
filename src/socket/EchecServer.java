package socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

//import javax.swing.JOptionPane;

import asset.Position;
import asset.Ship;

/**
 * A server for a network multi-player Echec game.  Modified and
 * extended from the class presented in Deitel and Deitel "Java How to
 * Program" book.  I made a bunch of enhancements and rewrote large sections
 * of the code.  The main change is instead of passing *data* between the
 * client and server, I made a TTTP (Echec protocol) which is totally
 * plain text, so you can test the game with Telnet (always a good idea.)
 * The strings that are sent in TTTP are:
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
 * A second change is that it allows an unlimited number of pairs of
 * players to play.
 */


public class EchecServer {

    /**
     * Runs the application. Pairs up clients that connect.
     */
    public static void main(String[] args) throws Exception {
    	ServerSocket listener = new ServerSocket(8901);
        System.out.println("Echec Server is Running");
        //JOptionPane.showMessageDialog(null, "Echec Server is running");
        System.out.println(listener.getInetAddress().getHostAddress());
        try {
            while (true) {
                Game game = new Game();
                Game.Player playerX = game.new Player(listener.accept(), 'X');
                Game.Player playerO = game.new Player(listener.accept(), 'O');
                playerX.setOpponent(playerO);
                playerO.setOpponent(playerX);
                game.currentPlayer = playerX;
                playerX.start();
                playerO.start();
            }
        } finally {
            listener.close();
        }
    }
}

/**
 * A two-player game.
 */
class Game {
	public final int XS=16;
	public final int YS=12;
    /**
     * A board has nine squares.  Each square is either unowned or
     * it is owned by a player.  So we use a simple array of player
     * references.  If null, the corresponding square is unowned,
     * otherwise the array cell stores a reference to the player that
     * owns it.
     */
    private Position[][] pos =new Position[XS][YS];
    public Ship[] ships=new Ship[22];
    		/*{////////////////////////////////////////////////////////////////
        null, null, null,
        null, null, null,
        null, null, null};*/

    /**
     * The current player.
     */
    Player currentPlayer;

    Ship selected;
    int round;
    String err="";
    Position[] toclear=new Position[3];
    ArrayList<Position> canatt=new ArrayList<Position>();
    ArrayList<Position> canmove=new ArrayList<Position>();
    public Game(){
    	for(int i=0;i<XS;i++){
    		for(int j=0;j<YS;j++){
    			pos[i][j]=new Position(i,j);
    		}
    	}
    	for(int i=0;i<2;i++){
    		ships[i]=new Ship(i,0);
    	}
    	for(int i=2;i<4;i++){
    		ships[i]=new Ship(i,1);
    	}
    	for(int i=4;i<7;i++){
    		ships[i]=new Ship(i,2);
    	}
    	for(int i=7;i<11;i++){
    		ships[i]=new Ship(i,3);
    	}
    	for(int i=11;i<16;i++){
    		ships[i]=new Ship(i,4);
    	}
    	for(int i=16;i<22;i++){
    		ships[i]=new Ship(i,5);
    	}
    	
    	Ship[] ram=new Ship[20];
    	for(int i=0;i<20;i++){
    		ram[i]=ships[i+2];
    	}
    	for(int i=0;i<10;i++){
    		int s1=(int)random(20);
    		int s2=(int)random(20);
    		Ship tmp=ram[s1];
    		ram[s1]=ram[s2];
    		ram[s2]=tmp;
    	}
    	for(int i=0;i<10;i++){
    		ram[2*i].side=false;
    	}
    	ships[0].side=false;
    	int leftpos=0;
    	int rightpos=YS-1;
    	for(int i=0;i<22;i++){
    		if(ships[i].side){
    			pos[0][leftpos].add(ships[i]);
    			leftpos++;
    		}
    		else{
    			pos[XS-1][rightpos].add(ships[i]);
    			rightpos--;
    		}
    		ships[i].oripos=ships[i].pos;
    	}
    	round=0;
    }
    
    /**
     * Returns whether the current state of the board is such that one
     * of the players is a winner.
     */
    public boolean hasWinner() {
    	
    	return (ships[0].pos!=null&&ships[0].pos.x==0)||(ships[1].pos!=null&&ships[1].pos.x==15);
        /*return
            (board[0] != null && board[0] == board[1] && board[0] == board[2])
          ||(board[3] != null && board[3] == board[4] && board[3] == board[5])
          ||(board[6] != null && board[6] == board[7] && board[6] == board[8])
          ||(board[0] != null && board[0] == board[3] && board[0] == board[6])
          ||(board[1] != null && board[1] == board[4] && board[1] == board[7])
          ||(board[2] != null && board[2] == board[5] && board[2] == board[8])
          ||(board[0] != null && board[0] == board[4] && board[0] == board[8])
          ||(board[2] != null && board[2] == board[4] && board[2] == board[6]);*/
    }

    /**
     * Returns whether there are no more empty squares.
     */
    public boolean boardFilledUp() {
        /*for (int i = 0; i < pos.length; i++) {
            if (pos[i] == null) {
                return false;
            }
        }*/
        return ships[0].pos==null&&ships[1].pos==null;
    }

    /**
     * Called by the player threads when a player tries to make a
     * move.  This method checks to see if the move is legal: that
     * is, the player requesting the move must be the current player
     * and the square in which she is trying to move must not already
     * be occupied.  If the move is legal the game state is updated
     * (the square is set and the next player becomes current) and
     * the other player is notified of the move so it can update its
     * client.
     */
    public void buildinstr(){
    	canatt=new ArrayList<Position>();
        canmove=new ArrayList<Position>();
    	for(int i=0;i<XS;i++){
    		for(int j=0;j<YS;j++){
    			legalMove(i,j, currentPlayer,false);
    		}
    	}
    }
    public synchronized boolean legalMove(int locationx,int locationy, Player player,boolean real) {/////////////////////////////////////////////////////////////////////////
        err="";
    	if (player == currentPlayer) {
        	if(selected==null){
        		err="...";
        	}
        	else{
        		if(selected.nsa!=0){
        			if(pos[locationx][locationy].ship==null||pos[locationx][locationy].ship.side==selected.side){
        			err="already attacked";
        			return false;
        			}
        		}
        		if(selected.type==0&&round<=1){
        			err="can't move TP";
        			return false;
        		}
        		if(pos[locationx][locationy].ship == null){
        			if(distanza(pos[locationx][locationy],selected.oripos)<=selected.speed||(round<=1&&((locationx<7&&locationx>=0&&currentPlayer.side)||(locationx>=9&&locationx<=15&&!currentPlayer.side)))){
        	        	if(real){
        				selected.move(pos[locationx][locationy]);
        	        	selected.moved=true;
        	        	selected.outofport=true;
        	        	if(selected!=null&&(selected.type==5&&!subdetected(selected))){
        	        		toclear[2]=toclear[0];
        	        		toclear[0]=null;
                        	toclear[1]=null;
                        }
        	        	err="moved";
        	            currentPlayer.opponent.otherPlayerMoved(locationx,locationy);
        	        	}
        	        	else{
        	        		canmove.add(pos[locationx][locationy]);
        	        	}
        	        }
        			else{
        				err="out of range";
        				return false;
        			}
        		}
        		else{
        			Ship ondes=pos[locationx][locationy].ship;
        			if(ondes==selected){
        				return true;
        			}
        			if(ondes.type==0&&round<=1){
        				err="can't move TP";
        				return false;
        			}
        			if(ondes.nsa!=0){
        				err="already attacked";
        				return false;
        			}
        			if(ondes.side==currentPlayer.side){
        				if(distanza(pos[locationx][locationy],selected.oripos)<=selected.speed&&distanza(ondes.oripos,selected.pos)<=ondes.speed||(round<=1&&((locationx<7&&locationx>=0&&currentPlayer.side)||(locationx>=9&&locationx<=15&&!currentPlayer.side)))){
        					if(real){
        					selected.pos.remove();
        					ondes.move(selected.pos);
            	        	pos[locationx][locationy].add(selected);
            	        	selected.moved=true;
            	        	ondes.moved=true;
            	        	selected.outofport=true;
            	        	ondes.outofport=true;
            	        	err="moved";
            	        	if(selected!=null&&(selected.type==5&&!subdetected(selected))){
            	        		Position tmp=toclear[0];
            	        		toclear[0]=toclear[1];
                            	toclear[1]=tmp;
                            }
            	            currentPlayer.opponent.otherPlayerMoved(locationx,locationy);
        					}
        					else{
            	        		canmove.add(pos[locationx][locationy]);
            	        	}
            	        }
        				else{
        					err="out of range";
        					return false;
        				}
        			}
        			else{
        				return legalAttack(selected,ondes,real);
        			}
        		}
        	}

        	
        }
        return true;
    }
    public synchronized boolean legalAttack(Ship sha,Ship shb,boolean real){
    	if(!shb.outofport){
    		err="in port";
    		return false;
    	}
    	if(round<=1){
    		err="not started";
    		return false;
    	}
    	if(distanza(sha.pos,shb.pos)>=sha.range){
    		err="out of range";
    		return false;
    	}
    	if(sha.att==0||sha.antisub==0&&shb.type==5){
    		err="this ship doesn't have required equipment";
    		return false;
    	}
    	if(sha.pos!=sha.oripos&&!(sha.type==4&&shb.type==5&&distanza(sha.pos,sha.oripos)<=1.5)){
    		err="already moved";
    		return false;
    	}
    	if(sha.nsa>=sha.maxnsa||(sha.nsa!=0&&(shb.type<=sha.type||sha.attacked[0].type<=sha.type))){
    		if(sha.nsa>=sha.maxnsa){
    			err="maximum number of attacks reached";
    		}
    		else if(sha.nsa!=0&&(shb.type<=sha.type||sha.attacked[0].type<=sha.type)){
    			err="can't simutaneously attack larger ships";
    		}
    		else{
    			err="unknown";
    		}
    		return false;
    	}
    	if(sha.attacked[0]==shb){
    		err="can't simutaneously attack the same ship";
    		return false;
    	}
    	if(sha.type==1&&sha.energy<2){
    		err="CV resting";
    		return false;
    	}
    	err="attacked";
    	if(real){
    	if(sha.type==1){
    		int antiair=getantiair(shb.pos.x,shb.pos.y);
    		if(1.0-0.2*antiair>random(1)){
    			err="attack succeed";
    		}
    		else{
    			err="attack failed";
    			sha.energy=0;
    			shb.health+=sha.att;
    		}
    	}
    	shb.health-=sha.att;
    	sha.attacked[sha.nsa]=shb;
    	sha.nsa++;
    	if(shb.health<=0){
			shb.pos.remove();
			shb.pos=null;	
		}
    	currentPlayer.opponent.otherPlayerMoved(0,0);
    	}
    	else{
    		err="";
    		canmove.add(shb.pos);
    	}
    	return true;
    }
    
    public int getantiair(int x,int y){
    	int n=0;
    	for(int i=x-1;i<=x+1;i++){
    		for(int j=y-1;j<=y+1;j++){
    			if(i<0||i>15||j<0||j>11){
    			//System.out.println("case1"+i+" "+j);
    			}
    			else if(pos[i][j].ship==null){
    				//System.out.println("case2"+i+" "+j);
    			}
    			else{
    				if(pos[i][j].ship.side!=currentPlayer.side){
    				if(pos[i][j].ship.antiair+0.01>=distanza(pos[i][j],pos[x][y])){
    				n=n+pos[i][j].ship.energy/2;//System.out.print("add");
    				}
    				}
    			}
    		}
    	}
    	return n;
    }
    public boolean subdetected(Ship sh){
    	int x=sh.pos.x;int y=sh.pos.y;
    	for(int i=x-2;i<=x+2;i++){
    		for(int j=y-2;j<=y+2;j++){
    			if(i<0||i>15||j<0||j>11){
    			//System.out.println("case1"+i+" "+j);
    			}
    			else if(pos[i][j].ship==null){
    				//System.out.println("case2"+i+" "+j);
    			}
    			else if(pos[i][j].ship.side==sh.side){
    				
    			}
    			else{
    				if((i-x)*(i-x)<=1&&(j-y)*(j-y)<=1){
    					return true;
    				}
    				//else if(pos[i][j].ship.type==4){
    					//return true;
    				//}
    			}
    		}
    	}
    	return false;
    }
    public static double distanza(Position p1,Position p2){
    	
    	return Math.sqrt(((p1.x-p2.x)*(p1.x-p2.x)+(p1.y-p2.y)*(p1.y-p2.y)));
    }
    /**
     * The class for the helper threads in this multithreaded server
     * application.  A Player is identified by a character mark
     * which is either 'X' or 'O'.  For communication with the
     * client the player has a socket with its input and output
     * streams.  Since only text is being communicated we use a
     * reader and a writer.
     */
    class Player extends Thread {
        char mark;
        boolean side;
        Player opponent;
        Socket socket;
        BufferedReader input;
        PrintWriter output;
        int numofsh=11;

        /**
         * Constructs a handler thread for a given socket and mark
         * initializes the stream fields, displays the first two
         * welcoming messages.
         */
        public Player(Socket socket, char mark) {
            this.socket = socket;
            this.mark = mark;
            if(mark=='O'){
            	side=true;
            }
            else{
            	side=false;
            }
            try {
                input = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
                output = new PrintWriter(socket.getOutputStream(), true);
                output.println("WELCOME " + mark);
                output.println("MESSAGE Waiting for opponent to connect");
            } catch (IOException e) {
                System.out.println("Player died: " + e);
            }
        }

        /**
         * Accepts notification of who the opponent is.
         */
        public void setOpponent(Player opponent) {
            this.opponent = opponent;
        }

        /**
         * Handles the otherPlayerMoved message.
         */
        public void otherPlayerMoved(int locationx,int locationy) {
        	String lx,ly;
        	if(locationx>9){
        		lx=""+locationx;
        	}
        	else{
        		lx="0"+locationx;
        	}
        	if(locationy>9){
        		ly=""+locationy;
        	}
        	else{
        		ly="0"+locationy;
        	}
        	if(round>1){
            output.println("OPPONENT_MOVED :" +maptoString(false,true));
        	}
            output.println(
                hasWinner() ? "DEFEAT" :boardFilledUp()?"TIE": "");
        }
        public void otherPlayerDone(){
        	if(round>1){
        	output.println("MESSAGE now your turn:" /*+maptoString(false,true)*/);
        	}
        	else{
        		output.println("MESSAGE now your turn:");
        	}
        }
        public void otherPlayerDone2(){
        	output.println("MESSAGE your opponent's turn again:" /*+maptoString(false,true)*/);
        }
        public void otherPlayerYouWin(){
        	output.println("VICTORY");
        }
        public void otherPlayerSend(String str){
        	output.println("FRAME"+str );
        }
        public void otherPlayerDie(){
        	output.println("FRAMD your opponent died");
        }
        public void refreash(boolean showsub){
        	round++;
    		output.println("REFRESH:" +maptoString(false,showsub));
    		round--;
        }
        public void nextround(Player p){
        	selected=null;
        	if(round==1){
        		currentPlayer.refreash(true);
        		currentPlayer.opponent.refreash(true);
        	}
        	if(p==null){
        	if(random(2)<1||round<=1){
        		currentPlayer = currentPlayer.opponent;
        		output.println("MESSAGE now your opponent's turn");
        		currentPlayer.otherPlayerDone();
        	}
        	else{
        		output.println("MESSAGE your turn again");
            	currentPlayer.opponent.otherPlayerDone2();
        	}
        	}
        	else{
        		
        		if(currentPlayer!=p){
        			currentPlayer=p;
            		output.println("MESSAGE now your opponent's turn");
            		currentPlayer.otherPlayerDone();
            	}
            	else{
            		currentPlayer=p;
            		output.println("MESSAGE your turn again");
                	currentPlayer.opponent.otherPlayerDone2();
            	}
        	}
        	round++;
        	for(int i=0;i<22;i++){
        		ships[i].oripos=ships[i].pos;
        		ships[i].moved=false;
        		ships[i].energy++;
        		if(ships[i].energy>2){
        			ships[i].energy=2;
        		}
        		ships[i].nsa=0;
        		ships[i].attacked=new Ship[ships[i].maxnsa];
        	}
        }
        public String maptoString(boolean instr,boolean showsub){
        	String str="";
        	for(int i=0;i<22;i++){
        		int fullblood=1;
        		if(ships[i].health!=ships[i].maxhealth){
        			fullblood=0;
        		}
        		int s=0;
        		if(ships[i].side){
        			s=1;
        		}
        		if(ships[i].pos==null||(round==1&&ships[i].side!=currentPlayer.side)||(round>1&&ships[i].type==5&&this.side!=ships[i].side&&!(subdetected(ships[i])))){
        			str+="-1"+","+"-1"+","+s+","+ships[i].type+","+fullblood+";";
        		}
        		else{
        			str+=ships[i].pos.x+","+ships[i].pos.y+","+s+","+ships[i].type+","+fullblood+";";
        		}
        	}
        	int x1,x2,y1,y2,c1,c2,x3,y3,c3;
        	if(toclear[0]!=null){
        		x1=toclear[0].x;
            	y1=toclear[0].y;
            	if(round==1&&toclear[0].ship!=null&&toclear[0].ship.side!=currentPlayer.side){
            		x1=-1;
            		y1=-1;
            	}
        	}
        	else{
        		x1=-1;
        		y1=-1;
        	}
        	
        	if(toclear[1]!=null){
        		x2=toclear[1].x;
            	y2=toclear[1].y;
        	}
        	else{
        		x2=-1;
        		y2=-1;
        	}
        	if(toclear[2]!=null){
        		x3=toclear[2].x;
            	y3=toclear[2].y;
        	}
        	else{
        		x3=-1;
        		y3=-1;
        	}
        	c1=0;c2=0;c3=0;
        	if(!instr){
        		c1=1;c2=1;
        	}
        	str+="_"+x1+","+y1+","+c1+";"+x2+","+y2+","+c2+";"+x3+","+y3+","+c3+"_";
        	if(instr){
        	for (Position p : canmove) {//canmove
        	    str+=p.x+","+p.y+";";
        	}
        	}
        	str+="_";
        	if(instr){
        	for (Position p : canatt) {//canattack
        	    str+=p.x+","+p.y+";";
        	}
        	}
			return str;
        	
        }

        /**
         * The run method of this thread.
         */
        public void run() {
            try {
                // The thread is only started after everyone connects.
                output.println("MESSAGE All players connected,your opponent's turn:"+maptoString(false,true));

                // Tell the first player that it is her turn.
                if (mark == 'X') {
                    output.println("MESSAGE Your move:"+maptoString(false,true));
                }

                // Repeatedly get commands from the client and process them.
                while (true) {
                    String command = input.readLine();
                    //command!=null
                    System.out.println(command);
                    if (command.startsWith("MOVE")) {
                        int locationx = Integer.parseInt(command.substring(5,7));
                        int locationy = Integer.parseInt(command.substring(8));
                        if(selected!=null){
                        toclear[0]=selected.pos;
                        }
                        else{
                        	toclear[0]=null;
                        }
                        toclear[1]=pos[locationx][locationy];
                        toclear[2]=null;
                        if (this==currentPlayer&&legalMove(locationx,locationy, this,true)) {
                        	//if(selected!=null&&selected.pos==pos[locationx][locationy]){
                            
                        	//}
                        	//else{
                        	
                        		output.println("VALID_MOVE "+err+".:"+maptoString(true,false));
                        	//}
                            output.println(hasWinner() ? "VICTORY"
                                        : boardFilledUp()? "TIE":"");
                       } else {
                    	   if(this!=currentPlayer){
                           	output.println("MESSAGE your opponent move,please wait");
                           }
                    	   else{
                            output.println("MESSAGE not legal move..."+err);
                    	   }
                        }
                    }
                    else if(command.startsWith("SEL")){
                    	int shx = Integer.parseInt(command.substring(4,6));
                    	int shy = Integer.parseInt(command.substring(7));
                    	if(this==currentPlayer&&pos[shx][shy].ship!=null&&pos[shx][shy].ship.side==this.side){
                    		selected=pos[shx][shy].ship;
                    		buildinstr();
                    		output.println("MESSAGE click:"+maptoString(true,false));
                    	}
                    }
                    else if(command.startsWith("DONE")){
                    	//currentPlayer = currentPlayer.opponent;
                    	selected=null;
                    	if(this==currentPlayer){
                    		nextround(null);
                    	}
                    }
                    else if(command.startsWith("FAIL")){
                    	this.opponent.otherPlayerYouWin();
                    	output.println("DEFEAT");
                    }
                    else if(command.startsWith("SEND")){
                    	this.opponent.otherPlayerSend(command.substring(4));
                    }
                    else if(command.startsWith("CHEAT")){
                    	this.opponent.otherPlayerSend("CHEAT");
                    }
                    else if(command.startsWith("ACCEPT")){
                    	nextround(this.opponent);
                    	this.opponent.otherPlayerSend("your opponent accepted your cheat request");
                    }
                    else if(command.startsWith("DECLINE")){
                    	this.opponent.otherPlayerSend("your opponent declined your cheat request");
                    }
                    else if(command.startsWith("IO")){
                    	nextround(this);
                    }
                    else if(command.startsWith("TU")){
                    	nextround(this.opponent);
                    }
                    else if (command.startsWith("QUIT")) {
                    	//this.otherPlayerDie();
                    	//this.opponent.otherPlayerDie();
                    	if(command.startsWith("QUITCLOSE")){
                    		this.otherPlayerSend("server closed");
                    		this.opponent.otherPlayerSend("server closed by your opponent");
                    	}
                    	try {socket.close();} catch (IOException e) {}
                    	if(command.startsWith("QUITCLOSE")){
                    		System.out.println("terminate");
                    		System.exit(0);
                    	}
                        return;
                    }
                }
            } catch (IOException e) {
                System.out.println("Player died: " + e);
                this.otherPlayerDie();
                this.opponent.otherPlayerDie();
                //output.println("MESSAGE your opponente died...");
                //JOptionPane.showMessageDialog(null, "player die");
            } finally {
                try {socket.close();} catch (IOException e) {}
            }
        }
    }
    public static double random(int num){
    	Random r = new Random();
        double rv =r.nextDouble();
        return rv*num;
    }
}