package com.github.dariakuzina.algorithms;
/*import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;*/

/**Realization of weighted quick union algorithm
 * The idea and the biggest part of code is taken from "Algorithms, 4th Edition" by Robert Sedgewick, Kevin Wayne*/
public class WeightedQuickUnion {
	/**Keeps index of parent (component id)*/
	private int[]id;
	/**Keeps size of root's component*/
	private int[]sz;
	/**Number of components*/
	private int count;
	public WeightedQuickUnion(int N){
		count=N;
		id=new int[N];
		for(int i=0;i<N;i++)
			id[i]=i;
		sz=new int[N];
		for(int i=0;i<N;i++)
			sz[i]=1;
	}
	public int getCount(){return count;}
	/**Checks if two vertex are connected (have the same root)
	 * @param p First vertex
	 * @param q Second vertex
	 * @return True if p and q are connected, false otherwise*/ 
	public boolean connected(int p,int q){
		return find(p)==find(q);
	}
	/**Searches for root of p*/
	private int find(int p){
		while (p!=id[p])p=id[p];
		return p;
	}
	/**If p and q are in the same component (have the same root) does nothing
	 * Otherwise chooses from two roots the one with the least component's size and merges two components to component with this root 
	 * @param p First vertex
	 * @param q Second vertex*/
	public void union(int p,int q){
		int i=find(p);
		int j=find(q);
		if(i==j)return;
		if(sz[i]<sz[j]){id[i]=j;sz[j]+=sz[i];}
		else{id[j]=i;sz[i]+=sz[j];}
		count--;
	}
	/*
	public static void main(String[] args) {
		try {
			Scanner in=new Scanner(new File("C:\\Users\\User\\Desktop\\tinyUF.txt"));
			int n=in.nextInt();
			WeightedQuickUnion myWQU=new WeightedQuickUnion(n);
			int p,q;
			while (in.hasNext()) {
				p=in.nextInt();
				q=in.nextInt();
				if(myWQU.connected(p, q))continue;
				myWQU.union(p, q);
				System.out.println(p+" "+q);
			}
			System.out.println(myWQU.getCount()+" components");
			in.close();
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
			e.printStackTrace();
		}
		
	}*/

}

