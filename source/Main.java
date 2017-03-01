
import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;


public class Main {
	
	public static void main(String []args) throws FileNotFoundException, UnsupportedEncodingException{
		File file = new File("input.mat");
		File prewrite = new File ("matrixstr.c");
		//Scanner s = new Scanner(file);
		
		Global.input = fileHandler(file);
		Global.output=fileHandler(prewrite);
		evaluator(Global.input);
		Global.output.add("}");
		for(Vari v : Global.variables){
		
		}
		outputFile(Global.output);
	}
	/*
	 * This method checks the type of the expression, and sends the expression to the 
	 * method that evaluates that type respectively.
	 */
	public static void evaluator(ArrayList<String> inputs){
		while(!inputs.isEmpty()){
			if(inputs.get(0).contains("#")){
				evaluatecomment(inputs.get(0));
				inputs.remove(0);
			} else if(inputs.get(0).contains("scalar")||inputs.get(0).contains("vector")||inputs.get(0).contains("matrix")){
				evaluatedeclaration(inputs.get(0));
				inputs.remove(0);
			} else if(inputs.get(0).contains("for")&&inputs.get(0).contains("(")&&inputs.get(0).contains(")")&&inputs.get(0).contains("{")){
				evaluateforloop(inputs.get(0));
				inputs.remove(0);
			} else if(inputs.get(0).contains("=")){
				evaluateexpr(inputs.get(0));
				inputs.remove(0);
			} else if(inputs.get(0).contains("}")){
				if(Global.nestedloop){
					Global.output.add("}");
					Global.nestedloop = false;
				}
				Global.output.add("}");
				inputs.remove(0);
			} else if(inputs.get(0).contains("print")&&inputs.get(0).contains("(")&&inputs.get(0).contains(")")){
				evaluateprint(inputs.get(0));
			
				inputs.remove(0);
			} else{
				inputs.remove(0);
			}
		}
		
		
	}
	/*
	 * pretty much self explanatory
	 */
	public static void evaluateprint(String s){
		String name="";
		Vari v = new Vari();
		if(s.contains("printsep")){
			Global.output.add("printf(\"-------\\n\");");
		}else if(s.contains("[")&&s.contains("]")){
			name = getexpression(s.substring(s.indexOf("(")+1,s.indexOf("[")));
			for(Vari i : Global.variables){
				if(i.getName().equals(name)) v=i;
			}
			if(v.getType()=="matrix"){
				Global.output.add("printf(\"%f\\n\", "+ v.getName()+".values["+s.substring(s.indexOf("[")+1,s.indexOf(",") ) +"-1]["+s.substring(s.indexOf(",")+1,s.indexOf("-1]")) + "]);" );
			}else{
				Global.output.add("printf(\"%f\\n\", "+ v.getName()+".values["+s.substring(s.indexOf("[")+1,s.indexOf("]") ) +"-1][0]);"  );
			}
		} else{
			name = getexpression(s.substring(s.indexOf("(")+1,s.indexOf(")")));
			for(Vari i : Global.variables){
				if(i.getName().equals(name)) v=i;
			}
			if(v.getType().equals("matrix")||v.getType().equals("vector")){
				Global.output.add("printMatrix(&"+v.getName()+");");
			}else {
				Global.output.add("printf(\"%f\\n\", "+ v.getName()+");");
			}
		}
	}
	/*
	 * This method checks if the statement is a decleration or operation and sends the statement to its coorelated method.
	 */
	public static void evaluateexpr(String s){
		
		String name = "";
		Vari v = new Vari();
		
		if(s.contains("{")&&s.contains("}")){
			name = getexpression(s.substring(0, s.indexOf('=')-1));
			for(int i=0;i<Global.variables.size();i++){
				if (Global.variables.get(i).getName().equals(name)) v= Global.variables.get(i);				
			}
			double[] values = getvalues(s, v.getColn()*v.getRow());
			if(v.getType().equals("matrix")){
				for(int i=0;i<v.getRow();i++){
					for(int j=0;j<v.getColn();j++){
						Global.output.add(v.getName()+".values["+i+"]["+ j+"]="+values[i*v.getColn()+j]+";");
					}
				}
			}else if(v.getType().equals("vector")){
					for(int i = 0;i<v.getRow();i++){
						Global.output.add(v.getName()+".values["+i+"][0]="+values[i]+";");
					}
			} 
			}else if(s.contains("sqrt")||s.contains("tr")||s.contains("+")||s.contains("*")||s.contains("-")||s.contains("choose")){
				name = getexpression(s.substring(0, s.indexOf('=')-1));
				name = name.replaceAll(" ", "");
				Vari expr = new Vari();
				for(Vari k:Global.variables){
					if(name.equals(k.getName())) expr =k;
				}
				s=s.replaceAll(" ","");
				s=s.substring(s.indexOf("=")+1);
				Vari send = new Vari(s, -1, -1);
				send = evaluateoperations(send);
				if(expr.getRow()==0&&expr.getColn()==0&&send.getColn()>0&&send.getRow()>0){
					Global.output.add(expr.getName() +"="+ send.getName()+".values[0][0];");
				}else{
					Global.output.add(expr.getName() +"="+ send.getName()+";");
				}
				
			} else {
				name = getexpression(s.substring(0, s.indexOf('=')-1));
				for(int i=0;i<Global.variables.size();i++){
					if (Global.variables.get(i).getName().equals(name)) v= Global.variables.get(i);	
				}
				Global.output.add(v.getName()+"=" +s.substring(s.indexOf('=')+1)+";");
				
			}
		
		
		
	}
	/*
	 * This method evaluated the operations recursively using a type of Vari, which I implemented to store the variables and
	 * recursive statements
	 */
	public static Vari evaluateoperations(Vari v){
		String s = v.getName();
		Vari i = new Vari();
		String temp ="";
		int paranth =0;
		if(!s.contains("(")){
				if(s.contains("+")){
					Vari x = evaluateoperations(new Vari(s.substring(0, s.indexOf('+')),-1,-1));
					Vari y = evaluateoperations(new Vari(s.substring(s.indexOf('+')+1,s.length()),-1,-1));
					if(x.getRow()==0&&y.getColn()==0&&x.getColn()==0&&y.getColn()==0){
						return new Vari(x.getName()+"+"+y.getName(),0,0);
					} else {
						return new Vari("MatrixOpr("+x.getName()+","+y.getName()+",'+')",x.getRow(),y.getColn());
					}
				}else if(s.contains("-")){
					Vari x = evaluateoperations(new Vari(s.substring(0, s.indexOf('-')),-1,-1));
					Vari y = evaluateoperations(new Vari(s.substring(s.indexOf('-')+1,s.length()),-1,-1));
					if(x.getRow()==0&&y.getColn()==0&&x.getColn()==0&&y.getColn()==0){
						return new Vari(x.getName()+"-"+y.getName(),0,0);
					} else {
						return new Vari("MatrixOpr("+x.getName()+","+y.getName()+",'-')",x.getRow(),y.getColn());
					}
				}else if(s.contains("*")){
					Vari x = evaluateoperations(new Vari(s.substring(0, s.indexOf('*')),-1,-1));
					Vari y = evaluateoperations(new Vari(s.substring(s.indexOf('*')+1,s.length()),-1,-1));
					if(x.getRow()==0&&y.getColn()==0&&x.getColn()==0&&y.getColn()==0){
						return new Vari(x.getName()+"*"+y.getName(),0,0);
					} else {
						return new Vari("Matrixmult("+x.getName()+","+y.getName()+")",x.getRow(),y.getColn());
					}
				}else{
					for(Vari x: Global.variables){
						if(s.equals(x.getName())) return x;
						
					}
				}
					
					
		} else{
			int seperator =0;
			ArrayList<Integer> indexofopers = new ArrayList<Integer>();
			for(int a=0;a<s.length();a++){
				if(s.charAt(a)=='(') paranth++;
				if(s.charAt(a)==')') paranth--;
				if(a!=s.length()-1){
					if(paranth==0&&(s.charAt(a+1)=='+'||s.charAt(a+1)=='*'||s.charAt(a+1)=='-')) indexofopers.add(a+1);
				}
			}
			if(!indexofopers.isEmpty()){
				for(int x: indexofopers){
					if(s.charAt(x)=='+'||s.charAt(x)=='-'){
						seperator =x;
						break;
					} 
					seperator =x;
				}
				if(s.charAt(seperator)=='*'){
					Vari x = evaluateoperations(new Vari(s.substring(0, seperator),-1,-1));
					Vari y = evaluateoperations(new Vari(s.substring(seperator+1,s.length()),-1,-1));
					if(x.getRow()==0&&y.getColn()==0&&x.getColn()==0&&y.getColn()==0){
						return new Vari(x.getName()+"*"+y.getName(),0,0);
					} else if(x.getRow()==0&&x.getColn()==0&&y.getRow()>0&&y.getColn()>0) {
						return new Vari("Matrixmult2("+y.getName()+","+x.getName()+")",y.getRow(),y.getColn());
					} else if(y.getRow()==0&&y.getColn()==0&&x.getRow()>0&&x.getColn()>0){
						return new Vari("Matrixmult2("+x.getName()+","+y.getName()+")",x.getRow(),x.getColn());
					} else {
						return new Vari("Matrixmult("+x.getName()+","+y.getName()+")",x.getRow(),y.getColn());
					}
				}else if(s.charAt(seperator)=='+'){
					Vari x = evaluateoperations(new Vari(s.substring(0, seperator),-1,-1));
					Vari y = evaluateoperations(new Vari(s.substring(seperator+1,s.length()),-1,-1));
					if(x.getRow()==0&&y.getColn()==0&&x.getColn()==0&&y.getColn()==0){
						return new Vari(x.getName()+"+"+y.getName(),0,0);
					} else {
						return new Vari("MatrixOpr("+x.getName()+","+y.getName()+",'+')",x.getRow(),y.getColn());
					}
				} else{
					Vari x = evaluateoperations(new Vari(s.substring(0, seperator),-1,-1));
					Vari y = evaluateoperations(new Vari(s.substring(seperator+1,s.length()),-1,-1));
					if(x.getRow()==0&&y.getColn()==0&&x.getColn()==0&&y.getColn()==0){
						return new Vari(x.getName()+"-"+y.getName(),0,0);
					} else {
						return new Vari("MatrixOpr("+x.getName()+","+y.getName()+",'-')",x.getRow(),y.getColn());
					}
				}
				
			}else{
				if(s.charAt(0)=='('&&s.charAt(s.length()-1)==')'){
					Vari x = evaluateoperations(new Vari(s.substring(1, s.length()-1),-1,-1));
					return new Vari(x.getName(),x.getRow(),x.getColn());
				} else if(s.contains("sqrt")){
					Vari x = evaluateoperations(new Vari(s.substring(s.indexOf("(")+1, s.length()-1),-1,-1));
					if(x.getRow()==0&&x.getColn()==0){
						return new Vari("sqrt("+x.getName()+")",0,0);
					} else if(x.getRow()==1&&x.getColn()==1){
						return new Vari("sqrt("+x.getName()+".values[0][0])",0,0);
					} else{
						return x;
					}
				} else if(s.contains("tr(")){
					Vari x = evaluateoperations(new Vari(s.substring(s.indexOf("(")+1, s.length()-1),-1,-1));
					if(x.getRow()!=0&&x.getColn()!=0){
						return new Vari("TransposeM("+x.getName()+")",x.getRow(),x.getColn());
					} else{
						return x;
					}
				}else{
					String s1="";
					String s2="";
					String s3="";
					String s4="";
					
					if(s.contains("[")){
						s1= s.substring(s.indexOf("(")+1,s.indexOf(",",s.indexOf(",")+1));
						s=s.substring(s.indexOf(",",s.indexOf(",")+1)+1);
						s2 = s.substring(0,s.indexOf(","));
						s=s.substring(s.indexOf(",")+1);
						s3 = s.substring(0,s.indexOf(","));
						s=s.substring(s.indexOf(",")+1);
						s4 = s.substring(0,s.indexOf(")"));
						
						
						
						String a1 = s1.substring(0, s1.indexOf("["));
						s1=s1.substring(s1.indexOf("[")+1);
						String a2 = s1.substring(0,s1.indexOf(","));
						s1=s1.substring(s1.indexOf(",")+1);
						String a3 = s1.substring(0,s1.indexOf("]"));
						s1=s1.substring(s1.indexOf("]")+1);
						String a4 = s1;
						return new Vari("choose("+a1+".values["+a2+"-1"+"]["+a3+"-1]"+a4+","+s2+","+s3+","+s4+")",0,0);
					}else{
						s=s.substring(s.indexOf('(')+1);
						s1 = s.substring(0,s.indexOf(","));
						s=s.substring(s.indexOf(",")+1);
						s2 = s.substring(0,s.indexOf(","));
						s=s.substring(s.indexOf(",")+1);
						s3 = s.substring(0,s.indexOf(","));
						s=s.substring(s.indexOf(",")+1);
						s4 = s.substring(0,s.indexOf(")"));
						return new Vari("choose("+s1+","+s2+","+s3+","+s4+")",0,0);
					}
				}
			}
			
			
		}
		return null;
	}
	/*
	 * Just a method to ease the pain.
	 */
	public static double[] getvalues(String s,int i){
		s = trimString(s, '{');
		String temp ="";
		double[] results = new double[i];
		while(s.charAt(0)==' '){
			s= s.substring(1);
		}
		for(int x= 0;x<i;x++){
			while(s.charAt(0)!=' '&&s.charAt(0)!='}'){
				temp += s.charAt(0);
				s=s.substring(1);
			}
			results[x]=Double.parseDouble(temp);
			while(s.charAt(0)==' '){
				s=s.substring(1);
			}
			
		   temp="";
		}
		return results;
	}
	/*
	 * gets the values of a matrix from matlang and returns the values.
	 */
	public static void evaluateforloop(String s){
		s.replaceAll(" ", "");
		String start ="";
		String end ="";
		String increment = "";
		String temp = "";
		String variable = "";
		String temp2 = "";
		if(s.contains(",")){
			Global.nestedloop = true;
			String start2 ="";
			String end2 = "";
			String increment2 = "";
			String variable2= "";
			
			s = trimString(s,'(');
			while(s.charAt(0)!=','){
				variable += s.charAt(0);
				s = s.substring(1);
			}
			s=s.substring(1);
			while(s.charAt(0)!='i'||s.charAt(1)!='n'){
				variable2 += s.charAt(0);
				s = s.substring(1);
			}
			s=s.substring(2);
			while(s.charAt(0)!=':'){
				start += s.charAt(0);
				s = s.substring(1);
			}
			s=s.substring(1);
			while(s.charAt(0)!=':'){
				end += s.charAt(0);
				s = s.substring(1);
			}
			s=s.substring(1);
			while(s.charAt(0)!=','){
				increment += s.charAt(0);
				s = s.substring(1);
			}
			s=s.substring(1);	
			while(s.charAt(0)!=':'){
				start2 += s.charAt(0);
				s = s.substring(1);
			}
			s=s.substring(1);
			while(s.charAt(0)!=':'){
				end2 += s.charAt(0);
				s = s.substring(1);
			}
			s=s.substring(1);
			while(s.charAt(0)!=')'){
				increment2 += s.charAt(0);
				s = s.substring(1);
			}
			temp = "for (int " +variable + "=" + start +";"+variable + "<=" + end + ";" + variable +  "=" + variable + "+" + increment + "){";
			temp2 = "for (int " +variable2 + "=" + start2 +";"+variable2 + "<=" + end2 + ";" + variable2 +  "=" + variable2 + "+" + increment2 + "){";
					
		} else{
			while(s.charAt(0)!='('){
				s = s.substring(1);
			}
			s = s.substring(1);
			while(s.charAt(0)!='i'||s.charAt(1)!='n'){
				variable += s.charAt(0);
				s = s.substring(1);
			}
			s = s.substring(2);			
			while(s.charAt(0)!=':'){
				start += s.charAt(0);
				s = s.substring(1);
			}
			s=s.substring(1);
			while(s.charAt(0)!=':'){
				end += s.charAt(0);
				s = s.substring(1);
			}
			s=s.substring(1);
			while(s.charAt(0)!=')'){
				increment += s.charAt(0);
				s = s.substring(1);
			}
			temp = "for (int " +variable + "=" + start +";"+variable + "<=" + end + ";" + variable +  "=" + variable + "+" + increment + "){";
			
		}
		Global.output.add(temp);
		Global.output.add(temp2);
		
	}
	/*
	 * A method to trim the strings from the given character.
	 */
	public static String trimString(String s, char c){
		while(s.charAt(0)!=c){
			s = s.substring(1);
		}
		s = s.substring(1);
		return s;
	}
	/*
	 * evaluates declaration of any type.
	 */
	public static void evaluatedeclaration(String s){
		
		String name ="";
		String temp ="";
		String row  ="";
		String coln ="";
		if(s.contains("scalar")){
			s=s.substring(6);
			while(s.charAt(0)==' '){
				s=s.substring(1);
			}
			while(!s.isEmpty()){
				name +=s.charAt(0);
				s=s.substring(1);
			}
			Vari vari = new Vari(name,"float",0,0);
			Global.variables.add(vari);
			temp =vari.type+ " " + vari.name+";";
			
		} else if(s.contains("matrix")){
			s=s.substring(6);
			while(s.charAt(0)==' '){
				s=s.substring(1);
			}
			while(s.charAt(0)!='['){
				name +=s.charAt(0);
				s=s.substring(1);
			}
			s=s.substring(1);
			while(s.charAt(0)!=','){
				row += s.charAt(0);
				s=s.substring(1);
			}
				s=s.substring(1);
			while(s.charAt(0)!=']'){
				coln += s.charAt(0);
				s=s.substring(1);
			}
			Vari vari = new Vari(name, "matrix",Integer.parseInt(row), Integer.parseInt(coln));
			Global.variables.add(vari);
			temp ="struct Matrix " + vari.name + ";" +"setMatrixProps(&" +vari.name+"," + vari.row+","+vari.coln+")"+";";
			
		} else {
			s=s.substring(6);
			while(s.charAt(0)==' '){
				s=s.substring(1);
			}
			while(s.charAt(0)!='['){
				name +=s.charAt(0);
				s=s.substring(1);
			}
			s=s.substring(1);
			while(s.charAt(0)!=']'){
				row += s.charAt(0);
				s=s.substring(1);
			}
			Vari vari = new Vari(name, "vector",Integer.parseInt(row), 1);
			Global.variables.add(vari);
			temp ="struct Matrix " + vari.name + ";" +"setMatrixProps(&" +vari.name+"," + vari.row+","+vari.coln+")"+";";
			
		}	
		
		Global.output.add(temp);
		
	}
	/*
	 * Evaluates the comments in the matlang input to output c
	 */
	public static void evaluatecomment(String s){
		Global.output.add("//"+s.substring(1));
	}
	/*
	 * Handles the given input file, takes the rows of the input file and returns them in an arraylist of type string.
	 */
	public static  ArrayList<String> fileHandler(File f) throws FileNotFoundException{
		Scanner s = new Scanner(f);
		ArrayList<String> lines = new ArrayList<String>(); 
		while(s.hasNext()){
			lines.add(s.nextLine());
			
		}
		s.close();
		return lines;
	}
	/*
	 * uses the writer to create a c output file
	 */
	public static void outputFile(ArrayList<String> output) throws FileNotFoundException, UnsupportedEncodingException{
		
		PrintWriter writer = new PrintWriter("translated.c","UTF-8");
		while(!output.isEmpty()){
		writer.println(output.get(0));
		output.remove(0);		
		}
		writer.close();
		
	}
	/*
	 * forgot what this method does during the implementation
	 */
	public static String getexpression(String s){
		String result = s;
		result.replaceAll(" ", "");
		
		
		return result;
	}
	
}
