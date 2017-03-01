
public class Vari {
		
		 String name;
		 String type;
		 int row;
		 int coln;
		 
		public Vari(String name, String type, int row, int coln) {
			
			this.name = name;
			this.type = type;
			this.row = row;
			this.coln = coln;
		}
		
	public Vari(String name, int row, int coln) {
			
			this.name = name;
			this.type = "recursion unit";
			this.row = row;
			this.coln = coln;
		}
		
		public Vari() {
			
		}
		
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public int getRow() {
			return row;
		}

		public void setRow(int row) {
			this.row = row;
		}

		public int getColn() {
			return coln;
		}

		public void setColn(int coln) {
			this.coln = coln;
		}

	
		

}
