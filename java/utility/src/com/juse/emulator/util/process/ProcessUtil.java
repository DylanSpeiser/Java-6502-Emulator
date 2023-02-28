package com.juse.emulator.util.process;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarFile;

import com.juse.emulator.util.config.ResourceCopy;

public class ProcessUtil
{

	public static void replaceProcessArgument(List<String> arguments, String name, String newValue)
	{
		if(arguments.contains(name))
		{
			for(int i = 0;i<arguments.size();i++)
			{
				if(arguments.get(i).equals(name))
				{
					arguments.set(i, newValue);
					break;	
				}
			}
		}
		else
		{
			arguments.add(newValue);
		}
	}	
	
	public static void replaceProcessArgumentValue(List<String> arguments, String name, String newValue)
	{
		if(arguments.contains(name))
		{
			for(int i = 0;i<arguments.size();i++)
			{
				if(arguments.get(i).equals(name))
				{
					if((i+1) < arguments.size())
					{
						arguments.set(i+1, newValue);
						break;
					}
				}
			}		
		}
		else
		{
			arguments.add(name);
			arguments.add(newValue);
		}
	}
	
	
	public static List<String> getProcessArguments()
	{
		List<String> arguments = new ArrayList<String>();
		
		String commandline = System.getProperty("sun.java.command");
		if(commandline!=null)
		{
			String[] args = commandline.split(" ");
			
			arguments.addAll(Arrays.asList(args));
			
			arguments.remove(0);
		}
		
		return arguments;
	}
	

	public static Class getProcessLaunchClass()
	{
		Class launch = null;
		
		String commandline = System.getProperty("sun.java.command");
		if(commandline!=null)
		{
			String[] args = commandline.split(" ");
			if(args!=null && args.length > 0)
			{
				try
				{
					launch = Class.forName(args[0]);
				}
				catch (ClassNotFoundException e)
				{
					e.printStackTrace();
				}
			}
		}
		
		return launch;
	}	
	
	public static void relauchWithExt(Class whichClass,String extDir, List<String> args)
	{
		relauchWithExt(whichClass,extDir,args,true) ;
	}
	
	public static void relauchWithExt(Class whichClass,String extDir, List<String> args, boolean bWait) 
	{		
		final List<String> cpe = new ArrayList<String>();		
		
		try
		{
			for(int i=0;i<args.size();i++)
			{
				String an = args.get(i);
				if(an.equals("--ext"))
				{
					args.set(i++,"");
					args.set(i,"");
				}
			}
			
			if (extDir!=null)
			{
				Path extPath = Paths.get(extDir);
				Files.walkFileTree(extPath, new FileVisitor<Path>()
				{

					@Override
					public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException
					{
						//System.out.println("FOUND DIR:" + dir.toAbsolutePath());
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
					{
						String catFile = file.toFile().getCanonicalPath();

						if (catFile.toLowerCase().endsWith(".jar"))
						{
							System.out.println("Found Extension Jar:" + file.toFile().getCanonicalPath());
							cpe.add(file.toFile().getCanonicalPath());
						}
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException
					{
						//System.out.println("FAILED:" + file.toAbsolutePath());
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException
					{
						//System.out.println("FOUND:" + dir.toAbsolutePath());
						return FileVisitResult.CONTINUE;
					}

				});
			}
			List<String> jvmArgs = new ArrayList<String>();
			int code = exec(whichClass,cpe,jvmArgs,args, bWait);
			System.exit(code);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static int exec(Class clazz, List<String> extClassPath, List<String> jvmArgs, List<String> args, boolean bWait) throws IOException, InterruptedException
	{
		String javaHome = System.getProperty("java.home");
		String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
		String classpath = System.getProperty("java.class.path");
		String className = clazz.getName();
		
		if(extClassPath!=null && extClassPath.size() > 0)
		{
			for(String cpe : extClassPath)
			{
				classpath+=(File.pathSeparator  + cpe);
			}
		}
		
		List<String> command = new ArrayList<>();
		command.add(javaBin);
		command.addAll(jvmArgs);
		command.add("-Djuse.relaunch=true");
		command.add("-cp");
		command.add(classpath);
		command.add(className);
		command.addAll(args);

		
		ProcessBuilder builder = new ProcessBuilder(command);
		Process process = builder.inheritIO().start();
		if(bWait)
		{
			process.waitFor();
			return process.exitValue();
		}
		return 0;
	}

	public static void extractResources(Class loadingClass, String resName,String destination)
	{
		URL fromURL = loadingClass.getClassLoader().getResource(resName);
		File toPath = new File(destination);

		if (!toPath.exists())
		{
			String url = fromURL.toString();
			String jarName = url.substring(0, url.indexOf('!')).replace("jar:file:", "");

			try
			{
				// System.out.println("fromURL:" + fromURL);
				// System.out.println("toPath:" + toPath.getAbsolutePath());
				System.out.println("Copying demo directory:\nFrom: " + jarName + "\nTo  : " + toPath.getCanonicalPath());

				ResourceCopy rc = new ResourceCopy();
				rc.copyResourceDirectory(new JarFile(jarName), resName, toPath);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			System.out.println("The demo directory already exists.");
		}
		// System.exit(0);
	}



}
