public class Point {
	double x;
	double y;
	
	public Point(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public double getX() {
		return x;
	}
	public void setX(double x) {
		this.x = x;
	}
	public double getY() {
		return y;
	}
	public void setY(double y) {
		this.y = y;
	}
	
	public Point multiply(Point other) {
		this.x*=other.getX();
		this.y*=other.getY();
		return this;
	}
	
	@Override
	public String toString() {
		return "("+this.x+","+this.y+")";
	}
}
