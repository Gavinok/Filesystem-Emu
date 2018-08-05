/*
 * @CreateTime: May 3, 2018 11:20 AM
 * @Author: Gavin Jaeger-Freeborn
 * @Contact: gavinfre@uvic.ca
 * @Last Modified By: Gavin Jaeger-Freeborn
 * @Last Modified Time: Aug 4, 2018 11:26 AM
 * @Student Number:T00611983
 * @ COMP 3411 Assignment 5
 * @Description: TFSFileSystem is used to controle the general organization and function of the file system using the 
 * TFSDiskInputOutput. this class contains the directory, FAT, and PCB when in memory.
 */
//package MainTFS;
import java.lang.*;
import java.io.*;
import java.util.*;

/* The Block class is used to simplify the use of 128 byte segmints.
When a block is needed simply construct a block with the data requested.
then use gerBlock to get the byte array from the block. setBlock is made private as
to avoid unregistered changes to the Block. */
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
/* The PartianControlBlock class is used to reprecent the PCB. The PCB
holds:
- a pointer to the Root Directory (represented by an int that represents the block it is held at)
- the size of the FAT
- the total number of data blocks
- a pointer to the first free block in the FAT(the list of free blocks is its own class)
- a linked list representation of the free space blocks */
class PartianControlBlock
{
	private freespaceList SpaceList;
	private static final int BlockSixeLimit = 128;
	//PCB takes one block for it's self
	private int RootDirLocation; 
	private int FirstFreeBlock;
	private int SizeofFAT;//number of possible entries(in Blocks)
	private int TotalDataBlocks;
	
	PartianControlBlock(int FirstBlock, int TotalSize)
	{
		FirstBlock = FirstBlock + 1; //for Boot Code Block
		FirstBlock = FirstBlock + 1; // for PCB
		TotalDataBlocks = TotalSize/BlockSixeLimit;
		SizeofFAT = TotalDataBlocks;

		int sizeOfEntrieTable = SizeofFAT * 4;
		int blocksTakenByFAT = sizeOfEntrieTable/BlockSixeLimit;
		FirstBlock = FirstBlock + blocksTakenByFAT; //remove the blocks that will be taken by the FAT
		RootDirLocation = FirstBlock; //the location of the RootDir is just after the FAT
		FirstBlock = FirstBlock + 1; //for the rootDIR
		FirstFreeBlock = FirstBlock; //real first free block index
		SpaceList = new freespaceList(FirstFreeBlock, TotalDataBlocks);

	}

	//convert a Block into a pcb
	PartianControlBlock(Block PCBBlock)
	{
		byte[] PCBbytes = PCBBlock.getBlock();
		String PCBString = new String(PCBbytes);

		for (int i = 0; PCBString.endsWith("*"); i++) {
			PCBString = PCBString.substring(0, PCBString.length() - 1);
		}
		Scanner scanner = new Scanner(PCBString);
		if(scanner.hasNextInt())
		{
			RootDirLocation = scanner.nextInt();
		}
		else
		{
			System.out.println("nop int found");
		}
		if(scanner.hasNextInt())
		{
			FirstFreeBlock = scanner.nextInt();
		}
		else
		{
			System.out.println("nop int found");
		}
		if(scanner.hasNextInt())
		{
			TotalDataBlocks = scanner.nextInt();
		}
		else
		{
			System.out.println("nop int found");
		}
		if(scanner.hasNextInt())
		{
			SizeofFAT = scanner.nextInt();
		}
		else
		{
			System.out.println("nop int found");
		}
		SpaceList = new freespaceList(FirstFreeBlock, TotalDataBlocks);
		scanner.close();
	}
	//converts the PCB into a block that is stored on disk
	public Block toBlock()
	{
		String PCBString = this.getRootDir() + " " + this.checkFirstFreeBlock() + " " + this.getTotalDataBlock() + " " + this.getSizeOfFAT();
		try{
			byte[] PCBData = PCBString.getBytes("UTF-8");//convert the string to bytes
		Block PCBBlock = new Block(PCBData);//convert the bytes to a block
		return PCBBlock;
		}
		catch(UnsupportedEncodingException e)
		{
			System.out.println("error encoding string to bytes");
			byte[] bumbby = new byte[10];
			return new Block(bumbby);
		}
		
	}
	public int getRootDir()
	{
		return RootDirLocation;
	}
	//returns the next free block without removing it from the list
	public int checkFirstFreeBlock()
	{
		int tempBlockHolder = FirstFreeBlock;
		UpdateFirstFreeBlock(SpaceList);
		return tempBlockHolder;
	}
	//returnt the index of the first free block and removes it from the list
	public int getFirstFreeBlock()
	{
		int tempBlockHolder = FirstFreeBlock;
		UpdateFirstFreeBlock(SpaceList);
		SpaceList.getFreeBlock();
		return tempBlockHolder;
	}
	//checks what the next avalable free block is
	private void UpdateFirstFreeBlock(freespaceList FreeSpace)
	{
		FirstFreeBlock = FreeSpace.checkFreeBlock();
	}

	/* when a blocks data is no longer needed it is added to the freespaceList */
	public void addFreeBlock(int IndexOfBlock)
	{
		SpaceList.addBlock(IndexOfBlock);
	}

	public int getTotalDataBlock()
	{
		return TotalDataBlocks;
	}
	public int getSizeOfFAT()
	{
		return SizeofFAT;
	}
	//returns the total free blocks
	public int getNumberOfFreeBlocks()
	{
		return SpaceList.size();
	}
	//convert the PCB into a string that can be read by the user
	public String toString()
	{
		String PCBString = "Root directory location it block number " + this.getRootDir() + " First free block is at block number " + this.checkFirstFreeBlock() + " Size of fat is " + this.getSizeOfFAT() + " total number blocks is " + this.getTotalDataBlock();
		return PCBString;
	}
	//A linked list representation of the free blocks in the TFSDiskFile
	private class freespaceList
	{
		private LinkedList<Integer> FreeSpaceLinkedList = new LinkedList<Integer>();

		freespaceList(int FirstFreeBlock, int lastBlock)
		{
			for(int i = FirstFreeBlock; i <= lastBlock; i++)
			{
				FreeSpaceLinkedList.push(new Integer(i));
			}
		}
		void addBlock(int FreeBlockLocation)
		{
			FreeSpaceLinkedList.push(new Integer(FreeBlockLocation));
		}
		int getFreeBlock()
		{
			if(!(FreeSpaceLinkedList.size() == 0))
			{
				return FreeSpaceLinkedList.pop();
			}
			else
			{
				System.out.println("no more free space");
				return -1;
			}
			
		}
		int checkFreeBlock()
		{
			if(!(FreeSpaceLinkedList.size() == 0))
			{
				return FreeSpaceLinkedList.peekLast();
			}
			else
			{
				System.out.println("no more free space");
				return -1;
			}
		}
		int size()
		{
			return FreeSpaceLinkedList.size();
		}
	}
}
/* The FileAllocationTable class is used to reprecent the FAT. The FAT
holds:
- an entrie table consisting of the location on the next block in this file
- a Block table consisting of the block associated with each location on the EntrieTable
- total number of entries in the table*/
class FileAllocationTable
{
	private int[] EntrieTable; 
	private Block[] BlockTable;
	private int tableSize; // the total number of entries avalable
	private static final int BlockSixeLimit = 128;

	FileAllocationTable(int TableSize){
		EntrieTable = new int[TableSize];
		BlockTable =  new Block[TableSize];
	}

	//converts an array of blocks to a new FileAllocationTable
	FileAllocationTable(Block[] updatedFATBlocks){

		
		System.out.println("Decoding FAT");
		byte[] RetrevedFATData = new byte[BlockSixeLimit];//convert the string to bytes
		Boolean StringCompletedFlag = true; 
		int oldLength = 0;
		String DumbbyString = new String();
		String StringRepresentation =  new String();
		for(int i = 0; StringCompletedFlag; i++)
		{
			try{
				DumbbyString = new String(updatedFATBlocks[i].getBlock(), "UTF-8");
				if(DumbbyString.endsWith("*"))
				{
					int counter = 0;
					while(DumbbyString.endsWith("*"))
					{
						counter++;
						DumbbyString = DumbbyString.substring(0, DumbbyString.length() - 1);
						StringCompletedFlag = false;
					}
					System.out.println("String complete");
					System.arraycopy(DumbbyString.getBytes(), 0, RetrevedFATData, oldLength, DumbbyString.getBytes().length);
					RetrevedFATData =  Arrays.copyOf(RetrevedFATData, RetrevedFATData.length - counter);
					StringRepresentation = new String(RetrevedFATData, "UTF-8");

				}else{
					System.arraycopy(updatedFATBlocks[i].getBlock(), 0, RetrevedFATData, oldLength, updatedFATBlocks[i].getBlock().length);
					oldLength = RetrevedFATData.length;
					RetrevedFATData =  Arrays.copyOf(RetrevedFATData, RetrevedFATData.length + BlockSixeLimit);	

				}
			}
			catch(Exception e)
			{
				System.out.println("failed to decode FAT");
				while(true){}
			}
		}
		this.StringtoEntrietable(StringRepresentation);
		System.out.println("done Decoding FAT");

	} 
	/* changes both the block and next entry at this index */
	void changeBlockAndEntrie(int Index, int Entrie, Block NewBlock)
	{
		setBlockTotable(Index, NewBlock);
		ChangeEntrie(Index, Entrie);
	}
	
	//updates the entrie table based on the String entered.
	private void StringtoEntrietable(String StringRep)
	{
		StringRep = StringRep.substring(1, StringRep.length() - 1);
		// The string you want to be an integer array.
		String[] intStrings = StringRep.split(", ");
		int[] values = new int[intStrings.length];
		for (int i = 0; i < values.length; i++){
			values[i] = Integer.parseInt(intStrings[i]);
		}
		this.EntrieTable = values;
	}
	void setBlockTotable(int Index, Block NewBlock)
	{
		BlockTable[Index] = NewBlock;
	}

	Block[] toBlocks()
	{
		System.out.println("creating FAT Block");
		String FATString  = Arrays.toString(this.getEntryTable());
		byte[] FATData = FATString.getBytes();//convert the string to bytes
		
		Block[] FATBlocks = new Block[(FATData.length/BlockSixeLimit) + 1];
		for(int i = 0; i < FATBlocks.length; i++)
		{
			int BytesinBlocks = i * BlockSixeLimit;
			if((BytesinBlocks + BlockSixeLimit) < FATData.length)
			{
				byte[] fatblock = Arrays.copyOfRange(FATData, BytesinBlocks, BytesinBlocks + BlockSixeLimit);
				FATBlocks[i] = new Block(fatblock);
			}
			else{
				byte[] fatblock = Arrays.copyOfRange(FATData, BytesinBlocks, FATData.length);
				FATBlocks[i] = new Block(fatblock);
			}

		} 	
		System.out.println("done creating FAT Block");
		return FATBlocks; 
	}

	/* returns the block located at the specified index */
	Block getBlockFromTable(int Index)
	{
		return BlockTable[Index];
	}

	void ChangeEntrie(int Index, int Entrie)
	{
		EntrieTable[Index] = Entrie;
	}
	/*returns the location of the next block in the FAT.*/
	Integer getEntrieFromTable(int Index)
	{
		return EntrieTable[Index];
	}
	/*returns a pointer to the EntrieTable.*/
	int[] getEntryTable()
	{
		return EntrieTable;
	}
	public String toString()
	{
		String FATString = " here is the FAT as a String SIZE of ENTRY TABLE " + tableSize + " ENTRY TABLE:" + Arrays.toString(EntrieTable);
		return FATString;
	}

}
/* the Directory holds the following
- a FileAttributes which describes the directory
- a linked list of FileAttributess that each represent a file */
class Directory
{
	private FileAttributes DirectoryInfo;
	private LinkedList<FileAttributes> DirectList = new LinkedList<FileAttributes>();
	public Directory(FileAttributes fileDetails)
	{
		DirectoryInfo = fileDetails;
	}
	Directory(boolean isfile, String FileName, int startBlock, int fileSize)
	{
		DirectoryInfo = (new FileAttributes(isfile,  FileName,  startBlock,  fileSize));
	}
	void addFile(String FileName, int startBlock, int fileSize)
	{
		DirectList.add(new FileAttributes( true, FileName, startBlock, fileSize));
	}
	public void addDirectory(String FileName, int startBlock, int fileSize)
	{
		DirectList.add(new FileAttributes( false ,  FileName, startBlock, fileSize));
	}
	
	FileAttributes getFile(String fileName)
	{
		int index = 0;
		while(index < DirectList.size())
		{
			if(DirectList.get(index).getName().equals(fileName))
			{
				return DirectList.get(index);
			}
			index++;
		}
		return null;
	}
	int findIndex(String fileName)
	{
		int index = 0;
		while(index < DirectList.size())
		{
			if(DirectList.get(index).getName().equals(fileName))
			{
				return index;
			}
			index++;
		}
		return 0;
	}
	FileAttributes getDirectoryInfo()
	{
		return DirectoryInfo;
	}
}
/*  the FileAttributes class holds the following infermation(based on the Project requirments):
	- if it is a file or directory
	- the name of the file/directory as an array of 15 bytes 
	- the first block in the FAT for this file(if its a file)
	- the file name as a string
	- the length of the name
	- the size of the file/directory */
class FileAttributes
{
	private boolean file;
	private int FirstBlock;// index of the first block in this file
	private byte[] nameOfFile = new byte[15]; // name of the file limited to 15 bytes as requested
	private String fileName;
	private int NameLength;//length of the name
	private int size;
	FileAttributes(boolean isfile, String FileName, int startBlock, int fileSize)
	{
		if(isfile)
		{
			System.out.println("this is a file");
			try {
				if(FileName.getBytes().length <= 15)
				{
					file = isfile;
					fileName = FileName;
					nameOfFile = FileName.getBytes();
					size = fileSize;
					FirstBlock = startBlock;
					NameLength = FileName.length();
				}
				else
				{
					System.out.println("name is too long");
				}
			} catch (Exception e) {
				System.out.println("failed in FileAttributes(boolean isfile, String FileName, int startBlock, int fileSize)");
			}
		}
		else
		{
			System.out.println("this is a Directory");
			if(FileName.getBytes().length <= 15)
			{
				file = isfile;
				try {
					
					nameOfFile = FileName.getBytes();
				} catch (Exception e) {
					System.out.println("failed at if(FileName.getBytes().length <= 15)");
				}
				size = fileSize;
				FirstBlock = startBlock;
				NameLength = FileName.length();
			}
			else{
				System.out.println("name is too long");
			}
		}
	}
	String getName()
	{
		return fileName;
	}
}

public class TFSFileSystem
{
	//main-------------------------------------------------------------------------
	 public static void main(String[] args){
		TFSFileSystem fs = new TFSFileSystem();
		System.out.println(fs.tfs_mkfs());// if a 0 is printed no errors occur if its 1 then the filesystem already existed
		//Block[] test =  FAT.toBlocks();
		//FileAllocationTable newFAT = new FileAllocationTable(test);
		System.out.println(fs.tfs_umount());
		System.out.println(fs.tfs_mount());
		System.out.println(fs.tfs_prmfs());// if a null is printed an errors occur since 
		//System.out.println(fs.tfs_exit());// the file should already be closed since it was not left open
	 }
	//main-------------------------------------------------------------------------


	private static TFSDiskInputOutput HardDisk;
	//the limit to how large a new file can be
	private static final int BlockSixeLimit = 128;
	private static final int FileSizeLimit = 65535;
	static PartianControlBlock PCB;
	private static FileAllocationTable FAT;
	private static Directory RootDIR;
	private static byte[] DiskFileName;
	private static int DiskFileNameLength;
	private static byte[] dummbyBlock = new byte[BlockSixeLimit];// this is an empty Block meant to be 
	
	/*
	 * TFS Constructor
	 */	


	public void TFSFileSystem()
	{
	}
	
	
	/*
	 * TFS API
	 */
	// - Create the file system on the hard disk 
/* 	- Initialize PCB and FAT in disk*/
	//returns 0 if no errors occured
	public static int tfs_mkfs()
	{
		int MKFSReturn = 1;
		String TrueDiskFileName = "TFSDiskFile";
		HardDisk = new TFSDiskInputOutput();//- Create the file system on the hard disk
		DiskFileName = TrueDiskFileName.getBytes();
		DiskFileNameLength = TrueDiskFileName.length();
		//create a File System on Harddrive;
		MKFSReturn = HardDisk.tfs_dio_create(DiskFileName, DiskFileNameLength, FileSizeLimit);
		PCB = new PartianControlBlock(0, FileSizeLimit);//PCB in disk -> memory
		FAT = new FileAllocationTable(PCB.getSizeOfFAT());
		MKFSReturn = tfs_sync();//wright the PCB and FAT to disk
		//not that the free blocks to store the FAT are used we can now store the RootDir next to it
		//the root directory will be given the name "/" as stated in the requirments
		String RootNameString = "/";
		MKFSReturn = tfs_create_dir(RootNameString.getBytes(), RootNameString.length()); //create the root directory
		return MKFSReturn;
	}
	//- Mount the file system 						
/* 	- PCB in disk -> memory 
	- FAT in disk -> memory */
	//NOT FULLY IMPLEMENTED
	//for now it will always return 0
	public static int tfs_mount()
	{
		System.out.println("Mounting PCB and FAT from disk into memory");
		Block PCBBlock = new Block();
		byte[] PCBBbytes = new byte[BlockSixeLimit];
		int MountResult = 0;
		MountResult = HardDisk.tfs_dio_open(DiskFileName, DiskFileNameLength);
			// we start at block 1 instaead of 0 since the BCB is meant to be there.
			// assert that the block can be properly read.
			if(_tfs_read_block(1, dummbyBlock) == -1)
			{
				System.out.println("failed mounting");
				MountResult = -1;
			}
			else{

				//read the PCB into memory
				_tfs_read_block(1, PCBBbytes);
				PCBBlock = new Block(PCBBbytes);

				//Update the PCB
				PCB = new PartianControlBlock(PCBBlock);
				String PCBString = PCB.toString();
				System.out.println(PCBString);

				//reading FAT into memory
				byte[] testingBlock = new byte[BlockSixeLimit];
				FileAllocationTable testingFAT =  new FileAllocationTable(PCB.getSizeOfFAT());
				int NumberOfFATBlocks = testingFAT.toBlocks().length;
				Block[] UpdatedFATBlocks = new Block[NumberOfFATBlocks];
				for (int i = 0; i < NumberOfFATBlocks; i++) {
					if(_tfs_read_block(2+i, dummbyBlock) == -1)
					{
						System.out.println("failed to mount");

						MountResult = -1;
					}else{
						//if the block can be read store it in testingBlock which is then made into a block in the array of UpdatedFATBlocks
						_tfs_read_block(2+i, testingBlock);
						UpdatedFATBlocks[i] = new Block(Arrays.copyOf(testingBlock, testingBlock.length));
					}
				}
				//update the FAT
				FAT = new FileAllocationTable(UpdatedFATBlocks);

			}
		return MountResult; 
	}			
	//- Unmount the file system 		
/* 	- PCB in memory -> disk 
	- FAT in memory -> disk  */
	public static int tfs_umount()
	{
		int unmountReturn = -1;
		System.out.println("unmounting the File System");
		unmountReturn = tfs_sync();
		return unmountReturn;
	}					
	//- Synchronize the file system 
/* 	- PCB in memory -> disk
	- FAT in memory -> disk  */
	//NOT FULLY IMPLEMENTED
	//for now it will always return 0
	public static int tfs_sync()	
	{
		int SYNCReturn = 0;
		System.out.println("wrighting pcb and fat from memory to disk");
		//for now it will always return 0 
		Block PCBBlock = PCB.toBlock();

		
		SYNCReturn = HardDisk.tfs_dio_open(DiskFileName, DiskFileNameLength);
			
		// we start at block 1 instaead of 0 since the BCB is meant to be there.
		//make sure the block can be read properly
		if(_tfs_read_block(1, dummbyBlock) == 0)
			{
			_tfs_write_block(1,  PCBBlock.getBlock());
			_tfs_read_block(1, dummbyBlock);
			String bro = new String(dummbyBlock);
			for (int i = 0; bro.endsWith("*"); i++) {
				bro = bro.substring(0, bro.length() - 1);
			}
			try{
				//check that the pcb has not been changed
				byte[] test = bro.getBytes("UTF-8");
				bro = new String(test, "UTF-8");
			}
			catch(UnsupportedEncodingException e)
			{
				System.out.println("failed to decript");
			}
			}
			else{
				System.out.println("failed to wright pcb");
				SYNCReturn = -1;
			}

			//Write FAT to Disk
			Block[] FATBlocks =  FAT.toBlocks();
			//make sure the block can be read properly
			System.out.println("wrighting FAT");

			//wright the FAT right after the PCB on disk
			for(int i = 0; i <(FATBlocks.length); i++) {
				if(_tfs_read_block(i+2, dummbyBlock) == 0)
				{
					SYNCReturn = _tfs_write_block(i+2,  FATBlocks[i].getBlock());
				}else{
					SYNCReturn = -1;
				}
			}
		if(SYNCReturn == -1)
			System.out.println("failed to sync");
		return SYNCReturn;
	}						
	//- Print PCB and FAT from the file system
	//returns null if there was an error
	public static String tfs_prrfs()	
	{
		System.out.println("printing printing the PCB and FAT from memory into a String");
		String PrrfsReturn;
		PartianControlBlock MainMemoryPCB = PCB;
		FileAllocationTable MainMemoryFAT = FAT;
		tfs_mount();
		PrrfsReturn = tfs_prmfs();
		tfs_umount();
		PCB = MainMemoryPCB;
		FAT = MainMemoryFAT;
		System.out.println("tfs_prrfs complete");
		return PrrfsReturn;
	}			
	//- Print PCB and FAT from the main memory 
	public static String tfs_prmfs()
	{
		System.out.println("printing the PCB and FAT from memory into a String");
		String PCBandFATString = PCB.toString() + FAT.toString(); 
		return PCBandFATString;
	}
	//originally missing from provided file
	/* - tfs_umount() 
	   - tfs_dio_close()  */
	public static String tfs_exit()
	{
		tfs_umount();
		System.out.println("closing file system");
		HardDisk.tfs_dio_close();
		return null;
	}
	//- Open the given file 
/* 	- Search the first block number: PCB in memory -> FAT in memory 
	- Create a new entry in FDT  */
	public static int tfs_open(byte[] name, int nlength)
	{
		return _tfs_open_fd(name, nlength);
	}			
	//- Read bytes from disk into buf 
/* 	File descriptor -> FDT -> FAT in memory -> Disk*/
	public static int tfs_read(int file_id, byte[] buf, int blength)	
	{
		return -1;
	}
	//- Write bytes from buf into disk 
/*	File descriptor -> FDT -> FAT in memory -> Disk*/
	public static int tfs_write(int file_id, byte[] buf, int blength)
	{
		return -1;
	}	
	//- Set the new file pointer
	public static int tfs_seek(int file_id, int position)
	{
		return -1;
	}	
	//- Close the given file 
/* 	- Delete the entry in FDT  */
	public static void tfs_close(int file_id)
	{
		_tfs_close_fd(file_id);
		return;
	}			
	//- Create the given file 
	public static int tfs_create(byte[] name, int nlength)
	{

		return -1;
	}		
	//- Delete the given file 
	public static int tfs_delete(byte[] name, int nlength)		
	{
		return -1;
	}	
	//- Create a directory
	//NOT FULLY IMPLEMENTED
	//for now it will always return 0 
	public static int tfs_create_dir(byte[] name, int nlength)	
	{
		String newDirName = new String(name);
		if(newDirName.equals("/"))
		{
			System.out.println("creating Root Directory");
		}
		else{
			System.out.println("creating subDirectory" + newDirName);
		}
		return 0;
	}	
	//- Delete a directory 
	public static int tfs_delete_dir(byte[] name, int nlength)	
	{
		return -1;
	}	
	
	
	/*
	 * TFS private methods to handle in-memory structures
	 */
	// 
 	private static int _tfs_read_block(int block_no, byte buf[])
 	{
 		return HardDisk.tfs_dio_read_block(block_no, buf);
 	}
 	
 	private static int _tfs_write_block(int block_no, byte buf[])
 	{
		return HardDisk.tfs_dio_write_block(block_no,  buf);
 	}
 	
 	private static int _tfs_open_fd(byte name[], int nlength)
 	{
 		return -1;
 	}
 	
 	private static int _tfs_seek_fd(int fd, int offset)
 	{
 		return -1;
 	}

 	
 	private static void _tfs_close_fd(int fd)
 	{
 		return;
 	}
 	
 	private static int _tfs_get_block_no_fd(int fd, int offset)
 	{
 		return -1;
 	}
}