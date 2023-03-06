/**
 * Class for representing a position on a 2-dimensional game board
 * @author Mai Ajspur
 * @version 9.2.2018
 */
public class Position{
	int col;
	int row;		
		
	public Position(int col, int row){
		this.col = col;
		this.row = row;
	}
	
	@Override
    public boolean equals(Object o) {
        if ( o == this ) 
            return true;
 
        if ( !(o instanceof Position) ) 
            return false;
         
        Position p = (Position) o;
        return p.row == this.row && p.col == this.col;
    }
	
	@Override
	public String toString(){
		return "(" + col +", " + row + ")";
	}
}