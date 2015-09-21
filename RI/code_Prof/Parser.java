package indexation;

import java.io.RandomAccessFile;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import core.*;
import java.io.Serializable;
public abstract class Parser implements Serializable{
	private static final long serialVersionUID = 1L;
	
	protected transient ArrayList<File> files; 
	protected transient int curF=0;
	protected transient RandomAccessFile br;
	protected String begin;
	protected String end;
	
	public Parser(String begin,String end){
		this.begin=begin;
		this.end=end;
	}
	public Parser(String begin){
		this(begin,"");
	}
	
	Document nextDocument(){
		try{
			Document d=null;
			String ligne="";
			boolean ok=false;
			//System.out.println(this.begin);
			while(!ok){
				StringBuilder st=new StringBuilder();
				boolean read=false;
				long start=0;
				long nbBytes=0;
				
				while(true){
					long curOff=br.getFilePointer();
					//System.out.println(curOff);
					ligne=br.readLine();
					if(ligne==null){
						if((this.end.length()==0) && read){
							nbBytes=curOff-start;
							read=false;
							ok=true;
						}
						break;
					}
					//ligne=ligne+"\n";
					//
					if(ligne.startsWith(this.begin)){
						//System.out.println(ligne);
						if((this.end.length()==0) && read){
							nbBytes=curOff-start;
							read=false;
							ok=true;
							br.seek(curOff);
							break;
						}
						else{
							read=true;
							start=curOff;
						}
					}
					if(read){
						st.append(ligne+"\n");
						
					}
					
					if((this.end.length()>0) && (ligne.startsWith(this.end))){
						read=false;
						ok=true;
						nbBytes=br.getFilePointer()-start;
						break;
					}
					
					
				}
				if (ok){
					String s=st.toString();
					//System.out.println(s);
					String from=files.get(curF).getAbsolutePath()+";"+start+";"+nbBytes;
					d=getDocument(s);
					d.set("from", from);
				}
				else{
					br.close();
					curF++;
					if(curF<files.size()){
						br=new RandomAccessFile(files.get(curF),"r");
					}
					else{
						return null;
					}
				}
			}
			
			return d;
		}
		catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	abstract Document getDocument(String str);
	
	public void init(String filename){
		try {
			files=getAllFiles(new File(filename));
			/*for(File ff:files){
				System.out.println(ff.getAbsolutePath());
			}*/
			curF=0;
			br=new RandomAccessFile(files.get(curF),"r");
			
		}
		catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	public ArrayList<File> getAllFiles(File file){
		ArrayList<File> ret=new ArrayList<File>();
		if(file.isDirectory()){
			File[] files=file.listFiles();
			for(File f:files){
				ret.addAll(getAllFiles(f));
			}
		}
		else{
			ret.add(file);
		}
		return ret;
	}
	
	public static Parser getParserCISI_CACM(){
		return new ParserCISI_CACM();
	}
	

}

/**
 * 
 * Format of input files :
 * <Document id=<id>>
 * <Text>
 * </Document>
 *
 */
class Parser1 extends Parser{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Parser1(){
		super("<Document","</Document");
	}
	
	Document getDocument(String str){
		HashMap<String,String> other=new HashMap<String,String>();
		String st[]=str.split("\n");
		String sti[]=st[0].split("=");
		String id=sti[1].substring(0,sti[1].indexOf(">"));
		StringBuilder text=new StringBuilder();
		for(int i=1;i<st.length-1;i++){
			text.append(st[i]+"\n");
		}
		Document doc=new Document(id,text.toString(),other);
		//System.out.println(doc.getId()+" => "+doc.getText());
		return doc;
	}
	
}


/**
 * 
 * Format of input files :
 * .I <id>
 * .T 
 * <Title>
 * .A <Author>
 * .K
 * <Keywords>
 * .W
 * <Text>
 * .X
 * <Links> 
 *
 */
class ParserCISI_CACM extends Parser{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;



	public ParserCISI_CACM(){
		super(".I");
	}
	
	
	
	Document getDocument(String str){
		//System.out.println(str);
		HashMap<String,String> other=new HashMap<String,String>();
		String st[]=str.split("\n");
		boolean modeT=false;
		boolean modeA=false;
		boolean modeK=false;
		boolean modeW=false;
		boolean modeX=false;
		String info="";
		String id="";
		String text="";
		String author="";
		String keyWords="";
		String links="";
		String title="";
		for(String s:st){
			if(s.startsWith(".I")){
				id=s.substring(3);
				continue;
			}
			if(s.startsWith(".")){
				if(modeW){
					text=info;
					info="";
					modeW=false;
				}
				if(modeA){
					author=info;
					info="";
					modeA=false;
				}
				if(modeK){
					keyWords=info;
					info="";
					modeK=false;
				}
				if(modeT){
					title=info;
					info="";
					modeT=false;
				}
				if(modeX){
					other.put("links", links);
					info="";
					modeX=false;
				}
			}
			
			if(s.startsWith(".W")){
				modeW=true;
				info=s.substring(2);
				continue;
			}
			if(s.startsWith(".A")){
				modeA=true;
				info=s.substring(2);
				continue;
			}
			if(s.startsWith(".K")){
				modeK=true;
				info=s.substring(2);
				continue;
			}
			if(s.startsWith(".T")){
				modeT=true;
				info=s.substring(2);
				continue;
			}
			if(s.startsWith(".X")){
				modeX=true;
				continue;
			}
			if(modeX){
				String l[]=s.split("\t");
				if(!l[0].equals(id)){
					links+=l[0]+";";
				}
				continue;
			}
			if((modeK) || (modeW) || (modeA) || (modeT)){
				info+=" "+s;
			}
		}
	
		if(modeW){
			text=info;
			info="";
			modeW=false;
		}
		if(modeA){
			author=info;
			info="";
			modeA=false;
		}
		if(modeK){
			keyWords=info;
			info="";
			modeK=false;
		}
		if(modeX){
			other.put("links", links);
			info="";
			modeX=false;
		}
		if(modeT){
			title=info;
			info="";
			modeT=false;
		}
		other.put("title", title);
		other.put("text", text);
		other.put("author", author);
		other.put("keywords", keyWords);
		
		Document doc=new Document(id,title+" \n "+author+" \n "+keyWords+" \n "+text,other);
		//System.out.println(doc.getId()+" => "+doc.getText());
		return doc;
	}
	
	
	

	
}
