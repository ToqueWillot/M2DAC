package com.fulldeep.indexation;

import java.io.RandomAccessFile;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

//import core.*;
import java.io.Serializable;
/**
 *
 * Format of input files :
 * <Document id=<id>>
 * <Text>
 * </Document>
 *
 */
 public class Parser1 extends Parser{

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
