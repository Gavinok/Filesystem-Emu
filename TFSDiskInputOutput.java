/*
 * @CreateTime: May 3, 2018 11:20 AM
 * @Author: Gavin Jaeger-Freeborn
 * @Contact: gavinfre@uvic.ca
 * @Last Modified By: Gavin Jaeger-Freeborn
 * @Last Modified Time: May 3, 2018 11:22 AM
 * @Student Number:T00611983
 * @ COMP 3411 Assignment 5
 * @Description: TFSDiskInputOutput is used to dirrectly access the TFSDiskFile
 * when unsing the TFSFileSystem and TFSShell 
 */

//package IO;
import java.io.*;
import java.util.*;

class Block<T>{
	
	byte[] BlockContents = new byte[128];
	int SizeLimit = 128;
	Block(byte[] NewContents)
	{
		this.setBlock(NewContents);
	}
	Block()
	{
	}
	/**
	 * @return the sizeLimit
	 */
	public int getSizeLimit() {
		return SizeLimit;
	}
	byte[] getBlock()
	{
			return BlockContents;
	}
	
	private int setBlock(byte[] NewContents){
		if(NewContents.length < 128)
		{

			System.out.println("block is less then 128 bytes");
			
			try {				
				String tempString = new String(NewContents, "UTF-8");
			
				while(tempString.length() < 128)
				{
					tempString =  tempString + "*";
				}
				System.out.println("block has been writen");

				try {
					BlockContents = tempString.getBytes("UTF-8");
				} catch (Exception e) {
					System.out.println("failed at setBlock(byte[] NewContents){");
					return 0;
				}
			
				return 1;
			} catch (Exception e) {
				System.out.println("failed at private int setBlock(byte[] NewContents){");
				return 2;
			}
		}
		if(NewContents.length > 128)
		{
			System.out.println("Block is too big");
			return -1;
		}
		else{
			BlockContents = NewContents;
			return 0;
		}
	}
}
public class TFSDiskInputOutput
{
	//main-------------------------------------------------------------------------
	public static void main(String[] args){
			 }
	//main-------------------------------------------------------------------------

	//the limit to how large a new file can be
	private static final int BlockSixeLimit = 128;
	private static final int FileSizeLimit = 65535;
	private static RandomAccessFile GlobalRAF = null;
	private static File CurrentFile = null;
	public static int CurrentFileSize = 0;
	public static byte[] CurrentBlock = new byte[BlockSixeLimit];
	/* - Create a disk file of (size blocks). The disk file is a real file in your system, in which TFS is implemented. 
	- Return 0 if there is no error.  */
	
	public static int tfs_dio_create(byte[] name, int nlength, int size)
	{
		RandomAccessFile raf = null;
		if((FileSizeLimit >= size) && (nlength <= 15))
		{
			int totalsize = (size*BlockSixeLimit);
			try
			{					
					String convertedToString = new String(name);
					File NewFile = new File(convertedToString);
				 
				if(NewFile.exists()) // make sure it doesnt already exist
				{
					System.out.println("IO: file already exists");
					return 1;
				}
				else
				{
					NewFile.createNewFile();
					raf = new RandomAccessFile(NewFile, "rw");
					raf.setLength(totalsize);
					System.out.println("IO: TFSDiskFile created");
				}
			}
			// file error
			catch (Exception  e) 
			{  
   				System.out.println("IO: Uh oh, got an IOException error!" + e.getMessage()); 
				return -1;
			}
			finally 
			{  
				// if the file opened okay, make sure we close it  
				if (raf != null) 
				{  
					try { raf.close(); }  
					catch (IOException ioe) { 
						System.out.println("IO: failed to close file"); 
						return -1;
					} 
				} 
			} 

		}
		else
		{
			String convertedToString = new String(name);
			System.out.println("IO: unsutable name of file size");
			return -1;
		}
		return 0;
	}	
	/* - Open a disk file 
	- Return 0 if there is no error. 	 */
	public static int tfs_dio_open(byte[] name, int nlength)
	{
		try
		{					
				String convertedToString = new String(name);
				CurrentFile = new File(convertedToString);
				
			if(CurrentFile.exists()) // make sure it doesnt already exist
			{
				GlobalRAF = new RandomAccessFile(CurrentFile, "rw");
				CurrentFileSize = (int)(GlobalRAF.length()/BlockSixeLimit);
				
			}
			else
			{
				System.out.println("IO: file does not exist"); 
				return -1;
			}
		}
		// file error
		catch (Exception  e) 
		{  
			System.out.println("IO: Uh oh, got an IOException error!" + e.getMessage()); 
			return -1;
		}
		return 0;
	}			
	/* 	- Get the total # of blocks of the disk file 
	- Return 0 if there is no error. 
	*/	
	public static int tfs_dio_get_size()
	{
		if(!(GlobalRAF == null))
		{
			try{
				CurrentFileSize = (int)(GlobalRAF.length()/BlockSixeLimit);
				
			}
			catch(IOException e)
			{
				System.out.println("IO: failed to determin size");
				return -1;
			}
			return (0);
		}
		else{
			System.out.println("IO: no file is open");
			return -1;
		}
	}							
/* 	- Read a block from the disk file 
	- Return 0 if there is no error.  */
	public static int tfs_dio_read_block(int block_no, byte[] buf)
	{
			if(!(GlobalRAF == null))
			{
				try{
					if(((block_no*BlockSixeLimit)+ BlockSixeLimit)< GlobalRAF.length())
					{
						GlobalRAF.seek(block_no*BlockSixeLimit);
						GlobalRAF.readFully(buf, 0, BlockSixeLimit);
						CurrentBlock = buf;
					}
				}
				catch(IOException e)
				{
					System.out.println("IO: Failed to read block");
					return -1;
				}
				catch(NullPointerException e)
				{
						System.out.println("IO: Block is currently empty");
					return 0;
				}
				return 0;
			}
			else
			{
				System.out.println("IO: no file is open");
				return -1;
			}
	
	}
/* 	- Write a block into the disk file 
	- Return 0 if there is no error.  */
	public static int tfs_dio_write_block(int block_no, byte[] buf)	
	{
		if(!(GlobalRAF == null))
		{
			try{
				GlobalRAF.seek(block_no*BlockSixeLimit);
				/* byte[] temp = new byte[BlockSixeLimit];
				temp = Arrays.copyOf(buf,BlockSixeLimit); */
				Block tempBlock = new Block(buf);
				GlobalRAF.write(tempBlock.getBlock());
			}
			catch(IOException e)
			{
				System.out.println("IO: failed to read block");
				return -1;
			}
			catch(IndexOutOfBoundsException e)
			{
				System.out.println("IO: this block number does ");
			}
			//System.out.println("IO: Block was wrighten ");
			return 0;
		}
		else
		{
			System.out.println("IO: no file is open");
			return -1;
		}
	}

/* 	- Close the disk file 
	- Return 0 if there is no error.  */
	public static int tfs_dio_close()		
	{
		if(!(GlobalRAF == null))
		{
			try{
				GlobalRAF.close();
				GlobalRAF = null;
				
			}
			catch(IOException e)
			{
				System.out.println("IO: failed to read block");
				return -1;
			}
			return 0;
		}
		else
		{
			System.out.println("IO: file is already closed");
			return -1;
		}
	}		
}