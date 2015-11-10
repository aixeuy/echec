package asset;

public class Position {
public int x,y;
public Ship ship;
public Position(int x,int y){
	this.x=x;this.y=y;
	ship=null;
}
public void add(Ship sh){
	this.ship=sh;
	//sh.oldpos=sh.pos;
	sh.pos=this;
}
public void remove(){
	//this.ship.pos=null;
	this.ship=null;
}
}
