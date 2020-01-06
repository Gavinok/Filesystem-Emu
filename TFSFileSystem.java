/*
 * @CreateTime: May 3, 2018 11:20 AM
 * @Author: Gavin Jaeger-Freeborn
 * @Contact: gavinfre@uvic.ca
 * @Last Modified By: Gavin Jaeger-Freeborn
 * @Last Modified Time: Aug 4, 2018 11:26 AM
 * @Student Number:T00611983
 * @ COMP 3411 Assignment 6
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
	public byte[] getBlock()
	{
		return BlockContents;
	}
	public String toString()
	{
		return new String(BlockContents);
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
					//System.out.println(new String(BlockContents));
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
		// gives the index of a free block by removing it
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
		// gives the index of a free block without removing it
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
	int getFirstBlock()
	{
		//1 block reserver + 1 block for pcb + the blocks taken by the fat
		FileAllocationTable tempFat = new FileAllocationTable(this.getSizeOfFAT());
		Block[] tempBlock = tempFat.toBlocks();
		return tempBlock.length + 2;
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
		this.setBlock(Index, NewBlock.getBlock());
		this.setEntrie(Index, Entrie);
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
	// block at the specified index.
	void setBlockTotable(int Index, Block NewBlock)
	{
		BlockTable[Index] = NewBlock;
	}
	//converts the FAT into a block to be written on disk
	public Block[] toBlocks()
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
	Block getBlock(int Index)
	{
		return BlockTable[Index];
	}
	/**
	 * @param entrieTable the entrieTable to set
	 */
	public void setBlock(int index, byte[] entrie) {
		BlockTable[index] = new Block(entrie);
	}

	void setEntrie(int Index, int Entrie)
	{
		EntrieTable[Index] = Entrie;
	}
	/*returns the location of the next block in the FAT.*/
	Integer getEntrie(int Index)
	{
		return EntrieTable[Index];
	}
	/*returns a pointer to the EntrieTable.*/
	int[] getEntryTable()
	{
		return EntrieTable;
	}
	
	//converts the FAT into a String
	public String toString()
	{
		String FATString = " here is the FAT as a String SIZE of ENTRY TABLE " + tableSize + " ENTRY TABLE:" + Arrays.toString(EntrieTable);
		return FATString;
	}

}
/* the Directory holds the following
- a FileAttributes which describes the directory
- [old]a linked list of FileAttributess that each represent a file 
- [new]a  linked list of Directorie since a directory alreadt specifies weither it is a file or not
and contains its own linked list this allows me to implement more directories below sub directories*/
class Directory
{
	private FileAttributes DirectoryInfo;
	private LinkedList<Directory> DirectList = new LinkedList<Directory>();

	public Directory(FileAttributes fileDetails)
	{
		DirectoryInfo = fileDetails;
	}
	Directory(String FileName, int isDir, int startBlock, int fileSize, int ParentBlock)
	{
		DirectoryInfo = (new FileAttributes(FileName,  isDir,  startBlock,  fileSize, ParentBlock));
	}
	/* void addFile(String FileName, int startBlock, int fileSize)
	{
		DirectList.add(new FileAttributes( true, FileName, startBlock, fileSize));
	} */
	public int getisDir()
	{
		return DirectoryInfo.getisDir();
	}
	public int getSize()
	{
		return DirectoryInfo.getSize();
	}
	public int getFirstBlock()
	{
		return DirectoryInfo.getFirstBlock();
	}
	public void addDirectory(Directory dir)
	{
		DirectList.add(dir);
	}
	public String getName()
	{
		return DirectoryInfo.getName();
	}
	//returns the directory with this name
	Directory getFile(String fileName)
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
	Directory getFile(int index)
	{
		return DirectList.get(index);
	}
	int getLinkedListSize()
	{
		return DirectList.size();
	}
	int getParentFirstBlock()
	{
		return DirectoryInfo.getParentFirstBlock();
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
	private int isDir;
	private int FirstBlock;// index of the first block in this file
	private int ParentFirstBlock; // index of the first block in this parent directory -1 if root
	private byte[] nameOfFile = new byte[15]; // name of the file limited to 15 bytes as requested 
	private String fileName;
	private int NameLength;//length of the name
	private int size;
	FileAttributes(String FileName, int newisDir, int startBlock, int fileSize, int ParentBlock)
	{
		if(newisDir == 0)
		{
			System.out.println("this is a file");
			try {
				if(FileName.getBytes().length <= 15)
				{
					isDir = newisDir;
					fileName = FileName;
					nameOfFile = FileName.getBytes();
					size = fileSize;
					FirstBlock = startBlock;
					NameLength = FileName.length();
					ParentFirstBlock = ParentBlock;
				}
				else
				{
					System.out.println("name is too long");
				}
			} catch (Exception e) {
				System.out.println("failed in FileAttributes())");
			}
		}
		else
		{
			System.out.println("this is a Directory");
			if(FileName.getBytes().length <= 15)
			{
				isDir = newisDir;
				try {
					
					nameOfFile = FileName.getBytes();
				} catch (Exception e) {
					System.out.println("failed at if(FileName.getBytes().length <= 15)");
				}
				size = fileSize;
				FirstBlock = startBlock;
				NameLength = FileName.length();
				ParentFirstBlock = ParentBlock;
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
	int getisDir()
	{
		return isDir;
	}
	int getFirstBlock()
	{
		return FirstBlock;
	}
	int getParentFirstBlock()
	{
		return ParentFirstBlock;
	}
	int getNameLength()
	{
		return NameLength;
	}
	byte[] getFileNameRaw()
	{
		return nameOfFile;
	}
	int getSize()
	{
		return size;
	}
	/**
	 * @param firstBlock the firstBlock to set
	 */
	public void setFirstBlock(int firstBlock) {
		this.FirstBlock = firstBlock;
	}
}
/* the fdt only holds
- a ArrayList of FileAttributes that each represent an open file making if function similar to how the directory used to*/
class fdt
{
	private ArrayList<FileAttributes> AttributeList = new ArrayList<FileAttributes>();
	fdt()
	{}
	FileAttributes get(int index)
	{
		return AttributeList.get(index);
	}
	void add(FileAttributes attributes)
	{
		AttributeList.add(attributes);
	}
	FileAttributes remove(int index)
	{
		return AttributeList.remove(index);
	}
}
public class TFSFileSystem
{
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
	private static fdt FDT = new fdt();
	/*
	 * TFS Constructor
	 */	

	private static int tfs_wright_data(int StartingBlock, byte[] data)
	{
		int WrightDataReturn = 0;
		Block[] DataBlocks = new Block[(data.length/BlockSixeLimit) + 1];
		for(int i = 0; i < DataBlocks.length; i++)
		{
			int BytesinBlocks = i * BlockSixeLimit;
			if((BytesinBlocks + BlockSixeLimit) < data.length)
			{
				byte[] tempBlockBytes = Arrays.copyOfRange(data, BytesinBlocks, BytesinBlocks + BlockSixeLimit);
				DataBlocks[i] = new Block(tempBlockBytes);
			}
			else{
				byte[] tempBlockBytes = Arrays.copyOfRange(data, BytesinBlocks, data.length);
				DataBlocks[i] = new Block(tempBlockBytes);
			}

		} 	

		for(int i = 0; i <(DataBlocks.length); i++) {
			if(_tfs_read_block(i+StartingBlock, dummbyBlock) == 0)
			{
				WrightDataReturn = _tfs_write_block(i+StartingBlock,  DataBlocks[i].getBlock());
			}else{
				WrightDataReturn = -1;
			}
		}
		return WrightDataReturn;
	}

	private static byte[] tfs_read_data(int StartingBlock, int NumberOfDataBlocks)
	{
		byte[] testingBlock = new byte[BlockSixeLimit];
		//reading data into memory
		Block[] UpdatedDataBlocks = new Block[NumberOfDataBlocks];
		for (int i = 0; i < NumberOfDataBlocks; i++) {
			if(_tfs_read_block(StartingBlock +i, dummbyBlock) == -1)
			{
				System.out.println("failed to mount");
				while(true){}
			}else{
				//if the block can be read store it in testingBlock which is then made into a block in the array of UpdatedFATBlocks
				_tfs_read_block(StartingBlock+i, testingBlock);
				UpdatedDataBlocks[i] = new Block(Arrays.copyOf(testingBlock, testingBlock.length));
			}
		}
		System.out.println(new String(UpdatedDataBlocks[0].getBlock()));
		//converts an array of blocks to a new FileAllocationTable		
		System.out.println("Decoding data");
		byte[] RetrevedData = new byte[BlockSixeLimit];//convert the string to bytes
		Boolean StringCompletedFlag = true; 
		int oldLength = 0;
		String DumbbyString = new String();
		String StringRepresentation =  new String();
		for(int i = 0; StringCompletedFlag; i++)
		{
			try{
				DumbbyString = new String(UpdatedDataBlocks[i].getBlock(), "UTF-8");
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
					System.arraycopy(DumbbyString.getBytes(), 0, RetrevedData, oldLength, DumbbyString.getBytes().length);
					RetrevedData =  Arrays.copyOf(RetrevedData, RetrevedData.length - counter);
					StringRepresentation = new String(RetrevedData, "UTF-8");

				}else{
					System.arraycopy(UpdatedDataBlocks[i].getBlock(), 0, RetrevedData, oldLength, UpdatedDataBlocks[i].getBlock().length);
					oldLength = RetrevedData.length;
					RetrevedData =  Arrays.copyOf(RetrevedData, RetrevedData.length + BlockSixeLimit);	

				}
			}
			catch(Exception e)
			{
				System.out.println("failed to decode Data");
				System.out.println(e);
			}
		}
		System.out.println("done Decoding Data");
		return RetrevedData;
	}
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
	
	// - Return the number of bytes written into the file or directory*/
	public static int tfs_write(int fd, byte[] buf, int blength)
	{
		FileAttributes At = FDT.get(fd); //Create a reference to the FileDescriptor
		int LocationOnDisk = At.getFirstBlock() + PCB.getFirstBlock() + 1; //first block is for FileAttributes
		int count = 0;
		for (int i = 0; i*BlockSixeLimit < blength; i++) {
			Block temp = new Block(Arrays.copyOfRange(buf, i*BlockSixeLimit, i*BlockSixeLimit + BlockSixeLimit));
			_tfs_write_block(LocationOnDisk, temp.getBlock());
			count++;
		}
		return count;
	} 
	//- Open the given file 
/* 	- Return file descriptor for the file or directory
	- name has a full path for the file or directory
	- Need to search name from the root directory  */
	public static FileAttributes tfs_open(byte[] name, int nlength)
	{
		return _tfs_open_fd(name, nlength);
	}			
	
	public static int tfs_seek(int file_id, int position)
	{
		return _tfs_seek_fd(file_id, position);
	}
	//- Close the given file 
/* 	- Delete the entry in FDT  */
	public static void tfs_close(int file_id)
	{
		
		_tfs_close_fd(file_id);
	}			
	//- Create the given file 
	public static int tfs_create(byte[] name, int nlength)
	{
		return -1;
	}
/* 	- Get the entry for name from the directory of which the first block number is block_no
	- name is not a full path
	- isDirectory, fbn and size are arrays of one element
	- Return a negative number if name does not exist in the directory */
	 public int _tfs_get_entry_dir(int block_no, byte[] name, byte nlength, byte[] is_directory, int[] fbn, int[] size)
	{
		byte[] testingHolder = tfs_read_data(block_no, 1);
		try {
			//convert block to string then to FileAttributes
			String testName = new String(testingHolder, "UTF-18");
			String[] Strs = testName.split("*");
			testName = Strs[0];
			FileAttributes file = new FileAttributes(Strs[0], Integer.parseInt(Strs[1]), Integer.parseInt(Strs[2]), Integer.parseInt(Strs[3]), Integer.parseInt(Strs[4]));
			
			//convert entered name to string
			String Name = new String(name, "UTF-18");
			//if names match enter values into arrays
			if(Name.equals(new String(testName)))
			{
				//get name on disk in string for comparison
				testName = Strs[0];
				//return file;
				is_directory[0] = (byte) (file.getisDir());
				fbn[0] = file.getParentFirstBlock();
				size[0] = file.getSize();
				return 0;
			}
			else{
				System.out.println("file not found tfs_get_entry_dir");
				return -1;
			}
		}
		catch (Exception e) {
			 System.out.println("failed to open fd tfs_get_entry_dir");
			 return 1;
		 }
	} 

	/*- Return the first block number of the parent directory in which name exists
	- name contains a full path */
 	public static int _tfs_search_dir(byte[] Fullname, int nlength)
	{
		
		try
		{
			String tempStr = new String(Fullname, "UTF-18");
			String[] DirNames = tempStr.split("/");
			Directory CurrentDir = RootDIR;
			for (int i = 0; i < DirNames.length; i++) {
				Directory NewDir =  CurrentDir.getFile(DirNames[i]);
				CurrentDir = NewDir;
				if(CurrentDir.getName().equals(DirNames[DirNames.length - 1]))
					break;
			}
			return CurrentDir.getParentFirstBlock();
		}
		catch(Exception e)
		{
			System.out.println("failed to search" + e);
		}
		return -1;
	} 
/* 	- Update the entry for name in the directory of which the first block number is block_no
	- name is not a full path */
	public static int _tfs_update_entry_dir(int block_no, byte[] name, byte nlength, byte is_directory, int fbn, int size)
 	{
		try {
			Directory Dir = new Directory(new String(name),  is_directory, block_no , size ,  fbn);
			//get the old name from disk
			byte[] testingHolder = tfs_read_data(block_no, 1);

			String testName = new String(testingHolder, "UTF-18");
			String[] Strs = testName.split("/");
			//isolate the name
			testName = Strs[0];
		
			if(Dir.getName().equals(new String(testName)))
			{
				Block[] DirBlocks = _tfs_encode_directory(Dir);
				//wright name on disk
				tfs_wright_data(block_no, DirBlocks[0].getBlock());
				//get next entry in fat
				int NextEntry = FAT.getEntrie(block_no);
				//wile not at the end of the file wright the data from fat at the next block
				//then move to the next block at the specified entry
				for (int i = 0; (NextEntry == -1); i++) {
					//wright the block data at NextEntry to disk
					tfs_wright_data(NextEntry + PCB.getFirstFreeBlock(), FAT.getBlock(NextEntry).getBlock());
					//get next entry from fat
					NextEntry = FAT.getEntrie(NextEntry);
				}
				return 0;
			} 
			else{
				System.out.println("names did not match");
				return -1;
			}
		} catch (Exception e) {
			System.out.println("failed to update" + e);
			return -1;
		}
	} 
	public static Block[] _tfs_encode_directory(Directory dencodedDirectory)
	{
		try 
		{
			String StringAttributes = dencodedDirectory.getName() +"*"+ dencodedDirectory.getisDir() +"*"+ dencodedDirectory.getFirstBlock() +"*"+ dencodedDirectory.getSize() +"*"+ dencodedDirectory.getParentFirstBlock() +"*";
			Block temp = new Block(StringAttributes.getBytes());
			Block[] encodedDirectoryBlocks = new Block[dencodedDirectory.getSize()];
			
			int NextEntry = FAT.getEntrie(dencodedDirectory.getFirstBlock());

			for (int i = 0; (NextEntry == -1); i++) {
				//wright the block data at NextEntry to encodedDirectoryBlocks
				encodedDirectoryBlocks[i] = FAT.getBlock(NextEntry);
				NextEntry = FAT.getEntrie(NextEntry);
			}
			return encodedDirectoryBlocks;
		} catch (Exception e) {
			System.out.println("failed to encoded Directory" + e);
			return null;
		}
	}
	public static Directory _tfs_decode_directory(byte[] encodedDirectory)
	{
		try {
			//convert block to string then to FileAttributes
			String testName = new String(encodedDirectory, "UTF-18");
			String[] Strs = testName.split("*");
			testName = Strs[0];
			FileAttributes file = new FileAttributes(Strs[0], Integer.parseInt(Strs[1]), Integer.parseInt(Strs[2]), Integer.parseInt(Strs[3]), Integer.parseInt(Strs[4]));
			Directory DecodeDirectory = new Directory(file);
			int currentEntrie = DecodeDirectory.getFirstBlock();
			Block DirBlock = new Block();

			while(!(currentEntrie == -1)){
				//get block at currentEntrie from fat into DirBlock
				DirBlock = FAT.getBlock(currentEntrie);
				//bytes from DirBlock are the placed into a string
				String testName2 = new String(DirBlock.getBlock(), "UTF-18");
				//string id split between *s 
				String[] Strs2 = testName2.split("*");
				//new directory is made based on this data
				Directory file2 = new Directory(Strs2[0], Integer.parseInt(Strs2[1]), Integer.parseInt(Strs2[2]), Integer.parseInt(Strs2[3]), Integer.parseInt(Strs2[4]));
				// directory is added to the parrent directory
				DecodeDirectory.addDirectory(file2);
				//current entry is set to the next block  acording to block
				currentEntrie = FAT.getEntrie(currentEntrie);
			}
			return DecodeDirectory;
		}
		catch(Exception e)
		{
			System.out.println( "failed to decode Directory" + e );
			return null;
		}
	}
	/* 	- Delete the entry for name from the directory of which the first blocknumber is block_no
	- name is not a full path
	- The whole size of the directory might be changed */
	public static int _tfs_delete_entry_dir(int block_no, byte[] name, byte nlength)
	{
		dummbyBlock = new byte[BlockSixeLimit];
		//now there is no name in fat so it can not be found
		String Name = new String(name);
		//get the old name from disk
		byte[] testingHolder = tfs_read_data(block_no, 1);
		
		try {
			String testName = new String(testingHolder, "UTF-18");
			String[] Strs = testName.split("/");
			//isolate the name
			testName = Strs[0];
			//if they match delete the dir
			if(Name.equals(new String(testName)))
			{
				FAT.setBlock(block_no, dummbyBlock);
				int nextEntry = FAT.getEntrie(block_no);
				while(!(nextEntry == -1))
				{
					PCB.addFreeBlock(nextEntry);
				}
				return 0;
			} 
			else{
				System.out.println("names did not match");
				return -1;
			}
		} 
		catch (Exception e) {
			System.out.println("failed to close fd");
			return -1;
		}
	}
 /*- Create an entry for name in the directory of which the first block number is block_no
	- name is not a full path
	- The whole size of the directory might be changed */
	public static int _tfs_create_entry_dir(int block_no, byte[] name, byte nlength, byte is_directory, int parentBlockNum, int size)
	{
		Directory newDir = new Directory(new String(name), (int) is_directory, block_no, size, parentBlockNum);
		Block[] encodedDir = _tfs_encode_directory(newDir);
		byte[] newDirData = FAT.getBlock(parentBlockNum).getBlock();
		_tfs_open_fd(name, (int)nlength);
		FAT.setBlock(block_no, encodedDir[0].getBlock());
		byte[] testingHolder = tfs_read_data(parentBlockNum, 1);
		try {
			Directory ParentDir = _tfs_decode_directory(testingHolder);
			ParentDir.addDirectory(new Directory( new String(name, "UTF-18"),  (int) is_directory, (int) block_no, size,parentBlockNum));
			return 0;
		}
		catch (Exception e) {
			 System.out.println("failed to create" + new String(name));
			 return -1;
		 }
	}

	//- Delete the given file 
	public static int tfs_delete(byte[] name, int nlength)		
	{
		FileAttributes file =  _tfs_open_fd(name, nlength);
		return _tfs_delete_entry_dir( file.getFirstBlock(),  name, (byte) nlength);
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
		return tfs_delete(name, nlength);
	}
/* 	- Read all entries in the directory fd into arrays
	- Return the number of entries */
	public static int tfs_read_dir(int fd, byte[] is_directory, byte[] nlength, byte[][] name, int[] first_block_no, int[] file_size)
	{
		byte[] encodedDir = FAT.getBlock(fd).getBlock();
		Directory Dir =  _tfs_decode_directory(encodedDir);
		for (int i = 0; i < Dir.getLinkedListSize(); i++) {
			FileAttributes Attributes = Dir.getFile(i).getDirectoryInfo();
			name[i] = Arrays.copyOf(Attributes.getName().getBytes(), Attributes.getName().getBytes().length);
			is_directory[i] = (byte) Attributes.getisDir();
			nlength[i]= (byte)(name.length);
			first_block_no[i]= Attributes.getFirstBlock();
			file_size[i]= Attributes.getSize();
		}
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
/*  - Return file descriptor for the file or directory
	- name has a full path for the file or directory
	- Need to search name from the root directory */
 	private static FileAttributes _tfs_open_fd(byte name[], int nlength)
 	{
		 
		int Entry =  _tfs_search_dir(name, nlength);
		byte[] testingHolder = tfs_read_data(Entry, 1);
		try {
			
			String testName = new String(testingHolder, "UTF-18");
			String[] Strs = testName.split("*");
			FileAttributes file = new FileAttributes(Strs[0], Integer.parseInt(Strs[1]), Integer.parseInt(Strs[2]), Integer.parseInt(Strs[3]), Integer.parseInt(Strs[4]));
			//get name on disk in string for comparison
			testName = Strs[0];
			return file;
		}
		catch (Exception e) {
			 System.out.println("failed to open fd");
			 return null;
		 }
	 }
	 
	//- Return the new file pointer
 	private static int _tfs_seek_fd(int fd, int offset)
 	{
		if (offset > 0){
			FDT.get(fd).setFirstBlock(offset);
			Directory dir = new Directory(FDT.get(fd));
			return dir.getFirstBlock(); 
		}
		
 		return -1;
 	}

/*  - Update the entry for the file or directory in the parent directory, if there is a change
	- Destroy the entry of fd from FDT */
 	private static void _tfs_close_fd(int fd)
 	{
		//remove Fileatributes from FDT
		FileAttributes AttributeHolder = FDT.remove(fd);
		_tfs_update_entry_dir(AttributeHolder.getFirstBlock(), AttributeHolder.getFileNameRaw(),  (byte)AttributeHolder.getFileNameRaw().length, (byte) AttributeHolder.getisDir(), AttributeHolder.getParentFirstBlock(), AttributeHolder.getSize());
 	}
 	
 	private static int _tfs_get_block_no_fd(int fd, int offset)
 	{
 		return FDT.get(fd).getFirstBlock();
 	}
}