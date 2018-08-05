/*
 * @CreateTime: May 3, 2018 11:20 AM
 * @Author: Gavin Jaeger-Freeborn
 * @Contact: gavinfre@uvic.ca
 * @Last Modified By: Gavin Jaeger-Freeborn
 * @Last Modified Time: May 3, 2018 11:26 AM
 * @Student Number:T00611983
 * @ COMP 3411 Assignment 5
 * @Description: TFSShell functions as a commandline for interacting with the TFSFileSystem.
 */
import java.io.*;
import java.util.*;

public class TFSShell extends Thread  
{
	TFSFileSystem myFileSystem;
	public TFSShell()
	{
	}
	
	public void run()
	{
		readCmdLine();
	}
	
	/*
	 * User interface routine
	 */
	 
	void readCmdLine()
	{
		String line, cmd, arg1, arg2, arg3, arg4;
		StringTokenizer stokenizer;
		Scanner scanner = new Scanner(System.in);

		System.out.println("Hal: Good morning, Dave!\n");
		
		while(true) {
			
			System.out.print("ush> ");
			
			line = scanner.nextLine();
			line = line.trim();
			stokenizer = new StringTokenizer(line);
			if (stokenizer.hasMoreTokens()) {
				cmd = stokenizer.nextToken();
				
				if (cmd.equals("mkfs"))
					mkfs();
				else if (cmd.equals("mount"))
					mount();
				else if (cmd.equals("sync"))
					sync();
				else if (cmd.equals("prrfs"))
					prrfs();
				else if(cmd.equals("prmfs"))
					prmfs();
				else if (cmd.equals("umount"))
					umount();

				else if (cmd.equals("mkdir")) {
					if (stokenizer.hasMoreTokens()) {
						arg1 = stokenizer.nextToken();
						mkdir(arg1);					
					}
					else
						System.out.println("Usage: mkdir directory");
				}
				else if (cmd.equals("rmdir")) {
					if (stokenizer.hasMoreTokens()) {
						arg1 = stokenizer.nextToken();
						rmdir(arg1);					
					}
					else
						System.out.println("Usage: rmdir directory");
				}
				else if (cmd.equals("ls")) {
					if (stokenizer.hasMoreTokens()) {
						arg1 = stokenizer.nextToken();
						ls(arg1);					
					}
					else
						System.out.println("Usage: ls directory");
				}
				else if (cmd.equals("create")) {
					if (stokenizer.hasMoreTokens()) {
						arg1 = stokenizer.nextToken();
						create(arg1);					
					}
					else
						System.out.println("Usage: create file");
				}
				else if (cmd.equals("rm")) {
					if (stokenizer.hasMoreTokens()) {
						arg1 = stokenizer.nextToken();
						rm(arg1);					
					}
					else
						System.out.println("Usage: rm file");
				}
				else if (cmd.equals("print")) {
					if (stokenizer.hasMoreTokens())
						arg1 = stokenizer.nextToken();
					else {
						System.out.println("Usage: print file position number");
						continue;
					}
					if (stokenizer.hasMoreTokens())
						arg2 = stokenizer.nextToken();
					else {
						System.out.println("Usage: print file position number");
						continue;
					}					
					if (stokenizer.hasMoreTokens())
						arg3 = stokenizer.nextToken();
					else {
						System.out.println("Usage: print file position number");
						continue;
					}	
					try {
						print(arg1, Integer.parseInt(arg2), Integer.parseInt(arg3));
					} catch (NumberFormatException nfe) {
						System.out.println("Usage: print file position number");
					}			
				}
				else if (cmd.equals("append")) {
					if (stokenizer.hasMoreTokens())
						arg1 = stokenizer.nextToken();
					else {
						System.out.println("Usage: append file number");
						continue;
					}
					if (stokenizer.hasMoreTokens())
						arg2 = stokenizer.nextToken();
					else {
						System.out.println("Usage: append file number");
						continue;
					}					
					try {
						append(arg1, Integer.parseInt(arg2));
					} catch (NumberFormatException nfe) {
						System.out.println("Usage: append file number");
					}			
				}
				else if (cmd.equals("cp")) {
					if (stokenizer.hasMoreTokens())
						arg1 = stokenizer.nextToken();
					else {
						System.out.println("Usage: cp file directory");
						continue;
					}
					if (stokenizer.hasMoreTokens())
						arg2 = stokenizer.nextToken();
					else {
						System.out.println("Usage: cp file directory");
						continue;
					}					
					cp(arg1, arg2);
				}
				else if (cmd.equals("rename")) {
					if (stokenizer.hasMoreTokens())
						arg1 = stokenizer.nextToken();
					else {
						System.out.println("Usage: rename src_file dest_file");
						continue;
					}
					if (stokenizer.hasMoreTokens())
						arg2 = stokenizer.nextToken();
					else {
						System.out.println("Usage: rename src_file dest_file");
						continue;
					}					
					rename(arg1, arg2);
				}
					
				else if (cmd.equals("exit")) {
					exit();
					System.out.println("\nHal: Good bye, Dave!\n");
					break;
				}
				
				else
					System.out.println("-ush: " + cmd + ": command not found");
			}
		}
		
		
	}


/*
 * You need to implement these commands
 */
 	//- Make a file system – Make new PCB and FAT in the file system 
	void mkfs()
	{
		myFileSystem.tfs_mkfs();
		return;
	}
	//- Mount a file system – Copy PCB and FAT in the file system into the main memory 
	void mount()
	{
		myFileSystem.tfs_mount();
		return;
	}
	void umount()
	{
		myFileSystem.tfs_umount();
		return;
	}
	//Synchronize the file system – Copy PCB and FAT in the main memory back to the file system on the disk 
	void sync()
	{
		myFileSystem.tfs_sync();
		return;
	}
	//Print PCB and FAT in the file system
	void prrfs()
	{
		//this will output a null until other methods are implemented
		System.out.println(myFileSystem.tfs_prrfs());
		return;
	}
	void prmfs()
	{
		System.out.println(myFileSystem.tfs_prmfs());
	}
	//- Make a directory if it does not exist 
	void mkdir(String directory)
	{
		return;
	}
	//- Remove a directory if it is empty 
	void rmdir(String directory)
	{
		return;
	}
	//- List file or directory names in the directory, with size 
	void ls(String directory)
	{
		return;
	}
	//- Create an empty file if it does not exist
	void create(String file)
	{
		return;
	}
	//- Remove a file 
	void rm(String file)
	{
		return;
	}
	//- Print number characters from the position in the file file 
	void print(String file, int position, int number)
	{
		return;
	}
	//- Append any number characters at the end of the file if it exits 
	void append(String file, int number)
	{
		return;
	}
	//- Copy a file into a directory if they exit and source_file does not exist under destination_directory 
	void cp(String file, String directory)
	{
		return;
	}
	//- Rename a file if source_file exists and destination_file does not exit 
	void rename(String source_file, String destination_file)
	{
		return;
	}
	//- Exit from the shell, i.e., shutdown the system 
	void exit()
	{
		myFileSystem.tfs_exit();
		return;
	}
}


/*
 * main method
 */

class TFSMain
{
	public static void main(String argv[]) throws InterruptedException
	{
		TFSFileSystem tfs = new TFSFileSystem();
		TFSShell shell = new TFSShell();
		
		shell.start();
//		try {
			shell.join();
//		} catch (InterruptedException ie) {}
	}
}
