package asset;

public class Ship {
	public int id;
	public int type;//0:Tp,1:CV,2:BB,3:LC,4:DD,5:SS
	public int health;
	public int maxhealth;
	public double speed;
	public double range;
	public double antiair;
	public int att;
	public int antisub;
	public int maxnsa;
	public int nsa;//num of ships atted
	public Ship[] attacked;
	//boolean selected;
	public boolean moved;
	public Position pos;
	//public Position oldpos;
	public Position oripos;
	public boolean outofport;
	public boolean side;//true for o,false for x
	public int energy;
	public Ship(int i,int t){
		id=i;
		type=t;
		att=1;
		antisub=1;
		maxhealth=2;
		speed=1.5;
		range=1.5;
		antiair=1.5;
		maxnsa=2;
		outofport=true;
		//selected=false;
		switch (t){
		case 0:outofport=false;att=0;antisub=0;maxhealth=1;range=0;antiair=0;maxnsa=0;break;
		case 1:antisub=0;speed=1;antiair=0;range=100;maxnsa=1;break;
		case 2:antisub=0;speed=1;range=2.9;break;
		case 3:break;
		case 4:maxhealth=1;speed=2.9;antiair=0;maxnsa=1;break;
		case 5:att=2;maxhealth=1;antiair=0;maxnsa=1;break;
		default:break;
		}
		nsa=0;
		health=maxhealth;
		attacked=new Ship[maxnsa];
		moved=false;
		pos=null;
		//oldpos=null;
		oripos=null;
		side=true;
		energy=2;
	}
	public void move(Position pos){
		this.pos.remove();
		pos.add(this);
	}
}
